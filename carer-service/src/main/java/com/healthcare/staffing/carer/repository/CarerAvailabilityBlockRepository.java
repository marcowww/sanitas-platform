package com.healthcare.staffing.carer.repository;

import com.healthcare.staffing.carer.domain.CarerAvailabilityBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarerAvailabilityBlockRepository extends JpaRepository<CarerAvailabilityBlock, UUID> {
    
    List<CarerAvailabilityBlock> findByCarerId(UUID carerId);
    
    Optional<CarerAvailabilityBlock> findByCarerIdAndBookingId(UUID carerId, UUID bookingId);
    
    @Query("SELECT cab FROM CarerAvailabilityBlock cab WHERE cab.carerId = :carerId " +
           "AND ((cab.startTime <= :endTime) AND (cab.endTime >= :startTime))")
    List<CarerAvailabilityBlock> findOverlappingBlocks(
        @Param("carerId") UUID carerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    void deleteByCarerIdAndBookingId(UUID carerId, UUID bookingId);
}
