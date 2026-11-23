# ElectroSathi

**On-device personalised learning assistant for electronics students — PS4 entry for the Bharat AI-SoC Challenge 2026–27** (MeitY Chips-to-Startup Programme + Arm; mentor institute: IIT Delhi).

ElectroSathi is an Android app that takes a learner from *interest → passion → profession*: a first-run Skill Check finds their real skill gaps, the app then orders their learning around those gaps, and **Volt**, an on-device AI tutor, answers electronics questions with the internet off.

> Phase 1 status (20 Nov 2026): the app is **100% offline**. Student profiles, login, all content, Volt chats and Skill Check history live in on-device SQLite — there is **no cloud backend anywhere** (Batch 14, 20 Nov 2026, removed Firebase/Firestore entirely). Volt is live inside the app (Llama 3.2 1B via ExecuTorch, Batch 9, proven on-device ~03:00 IST 12 Nov; 13.6 tokens/sec on a short answer, realme RMX3782). The Skill Check → My Pathway engine (Batch 10) and the in-app Volt model download (Batch 11) ran green in a combined build on 12 Nov ~15:03 IST; Volt control fixes (Batch 12: Stop button, one-question Quiz-me, auto-scroll) are installed; Copy + Attach (Batch 13) is built. Batches 15–16 (20 Nov 2026) added SQLite Volt chat history, temperature 0.6 with total inference time in every answer caption, saved Skill Check attempts with a My Progress improvement report, and English / हिन्दी language switching. Still to capture: the Batches 14–16 on-phone install + airplane-mode run, the tester-phone download proof, and a long-answer speed reading. Submission target: 24 Nov 2026 (25 Nov is buffer; deadline extended from 23 Nov).

## What the app does

- **Study** — Analog Circuits, Digital Electronics, and Signals & Systems, each with real topics and flashcards; Micro Lab (8051/8085 assembler + step simulator) inside Study.
- **Skill Check (first run)** — a ~12-question, ~3-minute quiz across the three subjects. Results are per-subject (*gap / shaky / strong*, shown as topic names), stored per student, and shape the Home screen, My Pathway and Study Plan. Retake anytime from Study — every attempt is saved.
- **My Progress** — first attempt vs latest, per subject, updating the moment a retake finishes (the real-time improvement report).
- **Exams** — GATE, ESE, ISRO, DRDO, BARC, SSC JE, RRB JE, PSUs via GATE, CDS/AFCAT, GRE/TOEFL, CAT, with real dates and live countdowns.
- **Projects Hub** — guided electronics projects from basic to advanced, with component costs in ₹.
- **Opportunities & Internships** — sector-wise career opportunities (India + abroad), internships with real Apply links, and a deadline tracker.
- **Live Hackathons** — current hackathons, kept fresh by the Content Lead.
- **E-Library** — ECE books and notes (books + notes only).
- **Volt (on-device AI tutor)** — Llama 3.2 1B Instruct running on the phone via ExecuTorch (XNNPACK CPU), temperature 0.6. No internet needed once the model is on the device; Volt admits when it is unsure instead of guessing. Chats are stored in on-device SQLite and reload when you return.
- **Language** — English / हिन्दी switch in the drawer; app screens and Volt switch instantly (lesson content stays English in Phase 1).
- **Admin Panel (no-code)** — the team manages internships, exams, projects, hackathons, books and study subjects from inside the app. No Android Studio needed to change content. Edits save to the on-device database on that phone; the first account created on a phone is that phone's admin.

## How it maps to PS4's example application areas

| Area | Status |
|---|---|
| Career guidance | ✅ Opportunities, internships, career maps |
| Skill development | ✅ Study content + Projects Hub |
| Exam preparation | ✅ Exams section with real dates + countdowns |
| Technical learning roadmaps | ✅ My Pathway (Batch 10: Skill Check → gaps → ordered pathway → study plan) + My Progress improvement report (Batch 16) |
| Professional certification pathways | — out of scope for this entry |
| Lifelong learning plans | — out of scope for this entry |

## Tech stack

- **Language / UI:** Kotlin, Jetpack Compose (Material 3), dark-first "midnight study" theme
- **Storage (100% on-device):** SQLite (`electrosathi.db`) — local accounts (salted SHA-256 password hashes), local content store, Volt chat history, Skill Check attempts. No server, no cloud backend, no network calls in normal use.
- **On-device AI:** ExecuTorch Android `org.pytorch:executorch-android:1.1.0` + `com.facebook.fbjni:fbjni:0.5.1` + Llama 3.2 1B Instruct (SpinQuant INT4, pre-exported `.pte`) — 1.1.0 is the exact runtime version that ran the model on the phone; do not use 1.5.1.
- **Build:** Android Gradle Plugin 8.5.2, Kotlin 2.0.20, compileSdk 34, minSdk 26, JDK 17

## Build it

1. Open the project in **Android Studio** (Ladybug or newer).
2. Sync Gradle, then **Run** on a physical Arm64 Android phone. No Firebase setup, no config file, no account needed — the app works fully offline.
3. On first launch, create a local account — the first account on a phone becomes that phone's admin.

### Volt model files (not in this repo)

The model is ~1.14 GB and is **not** committed. Download it once, on Wi-Fi, from Hugging Face (not gated — no account or token needed):

- Repo: `executorch-community/Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8-ET`
- Files: `Llama-3.2-1B-Instruct-SpinQuant_INT4_EO8.pte` (1,135,951,488 bytes) and `tokenizer.model` (rename to `tokenizer.bin`)
- In the finished app the model arrives by a one-time in-app download (Batch 11: Volt screen → Download, Wi-Fi recommended, resumable, size-checked). For testing, the same files can be pushed with adb: `adb push <file> /sdcard/Android/data/com.electrosathi.app/files/volt/`

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
- Showcase site: https://muse.ai/s/electrosathi-project-showcase-zxe6xozn7xyxn4xb/GpMBtDKCTrdWXTeSLClMOuw86_a9OW9mv5nX59g1Jt4
