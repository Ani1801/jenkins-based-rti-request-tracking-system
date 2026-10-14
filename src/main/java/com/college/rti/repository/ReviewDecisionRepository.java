package com.college.rti.repository;

import com.college.rti.model.ReviewDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewDecisionRepository extends JpaRepository<ReviewDecision, Long> {

    /**
     * Retrieve all review decisions recorded for a specific RTI request ID.
     */
    List<ReviewDecision> findByRequestIdOrderByDecidedAtAsc(Long requestId);
}
