# Build TaskNext with GitHub Actions

1. Create a GitHub repository, for example `TaskNext`.
2. Upload/commit the CONTENTS of this folder to the repository root.
3. Open the repository's **Actions** tab.
4. Choose **Build Android APK**.
5. Click **Run workflow** → **Run workflow**.
6. Open the finished workflow run.
7. Under **Artifacts**, download **TaskNext-debug-apk**.
8. Extract the downloaded ZIP. It contains `app-debug.apk`.
9. On Android, allow your browser/file manager to install unknown apps if Android asks, then open `app-debug.apk`.

The workflow also builds automatically whenever you push to the `main` branch.

## What was adjusted for GitHub build

- Android Gradle Plugin: 8.5.2 → 8.6.1, because this app compiles against API 35.
- GitHub Actions installs JDK 17, Android API 35, Build Tools 34.0.0, and Gradle 8.7.
- The project has no Gradle wrapper, so the workflow invokes `gradle` directly.
- Fixed `DayCompleteBanner()` so its use of `Modifier.weight()` has a `ColumnScope` receiver.

## Output

Debug APK:
`app/build/outputs/apk/debug/app-debug.apk`

For Google Play, do not use this debug APK. Configure release signing and build an Android App Bundle (`.aab`).
