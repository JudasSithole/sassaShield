# Implementation Plan - Offline Resilience & Local Security

Ensuring the app remains functional and protective even without Wi-Fi or mobile data by implementing local caching, offline heuristics, and manual blocklists.

## User Review Required

> [!NOTE]
> Offline protection relies on your previously identified scam calls. The app will "remember" high-risk numbers it has seen before.

## Proposed Changes

### 1. Data Layer Enhancements
- **[MODIFY] `ScamDao`**: Add `getScamCallByNumber(phone: String)` to allow quick lookup of previously identified callers.
- **[NEW] `LocalBlocklist` Entity**: A simple table for numbers the user has manually blocked.

### 2. Networking & Repository Logic
- **[MODIFY] `PhoneRepository`**:
    - Implement a "Cache-First" strategy: Check the local database before calling the API.
    - Handle network exceptions gracefully (e.g., timeout or no connection).
    - If offline, return a "Offline Verdict" based on local knowledge.

### 3. Call Screening Service (Offline Logic)
- **[MODIFY] `MyCallScreeningService`**:
    - If the API is unreachable, fall back to **Local Heuristics**:
        - Block numbers in the `LocalBlocklist`.
        - Flag international numbers (if the user is in South Africa, SASSA calls should be local).
        - Use the most recent cached risk level for that number.

### 4. UI Updates
- **[MODIFY] `MainActivity` (History Tab)**:
    - Add a "Block Number" button to each call log entry.
    - Add a "Unblock" option if a number was manually blocked.
    - Show an "Offline" status indicator if the API is unreachable.

## Verification Plan

### Automated Tests
- Test `PhoneRepository` behavior when the network is mocked as "unavailable".
- Verify that `getScamCallByNumber` returns the correct cached risk level.

### Manual Verification
- Disable Wi-Fi/Data and simulate a call from a number that was previously flagged (should be blocked via cache).
- Manually block a number in the UI and verify it is automatically rejected on the next call.
