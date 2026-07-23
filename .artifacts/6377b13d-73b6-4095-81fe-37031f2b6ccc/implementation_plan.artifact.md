# SASSA Shield — Android Client Implementation Plan

This plan outlines the steps to build the Android component of SASSA Shield, which will act as a real-time monitor for fraudulent SMS messages.

## User Review Required

> [!IMPORTANT]
> **SMS Permissions**: To provide real-time scanning, the app will require the `RECEIVE_SMS` permission. This is a sensitive permission that Google Play Store scrutinizes. For an MVP/Development build, we will proceed with it.
> **Notification Permissions**: On Android 13+, we will also need `POST_NOTIFICATIONS` to alert the user about detected scams.

## Proposed Changes

### [Android App]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/MainActivity.kt)
- Update the UI to show a "Shield Dashboard".
- Handle runtime permission requests for SMS and Notifications.

#### [NEW] [SmsReceiver.kt](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/SmsReceiver.kt)
- A `BroadcastReceiver` that listens for incoming SMS messages.
- Extract sender and message body.
- Trigger local (mock) classification logic.

#### [NEW] [ScamDetector.kt](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ScamDetector.kt)
- Logic to analyze message text.
- Initially will use rule-based keywords (SASSA, PIN, Verify, etc.) to match the "Hybrid Scoring" plan.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/src/main/xml/AndroidManifest.xml)
- Add `<uses-permission android:name="android.permission.RECEIVE_SMS" />`.
- Add `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`.
- Register the `SmsReceiver`.

#### [DELETE] [BakingScreen.kt](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/BakingScreen.kt)
#### [DELETE] [BakingViewModel.kt](file:///C:/Users/judas/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/BakingViewModel.kt)
- Cleanup legacy project code.

## Verification Plan

### Manual Verification
1. Launch app and grant SMS permissions.
2. Use an emulator or a real device to send an SMS containing "Your SASSA PIN has expired. Click here to verify: bit.ly/fake-sassa".
3. Verify that the app detects the message and shows a notification/alert.
4. Verify the dashboard updates with the detected scam.

### Automated Tests
- Unit test for `ScamDetector` to ensure keyword matching works as expected.
