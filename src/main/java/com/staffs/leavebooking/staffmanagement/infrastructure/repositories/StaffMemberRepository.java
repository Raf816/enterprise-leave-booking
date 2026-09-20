package com.staffs.leavebooking.staffmanagement.infrastructure.repositories;

import com.staffs.leavebooking.staffmanagement.infrastructure.entities.StaffMemberJpa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffMemberRepository extends CrudRepository<StaffMemberJpa, String> {

    List<StaffMemberJpa> findAll();

    List<StaffMemberJpa> findByDepartment(String department);

    List<StaffMemberJpa> findByEmploymentStatus(String employmentStatus);

    List<StaffMemberJpa> findByDepartmentAndEmploymentStatus(String department, String employmentStatus);

    List<StaffMemberJpa> findByLineManagerId(String lineManagerId);

    boolean existsByEmail(String email);
}
