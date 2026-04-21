package com.vanet.service;

import com.vanet.model.EncryptedPotholeLog;
import com.vanet.model.PotholeReport;
import com.vanet.repository.EncryptedPotholeLogRepository;
import com.vanet.repository.PotholeReportRepository;
import com.vanet.service.PQCEncryptionService.EncryptedPotholeData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PotholeService {

    // ✅ Manual logger instead of @Slf4j
    private static final Logger log = Logger.getLogger(PotholeService.class.getName());

    private final PotholeReportRepository repository;
    private final EncryptedPotholeLogRepository encryptedLogRepository;
    private final PQCEncryptionService pqcEncryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    // ✅ Manual constructor instead of @RequiredArgsConstructor
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
            Point location = geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
            );
            location.setSRID(4326);

            PotholeReport pothole = new PotholeReport();
            pothole.setLatitude(latitude);
            pothole.setLongitude(longitude);
            pothole.setLocation(location);
            pothole.setSeverity(severity);
            pothole.setDescription(description);
            pothole.setDeviceId(deviceId);
            pothole.setVerified(false);
            pothole.setVerificationCount(0);

            PotholeReport saved = repository.save(pothole);

            // Encrypt and store
            String potholeJson = objectMapper.writeValueAsString(saved);
            EncryptedPotholeData encrypted = pqcEncryptionService
                .encryptPotholeData(potholeJson);

            EncryptedPotholeLog encryptedLog = new EncryptedPotholeLog();
            encryptedLog.setPotholeId(saved.getId());
            encryptedLog.setEncryptedData(encrypted.ciphertext);
            encryptedLog.setEncapsulatedKey(encrypted.encapsulatedKey);
            encryptedLog.setAlgorithm(encrypted.algorithm);
            encryptedLog.setEncryptionTimestamp(encrypted.timestamp);
            encryptedLogRepository.save(encryptedLog);

            log.info("Pothole reported at " + latitude + ", " + longitude);
            return saved;

        } catch (Exception e) {
            log.severe("Error reporting pothole: " + e.getMessage());
            throw new RuntimeException("Failed to report pothole", e);
        }
    }

    public List<PotholeReport> getNearbyPotholes(Double lat, Double lng) {
        return repository.findNearby(lat, lng);
    }

    public List<PotholeReport> getVerifiedPotholes() {
        return repository.findByVerifiedTrue();
    }

    public PotholeReport verifyPothole(Long id) {
        try {
            PotholeReport pothole = repository.findById(id).orElseThrow();
            pothole.setVerificationCount(pothole.getVerificationCount() + 1);

            if (pothole.getVerificationCount() >= 5) {
                pothole.setVerified(true);
            }

            pothole.setUpdatedAt(LocalDateTime.now());
            PotholeReport updated = repository.save(pothole);

            String verificationJson = objectMapper.writeValueAsString(updated);
            EncryptedPotholeData encrypted = pqcEncryptionService
                .encryptPotholeData(verificationJson);

            EncryptedPotholeLog encryptedLog = new EncryptedPotholeLog();
            encryptedLog.setPotholeId(id);
            encryptedLog.setEncryptedData(encrypted.ciphertext);
            encryptedLog.setEncapsulatedKey(encrypted.encapsulatedKey);
            encryptedLog.setAlgorithm(encrypted.algorithm);
            encryptedLog.setEncryptionTimestamp(encrypted.timestamp);
            encryptedLogRepository.save(encryptedLog);

            return updated;

        } catch (Exception e) {
            log.severe("Error verifying pothole: " + e.getMessage());
            throw new RuntimeException("Failed to verify pothole", e);
        }
    }

    public List<EncryptedPotholeLog> getEncryptedLogs(Long potholeId) {
        return encryptedLogRepository.findByPotholeId(potholeId);
    }
}