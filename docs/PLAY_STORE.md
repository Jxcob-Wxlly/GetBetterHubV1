# Play Store readiness

## 1. Create a signing key (once)
```
keytool -genkeypair -v -keystore gbh-release.jks -alias gbh -keyalg RSA -keysize 2048 -validity 10000
```
Put the file next to `GetBetterHub-android/build.gradle.kts`, copy `keystore.properties.example` to `keystore.properties` and fill it in. Neither file is committed (see `.gitignore`).

## 2. Build the signed release
Android Studio: **Build > Generate Signed App Bundle / APK**, or from the terminal:
```
gradle assembleRelease -PapiBaseUrl=https://YOUR-API-HOST/
gradle bundleRelease  -PapiBaseUrl=https://YOUR-API-HOST/
```
Outputs: `app/build/outputs/apk/release/app-release.apk` and `app/build/outputs/bundle/release/app-release.aab`.
(For a demo against your PC, use `-PapiBaseUrl=http://localhost:5080/` with `adb reverse tcp:5080 tcp:5080`.)

## 3. Store listing assets
- App icon 512 x 512: `docs/images/play-store-icon-512.png` (cropped from your original logo).
- Phone screenshots: take at least 2 (ideally 5 to 8) on the phone: Home, Practice, Profile, Achievements, Settings, one non-English screen.
- Feature graphic 1024 x 500 (any simple banner with the icon and name).
- Short description (80 chars): _Practice smarter: drills, mood coaching, lyrics and challenges for musicians._
- Full description: reuse section 1 of `README.md`.

## 4. Play Console steps (screenshot for the PoE)
1. Create a developer account, then **Create app** (name: Get Better Hub, free, app).
2. Fill **Store listing** with the assets above.
3. Complete **App content**: privacy policy URL, data safety (account info, audio recordings), target audience 13+, ads: none.
4. Upload the `.aab` under **Testing > Internal testing > Create release**.
5. Take the Play Console screenshot showing the app dashboard / the release ready for review.
