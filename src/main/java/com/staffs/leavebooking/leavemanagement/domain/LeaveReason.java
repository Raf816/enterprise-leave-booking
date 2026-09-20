package com.staffs.leavebooking.leavemanagement.domain;

import com.staffs.leavebooking.common.domain.ValueObject;

import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentLength;
import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentNotEmpty;

public record LeaveReason(String reason) implements ValueObject {

    public static final int MAX_LENGTH = 500;

    public static final String REASON_NOT_EMPTY = "Leave reason cannot be empty";

    public static final String REASON_TOO_LONG = "Leave reason must not exceed " + MAX_LENGTH + " characters";

    public LeaveReason {
        reason = argumentNotEmpty(reason, REASON_NOT_EMPTY);
        argumentLength(reason, 1, MAX_LENGTH, REASON_TOO_LONG);
    }
}
