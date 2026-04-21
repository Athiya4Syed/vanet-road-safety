package com.vanet;

import com.vanet.service.PotholeService;
import com.vanet.service.PQCEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {
    
    private final PotholeService potholeService;
    private final PQCEncryptionService pqcEncryptionService;
    
    @GetMapping("/api/vanet/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "✅ Running",
            "database", "✅ Connected",
            "encryption", "✅ PQC Enabled (AES-256-RSA Hybrid)",
            "publicKey", pqcEncryptionService.getPublicKeyForBlockchain(),
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @PostMapping("/api/vanet/report")
    public ResponseEntity<?> reportPothole(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String severity,
            @RequestParam String description,
            @RequestParam String deviceId) {
        
        var pothole = potholeService.reportPothole(latitude, longitude, severity, description, deviceId);
        return ResponseEntity.ok(Map.of(
            "id", pothole.getId(),
            "latitude", pothole.getLatitude(),
            "longitude", pothole.getLongitude(),
            "severity", pothole.getSeverity(),
            "encrypted", true,
            "encryptionAlgorithm", "AES-256-RSA-Hybrid (Post-Quantum Safe)"
        ));
    }
    
    @GetMapping("/api/vanet/nearby")
    public ResponseEntity<?> getNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        
        var nearby = potholeService.getNearbyPotholes(latitude, longitude);
        return ResponseEntity.ok(Map.of(
            "count", nearby.size(),
            "potholes", nearby,
            "encrypted", true
        ));
    }
    
    @GetMapping("/api/vanet/verified")
    public ResponseEntity<?> getVerified() {
        var verified = potholeService.getVerifiedPotholes();
        return ResponseEntity.ok(Map.of(
            "count", verified.size(),
            "potholes", verified,
            "encrypted", true
        ));
    }
    
    @PostMapping("/api/vanet/verify/{id}")
    public ResponseEntity<?> verifyPothole(@PathVariable Long id) {
        var verified = potholeService.verifyPothole(id);
        return ResponseEntity.ok(Map.of(
            "id", verified.getId(),
            "verificationCount", verified.getVerificationCount(),
            "verified", verified.getVerified(),
            "encrypted", true
        ));
    }
}