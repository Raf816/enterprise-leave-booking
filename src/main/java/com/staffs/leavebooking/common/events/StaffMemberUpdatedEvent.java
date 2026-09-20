package com.staffs.leavebooking.common.events;

import java.time.LocalDate;

public record StaffMemberUpdatedEvent(
        Long id,
        LocalDate occurredOn,
        String staffMemberId,
        String managerId,
        String department
) implements RemoteEvent {

    public StaffMemberUpdatedEvent(LocalDate occurredOn, String staffMemberId,
                                    String managerId, String department) {
        this(null, occurredOn, staffMemberId, managerId, department);
    }

    @Override
    public StaffMemberUpdatedEvent withId(Long newId) {
        return new StaffMemberUpdatedEvent(newId, this.occurredOn, this.staffMemberId,
                this.managerId, this.department);
    }
}
