# SpendSense

Android-first offline personal finance tracker built from `trd.md` using spec-driven development.

## Architecture

Source layout:

```text
app/src/main/java/com/spendsense
core
  utils
  presentation
features
  ui
  data
  usecase
```

Current vertical slice:

```text
RawNotification
  -> sanitizer
  -> sensitive-message filter
  -> heuristic classifier
  -> deterministic parser
  -> validator
  -> Room repository
  -> Compose UI
```

## Run

Open the project in Android Studio and sync Gradle.

The project is pinned to JDK 21 for Gradle because LiteRT-LM artifacts are compiled with Java 21 bytecode.

## Bundled Local Model

SpendSense supports this local LLM order:

```text
Gemini Nano
  -> bundled LiteRT-LM model
  -> deterministic fallback
```

To package your own broad-device local model, add:

```text
app/src/main/assets/models/spendsense.litertlm
```

The app will copy that file into app-private storage on first use and route assistant requests through LiteRT-LM when Gemini Nano is unavailable.
