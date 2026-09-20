package com.staffs.leavebooking.leavemanagement.infrastructure.repositories;

import com.staffs.leavebooking.leavemanagement.infrastructure.entities.LeaveRequestJpa;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends CrudRepository<LeaveRequestJpa, String> {

    List<LeaveRequestJpa> findByStaffMemberId(String staffMemberId);

    List<LeaveRequestJpa> findByManagerId(String managerId);

    List<LeaveRequestJpa> findByStatus(String status);

    List<LeaveRequestJpa> findAll();

    List<LeaveRequestJpa> findByStaffMemberIdAndStatus(String staffMemberId, String status);

    @Query("SELECT r FROM leave_request r WHERE r.staffMemberId = :staffMemberId AND r.startDate <= :to AND r.endDate >= :from")
    List<LeaveRequestJpa> findByStaffMemberIdAndDateOverlap(@Param("staffMemberId") String staffMemberId,
                                                             @Param("from") LocalDate from,
                                                             @Param("to") LocalDate to);

    @Query("SELECT r FROM leave_request r WHERE r.staffMemberId = :staffMemberId AND r.status = :status AND r.startDate <= :to AND r.endDate >= :from")
    List<LeaveRequestJpa> findByStaffMemberIdAndStatusAndDateOverlap(@Param("staffMemberId") String staffMemberId,
                                                                      @Param("status") String status,
                                                                      @Param("from") LocalDate from,
                                                                      @Param("to") LocalDate to);

    List<LeaveRequestJpa> findByManagerIdAndStatus(String managerId, String status);

    @Query("SELECT r FROM leave_request r WHERE r.managerId = :managerId AND r.startDate <= :to AND r.endDate >= :from")
    List<LeaveRequestJpa> findByManagerIdAndDateOverlap(@Param("managerId") String managerId,
                                                        @Param("from") LocalDate from,
                                                        @Param("to") LocalDate to);

    @Query("SELECT r FROM leave_request r WHERE r.managerId = :managerId AND r.status = :status AND r.startDate <= :to AND r.endDate >= :from")
    List<LeaveRequestJpa> findByManagerIdAndStatusAndDateOverlap(@Param("managerId") String managerId,
                                                                  @Param("status") String status,
                                                                  @Param("from") LocalDate from,
                                                                  @Param("to") LocalDate to);

    @Query("SELECT r FROM leave_request r WHERE r.startDate <= :to AND r.endDate >= :from")
    List<LeaveRequestJpa> findByDateOverlap(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT r FROM leave_request r WHERE r.status = :status AND r.startDate <= :to AND r.endDate >= :from")
    List<LeaveRequestJpa> findByStatusAndDateOverlap(@Param("status") String status,
                                                      @Param("from") LocalDate from,
                                                      @Param("to") LocalDate to);
}
