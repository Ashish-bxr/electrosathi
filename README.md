# ElectroSathi

**On-device personalised learning assistant for electronics students — PS4 entry for the Bharat AI-SoC Challenge 2026–27** (MeitY Chips-to-Startup Programme + Arm; mentor institute: IIT Delhi).

ElectroSathi is an Android app that takes a learner from *interest → passion → profession*: a first-run Skill Check finds their real skill gaps, the app then orders their learning around those gaps, and **Volt**, an on-device AI tutor, answers electronics questions with the internet off.

The app is **100% offline**. Student profiles, login, all content, Volt chats and Skill Check history live in an on-device SQLite database — there is no server and no cloud backend. Volt runs Llama 3.2 1B on the phone itself via ExecuTorch (measured 13.6 tokens/sec on a short answer, realme RMX3782 / Dimensity 6100+).

## What the app does

- **Study** — Analog Circuits, Digital Electronics, and Signals & Systems, each with topics and flashcards.
- **Skill Check (first run)** — a ~12-question, ~3-minute quiz across the three subjects. Results are per-subject (*gap / shaky / strong*, shown as topic names), stored per student, and shape the Home screen, My Pathway and the Study Plan. Retake anytime from Study — every attempt is saved.
- **My Progress** — first attempt vs latest, per subject, updating the moment a retake finishes.
- **Exams** — GATE, ESE, ISRO, DRDO, BARC, SSC JE, RRB JE, PSUs via GATE, CDS/AFCAT, GRE/TOEFL, CAT, with real dates and live countdowns.
- **Projects Hub** — guided electronics projects from basic to advanced, with component costs in ₹.
- **Opportunities & Internships** — sector-wise career opportunities (India + abroad) and internships with real Apply links.
- **Live Hackathons** — current hackathons with registration deadlines.
- **E-Library** — ECE books and notes.
- **Volt (on-device AI tutor)** — Llama 3.2 1B Instruct running on the phone via ExecuTorch (XNNPACK CPU), temperature 0.6. No internet needed once the model is on the device. Chats are stored on-device and reload when you return, and Volt remembers your previous question in a conversation.
- **My Profile** — your details, Skill Check results and focus topics, all stored only on your phone.
- **Language** — English / हिन्दी switch in the drawer; app screens and Volt switch instantly (lesson content stays English in Phase 1).
- **Admin Panel (no-code)** — the team manages internships, exams, projects, hackathons, books and study subjects from inside the app — no Android Studio needed to change content. Edits save to the on-device database on that phone; the first account created on a phone is that phone's admin.

## How it maps to PS4's example application areas

| Area | Status |
| --- | --- |
| Career guidance | ✅ Opportunities, internships, career maps |
| Skill development | ✅ Study content + Projects Hub |
| Exam preparation | ✅ Exams section with real dates + countdowns |
| Technical learning roadmaps | ✅ My Pathway (Skill Check → gaps → ordered pathway → study plan) + My Progress report |
| Professional certification pathways | — out of scope for this entry |
| Lifelong learning plans | — out of scope for this entry |

## Tech stack

- **Language / UI:** Kotlin, Jetpack Compose (Material 3), dark-first "midnight study" theme
- **Storage (100% on-device):** SQLite (`electrosathi.db`) — local accounts (salted SHA-256 password hashes), local content store, Volt chat history, Skill Check attempts. No network calls in normal use.
- **On-device AI:** ExecuTorch Android `org.pytorch:executorch-android:1.1.0` + `com.facebook.fbjni:fbjni:0.5.1` + Llama 3.2 1B Instruct (SpinQuant INT4, pre-exported `.pte`)
- **Build:** Android Gradle Plugin 8.5.2, Kotlin 2.0.20, compileSdk 34, minSdk 26, JDK 17

## Build it

1. Open the project in **Android Studio** (Ladybug or newer).
2. Sync Gradle, then **Run** on a physical Arm64 Android phone. No server setup, no config file, no account needed — the app works fully offline.
3. On first launch, create a local account — the first account on a phone becomes that phone's admin.
4. Or install the ready APK from this repo's **Releases** page.

### Volt model files (not in this repo)

The model is ~1.14 GB and is **not** committed. Download it once, on Wi-Fi, from Hugging Face (not gated — no account or token needed):

- Repo: `executorch-community/Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8-ET`
- Files: `Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8.pte` (1,135,951,488 bytes) and `tokenizer.model` (rename to `tokenizer.bin`)

In the installed app the model arrives by a one-time in-app download (Volt screen → Download, Wi-Fi recommended, resumable, size-checked). For testing, the same files can be pushed with adb: `adb push <file> /sdcard/Android/data/com.electrosathi.app/files/volt/`

Use of the model is under the **Llama 3.2 Community License** — see the model repo for the licence text.

## Project structure

```
app/                  Android app module (Compose UI, data layer, Volt engine)
  src/main/java/com/electrosathi/app/
README.md             This file
```

## Team

Three-member student team — Build & Integration Lead, Content & Data Lead, Testing & Outreach Lead. Content is managed through the in-app Admin Panel; only the Build Lead touches Android Studio.

## Links

- Challenge: https://arm-education.github.io/Arm-Developer-Labs/Bharat_AI_SoC_2026_27.html
