package com.healthcare.staffing.carer.repository;

import com.healthcare.staffing.carer.domain.Carer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarerRepository extends JpaRepository<Carer, UUID> {
    
    Optional<Carer> findByEmail(String email);
    
    List<Carer> findByStatus(Carer.CarerStatus status);
    
    List<Carer> findByGrade(String grade);
    
    @Query("SELECT c FROM Carer c WHERE c.location = :location AND c.status = :status")
    List<Carer> findByLocationAndStatus(@Param("location") String location, 
                                       @Param("status") Carer.CarerStatus status);
    
    @Query("SELECT c FROM Carer c JOIN c.qualifications q WHERE q = :qualification")
    List<Carer> findByQualification(@Param("qualification") String qualification);
}
