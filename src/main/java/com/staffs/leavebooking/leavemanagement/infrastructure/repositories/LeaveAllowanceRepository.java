package com.staffs.leavebooking.leavemanagement.infrastructure.repositories;

import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveAllowanceJpa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveAllowanceRepository extends CrudRepository<LeaveAllowanceJpa, String> {

    Optional<LeaveAllowanceJpa> findByStaffMemberIdAndBusinessYearStart(String staffMemberId, Integer businessYearStart);

    Optional<LeaveAllowanceJpa> findFirstByStaffMemberIdOrderByBusinessYearStartDesc(String staffMemberId);

    List<LeaveAllowanceJpa> findByManagerId(String managerId);

    List<LeaveAllowanceJpa> findAll();

    List<LeaveAllowanceJpa> findByDepartment(String department);

    boolean existsByStaffMemberIdAndBusinessYearStart(String staffMemberId, Integer businessYearStart);
}
