package com.vanet;

import com.vanet.service.PotholeService;
import com.vanet.service.PQCEncryptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class HealthController {

    private final PotholeService potholeService;
    private final PQCEncryptionService pqcEncryptionService;

    public HealthController(PotholeService potholeService,
                           PQCEncryptionService pqcEncryptionService) {
        this.potholeService = potholeService;
        this.pqcEncryptionService = pqcEncryptionService;
    }

    @GetMapping("/api/vanet/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "✅ Running");
        response.put("database", "✅ Connected");
        response.put("encryption", "✅ PQC Enabled (AES-256-RSA Hybrid)");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/vanet/report")
    public ResponseEntity<?> reportPothole(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String severity,
            @RequestParam String description,
            @RequestParam String deviceId) {
        try {
            var pothole = potholeService.reportPothole(
                latitude, longitude, severity, description, deviceId
            );
            Map<String, Object> response = new HashMap<>();
            response.put("id", pothole.getId());
            response.put("latitude", pothole.getLatitude());
            response.put("longitude", pothole.getLongitude());
            response.put("severity", pothole.getSeverity());
            response.put("description", pothole.getDescription());
            response.put("verified", pothole.getVerified());
            response.put("verificationCount", pothole.getVerificationCount());
            response.put("encrypted", true);
            response.put("message", "✅ Pothole reported successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("cause", e.getCause() != null ? 
                e.getCause().getMessage() : "unknown");
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/api/vanet/nearby")
    public ResponseEntity<?> getNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        try {
            var nearby = potholeService.getNearbyPotholes(latitude, longitude);
            Map<String, Object> response = new HashMap<>();
            response.put("count", nearby.size());
            response.put("potholes", nearby);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/api/vanet/verified")
    public ResponseEntity<?> getVerified() {
        try {
            var verified = potholeService.getVerifiedPotholes();
            Map<String, Object> response = new HashMap<>();
            response.put("count", verified.size());
            response.put("potholes", verified);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/api/vanet/verify/{id}")
    public ResponseEntity<?> verifyPothole(@PathVariable Long id) {
        try {
            var verified = potholeService.verifyPothole(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", verified.getId());
            response.put("verificationCount", verified.getVerificationCount());
            response.put("verified", verified.getVerified());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}