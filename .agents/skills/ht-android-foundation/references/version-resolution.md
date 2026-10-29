# Version resolution

How to pick versions for an Android foundation so the build, the IDE and the documents all agree.
Stable releases only unless a source document requires otherwise. Record every fact in the
[compatibility matrix](../../../templates/version-compatibility-matrix-template.md).

## Order of gates

1. **IDE.** Find the creator's Android Studio version. Look up the highest AGP that version supports in Google's
   Studio-to-AGP table (`https://developer.android.com/build/releases/about-agp`). Never choose an AGP above it,
   even if it builds on the command line: the IDE will refuse to sync. If the version cannot be read, ask.
2. **AGP release notes.** `https://developer.android.com/build/releases/agp-<major>-<minor>-0-release-notes` has a
   Compatibility section: minimum Gradle, JDK, SDK build-tools and the highest supported compileSdk. The
   [AGP compatibility script](../scripts/agp-compat.sh) prints it together with the Studio row.
3. **Gradle.** Use at least the AGP minimum. The newest Gradle (`https://services.gradle.org/versions/current`)
   is acceptable when AGP accepts it; keep a cached distribution as a fallback.
4. **Libraries.** Read the newest stable from Maven metadata:
   Google Maven `https://dl.google.com/dl/android/maven2/<group path>/<artifact>/maven-metadata.xml`, and Maven
   Central `https://repo.maven.apache.org/maven2/<group path>/<artifact>/maven-metadata.xml`. Exclude alpha, beta,
   RC and milestone versions.
5. **compileSdk.** Some AndroidX libraries require the newest compileSdk. Raise compileSdk only as far as AGP supports
   and the installed platform allows; keep minSdk and targetSdk unchanged unless a document says otherwise.
6. **Kotlin.** On AGP 9 the Kotlin Gradle plugin comes from the build classpath. After every AGP or plugin change run
   `./gradlew buildEnvironment` and read the resolved Kotlin version; align the Compose compiler plugin to it.
7. **Processors.** Follow the installed `agp-9-upgrade` skill's minimums (KSP and Hilt) and use KSP, not kapt, wherever
   the processor supports it. Check each processor's own release notes for the AGP generation in use.
8. **Spec.** A document that names a library (for example "Navigation Compose") outranks a newer alternative.

## Procedure

1. Fill the matrix from the sources above, with the date and the source of each fact.
2. Pin versions in the version catalog; keep a single source of truth. Pin later-milestone libraries in the catalog
   only; add them to the module when something uses them.
3. Build. A gate that passes on paper but fails in a build is a gate you did not know about; add it to the
   troubleshooting reference.
4. List held-back versions with the blocking gate, so they can be raised when the gate changes.
