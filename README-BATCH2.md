# ElectroSathi — Batch 2: Clickable detail screens

This batch makes the app feel real: every exam, project and internship card
now opens a full detail page, with back navigation.

## Copy these files into your project (same relative paths, replace when asked)

NEW files:
- `app/src/main/java/com/electrosathi/app/data/Models.kt`
- `app/src/main/java/com/electrosathi/app/data/Repository.kt`
- `app/src/main/java/com/electrosathi/app/ui/screens/DetailScreens.kt`

REPLACE these two:
- `app/src/main/java/com/electrosathi/app/MainActivity.kt`
- `app/src/main/java/com/electrosathi/app/ui/screens/Screens.kt`

No new libraries were added, so no Gradle sync is needed.
Just **Build → Make Project** and Run.

## What changed

1. **Data layer** (`data/` package): `Models.kt` defines Exam/Project/Internship.
   `Repository.kt` is the single source of truth — hardcoded lists for now.
   Phase 2 note is in the file: when Firebase arrives, only the Repository's
   insides change; no screen needs rewriting.
2. **Detail screens** (`DetailScreens.kt`):
   - Exam detail: overview, eligibility, pattern, syllabus, dates, papers,
     cutoff + a "Make my prep plan" button (wired in Phase 3).
   - Project detail: level/time/cost chips, components list, numbered build
     steps, "what you'll learn", "Ask Volt" and "Mark as built" buttons.
   - Internship detail: org chips, highlighted deadline card, description,
     eligibility, "Apply now" + "Track this application" buttons.
3. **Navigation** (`MainActivity.kt`): new routes `examDetail/{examId}`,
   `projectDetail/{projectId}`, `internshipDetail/{internshipId}`. The top bar
   shows a back arrow on detail screens and the real item name as the title;
   the bottom bar hides on detail screens for a full-page feel.

## Try it

- Exams → tap **GATE (EC)** → full detail page → back arrow returns you.
- Projects → switch level chips → tap a project → build guide.
- Career → tap the **DRDO RCI** card → deadline card + Apply button.

Next up: Batch 3 (admin panel + Firebase) — your team posts internships
from their phones and they appear here instantly.
