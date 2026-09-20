package com.staffs.leavebooking.staffmanagement.infrastructure.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity(name = "staff_member")
@Table(name = "staff_member")
@Getter
@Setter
@ToString
public class StaffMemberJpa {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Surname is required")
    @Size(max = 50)
    @Column(name = "surname", nullable = false, length = 50)
    private String surname;

    @NotBlank(message = "Email is required")
    @Size(max = 150)
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "line_manager_id", length = 36)
    private String lineManagerId;

    @NotNull(message = "Hire date is required")
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotBlank(message = "Current role is required")
    @Size(max = 100)
    @Column(name = "`current_role`", nullable = false, length = 100)
    private String currentRole;

    @NotNull(message = "Start date of current role is required")
    @Column(name = "start_date_current_role", nullable = false)
    private LocalDate startDateCurrentRole;

    @Size(max = 20)
    @Column(name = "job_level", length = 20)
    private String jobLevel;

    @NotBlank(message = "Employment type is required")
    @Column(name = "employment_type", nullable = false, length = 20)
    private String employmentType;

    @NotBlank(message = "Employment status is required")
    @Column(name = "employment_status", nullable = false, length = 20)
    private String employmentStatus;
}
