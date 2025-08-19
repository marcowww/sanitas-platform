package com.healthcare.staffing.carer.repository;

import com.healthcare.staffing.carer.domain.CarerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CarerAvailabilityRepository extends JpaRepository<CarerAvailability, UUID> {
    
    List<CarerAvailability> findByCarerId(UUID carerId);
    
    @Query("SELECT ca FROM CarerAvailability ca WHERE ca.carerId = :carerId AND ca.date >= :fromDate AND ca.date <= :toDate")
    List<CarerAvailability> findByCarerIdAndDateRange(@Param("carerId") UUID carerId,
                                                      @Param("fromDate") LocalDate fromDate,
                                                      @Param("toDate") LocalDate toDate);
    
    List<CarerAvailability> findByCarerIdAndDate(UUID carerId, LocalDate date);
    
    void deleteByCarerIdAndDate(UUID carerId, LocalDate date);
}
