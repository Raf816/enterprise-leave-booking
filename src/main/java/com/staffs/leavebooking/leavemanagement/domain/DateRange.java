package com.staffs.leavebooking.leavemanagement.domain;

import com.staffs.leavebooking.common.domain.ValueObject;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static com.staffs.leavebooking.common.domain.DomainAssertions.argumentNotNull;

public record DateRange(LocalDate startDate, LocalDate endDate) implements ValueObject {

    public static final String START_DATE_NOT_NULL = "Start date cannot be null";

    public static final String END_DATE_NOT_NULL = "End date cannot be null";

    public static final String END_BEFORE_START = "End date must be on or after start date";

    public static final String START_DATE_IN_PAST = "Start date must be in the future";

    public DateRange {
        argumentNotNull(startDate, START_DATE_NOT_NULL);
        argumentNotNull(endDate, END_DATE_NOT_NULL);

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(END_BEFORE_START);
        }
    }

    public void validateFutureStart() {
        if (!startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(START_DATE_IN_PAST);
        }
    }

    public int workingDays() {
        int count = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            DayOfWeek day = current.getDayOfWeek();

            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }

            current = current.plusDays(1);
        }

        return count;
    }
}
