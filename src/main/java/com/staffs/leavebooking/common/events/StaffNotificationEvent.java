package com.staffs.leavebooking.common.events;

import java.time.LocalDate;

public record StaffNotificationEvent(
        Long id,
        LocalDate occurredOn,
        String staffMemberId,
        String leaveRequestId,
        String decision,
        String decidedBy,
        int numberOfDays
) implements RemoteEvent {

    public StaffNotificationEvent(LocalDate occurredOn, String staffMemberId, String leaveRequestId,
                                   String decision, String decidedBy, int numberOfDays) {
        this(null, occurredOn, staffMemberId, leaveRequestId, decision, decidedBy, numberOfDays);
    }

    @Override
    public StaffNotificationEvent withId(Long newId) {
        return new StaffNotificationEvent(newId, this.occurredOn, this.staffMemberId,
                this.leaveRequestId, this.decision, this.decidedBy, this.numberOfDays);
    }
}
