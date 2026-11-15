# Ruby's Inn Android app — build-ready project

This is a native Android WebView client for the Ruby's Inn Version 3 reception application. It is not the hotel server: the Windows 11 reception computer must run the V3 Node.js service and keep the database.

## App features
- Ruby's Inn dark-and-gold mobile shell
- First-launch server URL entry, saved on the phone
- Change server address later with the gear button
- Refresh, Android back navigation, page progress indicator
- JavaScript and DOM storage for the existing web app
- Opens telephone, WhatsApp, email and other non-web links with the relevant phone app
- Works with the server on a trusted private Wi-Fi network

## Build a debug installation APK on Windows
1. Install Android Studio and Android SDK Platform 35.
2. Install JDK 17 (Android Studio's bundled JBR is also suitable for Android Studio builds).
3. Extract this project folder.
4. Open the `rubys-inn-android` folder in Android Studio and wait for Gradle Sync.
5. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. Android Studio creates `app/build/outputs/apk/debug/app-debug.apk`.
7. Copy that APK to the Android phone, open it, allow installation from that source when prompted, and install.

Alternatively, install Gradle 8.9 and run `BUILD-APK-WINDOWS.bat` from this folder.

## Optional automated build
This project includes `.github/workflows/build-apk.yml`. Upload the project to a GitHub repository, open **Actions → Build Ruby's Inn Android APK → Run workflow**, then download the `rubys-inn-debug-apk` artifact from the completed run. The workflow creates a debug APK, suitable for testing and direct installation, not a Play Store release.

## Connect the phone
1. Start Ruby's Inn Version 3 on the Windows 11 reception server.
2. Put the Android phone and reception PC on the same trusted Wi-Fi network.
3. On Windows, run `ipconfig` and find the Wi-Fi adapter's full IPv4 address.
4. On first app launch enter `http://FULL-IP-ADDRESS:3000`, for example `http://192.168.1.25:3000` (example only; replace with the actual address).
5. Ensure Windows Firewall permits the app's server port on the Private network profile.

The APK does not contain the hotel database or server. It connects to the running V3 server. The app's initial URL field is intentionally blank/defaulted to an example; enter the actual server address. Do not expose this cleartext HTTP setup directly to the public internet. For access outside the local Wi-Fi, deploy HTTPS behind a properly secured reverse proxy first.

## Build configuration
- Application ID: `com.rubysinn.mobile`
- Version: `1.0.0` (versionCode 1)
- Minimum Android: Android 6.0 (API 23)
- Compile/target SDK: 35
- Android Gradle Plugin: 8.7.3
- Gradle: 8.9
- JDK: 17

## Release signing
The debug APK is signed with the standard debug key and is intended for testing/internal installation. For a production release, use Android Studio **Build → Generate Signed Bundle / APK**, create a private release keystore, and store it securely. Do not send the keystore or passwords in chat. Keep the same signing key for future app updates.

## Current build limitation
The project has been packaged and its source/configuration reviewed, but this workspace has no Android SDK or Gradle installation. Therefore no APK has been compiled or device-tested in this session. Use Android Studio or the included GitHub Actions workflow to produce the actual APK.
