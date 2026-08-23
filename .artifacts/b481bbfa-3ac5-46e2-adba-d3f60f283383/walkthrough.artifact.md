# Walkthrough - App Publishing Preparation

I have prepared **SASSA Shield** for public release by optimizing the code and build configuration.

## Changes Made

### 1. App Branding & Identity
- **[strings.xml](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/src/main/res/values/strings.xml)**:
    - Formally changed the app name to **SASSA Shield**.
    - Removed all template strings related to "Baking with Gemini" to reduce clutter.

### 2. Release Optimization (R8/ProGuard)
- **[build.gradle.kts](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/build.gradle.kts)**:
    - Enabled **Minification** and **Resource Shrinking** for the Release build type.
    - Configured the project to use a custom ProGuard rules file.
- **[proguard-rules.pro](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/proguard-rules.pro)**:
    - Added specific rules to protect **Room**, **Retrofit**, **Gson**, and **Coroutines** from being broken during the shrinking process.

### 3. Build Verification
- Verified that both the **Release APK** and **Android App Bundle (AAB)** build successfully.

## Next Steps for You

### 1. Generate Signed Bundle
To actually publish to Google Play, you need to sign the app with your own private key:
1. Go to **Build > Generate Signed Bundle / APK...**
2. Choose **Android App Bundle**.
3. Create a new **Key Store** (keep this safe! If you lose it, you can't update your app).
4. Follow the wizard to generate the `.aab` file.

### 2. Google Play Console
1. Upload the generated `.aab` file to the [Google Play Console](https://play.google.com/console).
2. **Important**: When asked about permissions, you must provide a strong justification for `RECEIVE_SMS` and `CallScreeningService`, explaining that they are the core safety features of your app.
