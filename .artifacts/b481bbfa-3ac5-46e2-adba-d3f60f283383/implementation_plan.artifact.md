# Implementation Plan - App Publishing Preparation

Preparing **SASSA Shield** for public release by optimizing the build, ensuring security compliance, and updating metadata.

## User Review Required

> [!CAUTION]
> **Google Play Store Policy Warning**:
> This app uses `RECEIVE_SMS` and `CallScreeningService`. Google Play has very strict policies for these:
> 1. **SMS/Call Log Permission**: Apps must be the **Default SMS** or **Default Phone** app to use these, OR they must fit a narrow "Safety/Emergency" exception.
> 2. **Review Process**: Expect a rigorous manual review. You may need to provide a video demonstration of why these permissions are essential to the core functionality.

> [!IMPORTANT]
> **Signing Key**: I cannot generate or store a production signing key for you. You will need to create one via **Build > Generate Signed Bundle / APK** once the configuration is ready.

## Proposed Changes

### 1. App Metadata & Branding
- **[MODIFY] `strings.xml`**:
    - Change `app_name` to "SASSA Shield".
    - Clean up unused template strings (Baking/Gemini leftovers).
- **[MODIFY] `AndroidManifest.xml`**: Ensure `android:label="@string/app_name"` is used.

### 2. Build Configuration (Release)
- **[MODIFY] `app/build.gradle.kts`**:
    - Enable **Minification** (isMinifyEnabled = true) and **Resource Shrinking** (isShrinkResources = true).
    - Link to a `proguard-rules.pro` file.
    - Increment `versionCode` and `versionName` for a fresh start.

### 3. ProGuard/R8 Rules
- **[NEW] `proguard-rules.pro`**:
    - Add rules to keep **Room**, **Retrofit**, **Gson**, and **Coroutine** classes to prevent crashes after obfuscation.

### 4. Code Cleanup
- **[MODIFY] `SmsReceiver.kt` & `MyCallScreeningService.kt`**: Ensure no debug `println` or sensitive data is logged in release builds.

## Verification Plan

### Build Verification
- Run `./gradlew assembleRelease` to ensure the release APK builds without errors.
- Run `./gradlew bundleRelease` to generate the Android App Bundle (AAB).

### Manual Verification
- Test the **Release** build on a device to ensure Room and Networking still work (R8 can sometimes "over-shrink" and cause crashes if rules are missing).
