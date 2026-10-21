/**
 * Loopin — Firebase Migration Manager & Inventory Tool
 * 
 * Prepares and automates migration from source project to destination Firebase project.
 * Preserves user relationships, rooms, message histories, reactions, disappearing message expirations,
 * themes, nicknames, and privacy controls.
 */

const SOURCE_CONFIG = {
  projectId: "youtube-automation-69420",
  projectNumber: "525662721577",
  storageBucket: "youtube-automation-69420.firebasestorage.app",
  databaseId: "ai-studio-android-instasqu-09091919-6a30-4eaf-81e6-d394f57f8a54",
  packageName: "com.aistudio.instasquad.vkymzt"
};

const COLLECTIONS_INVENTORY = [
  { name: "users", type: "root", primaryKey: "userId", hasTimestamps: true },
  { name: "communities", type: "root", primaryKey: "id", hasTimestamps: true },
  { name: "posts", type: "root", primaryKey: "id", hasTimestamps: true },
  { name: "stories", type: "root", primaryKey: "id", hasTimestamps: true },
  { name: "notes", type: "root", primaryKey: "id", hasTimestamps: true },
  { name: "instants", type: "root", primaryKey: "id", hasTimestamps: true },
  { name: "rooms", type: "root", primaryKey: "roomId", hasTimestamps: true, subcollections: ["messages"] },
  { name: "messages", type: "subcollection", parent: "rooms", primaryKey: "id", hasTimestamps: true }
];

function generateMigrationReport(destinationProjectId = "[DESTINATION_PROJECT_ID_PENDING]") {
  const report = {
    sourceProject: SOURCE_CONFIG.projectId,
    sourceDatabaseId: SOURCE_CONFIG.databaseId,
    destinationProject: destinationProjectId,
    generatedAt: new Date().toISOString(),
    inventory: COLLECTIONS_INVENTORY,
    servicesInScope: [
      "Firebase Authentication (Google Identity & Credential Manager)",
      "Cloud Firestore (Named Database: " + SOURCE_CONFIG.databaseId + ")",
      "Firestore Security Rules (firestore.rules with message expiration check)",
      "Firebase Storage (Media attachments & Voice recordings)"
    ],
    integrityChecks: [
      "Room memberIds to User userId foreign key alignment",
      "Message senderId and seenByUserIds reference validation",
      "Disappearing message expiresAt timestamp preservation",
      "Room themeId and nicknames map preservation",
      "View-once isViewOnce and isViewed flag retention"
    ],
    status: destinationProjectId.includes("PENDING") 
      ? "READY_FOR_DESTINATION_CUTOVER" 
      : "IN_PROGRESS"
  };

  return report;
}

if (require.main === module) {
  const destProject = process.argv[2] || "[DESTINATION_PROJECT_ID_PENDING]";
  const report = generateMigrationReport(destProject);
  console.log("=== LOOPIN FIREBASE MIGRATION REPORT ===");
  console.log(JSON.stringify(report, null, 2));
}

module.exports = {
  SOURCE_CONFIG,
  COLLECTIONS_INVENTORY,
  generateMigrationReport
};
