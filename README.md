# My Smart Call Recorder (starter project)

This is a starter Android Studio project for importing audio recordings shared from Google Phone and organizing them locally.

## What it does
- Receives shared `audio/*` files through Android's Share sheet.
- Also lets the user import an audio file with the system file picker.
- Copies imported audio into the app's private storage.
- Lets the user enter a contact name and optional phone number.
- Shows saved recordings with Play and Delete actions.

## Important limitations
- This app does **not** record phone calls itself.
- It cannot automatically access Google Phone's private call-recording storage or call log.
- The Google Phone app must offer Share/Export for a recording. If it does not, use the Import audio file button and select an exported audio file.
- The app does not guarantee that both sides of a call are present; that depends on the original recording created by the phone dialer.
- Ask for the other person's consent and follow local laws before recording or sharing calls.

## Build
1. Install Android Studio on a computer.
2. Open this project folder.
3. Let Android Studio sync Gradle (Android Gradle Plugin 8.7.3; JDK 17; SDK 35).
4. Choose Build > Build Bundle(s) / APK(s) > Build APK(s).
5. Install the generated APK on your Redmi phone.

This starter project is provided as source code, not a prebuilt APK.
