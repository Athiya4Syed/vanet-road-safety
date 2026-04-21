package com.vanet.model;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "pothole_reports")
public class PotholeReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(length = 20)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String deviceId;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private Integer verificationCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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

    // ✅ ALL GETTERS
    public Long getId() { return id; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Point getLocation() { return location; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public String getDeviceId() { return deviceId; }
    public Boolean getVerified() { return verified; }
    public Integer getVerificationCount() { return verificationCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ✅ ALL SETTERS
    public void setId(Long id) { this.id = id; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setLocation(Point location) { this.location = location; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setDescription(String description) { this.description = description; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public void setVerificationCount(Integer verificationCount) { this.verificationCount = verificationCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}