package com.codetutor.samplehabittrackerapp

import android.content.Context
import androidx.room.RoomDatabase
import com.codetutor.samplehabittrackerapp.core.time.Clock
import com.codetutor.samplehabittrackerapp.data.local.HabitTrackerDatabase
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import java.time.Instant
import java.time.ZoneId
import java.util.Collections
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * The LLD's gate-before-snapshot proof. Every command acquires the database gate first and only then reads the
 * clock, so a contender that waits for the gate sees the time at which it actually acquired it. The proof is a
 * SQL trace (Room's query callback, run inline on the executing thread) interleaved with a recording clock.
 *
 * A trace line is `tag|kind|detail`: tag is the Room instance ("A" or "B"), kind is `SQL` or `CLOCK`. The
 * callback runs before Room executes the statement, so a line marks when a statement is about to run.
 */
object GateOrderScenarios {
    private const val GATE = "UPDATE reconciliation_lock SET version = version + 1 WHERE lockId = 1"
    private val domainTable = Regex("""\b(habits|occurrences|app_state|badges|notification_events|target_changes)\b""")
    private val us = Locale.US

    class Trace {
        private val lines: MutableList<String> = Collections.synchronizedList(mutableListOf())
        fun add(line: String) { lines += line }
        fun snapshot(): List<String> = synchronized(lines) { lines.toList() }
        fun clear() = lines.clear()
        override fun toString() = snapshot().withIndex().joinToString("\n") { (i, l) -> "%3d  %s".format(i, l) }
    }

    /** Wraps the shared test clock so each read is recorded in the same ordered trace as the SQL. */
    private class RecordingClock(private val tag: String, private val delegate: Clock, private val trace: Trace) : Clock {
        override fun now(): Instant = delegate.now().also { trace.add("$tag|CLOCK|now=$it") }
        override fun zone(): ZoneId = delegate.zone()
    }

    private fun tracing(tag: String, trace: Trace, onStatement: (String) -> Unit = {}): (RoomDatabase.Builder<HabitTrackerDatabase>) -> Unit = { builder ->
        // A direct executor runs the callback on the thread about to execute the statement, so trace order is real order.
        builder.setQueryCallback({ sql, _ ->
            val statement = sql.replace(Regex("\\s+"), " ").trim()
            trace.add("$tag|SQL|$statement")
            onStatement(statement)
        }, Executor { it.run() })
    }

    private fun List<String>.of(tag: String) = filter { it.startsWith("$tag|") }

    /**
     * The statements of [tag]'s outermost command transaction number [n] (0-based), from its BEGIN to its matching
     * END, with clock reads in place. Room nests a BEGIN/END around each DAO statement inside a command's
     * transaction, and its invalidation tracker runs transactions of its own between commands (when, depends on
     * the platform), so a transaction that only touches the tracker's table is not a command.
     */
    private fun List<String>.transaction(tag: String, n: Int): List<String> {
        val mine = of(tag)
        val commands = mutableListOf<List<String>>()
        var depth = 0
        var start = -1
        for (i in mine.indices) {
            val kind = mine[i].removePrefix("$tag|")
            if (kind.startsWith("SQL|BEGIN")) {
                if (depth == 0) start = i
                depth++
            } else if (kind.startsWith("SQL|END")) {
                depth--
                if (depth == 0) {
                    val body = mine.subList(start, i + 1)
                    val statements = body.filter { line ->
                        val k = line.removePrefix("$tag|SQL|")
                        line.startsWith("$tag|SQL|") && !k.startsWith("BEGIN") && !k.startsWith("TRANSACTION") && !k.startsWith("END")
                    }
                    val trackerOnly = statements.isNotEmpty() && statements.all { it.contains("room_table_modification_log") }
                    if (!trackerOnly) commands += body
                }
            }
        }
        return commands.getOrNull(n) ?: error("no command transaction $n for $tag in:\n${this.joinToString("\n")}")
    }

    /**
     * Inside one transaction: BEGIN, then the gate update, then the clock snapshot, and only then any statement
     * that touches a domain table. Nothing domain-related and no clock read may precede the gate.
     */
    private fun assertGateFirst(tag: String, transaction: List<String>, what: String) {
        val gate = transaction.indexOfFirst { it == "$tag|SQL|$GATE" }
        val snapshot = transaction.indexOfFirst { it.startsWith("$tag|CLOCK|") }
        val firstDomain = transaction.indexOfFirst { it.startsWith("$tag|SQL|") && it != "$tag|SQL|$GATE" && domainTable.containsMatchIn(it) }
        val message = "$what\n${transaction.joinToString("\n")}"
        assertTrue("gate update present. $message", gate >= 0)
        assertTrue("clock read present. $message", snapshot >= 0)
        assertTrue("gate update precedes the clock snapshot. $message", gate < snapshot)
        assertTrue("the transaction began before the gate. $message", transaction.indexOfFirst { it.startsWith("$tag|SQL|BEGIN") } < gate)
        if (firstDomain >= 0) assertTrue("clock snapshot precedes every domain read and write. $message", snapshot < firstDomain)
        assertEquals("exactly one clock snapshot per command. $message", 1, transaction.count { it.startsWith("$tag|CLOCK|") })
        assertEquals("exactly one gate update per command. $message", 1, transaction.count { it == "$tag|SQL|$GATE" })
    }

    /** Both gated commands, create and reconcile, on a file-backed WAL database. */
    fun theGateUpdatePrecedesTheClockSnapshotAndEveryDomainStatement(context: Context) = runBlocking {
        val fileName = "gate-order-single.db"
        context.deleteDatabase(fileName)
        val trace = Trace()
        val clock = ManualClock()
        val fx = RepositoryFixture(context, fileName, clock, RecordingClock("A", clock, trace), tracing("A", trace))
        try {
            fx.provider.get() // open and create first, so only commands are traced
            trace.clear()

            fx.repository.createHabit(RawHabitInput("Meditation", "minutes", "10", Frequency.DAILY, 0), us)
            clock.instant = clock.instant.plusSeconds(3600)
            fx.repository.reconcile()

            val lines = trace.snapshot()
            assertGateFirst("A", lines.transaction("A", 0), "CreateHabit")
            assertGateFirst("A", lines.transaction("A", 1), "ReconcileTracking")
            assertEquals("the gate advanced once per command", 2L, fx.count("SELECT version FROM reconciliation_lock"))
        } finally {
            fx.close()
            context.deleteDatabase(fileName)
        }
    }

    /**
     * Contender A holds the gate. Contender B, a second Room instance on the same file, begins its transaction and
     * has to wait. While B waits the clock moves forward; B must capture its snapshot after it finally acquires the
     * gate, so it sees the later time, not the time at which it arrived.
     */
    fun aContenderWaitingForTheGateSnapshotsTheClockOnlyAfterAcquiringIt(context: Context) = runBlocking {
        val fileName = "gate-order-contention.db"
        context.deleteDatabase(fileName)
        val trace = Trace()
        val clock = ManualClock()
        val arrival = clock.instant
        val later = arrival.plusSeconds(3600)

        val aHoldsGate = CountDownLatch(1)
        val bHasArrived = CountDownLatch(1)
        // Hooks act only on the traced commands, never on opening the databases or on the setup create.
        val armed = AtomicBoolean(false)
        val bArmed = AtomicBoolean(false)
        var aPaused = false
        var aSawGate = false

        val a = RepositoryFixture(context, fileName, clock, RecordingClock("A", clock, trace), tracing("A", trace) { sql ->
            if (!armed.get()) return@tracing
            if (sql == GATE) aSawGate = true
            // A's first domain read after the gate: A is inside its transaction and holds the database write lock.
            if (aSawGate && !aPaused && sql.startsWith("SELECT") && domainTable.containsMatchIn(sql)) {
                aPaused = true
                aHoldsGate.countDown()
                check(bHasArrived.await(20, TimeUnit.SECONDS)) { "contender B never arrived" }
                Thread.sleep(300) // let B reach SQLite's wait for the write lock
                clock.instant = later // time passes while B waits
            }
        })
        a.provider.get()
        // The habit exists before tracing starts, so both contenders have real work to catch up.
        a.repository.createHabit(RawHabitInput("Meditation", "minutes", "10", Frequency.DAILY, 0), us)
        val b = RepositoryFixture(context, fileName, clock, RecordingClock("B", clock, trace), tracing("B", trace) { if (bArmed.get()) bHasArrived.countDown() })
        try {
            b.provider.get()
            trace.clear()
            armed.set(true)

            var failure: Throwable? = null
            val workerA = thread(name = "contender-A") { try { runBlocking { a.repository.reconcile() } } catch (t: Throwable) { failure = t } }
            check(aHoldsGate.await(20, TimeUnit.SECONDS)) { "contender A never held the gate\n$trace" }
            bArmed.set(true)
            val workerB = thread(name = "contender-B") { try { runBlocking { b.repository.reconcile() } } catch (t: Throwable) { failure = t } }
            workerA.join(60_000); workerB.join(60_000)
            assertEquals("no contender fails: $failure\n$trace", null, failure)

            val lines = trace.snapshot()
            assertGateFirst("A", lines.transaction("A", 0), "contender A")
            assertGateFirst("B", lines.transaction("B", 0), "contender B (waited)")

            val aClock = lines.single { it.startsWith("A|CLOCK|") }
            val bClock = lines.single { it.startsWith("B|CLOCK|") }
            assertEquals("A captured the time at which it held the gate\n$trace", "A|CLOCK|now=$arrival", aClock)
            assertEquals("B captured the later time, after acquiring the gate\n$trace", "B|CLOCK|now=$later", bClock)

            val bBegan = lines.indexOfFirst { it.startsWith("B|SQL|BEGIN") }
            val aEnded = lines.indexOfLast { it.startsWith("A|SQL|END") }
            val bGate = lines.indexOf("B|SQL|$GATE")
            assertTrue("B had arrived before A finished\n$trace", bBegan in 0 until aEnded)
            assertTrue("B updated the gate only after A's transaction ended\n$trace", aEnded < bGate)
            assertEquals("the gate advanced once per gated transaction (create, A, B)", 3L, a.count("SELECT version FROM reconciliation_lock"))
        } finally {
            a.close(); b.close()
            context.deleteDatabase(fileName)
        }
    }
}
