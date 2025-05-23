const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize the Firebase Admin SDK only once
admin.initializeApp();

/**
 * Cloud Function triggered when a NEW Firebase Authentication user is created.
 * Reads the intended role from a temporary Firestore document (/pendingRoles/{uid}),
 * sets the corresponding initial custom claim ('patient' or 'pending_professional'),
 * and ensures the user document in Firestore reflects this initial state.
 */
exports.assignInitialRoleAndStatus = functions.auth.user().onCreate(async (user) => {
  const uid = user.uid;
  let initialRole = "patient"; // Default role if nothing else is specified
  let initialStatus = "active"; // Default status for patients

  const roleDocRef = admin.firestore().collection("pendingRoles").doc(uid);

  functions.logger.log(`New user created: ${uid}, Email: ${user.email || "N/A"}`);

  try {
    const roleDoc = await roleDocRef.get();
    if (roleDoc.exists && roleDoc.data().intendedRole) {
      const intendedRole = roleDoc.data().intendedRole;
      functions.logger.log(`Found pending role marker for ${uid}. Intended role: ${intendedRole}`);
      if (intendedRole === "professional") {
        initialRole = "pending_professional"; // Set initial claim for professionals needing verification
        initialStatus = "pending"; // Set status to pending in Firestore
      }
      // Add other intendedRole checks here if necessary (e.g., for admins, though not typical via public signup)
      // else if (intendedRole === "admin") { initialRole = "admin"; initialStatus = "active"; }

      // Delete the temporary marker document now that we've read it
      await roleDocRef.delete();
      functions.logger.log(`Deleted pending role marker for ${uid}.`);
    } else {
      functions.logger.log(`No pending role marker found for ${uid}. Defaulting to role: ${initialRole}.`);
    }
  } catch (error) {
    functions.logger.error(`Error reading/deleting pending role for ${uid}:`, error);
    // Continue with default role, but log the error
  }

  functions.logger.log(`Setting initial claims for ${uid} to: { role: ${initialRole} }`);

  // Set the custom claim for the user in Firebase Authentication
  try {
    await admin.auth().setCustomUserClaims(uid, {role: initialRole});
    functions.logger.log(`Successfully set initial custom claim for ${uid}.`);

    // Ensure the user document in Firestore reflects this initial state.
    // Use merge: true to avoid overwriting fields potentially written by the client
    // during the signup process (like name, licenseNumber, specialty).
    await admin.firestore().collection("users").doc(uid).set({
      uid: uid, // Store UID in the document itself
      email: user.email || null, // Store email if available
      role: initialRole,
      status: initialStatus,
      createdAt: admin.firestore.FieldValue.serverTimestamp(), // Add creation timestamp
    }, {merge: true});
    functions.logger.log(`Updated Firestore document for ${uid} with initial role and status.`);
  } catch (error) {
    functions.logger.error(`Error setting initial custom claims or Firestore data for ${uid}:`, error);
    // Consider adding more robust error handling here if needed
  }

  // --- TEMPORARY CODE BLOCK FOR CREATING AN ADMIN ---
  // !! REMOVE OR COMMENT OUT AFTER CREATING YOUR ADMIN USER !!
  /*
  if (user.email === "your.admin.email@example.com") { // <-- REPLACE WITH YOUR ADMIN EMAIL
    functions.logger.log(`Assigning ADMIN role claim to ${uid}`);
    try {
      await admin.auth().setCustomUserClaims(uid, { role: "admin" });
      await admin.firestore().collection("users").doc(uid).set({
          role: "admin",
          status: "active"
      }, { merge: true });
      functions.logger.log(`Successfully set ADMIN claim and Firestore role for ${uid}`);
    } catch(adminError) {
      functions.logger.error(`Error setting ADMIN claim for ${uid}`, adminError);
    }
  }
  */
  // --- END OF TEMPORARY ADMIN CODE BLOCK ---
});


/**
 * Cloud Function triggered when a document in the 'users' collection is updated.
 * Specifically checks if the 'status' field was changed to 'approved' or 'rejected'
 * for a user whose role was 'pending_professional'.
 * If approved, updates the custom claim to 'professional'.
 * If rejected, disables the user's Authentication account.
 */
exports.updateProfessionalClaimOnApproval = functions.firestore
    .document("users/{userId}")
    .onUpdate(async (change, context) => {
      const userId = context.params.userId;
      const newData = change.after.data(); // Data after the change
      const previousData = change.before.data(); // Data before the change

      // Ensure data exists and status/role fields are present before proceeding
      if (!newData || !previousData || !newData.status || !previousData.status || !newData.role) {
        functions.logger.log(`User document update for ${userId} missing necessary fields, skipping claim update.`);
        return null;
      }

      const previousStatus = previousData.status;
      const newStatus = newData.status;
      const currentRole = newData.role; // Use the latest role for checks

      functions.logger.log(`User document updated for ${userId}. PrevStatus: ${previousStatus}, NewStatus: ${newStatus}, Role: ${currentRole}`);

      // --- Handle Approval ---
      // Check: Status changed TO 'approved' AND the status wasn't 'approved' before AND the current/previous role indicates they are/were a pending professional.
      if (newStatus === "approved" && previousStatus !== "approved" && (currentRole === "pending_professional" || previousData.role === "pending_professional")) {
        functions.logger.log(`Approving professional ${userId}. Updating custom claim to 'professional'.`);
        try {
        // Update the custom claim in Firebase Authentication
          await admin.auth().setCustomUserClaims(userId, {role: "professional"});
          functions.logger.log(`Successfully set 'professional' custom claim for ${userId}.`);

          // Update the role field in Firestore for consistency
          await change.after.ref.update({role: "professional"});
          functions.logger.log(`Updated Firestore role to 'professional' for ${userId}.`);

          // Optional: Send a notification (e.g., FCM) to the user about approval
          // ... implementation needed ...
        } catch (error) {
          functions.logger.error(`Error setting 'professional' custom claim or Firestore role for ${userId} on approval:`, error);
        }
      }
      // --- Handle Rejection ---
      // Check: Status changed TO 'rejected' AND the status wasn't 'rejected' before AND the role indicates they are/were a pending professional.
      else if (newStatus === "rejected" && previousStatus !== "rejected" && (currentRole === "pending_professional" || previousData.role === "pending_professional")) {
        functions.logger.log(`Rejecting professional ${userId}. Disabling Authentication account.`);
        try {
          // Disable the user's account in Firebase Authentication
          await admin.auth().updateUser(userId, {disabled: true});
          functions.logger.log(`Successfully disabled Auth account for rejected user ${userId}.`);

          // Optional: Update Firestore role to 'rejected' for clarity?
          // await change.after.ref.update({ role: 'rejected' });

          // Optional: Send a notification to the user about rejection
          // ... implementation needed ...
        } catch (error) {
          functions.logger.error(`Error disabling Auth account for rejected user ${userId}:`, error);
        }
      }
      // --- Handle other status changes if needed ---
      // else if (newStatus === 'active' && previousStatus === 'suspended') { ... }

      // If none of the specific conditions are met, just exit the function.
      return null;
    });
