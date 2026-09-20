package com.staffs.leavebooking.leavemanagement.domain.events;

import com.staffs.leavebooking.common.events.LocalEvent;

import java.time.LocalDate;

public record LeaveRequestCancelledEvent(
        
        Long id,

        LocalDate occurredOn,

        String leaveRequestId,

        String staffMemberId,

        String cancelledBy,

        int numberOfDays,

        boolean wasPreviouslyApproved
) implements LocalEvent {

    public LeaveRequestCancelledEvent(LocalDate occurredOn, String leaveRequestId,
                                       String staffMemberId, String cancelledBy,
                                       int numberOfDays, boolean wasPreviouslyApproved) {
        this(null, occurredOn, leaveRequestId, staffMemberId, cancelledBy,
                numberOfDays, wasPreviouslyApproved);
    }

    @Override
    public LeaveRequestCancelledEvent withId(Long newId) {
        return new LeaveRequestCancelledEvent(newId, this.occurredOn, this.leaveRequestId,
                this.staffMemberId, this.cancelledBy, this.numberOfDays, this.wasPreviouslyApproved);
    }
}
