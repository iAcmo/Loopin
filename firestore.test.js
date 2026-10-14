const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot write user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc("alice_123").set({
      userId: "alice_123",
      username: "alice",
      squadId: "ALPHA",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can create own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_w",
      squadId: "ALPHA",
      displayName: "Alice Wonderland",
      bio: "Living the dream",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot update someone else's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      username: "bobby",
      squadId: "BETA",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      username: "hacked",
    })
  );
});

test("Authenticated user: can create post and like it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      userId: ALICE_UID,
      username: "alice_w",
      caption: "Hello squad!",
      squadId: "ALPHA",
      likesCount: 0,
      createdAt: new Date(),
    })
  );

  // Bob can like the post by updating likesCount
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      likesCount: 1,
    })
  );
});

test("Authenticated user: can post story and music note", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("stories").doc("story_1").set({
      userId: ALICE_UID,
      username: "alice_w",
      mediaUrl: "https://example.com/story.jpg",
      squadId: "ALPHA",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      username: "alice_w",
      squadId: "ALPHA",
      songTitle: "Blinding Lights",
      artist: "The Weeknd",
      noteText: "Vibing in the studio",
      createdAt: new Date(),
    })
  );
});

test("Squad room: member can send message in room", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("rooms").doc("room_alpha").set({
      roomId: "room_alpha",
      squadId: "ALPHA",
      name: "Squad Alpha General",
      isSquadRoom: true,
      memberIds: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("rooms").doc("room_alpha").collection("messages").doc("msg_1").set({
      senderId: ALICE_UID,
      senderName: "Alice",
      text: "Welcome to the squad!",
      type: "text",
      createdAt: new Date(),
    })
  );
});
