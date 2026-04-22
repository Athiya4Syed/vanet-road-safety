package com.vanet.repository;

import com.vanet.model.PotholeReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PotholeReportRepository extends JpaRepository<PotholeReport, Long> {

    List<PotholeReport> findByVerifiedTrue();

    // Simple distance query without PostGIS
    @Query(value = "SELECT * FROM pothole_reports WHERE " +
            "ABS(latitude - :lat) < 0.02 AND " +
            "ABS(longitude - :lng) < 0.02",
            nativeQuery = true)
    List<PotholeReport> findNearby(
        @Param("lat") Double lat,
        @Param("lng") Double lng
    );
}