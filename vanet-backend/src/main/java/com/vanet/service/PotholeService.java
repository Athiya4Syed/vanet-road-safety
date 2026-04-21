package com.vanet.service;

import com.vanet.model.EncryptedPotholeLog;
import com.vanet.model.PotholeReport;
import com.vanet.repository.EncryptedPotholeLogRepository;
import com.vanet.repository.PotholeReportRepository;
import com.vanet.service.PQCEncryptionService.EncryptedPotholeData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PotholeService {
    
    private final PotholeReportRepository repository;
    private final EncryptedPotholeLogRepository encryptedLogRepository;
    private final PQCEncryptionService pqcEncryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();
    
    /**
     * Report a pothole with PQC encryption
     */
    public PotholeReport reportPothole(Double latitude, Double longitude, String severity, 
                                       String description, String deviceId) {
        try {
            // Create geometry point
            Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            location.setSRID(4326);
            
            // Create pothole report
            PotholeReport pothole = new PotholeReport();
            pothole.setLatitude(latitude);
            pothole.setLongitude(longitude);
            pothole.setLocation(location);
            pothole.setSeverity(severity);
            pothole.setDescription(description);
            pothole.setDeviceId(deviceId);
            pothole.setVerified(false);
            pothole.setVerificationCount(0);
            
            // Save to database
            PotholeReport saved = repository.save(pothole);
            
            // Encrypt the pothole data for secure storage
            String potholeJson = objectMapper.writeValueAsString(saved);
            EncryptedPotholeData encrypted = pqcEncryptionService.encryptPotholeData(potholeJson);
            
            // Store encrypted log
            EncryptedPotholeLog encryptedLog = new EncryptedPotholeLog();
            encryptedLog.setPotholeId(saved.getId());
            encryptedLog.setEncryptedData(encrypted.ciphertext);
            encryptedLog.setEncapsulatedKey(encrypted.encapsulatedKey);
            encryptedLog.setAlgorithm(encrypted.algorithm);
            encryptedLog.setEncryptionTimestamp(encrypted.timestamp);
            encryptedLogRepository.save(encryptedLog);
            
            log.info("✅ Pothole #{} reported and encrypted with PQC", saved.getId());
            return saved;
            
        } catch (Exception e) {
            log.error("❌ Error reporting pothole", e);
            throw new RuntimeException("Failed to report pothole", e);
        }
    }
    
    /**
     * Get nearby potholes
     */
    public List<PotholeReport> getNearbyPotholes(Double latitude, Double longitude) {
        return repository.findNearby(latitude, longitude);
    }
    
    /**
     * Get all verified potholes
     */
    public List<PotholeReport> getVerifiedPotholes() {
        return repository.findByVerifiedTrue();
    }
    
    /**
     * Verify a pothole (with PQC logging)
     */
    public PotholeReport verifyPothole(Long id) {
        try {
            PotholeReport pothole = repository.findById(id).orElseThrow();
            pothole.setVerificationCount(pothole.getVerificationCount() + 1);
            
            if (pothole.getVerificationCount() >= 5) {
                pothole.setVerified(true);
                log.info("✅ Pothole #{} verified (5+ confirmations)", id);
            }
            
            pothole.setUpdatedAt(LocalDateTime.now());
            PotholeReport updated = repository.save(pothole);
            
            // Log encrypted verification
            String verificationJson = objectMapper.writeValueAsString(updated);
            EncryptedPotholeData encrypted = pqcEncryptionService.encryptPotholeData(verificationJson);
            
            EncryptedPotholeLog encryptedLog = new EncryptedPotholeLog();
            encryptedLog.setPotholeId(id);
            encryptedLog.setEncryptedData(encrypted.ciphertext);
            encryptedLog.setEncapsulatedKey(encrypted.encapsulatedKey);
            encryptedLog.setAlgorithm(encrypted.algorithm);
            encryptedLog.setEncryptionTimestamp(encrypted.timestamp);
            encryptedLogRepository.save(encryptedLog);
            
            return updated;
            
        } catch (Exception e) {
            log.error("❌ Error verifying pothole", e);
            throw new RuntimeException("Failed to verify pothole", e);
        }
    }
    
    /**
     * Get encrypted pothole logs for a pothole
     */
    public List<EncryptedPotholeLog> getEncryptedLogs(Long potholeId) {
        return encryptedLogRepository.findByPotholeId(potholeId);
    }
}