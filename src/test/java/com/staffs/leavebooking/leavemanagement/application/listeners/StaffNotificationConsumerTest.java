package com.staffs.leavebooking.leavemanagement.application.listeners;

import com.staffs.leavebooking.common.events.StaffNotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
@DisplayName("StaffNotificationConsumer (RabbitMQ Consumer)")
class StaffNotificationConsumerTest {

    @InjectMocks
    private StaffNotificationConsumer consumer;

    @Test
    @DisplayName("Should process APPROVED notification event without error")
    void shouldProcessApprovedNotification() {
        StaffNotificationEvent event = new StaffNotificationEvent(
                LocalDate.now(), "staff-001", "lr-001", "APPROVED", "mgr-001", 5);

        assertThatNoException().isThrownBy(() -> consumer.receive(event));
    }

    @Test
    @DisplayName("Should process REJECTED notification event without error")
    void shouldProcessRejectedNotification() {
        StaffNotificationEvent event = new StaffNotificationEvent(
                LocalDate.now(), "staff-002", "lr-002", "REJECTED", "mgr-001", 3);

        assertThatNoException().isThrownBy(() -> consumer.receive(event));
    }

    @Test
    @DisplayName("Should process CANCELLED notification event without error")
    void shouldProcessCancelledNotification() {
        StaffNotificationEvent event = new StaffNotificationEvent(
                LocalDate.now(), "staff-003", "lr-003", "CANCELLED", "staff-003", 2);

        assertThatNoException().isThrownBy(() -> consumer.receive(event));
    }
}
