# Loopin — Complete Chat Experience Overhaul & Instagram DM Feature Parity

Loopin's direct messaging system will be overhauled to achieve full feature parity with modern Instagram DMs using a cosmic translucent Liquid Glass design system in Android Jetpack Compose. This upgrade resolves participant identity in 1-to-1 and group chats, introduces an interactive conversation details hub, per-chat themes, server-enforced disappearing messages, customizable nicknames, in-chat search, voice audio notes, shared media galleries, comprehensive privacy controls, and automated Firebase migration preparation.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed during the Phase 1 clarification interview:
> - **Destination Firebase Migration**: Prepare export/backup tools, data migration scripts, and verified security rules before executing the live project cutover.
> - **Prioritized Instagram DM Features**: Deep implementation of **Voice audio notes (recording, playback, duration visualization)** and a **Shared media gallery (photos, voice notes, links, and music)** in conversation details.
> - **Disappearing Messages Timers**: Supporting **24 hours**, **7 days**, and **After Viewing (View-Once media & messages)** with authoritative server timestamp validation.
> - **Framework & Visual Theme**: Built in **Android Kotlin & Jetpack Compose** with custom Liquid Glass components (`GlassCard`, `GlassButton`, `GlassNavBar`, `GlassModalSheet`), backed by deep `#323232` in dark mode and clean white `#FFFFFF` in light mode with pointer-based specular refraction.

---

### 1. Overview & Core Concept

- **What It Does**: Transforms Loopin's messaging into a responsive, secure, and visually striking direct messaging experience. Conversations display strictly the other participant's identity (or group metadata), clicking the header opens a dedicated conversation details dashboard (profile preview, shared media gallery, theme selector, nickname manager, disappearing messages timer, mute/block/restrict actions), and messages support custom themes, ephemeral expiration, rich voice notes, reactions, and unsend capabilities.
- **Target Audience / Persona**: Social messaging users who demand intuitive Instagram-level conversation fluidity combined with privacy controls and high-aesthetic liquid glassmorphism.
- **Key Value**: Delivers the familiarity and feature richness of Instagram DMs without identity confusion, backed by zero-trust Firestore security rules and client-side specular fluidity.

---

### 2. User Experience & Visual Design

#### Key User Flows

1. **Inbox / Room List (`DirectMessagesScreen`)**:
   - Distinct **Direct** and **Groups** tabs.
   - For 1-to-1 rooms, the list dynamically computes and displays the **other user's avatar, display name, and active status**, never showing the current user's own identity.
   - Shows active unread indicators, typing animation, last message preview, and timestamps.
   - Floating action button to start private 1-to-1 chats or create multi-member groups.

2. **Chat Conversation View (`ChatRoomScreen`)**:
   - **Header**: Displays only the other participant's avatar, display name, username, and live active/typing indicator. Clicking anywhere on the header navigates to the **Conversation Details Hub**.
   - **Messages Area**: Rendered with the active conversation theme (e.g. Iris Glow, Cosmic Midnight, Sunset Ember, Emerald Sea, Neon Ocean). Sent messages feature gradient bubbles with subtle specular sheen; received messages feature translucent frosted glass.
   - **Disappearing Messages Banner**: An ambient unobtrusive pill informs participants if disappearing messages are active (e.g., "⚡ Disappearing messages: 24 hours" or "⚡ View once active").
   - **Interactive Input Bar**: Text input, photo picker, camera capture, **Voice audio note recorder with hold-to-record and wave meter**, Spotify song share, and message effects picker.

3. **Conversation Details Hub (`ChatDetailsScreen`)**:
   - **Participant Card**: Avatar, display name, `@username`, online badge, with direct shortcut buttons (Profile, Search, Notifications Mute).
   - **Shared Media Gallery**: Dedicated tabbed gallery displaying shared photos, voice notes, links, and music tracks sent within the conversation.
   - **Theme Selector**: Interactive carousel previewing chat theme gradients and immediate live switching.
   - **Disappearing Messages**: Configurable selector (Off, 24 Hours, 7 Days, After Viewing / View Once) with clear explanation of expiration behavior.
   - **Nicknames Manager**: Per-conversation nickname editor displaying assigned nicknames vs. official account handles.
   - **Privacy & Safety Actions**: Toggle mute messages, toggle read receipts, restrict account, block user, report conversation.

#### Visual Identity & Theme Tokens

- **Aesthetic Direction**: Cosmic Liquid Glass with refractive specular highlights, soft depth blurs, and luminous rim borders.
- **Color Palette**:
  - Dark Mode Base: `#323232` backing atmosphere with layered cosmic dark glass fills (`#1A1E2E`, `#141724`, alpha overlays).
  - Light Mode Base: `#FFFFFF` backing surface with crystalline frosty white translucent glass fills.
  - Accent Tones: Loopin Iris (`#7E74F1`), Iris Glow (`#A69FFF`), Online Green (`#22C55E`), Like Red (`#FF4869`), Spotify Green (`#1DB954`).
