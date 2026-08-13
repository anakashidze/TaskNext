# TaskNext

Android app matching the spec: pick one task to focus on per day, cycle
through a backlog, track history.

## How to open this

1. Install Android Studio (Ladybug or newer).
2. `File → Open` → select this `TaskNext` folder.
3. Let Gradle sync (Android Studio will generate the Gradle wrapper for you
   automatically the first time — no wrapper jar is checked in here on
   purpose, since it's a binary I can't fetch from this environment).
4. Run on your S22 (USB debugging on) or an emulator.
   `minSdk` is 26 so it installs on your S22 regardless of exactly which
   Android/One UI version it's currently on; `targetSdk`/`compileSdk` are 35.

## What's implemented

- **One mandatory task per day.** It's the only thing that can mark the day
  "complete." Completing it triggers a full green "Day complete" screen —
  no active-task list, no more prompts today.
- **Bonus tasks** (up to 2 more, added from the list) are reminder-only:
  completing them logs to history but never ends the day, and they stay
  visible under the green banner if still open, since they're your own
  reminders and shouldn't silently disappear.
- Completing the mandatory task opens a **"Set tomorrow's focus"** dialog —
  it does not start a new task today. It suggests the next item in your
  backlog by default, or you can pick any other pool task. That choice is
  queued and only becomes tomorrow's mandatory task when the calendar day
  actually rolls over (checked each time the app opens).
- **Task list / backlog**: ordered so the **last item is tomorrow's task**,
  and items above it are further out — matches how you described it. Move
  items up/down to reorder; the bottom-most item is always what gets
  suggested (and used, if auto-fill were added) for tomorrow.
- **Settings**: notifications on/off, default reminder time, snooze
  minutes, dark theme, history grouping.
- **History**: every completed task (mandatory or bonus) with date/time,
  reachable from Settings.

- Reminders use `AlarmManager.setExactAndAllowWhileIdle` + a
  `BroadcastReceiver` that posts a notification.

## Known gaps / things you'll likely want to adjust

- Reminders are rescheduled only when a task is added/edited, and are not
  currently re-armed automatically after a phone reboot (the `BootReceiver`
  is registered but is a no-op — see the comment in that file for what a
  full implementation would do).
- No drag-and-drop reordering yet — up/down arrow buttons instead. Easy
  upgrade later with `androidx.compose.foundation` reorderable-list APIs.
- Only 3 active slots are wired in the UI/repository logic, matching "one
  mandatory, plus optionally a second or third" from your spec.
- App icon is a placeholder vector checkmark — swap
  `ic_launcher_foreground.xml` / `ic_launcher_background` color for
  something real whenever you like.
