package com.staffs.leavebooking.common.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class EventStoreCleanupJob {

    private static final int RETENTION_DAYS = 30;

    private final EventStoreService eventStoreService;

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldEvents() {
        log.info("Event store cleanup job started (retention: {} days)", RETENTION_DAYS);

        int purged = eventStoreService.purgeOldEvents(RETENTION_DAYS);

        log.info("Event store cleanup job completed: {} events purged", purged);
    }
}
