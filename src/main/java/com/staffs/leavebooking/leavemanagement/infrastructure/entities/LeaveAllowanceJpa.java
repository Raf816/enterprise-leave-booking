package com.staffs.leavebooking.leavemanagement.infrastructure.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "leave_allowance")
@Table(name = "leave_allowance",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_allowance_staff_year",
                columnNames = {"staff_member_id", "business_year_start"}
        ))
@Getter
@Setter
@ToString
public class LeaveAllowanceJpa {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "Staff member ID is required")
    @Column(name = "staff_member_id", nullable = false, length = 36)
    private String staffMemberId;

    @NotBlank(message = "Manager ID is required")
    @Column(name = "manager_id", nullable = false, length = 36)
    private String managerId;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Surname is required")
    @Column(name = "surname", nullable = false, length = 50)
    private String surname;

    @Column(name = "department", length = 100)
    private String department;

    @NotNull(message = "Business year start is required")
    @Column(name = "business_year_start", nullable = false)
    private Integer businessYearStart;

    @NotNull(message = "Business year end is required")
    @Column(name = "business_year_end", nullable = false)
    private Integer businessYearEnd;

    @Positive(message = "Total entitlement must be positive")
    @Column(name = "total_entitlement", nullable = false)
    private int totalEntitlement;

    @PositiveOrZero(message = "Days used cannot be negative")
    @Column(name = "days_used", nullable = false)
    private int daysUsed;

    @PositiveOrZero(message = "Days pending cannot be negative")
    @Column(name = "days_pending", nullable = false)
    private int daysPending;
}
