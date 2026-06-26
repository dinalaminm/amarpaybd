package com.amarpaybd.controller;

import com.amarpaybd.model.VerifyRequest;
import com.amarpaybd.model.VerifyResponse;
import com.amarpaybd.service.VerifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // সব domain থেকে call করতে পারবে
public class VerifyController {

    @Autowired
    private VerifyService verifyService;

    // ── POST /api/verify ──
    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verify(@RequestBody VerifyRequest req) {
        // Basic validation
        if (req.getLicenseKey() == null || req.getLicenseKey().isEmpty()) {
            return ResponseEntity.badRequest().body(VerifyResponse.fail("licenseKey is required"));
        }
        if (req.getTrxId() == null || req.getTrxId().isEmpty()) {
            return ResponseEntity.badRequest().body(VerifyResponse.fail("trxId is required"));
        }

        try {
            VerifyResponse response = verifyService.verify(req);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(VerifyResponse.fail("Server error: " + e.getMessage()));
        }
    }

    // ── GET /api/status — server alive কিনা দেখার জন্য ──
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("{\"status\":\"AmarPayBd API running\"}");
    }
}
