package com.staffs.leavebooking.leavemanagement.domain.events;

import com.staffs.leavebooking.common.events.LocalEvent;

import java.time.LocalDate;

public record LeaveRequestSubmittedEvent(
        
        Long id,

        LocalDate occurredOn,

        String leaveRequestId,

        String staffMemberId,

        int numberOfDays
) implements LocalEvent {

    public LeaveRequestSubmittedEvent(LocalDate occurredOn, String leaveRequestId,
                                      String staffMemberId, int numberOfDays) {
        this(null, occurredOn, leaveRequestId, staffMemberId, numberOfDays);
    }

    @Override
    public LeaveRequestSubmittedEvent withId(Long newId) {
        return new LeaveRequestSubmittedEvent(newId, this.occurredOn, this.leaveRequestId,
                this.staffMemberId, this.numberOfDays);
    }
}
