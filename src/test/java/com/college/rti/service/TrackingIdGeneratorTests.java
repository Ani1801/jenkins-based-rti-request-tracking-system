package com.college.rti.service;

import com.college.rti.model.TrackingSequence;
import com.college.rti.repository.TrackingSequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Year;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TrackingIdGeneratorTests {

    @Autowired
    private TrackingIdGenerator trackingIdGenerator;

    @Autowired
    private TrackingSequenceRepository sequenceRepository;

    @BeforeEach
    void setUp() {
        sequenceRepository.deleteAll();
    }

    @Test
    @DisplayName("Should generate tracking ID conforming to RTI-YYYY-XXXXXX format with current year")
    void shouldGenerateFormattedTrackingId() {
        int currentYear = Year.now().getValue();
        String id1 = trackingIdGenerator.generateNextTrackingId();
        String id2 = trackingIdGenerator.generateNextTrackingId();

        Pattern pattern = Pattern.compile("^RTI-" + currentYear + "-\\d{6}$");
        assertThat(id1).matches(pattern);
        assertThat(id2).matches(pattern);

        assertThat(id1).isEqualTo("RTI-" + currentYear + "-000001");
        assertThat(id2).isEqualTo("RTI-" + currentYear + "-000002");
    }

    @Test
    @DisplayName("Should generate sequential tracking IDs for specified year")
    void shouldGenerateSequentialIdsForGivenYear() {
        String id1 = trackingIdGenerator.generateNextTrackingId(2027);
        String id2 = trackingIdGenerator.generateNextTrackingId(2027);

        assertThat(id1).isEqualTo("RTI-2027-000001");
        assertThat(id2).isEqualTo("RTI-2027-000002");

        TrackingSequence seq = sequenceRepository.findById(2027).orElseThrow();
        assertThat(seq.getCurrentVal()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should handle concurrent tracking ID generation without generating duplicate IDs")
    void shouldHandleConcurrentGenerationWithoutDuplicates() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        Set<String> generatedIds = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    String id = trackingIdGenerator.generateNextTrackingId(2030);
                    generatedIds.add(id);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // Start all threads simultaneously
        doneLatch.await();
        executor.shutdown();

        assertThat(generatedIds).hasSize(threadCount);
    }

    @Test
    @DisplayName("Should maintain independent sequence numbers across different calendar years")
    void shouldMaintainIndependentSequencesAcrossYears() {
        String id2025_1 = trackingIdGenerator.generateNextTrackingId(2025);
        String id2026_1 = trackingIdGenerator.generateNextTrackingId(2026);
        String id2025_2 = trackingIdGenerator.generateNextTrackingId(2025);
        String id2026_2 = trackingIdGenerator.generateNextTrackingId(2026);

        assertThat(id2025_1).isEqualTo("RTI-2025-000001");
        assertThat(id2026_1).isEqualTo("RTI-2026-000001");
        assertThat(id2025_2).isEqualTo("RTI-2025-000002");
        assertThat(id2026_2).isEqualTo("RTI-2026-000002");
    }

    @Test
    @DisplayName("Should generate monotonic sequential IDs in single-threaded invocations")
    void shouldGenerateMonotonicSequentialIds() {
        for (int i = 1; i <= 5; i++) {
            String id = trackingIdGenerator.generateNextTrackingId(2029);
            assertThat(id).isEqualTo(String.format("RTI-2029-%06d", i));
        }
    }
}
