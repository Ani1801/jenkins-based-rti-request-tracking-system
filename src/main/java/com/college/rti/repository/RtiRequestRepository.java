package com.college.rti.repository;

import com.college.rti.model.RequestStatus;
import com.college.rti.model.RtiRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RtiRequestRepository extends JpaRepository<RtiRequest, Long> {

    /**
     * Look up an RTI Request by its unique public tracking identifier.
     */
    Optional<RtiRequest> findByTrackingId(String trackingId);

    /**
     * Check if a tracking identifier is already in use.
     */
    boolean existsByTrackingId(String trackingId);

    /**
     * Retrieve all requests matching a specific status, ordered by submission time ascending.
     */
    List<RtiRequest> findByCurrentStatusOrderByCreatedAtAsc(RequestStatus currentStatus);

    /**
     * Retrieve requests matching any of the given statuses (e.g. pending reviewer queue).
     */
    List<RtiRequest> findByCurrentStatusInOrderByCreatedAtAsc(List<RequestStatus> statuses);
}
