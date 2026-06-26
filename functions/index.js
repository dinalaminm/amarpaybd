const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();

// ─────────────────────────────────────────────
//  verifyTransaction  —  POST /verifyTransaction
//  Body: { license_key, transaction_id }
// ─────────────────────────────────────────────
exports.verifyTransaction = onRequest(
  { cors: true, region: "asia-south1" },
  async (req, res) => {

    // শুধু POST allow
    if (req.method !== "POST") {
      return res.status(405).json({ success: false, error: "Method not allowed" });
    }

    const { license_key, transaction_id } = req.body || {};

    // ── ১. Input validation ──
    if (!license_key || !transaction_id) {
      return res.status(400).json({
        success: false,
        error: "license_key এবং transaction_id দুটোই দরকার"
      });
    }

    try {
      // ── ২. License key চেক করো ──
      const licenseSnap = await db.collection("licenses").doc(license_key).get();

      if (!licenseSnap.exists) {
        return res.status(403).json({
          success: false,
          error: "Invalid license key"
        });
      }

      const license = licenseSnap.data();

      // Active কিনা দেখো
      if (!license.active) {
        return res.status(403).json({
          success: false,
          error: "License টি active নেই"
        });
      }

      // Expired কিনা দেখো
      if (license.expiresAt) {
        const expDate = license.expiresAt.toDate
          ? license.expiresAt.toDate()
          : new Date(license.expiresAt);
        if (expDate < new Date()) {
          return res.status(403).json({
            success: false,
            error: "License মেয়াদ শেষ হয়ে গেছে"
          });
        }
      }

      // ── ৩. Transaction খোঁজো ──
      // প্রথমে document ID দিয়ে খোঁজো
      let txnData = null;
      let txnDocId = null;

      const directSnap = await db
        .collection("transactions")
        .doc(transaction_id)
        .get();

      if (directSnap.exists) {
        txnData  = directSnap.data();
        txnDocId = directSnap.id;
      } else {
        // trxID field দিয়ে খোঁজো
        const querySnap = await db
          .collection("transactions")
          .where("trxID", "==", transaction_id)
          .where("licenseKey", "==", license_key)
          .limit(1)
          .get();

        if (!querySnap.empty) {
          txnData  = querySnap.docs[0].data();
          txnDocId = querySnap.docs[0].id;
        }
      }

      if (!txnData) {
        return res.status(404).json({
          success: false,
          error: "Transaction পাওয়া যায়নি"
        });
      }

      // ── ৪. এই license-এর transaction কিনা চেক করো ──
      if (txnData.licenseKey && txnData.licenseKey !== license_key) {
        return res.status(403).json({
          success: false,
          error: "এই transaction তোমার license-এর নয়"
        });
      }

      // ── ৫. Already used কিনা চেক করো ──
      if (txnData.status && txnData.status !== "unused") {
        return res.status(200).json({
          success: false,
          already_used: true,
          error: `Transaction ইতোমধ্যে ব্যবহৃত হয়েছে (status: ${txnData.status})`,
          data: {
            trxID:     txnData.trxID,
            amount:    txnData.amount,
            method:    txnData.method,
            timestamp: txnData.timestamp
          }
        });
      }

      // ── ৬. Status "used" করে আপডেট করো ──
      await db.collection("transactions").doc(txnDocId).update({
        status:   "used",
        usedAt:   new Date(),
        usedBy:   license_key
      });

      // ── ৭. Success response ──
      return res.status(200).json({
        success:  true,
        verified: true,
        data: {
          trxID:     txnData.trxID,
          amount:    txnData.amount,
          method:    txnData.method,
          sender:    txnData.sender,
          timestamp: txnData.timestamp
        }
      });

    } catch (err) {
      console.error("verifyTransaction error:", err);
      return res.status(500).json({
        success: false,
        error:   "Server error: " + err.message
      });
    }
  }
);


// ─────────────────────────────────────────────
//  checkLicense  —  POST /checkLicense
//  Body: { license_key }
//  ইউজার app startup-এ license valid কিনা চেক করবে
// ─────────────────────────────────────────────
exports.checkLicense = onRequest(
  { cors: true, region: "asia-south1" },
  async (req, res) => {

    if (req.method !== "POST") {
      return res.status(405).json({ success: false, error: "Method not allowed" });
    }

    const { license_key } = req.body || {};

    if (!license_key) {
      return res.status(400).json({ success: false, error: "license_key দরকার" });
    }

    try {
      const snap = await db.collection("licenses").doc(license_key).get();

      if (!snap.exists) {
        return res.status(200).json({ success: false, valid: false, error: "Invalid license" });
      }

      const license = snap.data();

      if (!license.active) {
        return res.status(200).json({ success: false, valid: false, error: "License active নেই" });
      }

      let expired = false;
      if (license.expiresAt) {
        const expDate = license.expiresAt.toDate
          ? license.expiresAt.toDate()
          : new Date(license.expiresAt);
        if (expDate < new Date()) expired = true;
      }

      if (expired) {
        return res.status(200).json({ success: false, valid: false, error: "License মেয়াদ শেষ" });
      }

      return res.status(200).json({
        success: true,
        valid:   true,
        license: {
          active:    license.active,
          expiresAt: license.expiresAt || null
        }
      });

    } catch (err) {
      return res.status(500).json({ success: false, error: err.message });
    }
  }
);
