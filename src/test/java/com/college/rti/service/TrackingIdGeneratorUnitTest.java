package com.college.rti.service;

import com.college.rti.model.TrackingSequence;
import com.college.rti.repository.TrackingSequenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingIdGeneratorUnitTest {

    @Mock
    private TrackingSequenceRepository sequenceRepository;

    @InjectMocks
    private TrackingIdGenerator trackingIdGenerator;

    @Test
    @DisplayName("Unit: Should initialize new sequence starting at 1 when row does not exist")
    void shouldInitializeSequenceWhenNotExists() {
        int year = 2026;
        when(sequenceRepository.findBySequenceYearForUpdate(year)).thenReturn(Optional.empty());
        when(sequenceRepository.saveAndFlush(any(TrackingSequence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String id = trackingIdGenerator.generateNextTrackingId(year);

        assertThat(id).isEqualTo("RTI-2026-000001");
        verify(sequenceRepository).findBySequenceYearForUpdate(year);
    }

    @Test
    @DisplayName("Unit: Should increment existing sequence value and format with zero padding")
    void shouldIncrementExistingSequence() {
        int year = 2026;
        TrackingSequence existingSequence = new TrackingSequence(year, 99L);
        when(sequenceRepository.findBySequenceYearForUpdate(year)).thenReturn(Optional.of(existingSequence));
        when(sequenceRepository.saveAndFlush(existingSequence)).thenReturn(existingSequence);

        String id = trackingIdGenerator.generateNextTrackingId(year);

        assertThat(id).isEqualTo("RTI-2026-000100");
        assertThat(existingSequence.getCurrentVal()).isEqualTo(100L);
        verify(sequenceRepository).saveAndFlush(existingSequence);
    }

    @Test
    @DisplayName("Unit: Default parameterless method should generate tracking ID for current year")
    void shouldDefaultToCurrentYear() {
        int currentYear = Year.now().getValue();
        TrackingSequence sequence = new TrackingSequence(currentYear, 5L);
        when(sequenceRepository.findBySequenceYearForUpdate(currentYear)).thenReturn(Optional.of(sequence));
        when(sequenceRepository.saveAndFlush(sequence)).thenReturn(sequence);

        String id = trackingIdGenerator.generateNextTrackingId();

        assertThat(id).isEqualTo(String.format("RTI-%d-000006", currentYear));
    }
}
