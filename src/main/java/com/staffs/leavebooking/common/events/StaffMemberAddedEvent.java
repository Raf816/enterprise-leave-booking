package com.staffs.leavebooking.common.events;

import java.time.LocalDate;

public record StaffMemberAddedEvent(
        Long id,
        LocalDate occurredOn,
        String staffMemberId,
        String firstName,
        String surname,
        String email,
        String managerId,
        String department,
        int defaultEntitlement
) implements RemoteEvent {

    public StaffMemberAddedEvent(LocalDate occurredOn, String staffMemberId, String firstName,
                                  String surname, String email, String managerId,
                                  String department, int defaultEntitlement) {
        this(null, occurredOn, staffMemberId, firstName, surname, email,
                managerId, department, defaultEntitlement);
    }

    @Override
    public StaffMemberAddedEvent withId(Long newId) {
        return new StaffMemberAddedEvent(newId, this.occurredOn, this.staffMemberId,
                this.firstName, this.surname, this.email, this.managerId,
                this.department, this.defaultEntitlement);
    }
}
