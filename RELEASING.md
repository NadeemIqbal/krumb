# Releasing

Maintainer-only. Krumb publishes to **Maven Central** (Sonatype Central Portal)
via the [Vanniktech maven-publish plugin](https://github.com/vanniktech/gradle-maven-publish-plugin).

All three library modules (`krumb-core`, `krumb-compose`, `krumb-material3`)
publish together, each with every platform variant (android, jvm, iosX64,
iosArm64, iosSimulatorArm64, wasmJs) plus the root Kotlin Multiplatform
metadata artifact.

## One-time setup

1. **Verify the namespace.** `io.github.nadeemiqbal` must be a verified
   namespace on your [Central Portal](https://central.sonatype.com) account
   (verified by owning the `NadeemIqbal` GitHub account).

2. **Credentials.** Add to `~/.gradle/gradle.properties` (never commit these):

   ```properties
   mavenCentralUsername=<central-portal-token-user>
   mavenCentralPassword=<central-portal-token-pass>
   signingInMemoryKey=<armored-gpg-private-key>
   signingInMemoryKeyId=<key-id>
   signingInMemoryKeyPassword=<key-password>
   ```

## Cutting a release

1. Bump `krumbVersion` in the root `build.gradle.kts`.

2. **Dry run** — verify every artifact builds and signs cleanly:

   ```bash
   ./gradlew publishToMavenLocal
   ```

   Inspect `~/.m2/repository/io/github/nadeemiqbal/` — each artifact should
   have a matching `.asc` signature.

3. **Upload to Central:**

   ```bash
   ./gradlew publishToMavenCentral
   ```

   This uploads to a staging repository. `automaticRelease` is **off**, so
   nothing goes live yet.

4. **Approve** the staged release in the Central Portal web UI.

5. Tag the release:

   ```bash
   git tag v<version> && git push origin v<version>
   ```

> `publishAndReleaseToMavenCentral` skips the manual approval gate — avoid it
> until the release process is well-trusted.

## Signing notes

`signAllPublications()` only runs when `signingInMemoryKey` is present, so
`publishToMavenLocal` works credential-free for local verification while real
Central uploads are always signed.
