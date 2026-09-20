package com.staffs.leavebooking.leavemanagement.domain.events;

import com.staffs.leavebooking.common.events.LocalEvent;

import java.time.LocalDate;

public record LeaveRequestApprovedEvent(
        
        Long id,

        LocalDate occurredOn,

        String leaveRequestId,

        String staffMemberId,

        String managerId,

        int numberOfDays
) implements LocalEvent {

    public LeaveRequestApprovedEvent(LocalDate occurredOn, String leaveRequestId,
                                      String staffMemberId, String managerId, int numberOfDays) {
        this(null, occurredOn, leaveRequestId, staffMemberId, managerId, numberOfDays);
    }

    @Override
    public LeaveRequestApprovedEvent withId(Long newId) {
        return new LeaveRequestApprovedEvent(newId, this.occurredOn, this.leaveRequestId,
                this.staffMemberId, this.managerId, this.numberOfDays);
    }
}
