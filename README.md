# Muslim Life — Android project starter

## What is included in this ZIP
- Native Android app using Kotlin + Jetpack Compose.
- Prayer time calculation using the Adhan library, with selectable calculation method and Hanafi/Shafi Asr setting.
- User-entered latitude/longitude saved on the device.
- Manual prayer-time entry mode.
- Ishraq and Tahajjud guidance.
- Qibla bearing calculation (bearing only; no live compass sensor yet).
- Tasbih counter with 33/99/100 targets; count saved locally.
- Ramadan section with fasting/iftar information and a dua.
- Quran tab placeholder for Arabic Quran text and Hindi/Urdu translations.
- GitHub Actions workflow to build a debug APK.

## Important: what is not finished
This is a substantial starter project, **not yet a complete Play Store-ready religious app**. It does not yet include automatic GPS permission/location lookup, prayer/iftar notifications, full month Ramadan timetable, a verified full Quran database, complete Hindi/Urdu Quran translations, audio recitation, full multilingual UI, offline Quran packs, Hijri calendar, or a live rotating Qibla compass. These need verified data, testing, and additional implementation. Sample/algorithmic prayer times must be checked against the user's local mosque or official calendar before relying on them. Quran and dua content must be reviewed by qualified sources before release.

## Upload from an Android phone
1. Download and extract `Muslim-Life-Android.zip`.
2. Open your GitHub repository `muslim-life-app`.
3. Choose **Add file → Upload files**.
4. Select all extracted files and folders (including `.github`) from the extracted project. Do not upload only the outer ZIP.
5. Commit the upload to branch `main`.
6. Open the **Actions** tab. Choose **Build Muslim Life APK** and run it if it did not start automatically.
7. When successful, open the run and download the `Muslim-Life-debug-APK` artifact.

The debug APK is for testing only. Google Play requires a properly signed release Android App Bundle (AAB), store listing, privacy policy, testing, and Play Console setup.
