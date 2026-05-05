package com.vanet.service;

import com.vanet.model.EncryptedPotholeLog;
import com.vanet.model.PotholeReport;
import com.vanet.repository.EncryptedPotholeLogRepository;
import com.vanet.repository.PotholeReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PotholeService {

    private static final Logger log =
        Logger.getLogger(PotholeService.class.getName());

    private final PotholeReportRepository repository;
    private final EncryptedPotholeLogRepository encryptedLogRepository;
    private final PQCEncryptionService pqcEncryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PotholeService(
            PotholeReportRepository repository,
            EncryptedPotholeLogRepository encryptedLogRepository,
            PQCEncryptionService pqcEncryptionService) {
        this.repository = repository;
        this.encryptedLogRepository = encryptedLogRepository;
        this.pqcEncryptionService = pqcEncryptionService;
    }

    public PotholeReport reportPothole(Double latitude, Double longitude,
                                       String severity, String description,
                                       String deviceId) {
        try {
            PotholeReport pothole = new PotholeReport();
            pothole.setLatitude(latitude);
            pothole.setLongitude(longitude);
            pothole.setSeverity(severity);
            pothole.setDescription(description);
            pothole.setDeviceId(deviceId);
            pothole.setVerified(false);
            pothole.setVerificationCount(0);

            PotholeReport saved = repository.save(pothole);
            log.info("Pothole saved with ID: " + saved.getId());

            try {
                String potholeJson = objectMapper.writeValueAsString(saved);
                PQCEncryptionService.EncryptedPotholeData encrypted =
                    pqcEncryptionService.encryptPotholeData(potholeJson);

                EncryptedPotholeLog encryptedLog = new EncryptedPotholeLog();
                encryptedLog.setPotholeId(saved.getId());
                encryptedLog.setEncryptedData(encrypted.ciphertext);
                encryptedLog.setEncapsulatedKey(encrypted.encapsulatedKey);
                encryptedLog.setAlgorithm(encrypted.algorithm);
                encryptedLog.setEncryptionTimestamp(encrypted.timestamp);
                encryptedLogRepository.save(encryptedLog);
                log.info("Encrypted log saved");
            } catch (Exception encryptError) {
                log.warning("Encryption failed but pothole saved: " + 
                    encryptError.getMessage());
            }

            return saved;

        } catch (Exception e) {
            log.severe("Error saving pothole: " + e.getMessage());
            throw new RuntimeException("Failed to report pothole: " + 
                e.getMessage(), e);
        }
    }

    public List<PotholeReport> getNearbyPotholes(Double lat, Double lng) {
        try {
            return repository.findNearby(lat, lng);
        } catch (Exception e) {
            log.severe("Error getting nearby: " + e.getMessage());
            throw new RuntimeException("Failed to get nearby potholes", e);
        }
    }

    public List<PotholeReport> getVerifiedPotholes() {
        try {
            return repository.findByVerifiedTrue();
        } catch (Exception e) {
            log.severe("Error getting verified: " + e.getMessage());
            throw new RuntimeException("Failed to get verified potholes", e);
        }
    }

    public PotholeReport verifyPothole(Long id) {
        try {
            PotholeReport pothole = repository.findById(id).orElseThrow();
            pothole.setVerificationCount(pothole.getVerificationCount() + 1);
            if (pothole.getVerificationCount() >= 5) {
                pothole.setVerified(true);
            }
            pothole.setUpdatedAt(LocalDateTime.now());
            return repository.save(pothole);
        } catch (Exception e) {
            log.severe("Error verifying: " + e.getMessage());
            throw new RuntimeException("Failed to verify pothole", e);
        }
    }
}