package com.staffs.leavebooking.leavemanagement.application.handlers;

import com.staffs.leavebooking.common.domain.Identity;
import com.staffs.leavebooking.leavemanagement.application.commands.AmendEntitlementCommand;
import com.staffs.leavebooking.leavemanagement.application.mappers.LeaveAllowanceDomainToJpaMapper;
import com.staffs.leavebooking.leavemanagement.application.mappers.LeaveAllowanceJpaToDomainMapper;
import com.staffs.leavebooking.leavemanagement.domain.LeaveAllowance;
import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;
import com.staffs.leavebooking.leavemanagement.infrastructure.repositories.LeaveAllowanceRepository;
import com.staffs.leavebooking.leavemanagement.ui.exceptions.LeaveAllowanceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Slf4j
@AllArgsConstructor
public class LeaveAllowanceApplicationService {

    private final LeaveAllowanceRepository leaveAllowanceRepository;

    @Transactional
    public void reserveDays(String staffMemberId, int numberOfDays) {
        LeaveAllowanceJpa jpa = findCurrentAllowance(staffMemberId);
        LeaveAllowance allowance = LeaveAllowanceJpaToDomainMapper.toDomain(jpa);

        allowance.reserveDays(numberOfDays);

        LeaveAllowanceDomainToJpaMapper.updateJpa(allowance, jpa);
        leaveAllowanceRepository.save(jpa);
        log.info("Reserved {} days for staff member {}", numberOfDays, staffMemberId);
    }

    @Transactional
    public void confirmDays(String staffMemberId, int numberOfDays) {
        LeaveAllowanceJpa jpa = findCurrentAllowance(staffMemberId);
        LeaveAllowance allowance = LeaveAllowanceJpaToDomainMapper.toDomain(jpa);

        allowance.confirmDays(numberOfDays);

        LeaveAllowanceDomainToJpaMapper.updateJpa(allowance, jpa);
        leaveAllowanceRepository.save(jpa);
        log.info("Confirmed {} days for staff member {}", numberOfDays, staffMemberId);
    }

    @Transactional
    public void releasePendingDays(String staffMemberId, int numberOfDays) {
        LeaveAllowanceJpa jpa = findCurrentAllowance(staffMemberId);
        LeaveAllowance allowance = LeaveAllowanceJpaToDomainMapper.toDomain(jpa);

        allowance.releasePendingDays(numberOfDays);

        LeaveAllowanceDomainToJpaMapper.updateJpa(allowance, jpa);
        leaveAllowanceRepository.save(jpa);
        log.info("Released {} pending days for staff member {}", numberOfDays, staffMemberId);
    }

    @Transactional
    public void creditBackDays(String staffMemberId, int numberOfDays) {
        LeaveAllowanceJpa jpa = findCurrentAllowance(staffMemberId);
        LeaveAllowance allowance = LeaveAllowanceJpaToDomainMapper.toDomain(jpa);

        allowance.creditBackDays(numberOfDays);

        LeaveAllowanceDomainToJpaMapper.updateJpa(allowance, jpa);
        leaveAllowanceRepository.save(jpa);
        log.info("Credited back {} days for staff member {}", numberOfDays, staffMemberId);
    }

    @Transactional
    public void amendEntitlement(AmendEntitlementCommand command) {
        LeaveAllowanceJpa jpa = leaveAllowanceRepository.findById(command.leaveAllowanceId())
                .orElseThrow(() -> new LeaveAllowanceNotFoundException(command.leaveAllowanceId()));

        LeaveAllowance allowance = LeaveAllowanceJpaToDomainMapper.toDomain(jpa);
        allowance.amendEntitlement(command.newEntitlement());

        LeaveAllowanceDomainToJpaMapper.updateJpa(allowance, jpa);
        leaveAllowanceRepository.save(jpa);
        log.info("Entitlement amended to {} for allowance {}", command.newEntitlement(), command.leaveAllowanceId());
    }

    @Transactional
    public void createAllowanceForNewStaff(String staffMemberId, String managerId,
                                            String firstName, String surname,
                                            String department, int defaultEntitlement) {
        int currentYear = LocalDate.now().getYear();

        if (leaveAllowanceRepository.existsByStaffMemberIdAndBusinessYearStart(staffMemberId, currentYear)) {
            log.warn("Allowance already exists for staff {} in year {}. Skipping.", staffMemberId, currentYear);
            return;
        }

        Identity<LeaveAllowance> newId = Identity.generateId();
        LeaveAllowance allowance = LeaveAllowance.createNew(
                newId, staffMemberId, managerId, firstName, surname, department, defaultEntitlement
        );

        leaveAllowanceRepository.save(LeaveAllowanceDomainToJpaMapper.toJpa(allowance));
        log.info("Created leave allowance {} for new staff member {}", newId.id(), staffMemberId);
    }

    @Transactional
    public void updateStaffDetails(String staffMemberId, String managerId, String department) {
        LeaveAllowanceJpa jpa = findCurrentAllowance(staffMemberId);
        LeaveAllowance allowance = LeaveAllowanceJpaToDomainMapper.toDomain(jpa);

        allowance.updateStaffDetails(managerId, department);

        LeaveAllowanceDomainToJpaMapper.updateJpa(allowance, jpa);
        leaveAllowanceRepository.save(jpa);
        log.info("Updated staff details on allowance for staff member {}", staffMemberId);
    }

    private LeaveAllowanceJpa findCurrentAllowance(String staffMemberId) {
        return leaveAllowanceRepository
                .findFirstByStaffMemberIdOrderByBusinessYearStartDesc(staffMemberId)
                .orElseThrow(() -> new LeaveAllowanceNotFoundException(staffMemberId));
    }
}
