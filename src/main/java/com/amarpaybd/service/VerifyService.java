package com.amarpaybd.service;

import com.amarpaybd.model.VerifyRequest;
import com.amarpaybd.model.VerifyResponse;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class VerifyService {

    public VerifyResponse verify(VerifyRequest req) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // ── 1. License Key valid কিনা check ──
        DocumentSnapshot licenseSnap = db.collection("licenses")
            .document(req.getLicenseKey()).get().get();

        if (!licenseSnap.exists())
            return VerifyResponse.fail("Invalid license key");

        Boolean active = licenseSnap.getBoolean("active");
        if (active == null || !active)
            return VerifyResponse.fail("License key is inactive");

        Date expiresAt = licenseSnap.getDate("expiresAt");
        if (expiresAt != null && expiresAt.before(new Date()))
            return VerifyResponse.fail("License key has expired");

        // ── 2. Transaction খোঁজো ──
        DocumentReference trxRef = db.collection("transactions").document(req.getTrxId());
        DocumentSnapshot trxSnap = trxRef.get().get();

        if (!trxSnap.exists())
            return VerifyResponse.fail("Transaction not found");

        // ── 3. Already used check ──
        if ("used".equals(trxSnap.getString("status")))
            return VerifyResponse.fail("Transaction already used");

        // ── 4. Amount match (optional) ──
        if (req.getAmount() != null) {
            Double dbAmount = trxSnap.getDouble("amount");
            if (dbAmount == null || Math.abs(dbAmount - req.getAmount()) > 0.01)
                return VerifyResponse.fail("Amount mismatch. Expected: " + req.getAmount() + ", Found: " + dbAmount);
        }

        // ── 5. Method match (optional) ──
        if (req.getMethod() != null && !req.getMethod().isEmpty()) {
            String dbMethod = trxSnap.getString("method");
            if (!req.getMethod().equalsIgnoreCase(dbMethod))
                return VerifyResponse.fail("Payment method mismatch");
        }

        // ── 6. Transaction "used" mark করো ──
        Map<String, Object> update = new HashMap<>();
        update.put("status",    "used");
        update.put("usedAt",    new Date());
        update.put("usedByKey", req.getLicenseKey());
        trxRef.update(update).get();

        // ── 7. Success response ──
        return VerifyResponse.success(
            req.getTrxId(),
            trxSnap.getDouble("amount"),
            trxSnap.getString("method"),
            trxSnap.getString("sender"),
            trxSnap.getDate("timestamp") != null ? trxSnap.getDate("timestamp").toString() : null
        );
    }
}
