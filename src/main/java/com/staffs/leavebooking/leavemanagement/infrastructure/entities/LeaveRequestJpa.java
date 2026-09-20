package com.staffs.leavebooking.leavemanagement.infrastructure.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity(name = "leave_request")
@Table(name = "leave_request")
@Getter
@Setter
@ToString
public class LeaveRequestJpa {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "Staff member ID is required")
    @Column(name = "staff_member_id", nullable = false, length = 36)
    private String staffMemberId;

    @NotBlank(message = "Manager ID is required")
    @Column(name = "manager_id", nullable = false, length = 36)
    private String managerId;

    @NotBlank(message = "Leave type is required")
    @Column(name = "leave_type", nullable = false, length = 20)
    private String leaveType;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Positive(message = "Number of days must be positive")
    @Column(name = "number_of_days", nullable = false)
    private int numberOfDays;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    @Column(name = "reason", length = 500)
    private String reason;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull(message = "Submitted on date is required")
    @Column(name = "submitted_on", nullable = false)
    private LocalDate submittedOn;

    @Column(name = "decided_on")
    private LocalDate decidedOn;

    @Column(name = "decided_by", length = 36)
    private String decidedBy;

    @Size(max = 500, message = "Decision reason must not exceed 500 characters")
    @Column(name = "decision_reason", length = 500)
    private String decisionReason;

    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
}
