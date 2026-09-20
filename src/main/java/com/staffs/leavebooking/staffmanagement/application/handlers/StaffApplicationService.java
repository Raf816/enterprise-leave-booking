package com.staffs.leavebooking.staffmanagement.application.handlers;

import com.staffs.leavebooking.common.domain.Email;
import com.staffs.leavebooking.common.domain.FullName;
import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.common.events.DomainEventManager;
import com.staffs.leavebooking.staffmanagement.application.commands.AddStaffMemberCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdateDepartmentCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdatePlacementCommand;
import com.staffs.leavebooking.staffmanagement.application.commands.UpdateStatusCommand;
import com.staffs.leavebooking.staffmanagement.application.mappers.StaffMemberDomainToJpaMapper;
import com.staffs.leavebooking.staffmanagement.application.mappers.StaffMemberJpaToDomainMapper;
import com.staffs.leavebooking.staffmanagement.domain.EmploymentStatus;
import com.staffs.leavebooking.staffmanagement.domain.EmploymentType;
import com.staffs.leavebooking.staffmanagement.domain.StaffMember;
import com.staffs.leavebooking.staffmanagement.infrastructure.repositories.StaffMemberRepository;
import com.staffs.leavebooking.staffmanagement.ui.exceptions.StaffMemberNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
@AllArgsConstructor
public class StaffApplicationService {

    private final StaffMemberRepository staffMemberRepository;

    private final DomainEventManager domainEventManager;

    @Transactional
    public String addNewStaffMember(AddStaffMemberCommand command) {
        return addNewStaffMemberWithId(Identity.generateId().id(), command);
    }

    @Transactional
    public String addNewStaffMemberWithId(String staffId, AddStaffMemberCommand command) {
        if (staffMemberRepository.existsByEmail(command.email())) {
            throw new DataIntegrityViolationException(
                    "A staff member with email " + command.email() + " already exists.");
        }

        Identity<StaffMember> id = Identity.of(staffId);

        StaffMember staffMember = StaffMember.createNew(
                id,
                new FullName(command.firstName(), command.surname()),
                new Email(command.email()),
                command.department(),
                command.lineManagerId(),
                command.hireDate(),
                command.currentRole(),
                command.startDateOfCurrentRole(),
                command.jobLevel(),
                parseEmploymentType(command.employmentType())
        );

        staffMemberRepository.save(StaffMemberDomainToJpaMapper.toJpa(staffMember));
        log.info("Staff member {} created with id {} (status: PENDING_SETUP)", command.email(), staffId);
        return staffId;
    }

    @Transactional
    public String createSkeletonStaffMember(String firebaseUid, String firstName, String surname, String email) {
        if (staffMemberRepository.existsByEmail(email)) {
            log.warn("Staff record already exists for email {}. Skipping skeleton creation.", email);
            return firebaseUid;
        }

        Identity<StaffMember> id = Identity.of(firebaseUid);

        StaffMember staffMember = StaffMember.createSkeleton(
                id,
                new FullName(firstName, surname),
                new Email(email)
        );

        staffMemberRepository.save(StaffMemberDomainToJpaMapper.toJpa(staffMember));
        log.info("Skeleton staff record created for {} (uid: {}, status: PENDING_SETUP)", email, firebaseUid);
        return firebaseUid;
    }

    @Transactional
    public void updateDepartment(UpdateDepartmentCommand command) {
        StaffMember staffMember = loadDomainAggregate(command.staffMemberId());

        // null in the command means keep the current value
        String effectiveDepartment = command.department() != null ? command.department() : staffMember.department();
        String effectiveManager = command.lineManagerId() != null ? command.lineManagerId() : staffMember.lineManagerId();

        staffMember.updateDepartment(effectiveDepartment, effectiveManager);

        staffMemberRepository.save(StaffMemberDomainToJpaMapper.toJpa(staffMember));

        dispatchAndClear(staffMember);

        log.info("Staff member {} department updated to {}", command.staffMemberId(), effectiveDepartment);
    }

    @Transactional
    public void updatePlacement(UpdatePlacementCommand command) {
        StaffMember staffMember = loadDomainAggregate(command.staffMemberId());

        String effectiveRole = command.currentRole() != null ? command.currentRole() : staffMember.currentRole();
        LocalDate effectiveStartDate = command.startDateOfCurrentRole() != null
                ? command.startDateOfCurrentRole() : staffMember.startDateOfCurrentRole();
        String effectiveJobLevel = command.jobLevel() != null ? command.jobLevel() : staffMember.jobLevel();
        String effectiveType = command.employmentType() != null ? command.employmentType() : staffMember.employmentType().name();

        staffMember.updatePlacement(
                effectiveRole,
                effectiveStartDate,
                effectiveJobLevel,
                EmploymentType.valueOf(effectiveType)
        );

        staffMemberRepository.save(StaffMemberDomainToJpaMapper.toJpa(staffMember));
        log.info("Staff member {} placement updated", command.staffMemberId());
    }

    @Transactional
    public void updateStatus(UpdateStatusCommand command) {
        StaffMember staffMember = loadDomainAggregate(command.staffMemberId());

        staffMember.updateStatus(EmploymentStatus.valueOf(command.employmentStatus()));

        staffMemberRepository.save(StaffMemberDomainToJpaMapper.toJpa(staffMember));

        dispatchAndClear(staffMember);

        log.info("Staff member {} status updated to {}", command.staffMemberId(), command.employmentStatus());
    }

    // publishes any pending domain events and clears them from the aggregate
    private void dispatchAndClear(StaffMember aggregate) {
        if (aggregate.domainEventsExist()) {
            domainEventManager.manageDomainEvents(
                    this.getClass().getSimpleName(),
                    aggregate.listOfDomainEvents()
            );
            aggregate.clearDomainEvents();
        }
    }

    private StaffMember loadDomainAggregate(String staffMemberId) {
        return staffMemberRepository.findById(staffMemberId)
                .map(StaffMemberJpaToDomainMapper::toDomain)
                .orElseThrow(() -> new StaffMemberNotFoundException(staffMemberId));
    }

    private EmploymentType parseEmploymentType(String type) {
        try {
            return EmploymentType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid employment type: '" + type + "'. Valid values are: FULL_TIME, PART_TIME, CONTRACT");
        }
    }
}
