package com.college.rti.service;

import com.college.rti.model.TrackingSequence;
import com.college.rti.repository.TrackingSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * Service generating unique tracking IDs in the format RTI-YYYY-XXXXXX
 * backed by a pessimistic write-locked database sequence row.
 */
@Service
public class TrackingIdGenerator {

    private final TrackingSequenceRepository sequenceRepository;

    public TrackingIdGenerator(TrackingSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Generates a new formatted tracking ID for the given year (defaults to current year).
     * Execution uses MANDATORY propagation when called inside an active transaction,
     * or REQUIRED propagation to create its own transaction boundary.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String generateNextTrackingId() {
        int currentYear = Year.now().getValue();
        return generateNextTrackingId(currentYear);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public synchronized String generateNextTrackingId(int year) {
        TrackingSequence sequence = sequenceRepository.findBySequenceYearForUpdate(year)
                .orElseGet(() -> {
                    TrackingSequence newSeq = new TrackingSequence(year, 0L);
                    return sequenceRepository.saveAndFlush(newSeq);
                });

        long nextVal = sequence.getCurrentVal() + 1;
        sequence.setCurrentVal(nextVal);
        sequenceRepository.saveAndFlush(sequence);

        return String.format("RTI-%d-%06d", year, nextVal);
    }
}
