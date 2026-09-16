# TGC Giving Admin — Android source

Native Android (Kotlin, Jetpack Compose, min SDK 26) implementation of the
Rongai Campus giving-acknowledgement system, branded for The GO Church, plus
a second, independent "Follow Up" text sequence.

This is real, compilable source meant to be opened in Android Studio — not a
browser prototype. It has been checked by hand for syntax and by an
esbuild-based cross-check of the earlier web prototype's equivalent logic,
but it has **not** been built with the actual Android Gradle Plugin/SDK
(unavailable in this environment), so treat the first `./gradlew build` as
the real compile check and expect to fix minor version-compatibility nits
(AGP/Kotlin/Compose versions drift quickly).

## Opening the project
1. Open this folder directly in Android Studio (Koala/2024.1+ recommended).
2. Let Gradle sync — it will pull the versions pinned in `build.gradle.kts`
   and `app/build.gradle.kts`. Bump AGP/Kotlin/Compose BOM versions if your
   Studio install is older or newer.
3. Run on a device or emulator with SIM/SMS support — the Android *emulator*
   can receive test SMS via `adb emu sms send <number> <text>` for the
   M-PESA parsing flow, but cannot send real SMS. A physical device is
   needed to test outbound sending.

## What's implemented, file by file
- `data/` — Room entities and DAOs: `TransactionEntity` (Giving Queue),
  `TemplateEntity`, `SettingsEntity`, and three new tables for the Follow Up
  feature: `FollowUpStepEntity` (sequence steps), `FollowUpEnrolleeEntity`
  (name + mobile number), `FollowUpSentLogEntity` (prevents double-sends).
- `parser/MpesaParser.kt` — the regex engine from the spec, unchanged.
- `engine/` — `TemplateEngine` (token replacement, shared by both
  sequences) and `SmsDispatchEngine` (SmsManager wrapper).
- `notification/`, `receiver/` — the tagging notification and its
  Tithe/Offering/Seed action buttons.
- `work/` — every background job:
  - `GivingReplyWorker` — one-time job, delayed by **Settings → Message
    Timing** (the "how long until messages go out" control), sends the
    giving reply.
  - `GoogleSheetsSyncWorker` — hourly (or manual) push of unsynced giving
    transactions to the webhook.
  - `FollowUpCheckWorker` — runs every 6 hours (and on demand via "Run Now"
    in the Follow Up screen), checks every enrollee against the sequence,
    and sends whatever step is due.
  - `WorkScheduler` — the single place all of the above get scheduled from.
- `export/` — JSON export (via the platform's built-in `org.json`) and a
  minimal hand-rolled `.docx` writer, see note below.
- `repository/` — `GivingRepository` and `FollowUpRepository`, the only
  things the ViewModels talk to.
- `ui/` — four Compose screens wired to a bottom navigation bar: Giving
  Queue (Dashboard), **Follow Up** (new), Templates, Settings. Colors in
  `ui/theme/Color.kt` are sampled directly from the supplied logo file.
- `apps-script/webhook.gs` — paste into the target Google Sheet's Apps
  Script editor and deploy as a Web App; routes rows to a "Giving" tab or
  a "Follow Up" tab.

## The Follow Up feature (this request)
A second, independent sequence from the everyday Giving Queue:
- **Sequence builder** (Follow Up screen, top card): add or remove any
  number of messages; each has its own "day offset" (days after
  enrollment) and its own custom text with `<first name>` / `<full name>`
  tokens.
- **Enrollment**: a name + mobile number form adds a person to
  `followup_enrollees`; the list below shows how many of the sequence's
  messages have gone out and what's due next.
- **Scheduling**: `FollowUpCheckWorker` is the real equivalent of "how long
  until messages go out" for this sequence — it compares each enrollee's
  `enrolledTimestamp` against each step's `dayOffset` and sends anything
  due, logging it so nothing repeats.

## Two intentional deviations from the original spec — and why
1. **DOCX export uses a small hand-rolled OOXML writer instead of Apache
   POI.** `poi-ooxml` pulls in `javax.xml.stream` and AWT classes that are
   unreliable or absent on Android; the hand-rolled writer produces the
   same valid `.docx` (title + table) with zero extra dependencies and no
   runtime surprises.
2. **Kotlinx Serialization was dropped in favor of `org.json`**, which
   ships with the Android platform — one less dependency to version-match,
   for a payload this simple (flat objects, no nested generics).

## Known gaps to close before shipping
- The Follow Up sent-log is not yet pushed to the Google Sheet the way
  giving transactions are — `webhook.gs` already expects a `type:
  "followup"` row shape for this, but `FollowUpCheckWorker` doesn't send it
  yet. Small addition: emit a matching JSON payload after each send, the
  same way `GoogleSheetsSyncWorker` does for giving transactions.
- No launcher app icon artwork beyond a placeholder vector mark — swap
  `drawable/ic_launcher_foreground.xml` for real brand artwork.
- No automated tests yet (regex parser and TemplateEngine are the highest
  value targets for unit tests, since they're pure functions).
- Google Sheet "Test Connection" does a real POST to the webhook and only
  checks for a 2xx response — it doesn't verify the sheet name/tab exists.
