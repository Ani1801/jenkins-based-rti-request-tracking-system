package com.college.rti.service;

import com.college.rti.dto.RequestSubmissionDto;
import com.college.rti.model.StatusHistory;
import com.college.rti.repository.RtiRequestRepository;
import com.college.rti.repository.StatusHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class RequestServiceRollbackTests {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RtiRequestRepository requestRepository;

    @MockBean
    private StatusHistoryRepository historyRepositoryMock;

    @Test
    @DisplayName("Should rollback RtiRequest persistence if StatusHistory persistence fails")
    void shouldRollbackWhenStatusHistoryFails() {
        // Mock failure on status history save
        doThrow(new RuntimeException("Simulated status history DB write failure"))
                .when(historyRepositoryMock).save(any(StatusHistory.class));

        RequestSubmissionDto dto = new RequestSubmissionDto(
                "Rollback Test User",
                "rollback@example.com",
                "9876543210",
                "ADMINISTRATIVE",
                "Request testing transaction rollback behavior on audit error."
        );

        long countBefore = requestRepository.count();

        assertThatThrownBy(() -> requestService.submitRequest(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated status history DB write failure");

        long countAfter = requestRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }
}
