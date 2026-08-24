# _init_ /space

**A frictionless capture inbox for thoughts, moments, reminders, and voice notes.**

![Downloads](https://img.shields.io/github/downloads/rKyzen/_init_-space/total?style=flat-square&label=downloads)
[![Download Latest Release](https://img.shields.io/badge/⬇%20Download-Latest%20Release-white?style=for-the-badge)](https://github.com/rKyzen/_init_-space/releases/latest)

`_init_ /space` is the second utility in the `_init_` suite, joining `_init_ /files`. Designed with extreme digital minimalism and bare-metal performance, `_init_ /space` serves as a lightning-fast inbox to capture anything on your screen, mind, or voice before it's gone.

---

## Why

Most note and capture apps suffer from feature creep, slow startup times, and formatting clutter. `_init_ /space` treats capture as a core operating system utility: trigger a hardware gesture from any screen, attach a thought or voice memo, and get right back to what you were doing. All memory is indexed locally, searchable in milliseconds, and free of cloud dependencies.

---

## Features

- **Hardware Shortcut Capture** — Trigger capture from anywhere across the OS by pressing Volume Up + Volume Down simultaneously.
- **Instant Overlay HUD** — Minimal non-intrusive capture panel with quick note input and one-tap AAC voice memo recording.
- **Multimodal AI Summaries** — Free OpenRouter AI summaries (`openrouter/free`, `google/gemma-4-31b-it:free`, etc.) that synthesize screenshot context with user notes.
- **Reminders & Calendar Sync** — Set scheduled alarm reminders with direct export to Android Calendar and System Clock alarms.
- **Chronological Timeline & Search** — Filter captures by all, notes, voice recordings, starred favorites, and active reminders with instant substring search.
- **Audio Memo Player** — Inline waveform audio playback controller for saved voice recordings.
- **Boot Sequence Splash** — Full-screen startup video on cold launch true to the `_init_` aesthetic.
- **Local-First & Private** — Direct SQLite storage on-device with zero analytics, tracking, or remote database synchronization.

---

## Design System

Strictly aligned with `_init_ /files` and the `_init_` product family:

- **Typography (Headers):** `Michroma` — used sparingly, ALL CAPS, for titles, section headers, and brand marks.
- **Typography (Body & Controls):** `JetBrains Mono` — used for all notes, labels, timestamps, metadata, and counters.
- **Palette:** Dark-first, near-black canvas (`#000000` / `#0C0C0C`). **Zero red anywhere in the UI** — warnings and destructive actions use muted amber (`#D99B26`) and monochrome outlines.
- **Motion:** Mechanical directional slide transitions on tab switching, spring scale physics on navigation controls, and subtle alpha fades.

---

## Architecture & Tech Stack

- **UI Framework:** 100% Jetpack Compose (Compose BOM 2024.12.01, Material 3)
- **Target Platform:** Android SDK 36 (Android 15 / UpsideDownCake+), Min SDK 28
- **Database:** High-performance direct `SQLiteOpenHelper` with reactive `StateFlow` streams (preserving `essential_space_db` table schema migrations)
- **Hardware Interception:** Android Accessibility Service utilizing synchronous `FLAG_REQUEST_FILTER_KEY_EVENTS` for volume slider suppression
- **Media:** Android Media3 ExoPlayer & high-fidelity AAC voice recording
- **Image Loading:** Coil 2.7.0 with video thumbnail decoders
- **AI Backend:** OpenRouter HTTP chat completions with encrypted token protection

---

## Getting Started

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/rKyzen/_init_-space.git
   cd _init_-space
   ```
2. Configure `local.properties` (optional for custom AI keys):
   ```properties
   openrouter.api.key=YOUR_OPENROUTER_KEY
   openrouter.model=openrouter/free
   ```
3. Assemble debug APK:
   ```powershell
   .\gradlew assembleDebug
   ```
   The compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Part of the `_init_` Suite

`_init_` is a boot-sequence-inspired suite of Android system utilities built for performance and digital minimalism.
- [`_init_ /files`](https://github.com/rKyzen/_init_-files) — System file explorer
- [`_init_ /space`](https://github.com/rKyzen/_init_-space) — Memory and capture inbox

---

## License

MIT
