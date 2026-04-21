package com.vanet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "encrypted_pothole_logs")
public class EncryptedPotholeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long potholeId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String encryptedData;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String encapsulatedKey;

    @Column(nullable = false)
    private String algorithm;

    @Column(nullable = false)
    private Long encryptionTimestamp;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getPotholeId() { return potholeId; }
    public String getEncryptedData() { return encryptedData; }
    public String getEncapsulatedKey() { return encapsulatedKey; }
    public String getAlgorithm() { return algorithm; }
    public Long getEncryptionTimestamp() { return encryptionTimestamp; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setPotholeId(Long potholeId) { this.potholeId = potholeId; }
    public void setEncryptedData(String encryptedData) { this.encryptedData = encryptedData; }
    public void setEncapsulatedKey(String encapsulatedKey) { this.encapsulatedKey = encapsulatedKey; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public void setEncryptionTimestamp(Long encryptionTimestamp) { this.encryptionTimestamp = encryptionTimestamp; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}