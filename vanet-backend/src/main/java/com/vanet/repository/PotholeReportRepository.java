package com.vanet.repository;

import com.vanet.model.PotholeReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PotholeReportRepository extends JpaRepository<PotholeReport, Long> {
    
    // Find all verified potholes
    List<PotholeReport> findByVerifiedTrue();
    
    // Find nearby potholes (within 2km)
    @Query(value = "SELECT * FROM pothole_reports WHERE ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), 2000)", 
           nativeQuery = true)
    List<PotholeReport> findNearby(@Param("lat") Double lat, @Param("lng") Double lng);
    
    // Find unverified potholes nearby
    @Query(value = "SELECT * FROM pothole_reports WHERE verified = false AND ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), 500)", 
           nativeQuery = true)
    List<PotholeReport> findUnverifiedNearby(@Param("lat") Double lat, @Param("lng") Double lng);
}
