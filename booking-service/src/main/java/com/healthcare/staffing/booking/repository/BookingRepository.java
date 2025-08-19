package com.healthcare.staffing.booking.repository;

import com.healthcare.staffing.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    List<Booking> findByFacilityIdAndStatus(UUID facilityId, Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.startTime >= :startDate AND b.endTime <= :endDate")
    List<Booking> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    List<Booking> findByAssignedCarerId(UUID carerId);
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.startTime >= :fromDate")
    List<Booking> findByStatusAndStartTimeAfter(@Param("status") Booking.BookingStatus status,
                                              @Param("fromDate") LocalDateTime fromDate);
}
