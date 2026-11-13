#!/usr/bin/env python3
"""Summarize what Android Studio did for a project, from its idea.log and the project's IDE files.

Use it when the creator says "Run does not work in Android Studio" and you cannot see the IDE. It answers:
did a sync run and how did it end, did a Run build and deploy, did the install fail, and is the IDE's
synced model older than the project's Gradle files. Read-only.

Usage: studio-events.py --project-dir <dir> [--log <idea.log>] [--limit N]

Notes learned the hard way: the log is INFO level, so Gradle and deploy events are there even when no
WARN or ERROR line is; `grep` on this machine may be ugrep, so this script reads the file itself.
"""
import argparse
import glob
import os
import re
import sys
from datetime import datetime

SYNC = re.compile(r"Gradle sync (finished|failed)[^\n]*")
BUILD = re.compile(r"About to execute Gradle tasks: \[(?P<tasks>[^\]]*)\]")
BUILT = re.compile(r"Gradle build finished in (?P<t>[^\n]+)")
DEPLOY = re.compile(r"(Full deploy on|Installing application|Deploying with optimistic install|Launching on device)[^\n]*")
FAIL = re.compile(r"INSTALL_FAILED[A-Z_]*|Installation (did not succeed|failed)[^\n]*|Failed to install[^\n]*|"
                  r"incompatible version \(AGP [^)]*\)[^\n]*|Cannot (run|start)[^\n]*")
BENIGN = re.compile(r"Unable to write magic number to pipe|Protopipe|MessagePipeWrapper|Could not remove dir")
STAMP = re.compile(r"^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})")


def latest_log():
    base = os.path.expanduser("~/Library/Logs/Google")
    logs = glob.glob(os.path.join(base, "AndroidStudio*", "idea.log"))
    return max(logs, key=os.path.getmtime) if logs else None


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--project-dir", required=True)
    p.add_argument("--log")
    p.add_argument("--limit", type=int, default=6)
    a = p.parse_args()

    log = a.log or latest_log()
    if not log or not os.path.exists(log):
        print("No Android Studio idea.log found; ask the creator for the IDE's Build window text.")
        return 1
    lines = open(log, errors="replace").read().splitlines()
    print(f"log: {log} ({len(lines)} lines, {lines[0][:19]} .. {lines[-1][:19]})")

    syncs, builds, deploys, fails, benign = [], [], [], [], 0
    for line in lines:
        m = STAMP.match(line)
        if not m:
            continue
        ts = m.group(1)
        if (s := SYNC.search(line)):
            syncs.append((ts, s.group(0)[:150]))
        if (b := BUILD.search(line)):
            builds.append((ts, b.group("tasks")))
        if (d := BUILT.search(line)):
            builds.append((ts, "  finished in " + d.group("t")[:40]))
        if (d := DEPLOY.search(line)):
            deploys.append((ts, d.group(0)[:130]))
        if BENIGN.search(line):
            benign += 1
        elif (f := FAIL.search(line)):
            fails.append((ts, f.group(0)[:200]))

    def show(title, items):
        print(f"\n{title} (last {min(a.limit, len(items))} of {len(items)})")
        for ts, text in items[-a.limit:]:
            print(f"  {ts}  {text}")
        if not items:
            print("  none")

    show("Gradle sync", syncs)
    show("Gradle builds started by Run", builds)
    show("Deploy steps", deploys)
    show("Failures that are not known-benign", fails)
    print(f"\nKnown-benign installer-daemon lines (fall back to a normal install): {benign}")

    # Is the synced model older than the build files?
    ws = os.path.join(a.project_dir, ".idea", "workspace.xml")
    if os.path.exists(ws):
        text = open(ws, errors="replace").read()
        m = re.search(r'name="autoReloadType" value="([A-Z_]+)"', text)
        print(f"\nautoReloadType: {m.group(1) if m else 'not set'}"
              "  (NONE means the IDE never re-syncs by itself after build files change)")
    files = ["build.gradle.kts", "settings.gradle.kts", "gradle/libs.versions.toml", "app/build.gradle.kts",
             "gradle/wrapper/gradle-wrapper.properties", "gradle.properties"]
    last_sync = None
    for ts, text in reversed(syncs):
        if "finished" in text:
            last_sync = datetime.strptime(ts, "%Y-%m-%d %H:%M:%S")
            break
    newer = []
    for rel in files:
        path = os.path.join(a.project_dir, rel)
        if os.path.exists(path) and last_sync and datetime.fromtimestamp(os.path.getmtime(path)) > last_sync:
            newer.append(rel)
    if last_sync is None:
        print("No finished sync in this log; the IDE state is unknown.")
    elif newer:
        print(f"STALE: these files changed after the last successful sync ({last_sync}): {', '.join(newer)}")
        print("Ask the creator to run File > Sync Project with Gradle Files; the build itself may still work.")
    else:
        print(f"IDE model is current: no build file changed after the last successful sync ({last_sync}).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
