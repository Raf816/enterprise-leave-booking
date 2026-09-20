package com.staffs.leavebooking.common.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventStoreCleanupJob (Scheduled Purge)")
class EventStoreCleanupJobTest {

    @Mock
    private EventStoreService eventStoreService;

    @InjectMocks
    private EventStoreCleanupJob cleanupJob;

    @Test
    @DisplayName("Should delegate to EventStoreService.purgeOldEvents with 30 day retention")
    void shouldDelegateWithCorrectRetention() {
        when(eventStoreService.purgeOldEvents(30)).thenReturn(5);

        cleanupJob.cleanupOldEvents();

        verify(eventStoreService).purgeOldEvents(30);
    }

    @Test
    @DisplayName("Should complete successfully even when no events to purge")
    void shouldHandleZeroPurged() {
        when(eventStoreService.purgeOldEvents(30)).thenReturn(0);

        cleanupJob.cleanupOldEvents();

        verify(eventStoreService).purgeOldEvents(30);
    }
}
