package com.staffs.leavebooking.common.events;

import java.time.LocalDate;

public record ManagerNotificationEvent(
        Long id,
        LocalDate occurredOn,
        String managerId,
        String staffMemberId,
        String staffName,
        String leaveRequestId,
        LocalDate startDate,
        LocalDate endDate,
        int numberOfDays,
        String reason
) implements RemoteEvent {

    public ManagerNotificationEvent(LocalDate occurredOn, String managerId, String staffMemberId,
                                     String staffName, String leaveRequestId, LocalDate startDate,
                                     LocalDate endDate, int numberOfDays, String reason) {
        this(null, occurredOn, managerId, staffMemberId, staffName, leaveRequestId,
                startDate, endDate, numberOfDays, reason);
    }

    @Override
    public ManagerNotificationEvent withId(Long newId) {
        return new ManagerNotificationEvent(newId, this.occurredOn, this.managerId, this.staffMemberId,
                this.staffName, this.leaveRequestId, this.startDate, this.endDate, this.numberOfDays, this.reason);
    }
}
