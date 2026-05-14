# Contributing to Krumb

Thanks for your interest! Issues and pull requests are welcome.

## Project layout

```
krumb-core/        Pure-Kotlin engine — no UI dependency. All logic + unit tests live here.
krumb-compose/     Compose UI host, animations, gestures, custom-content registry.
krumb-material3/   Material 3 themed defaults layered on krumb-compose.
sample/            Showcase apps — shared UI + per-platform entry points.
```

`krumb-core` must stay UI-framework-agnostic — **do not add Compose dependencies to it**.

## Building & testing

```bash
./gradlew :krumb-core:desktopTest        # engine unit tests
./gradlew :krumb-compose:desktopTest     # host UI tests
./gradlew :krumb-material3:assemble      # M3 module compiles
./gradlew :sample:desktopApp:run         # manual smoke test
```

Run the relevant tests before opening a PR. New behavior in `krumb-core`
should come with `kotlin.test` coverage.

## Pull requests

- Keep changes focused — one concern per PR.
- Match the existing code style (`kotlin.code.style=official`).
- Public API gets KDoc; internal classes don't need it.
- Describe the *why* in the PR description, not just the *what*.

## Reporting bugs

Open an issue with the platform(s) affected, Krumb version, and a minimal
repro. Screenshots/GIFs help for visual bugs.

## Releases

Publishing is maintainer-only — see [RELEASING.md](RELEASING.md).
