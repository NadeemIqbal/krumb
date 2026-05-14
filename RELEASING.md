# Releasing

Maintainer-only. Krumb publishes to **Maven Central** (Sonatype Central Portal)
via the [Vanniktech maven-publish plugin](https://github.com/vanniktech/gradle-maven-publish-plugin).

All three library modules (`krumb-core`, `krumb-compose`, `krumb-material3`)
publish together, each with every platform variant (android, jvm, iosX64,
iosArm64, iosSimulatorArm64, wasmJs) plus the root Kotlin Multiplatform
metadata artifact.

## Cutting a release (CI — the normal path)

The version is **driven by the git tag** (`KRUMB_RELEASE_VERSION`), so there's
no version to bump in code:

```bash
git tag v0.2.0
git push origin v0.2.0
```

`.github/workflows/release.yml` then:

1. runs the unit tests,
2. publishes all modules + platforms to a **staged** Central Portal deployment
   (version `0.2.0`, from the tag),
3. creates a GitHub Release with auto-generated notes.

Finally, **approve the staged deployment** in the
[Central Portal](https://central.sonatype.com) UI — that's the one
irreversible step, kept manual on purpose (`automaticRelease = false`).

> To fully automate (skip the manual approval), change the publish step to
> `publishAndReleaseToMavenCentral` and set `automaticRelease = true` in the
> root `build.gradle.kts`.

## One-time setup

### 1. Namespace

`io.github.nadeemiqbal` must be a verified namespace on the
[Central Portal](https://central.sonatype.com) account (verified by owning
the `NadeemIqbal` GitHub account).

### 2. GitHub Actions secrets

Add these as repository secrets (`gh secret set <NAME>` or repo Settings →
Secrets and variables → Actions):

| Secret | Value |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Central Portal token username |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal token password |
| `SIGNING_IN_MEMORY_KEY` | armored GPG private key |
| `SIGNING_IN_MEMORY_KEY_ID` | GPG key id |
| `SIGNING_IN_MEMORY_KEY_PASSWORD` | GPG key password |

### 3. Local credentials (optional — for local publishing only)

In `~/.gradle/gradle.properties` (never commit):

```properties
mavenCentralUsername=<central-portal-token-user>
mavenCentralPassword=<central-portal-token-pass>
signingInMemoryKey=<armored-gpg-private-key>
signingInMemoryKeyId=<key-id>
signingInMemoryKeyPassword=<key-password>
```

## Releasing locally (fallback)

```bash
# dry run — verify every artifact builds and signs cleanly
./gradlew publishToMavenLocal -PkrumbVersion=0.2.0

# upload a staged deployment to Central
./gradlew publishToMavenCentral -PkrumbVersion=0.2.0
```

`signAllPublications()` only runs when `signingInMemoryKey` is present, so
`publishToMavenLocal` works credential-free for local verification.

## Versioning notes

- Dev builds with no tag/property default to `0.1.0-SNAPSHOT`.
- The tag drives the version: `v0.2.0` → `0.2.0` (the `v` prefix is stripped).
- Maven Central rejects duplicate versions — never re-tag an already-published version.
