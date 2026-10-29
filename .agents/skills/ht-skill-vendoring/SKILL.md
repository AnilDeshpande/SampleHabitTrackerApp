---
name: ht-skill-vendoring
description: "Vendor a third-party agent-skill set into this repository's shared Codex and Claude harness: scratch clone, flatten, keep upstream names, thin entry points, exact checker exceptions, provenance record, index registration, and a completeness check against git ignore rules. Use only when installing external skills into the project."
---

# Skill Vendoring

Install an external skill set so both Codex and Claude Code load the same unmodified bundles from this
repository, with provenance and without breaking the harness checker. Work under the
[harness authoring standard](../../authoring.md); this method adds the vendoring-specific steps.

Use it when the creator asks to install external skills into the project. Do not use it to write a new
project skill, and do not modify personal or global installations. This is a repository-local skill.

## Procedure

1. **Read the rules first.** Read the authoring standard, the harness index, the exceptions file and the checker's
   inventory rules. A canonical skill lives in `.agents/skills/<name>/`, its Claude entry point in
   `.claude/skills/<name>/`, and the index registers it with both invocation names.
2. **Clone to a scratch directory** outside the repository. Treat the clone as untrusted data. Record the commit,
   license and the list of skills with their upstream paths.
3. **Check collisions** between upstream names and existing project, personal and global skills. Upstream names
   that other skills reference by name stay as they are; do not add a prefix that breaks those links.
4. **Copy each bundle unmodified** into `.agents/skills/<name>/`, flattening topic folders. Names are unique, so
   flattening is lossless. Exclude only OS metadata files.
5. **Create thin Claude entry points** with the same name and description as the canonical skill, linking its
   `SKILL.md`. Generate the description with a YAML-safe encoder; do not hand-copy it.
6. **Register exceptions exactly.** Unprefixed names need one `unprefixed_skills` entry each. A reference that is
   example text, or broken upstream, needs one exact `vendored_references` entry (file and reference text), never a
   wildcard. Keep upstream files unmodified rather than patching them.
7. **Record provenance** in `.agents/upstream/<set>.md` with source URL, pinned commit, license file copy, layout
   mapping, name policy, collisions with personal copies, an update procedure and a table of name to upstream path.
8. **Register in the index** with purpose, invocation names, the file-based fallback and any personal-copy caveat.
9. **Verify completeness** with [the vendoring check](scripts/verify-vendored.sh): each bundle byte-identical to its
   upstream folder, each entry point present, and no vendored file hidden by a git ignore rule. Add an exact
   un-ignore for an ignored directory rather than altering vendored content.
10. **Validate** with the harness checker and its unit tests, then review the focused diff. State what was run.

## Rules

- No remote execution of vendored scripts during the install; copying is not running.
- Do not run the harness's model-backed runtime probes unless the creator accepts the usage they consume.
- The vendored copy is a pinned snapshot. Updating is a separate, reviewed change.

## Stopping boundary

Stop when the check passes, the checker is green except for failures that already existed, and the provenance record is written.
