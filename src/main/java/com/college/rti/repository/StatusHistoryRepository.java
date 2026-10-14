package com.college.rti.repository;

import com.college.rti.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    /**
     * Retrieve full chronological audit history for an RTI request by its internal ID.
     */
    List<StatusHistory> findByRequestIdOrderByTimestampAsc(Long requestId);

    /**
     * Retrieve full chronological audit history for an RTI request by its public Tracking ID.
     */
    List<StatusHistory> findByRequestTrackingIdOrderByTimestampAsc(String trackingId);
}
