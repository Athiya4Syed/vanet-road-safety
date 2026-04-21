package com.vanet.repository;

import com.vanet.model.EncryptedPotholeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EncryptedPotholeLogRepository extends JpaRepository<EncryptedPotholeLog, Long> {
    
    List<EncryptedPotholeLog> findByPotholeId(Long potholeId);
    
    List<EncryptedPotholeLog> findByAlgorithm(String algorithm);
}