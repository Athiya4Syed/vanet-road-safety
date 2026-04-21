package com.vanet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "encrypted_pothole_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDateTime createdAt = LocalDateTime.now();
}