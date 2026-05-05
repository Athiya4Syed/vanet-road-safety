package com.vanet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pothole_reports")
public class PotholeReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "verification_count", nullable = false)
    private Integer verificationCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public String getDeviceId() { return deviceId; }
    public Boolean getVerified() { return verified; }
    public Integer getVerificationCount() { return verificationCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setDescription(String description) { this.description = description; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public void setVerificationCount(Integer count) { this.verificationCount = count; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}