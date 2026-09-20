package com.staffs.leavebooking.leavemanagement.domain;

import com.staffs.leavebooking.common.domain.ValueObject;

public record BusinessYear(int startYear, int endYear) implements ValueObject {

    public static final String INVALID_START_YEAR = "Start year must be a positive value";

    public static final String END_YEAR_MUST_FOLLOW_START = "End year must be start year + 1";

    public BusinessYear {
        if (startYear <= 0) {
            throw new IllegalArgumentException(INVALID_START_YEAR);
        }

        if (endYear != startYear + 1) {
            throw new IllegalArgumentException(END_YEAR_MUST_FOLLOW_START);
        }
    }

    public static BusinessYear current() {
        int currentYear = java.time.LocalDate.now().getYear();
        return new BusinessYear(currentYear, currentYear + 1);
    }

    @Override
    public String toString() {
        return startYear + "-" + endYear; // Format as "YYYY-YYYY"
    }
}
