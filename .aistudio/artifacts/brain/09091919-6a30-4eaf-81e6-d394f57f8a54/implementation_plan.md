# Implementation Plan: Switch Firebase Backend to 'youtube-automation-69420'

Switch the application's Firebase project backend from `loopin-social` back to `youtube-automation-69420` while keeping all existing Instagram-style features (Feed, DM chats, Notes, App Lock, Full Profile, Refraction UI) fully intact.

---

### User Review Required

> [!IMPORTANT]
> Switching Firebase backend projects updates `google-services.json` and Firestore database references to point to `youtube-automation-69420`.
> - **Authentication & Database**: The app will connect to the `youtube-automation-69420` Firebase project.
> - **Feature Preservation**: All code, UI styling (liquid glass refraction, Instagram notes, post options, DM unsend, full-page profiles, app lock) will remain 100% active and preserved.

---

### Proposed Plan & Milestones

#### 1. Keystore & Credentials Verification
- Extract SHA-1 and SHA-256 certificate fingerprints from `debug.keystore` using `keytool` to ensure Google Sign-In and OAuth credentials match `youtube-automation-69420`.
- Verify the package name `com.aistudio.instasquad.vkymzt` matches the registered Android client.

#### 2. Firebase Backend Re-provisioning & Configuration Cutover
- Trigger the Firebase setup UI / consent flow with project ID `youtube-automation-69420` and target database ID (`ai-studio-android-instasqu-09091919-6a30-4eaf-81e6-d394f57f8a54`).
- Update `app/google-services.json` and `app/src/main/res/values/firebase_applet_config.xml` to point to `youtube-automation-69420`.
- Update `scripts/firebase_migration_manager.js` to reflect the active configuration.

#### 3. Security Rules & Schema Deployment
- Validate and deploy `firestore.rules` to `youtube-automation-69420` using `deploy_firebase`.
- Ensure rules allow:
  - Users collection read/write
  - Posts collection (likes, comments, caption updates, deletion)
  - Notes collection (24-hour expiration, custom colors)
  - Rooms & Messages collections (unsend, reactions, disappearing messages)
  - Communities & Stories collections

#### 4. Verification & Testing
- Run `compile_applet` to verify compilation and dependency resolution with the new Firebase configuration.
- Verify user authentication and database operations connect cleanly.
