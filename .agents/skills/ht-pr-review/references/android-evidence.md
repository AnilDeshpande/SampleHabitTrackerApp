# Android review evidence

Select the checks that exercise the approved contracts affected by the change. Use
[emulator validation](../../ht-emulator-validation/SKILL.md) for device ownership, launch and cleanup, and
[device tiers](../../ht-device-tiers/SKILL.md) for platform-specific test results.

## Runtime and persistence

- For time-derived state, keep the same subscriber alive across the transition. Exercise a real create commit with an idle
  ticker, or a clock change that updates only the reconciliation gate. Assert the finalized period or emitted state. A look-up
  count, new subscription, or direct catch-up invocation can miss the real-path defect.
- To verify gate-before-clock, use a command-scoped SQL trace and recording clock, including a waiting contender when required.
  Compare canonical trigger definitions and exercise the guards on the migrated database. Read the
  [test-design guidance](../../ht-increment-implementation/SKILL.md#time-and-room-tests) for timing and SQL test setup.
- For a Compose action's accessible name, assert the name on the actionable node in the merged semantics tree. Compare a
  known-good control when interpreting a UIAutomator dump. A wrapper may be empty while the name appears on a sibling with the
  same bounds. If only merged semantics were verified, report that scope. Exercise both the permission-dialog and ordinary
  path for transient feedback. For clock display changes, use the
  [emulator time guidance](../../ht-emulator-validation/references/adb-gotchas.md#verifying-live-time-and-time-zone-changes).

## Security and privacy

For changes affecting entry points, runtime permissions, dependencies or user data, inspect merged debug and release manifests
and effective permissions. For each exported component under review, trace its callers and accepted inputs, permission checks,
and any URI grants. Check dynamic receiver registrations and identify debug-only exposure. Use
[Android intent security](../../android-intent-security/SKILL.md) and
[Android permissions security](../../android-permissions-security/SKILL.md) for the relevant specialist checks.

Locate the actual Room database, WAL and SHM files, DataStore and other user files. Compare their storage domains with the
approved privacy contract. Verify `allowBackup` on older APIs, any legacy backup rules, and the referenced extraction rule sets
for cloud backup and device transfer. Include device-protected storage if the app uses it.

Record three levels of evidence separately. Static checks cover the packaged configuration. Backup eligibility uses the
per-package transport result on disposable devices with synthetic data, even if the overall command reports success. A
completed backup/restore or device-to-device transfer requires a successful run and inspection of the restored data. A missing
transport, preflight rejection or ineligible control cannot establish transfer behavior. Keep a required unverified check visible
in the merge verdict.
