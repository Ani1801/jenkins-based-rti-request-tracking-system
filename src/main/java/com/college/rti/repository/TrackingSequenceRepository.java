package com.college.rti.repository;

import com.college.rti.model.TrackingSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackingSequenceRepository extends JpaRepository<TrackingSequence, Integer> {

    /**
     * Lock the sequence row with PESSIMISTIC_WRITE (SELECT ... FOR UPDATE) to guarantee
     * collision-free sequential tracking numbers in concurrent environments.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TrackingSequence s WHERE s.sequenceYear = :year")
    Optional<TrackingSequence> findBySequenceYearForUpdate(@Param("year") Integer year);
}
