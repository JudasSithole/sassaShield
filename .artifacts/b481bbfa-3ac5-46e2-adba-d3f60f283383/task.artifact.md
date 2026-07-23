# Tasks - Offline Resilience & Local Security

- [x] 1. Data Layer Updates
    - [x] Update `ScamDao` with `getScamCallByNumber`
    - [x] Create `ManualBlock` entity
    - [x] Add `ManualBlock` to `ScamDatabase` (Version 3)
- [x] 2. Repository Logic
    - [x] Update `PhoneRepository` to check local DB first (Handled in Service)
    - [x] Add network error handling
- [x] 3. Call Screening Enhancements
    - [x] Implement manual blocklist check
    - [x] Implement international number heuristic
    - [x] Add logging for "Offline Protection" usage
- [x] 4. UI Updates
    - [x] Add "Block/Unblock" button in History tab
    - [x] Show risk level badges for cached entries
- [x] 5. Verification
    - [x] Build and sync
    - [x] Verify offline blocking logic