- **Interactive Motion**:
  - Pointer-based highlight refraction tracking swipe/touch gestures with spring physics.
  - Spring-scale bounce (`0.95f` -> `1.0f`) on buttons and cards.
  - Smooth slide-up modals and tab transitions.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dynamic Participant Identity Resolution**
  - *Chosen Approach*: Compute the other participant's identity at runtime by comparing `room.memberIds` and `room.memberUsernames` against `currentUserId`. Store participant metadata maps (`memberDetails: Map<String, UserSummary>`) on `Room` documents to avoid stale names.
  - *Why*: Prevents the bug where a user sees their own name/avatar in a 1-to-1 chat header.
- **Decision 2: Server-Enforced Disappearing Messages & View-Once Media**
  - *Chosen Approach*: Authoritative `expiresAt: Timestamp?` and `isViewOnce: Boolean` stored on each `RoomMessage`. Repository flows filter out expired messages client-side, while Firestore security rules reject read/list operations where `resource.data.expiresAt < request.time`.
  - *Why*: Ensures messages cannot be retrieved once expired, maintaining zero-trust privacy.
- **Decision 3: Dedicated Voice Notes & Shared Media Gallery**
  - *Chosen Approach*: Add first-class voice message recording/playback state in the chat input bar and compile media collections (images, voice notes, tracks) for instant browsing in `ChatDetailsScreen`.
  - *Why*: Delivers the highest-requested Instagram DM parity feature without requiring external cloud storage dependencies for local waveforms.
- **Decision 4: Safe Firebase Migration Preparation**
  - *Chosen Approach*: Provide structured migration export scripts, automated collection validation, and backup tooling that verify data volume and document counts before project cutover.
  - *Why*: Prevents data loss and ensures referential integrity across users, rooms, and message histories.

---

### 4. Technical Architecture & Data Strategy (Technical Reference)

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  ┌───────────────────────┐  ┌────────────────────────────────────────┐ │
│  │ DirectMessagesScreen  │  │ ChatRoomScreen (with Theme & Voice)    │ │
│  └───────────┬───────────┘  └───────────────────┬────────────────────┘ │
│              │                                  │                      │
│              ▼                                  ▼                      │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  ChatDetailsScreen (Profile, Themes, Expire, Nicknames, Gallery) │  │
│  └──────────────────────────────────┬───────────────────────────────┘  │
└─────────────────────────────────────┼──────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        MainViewModel & StateFlows                      │
│  • observeRooms() • observeMessages(roomId) • currentRoom              │
│  • recordVoiceNote() • updateChatTheme() • setDisappearingTimer()      │
│  • setNickname() • loadSharedMediaGallery(roomId)                      │
└─────────────────────────────────────┼──────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     InstaSquadRepository (Data Layer)                  │
│  • Room CRUD & Member Details Mapping                                  │
│  • Expired Message & View-Once Filtering                               │
│  • Firestore Security Rules Enforcement (request.auth != null)         │
│  • Migration Export & Verification Tooling                             │
└─────────────────────────────────────┼──────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      Cloud Firestore & Authentication                  │
│  /rooms/{roomId}                                                       │
│    ├── themeId: "iris_glow" | "cosmic_midnight" | ...                  │
│    ├── disappearingDurationSec: 86400 | 604800 | -1 (view-once)        │
│    ├── nicknames: { "uid1": "Alex", "uid2": "Sam" }                    │
│    ├── mutedByUserIds: ["uid1"]                                        │
│    └── /messages/{messageId}                                           │
│          ├── text, type: "text" | "voice" | "media" | "spotify"        │
│          ├── audioDurationSec, mediaUrl, reactions                     │
│          ├── expiresAt: Timestamp, isViewOnce: Boolean                 │
│          └── status: "sent" | "delivered" | "seen"                     │
└────────────────────────────────────────────────────────────────────────┘
```

#### Phased Execution Plan

1. **Phase 1: Domain Models & Repository Extensions**: Update `Room` and `RoomMessage` models, Firestore security rules in `firestore.rules`, and repository methods for voice notes, theme updates, nickname editing, disappearing message timers, and message expiration filtering.
2. **Phase 2: Conversation Identity & Interactive Header**: Overhaul `ChatRoomScreen` header so 1-to-1 chats strictly display the other participant's avatar, name, and online status. Wire header click to open the new `ChatDetailsScreen`.
3. **Phase 3: Conversation Details Hub (`ChatDetailsScreen`)**: Build the dedicated details screen with profile identity, in-chat search, shared media browser (photos, audio notes, tracks), theme switcher, disappearing messages selector (24h, 7d, after viewing), nickname manager, and mute/block controls.
4. **Phase 4: Liquid Glass Voice Messaging & Chat Theming**: Implement interactive voice note recording with waveform preview, playback controls, and per-chat theme engine with live gradient previews and dark (`#323232`)/light (`#FFFFFF`) contrast guarantees.
5. **Phase 5: Migration Preparation & Verification**: Provide migration export scripts, automated validation tests (`gradle :app:testDebugUnitTest`), verify compilation (`compile_applet`), and ensure zero regressions across feed, communities, instants, and story features.
