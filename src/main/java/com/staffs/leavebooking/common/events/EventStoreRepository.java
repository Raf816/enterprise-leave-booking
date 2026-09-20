package com.staffs.leavebooking.common.events;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventStoreRepository extends CrudRepository<EventStoreJpa, Long> {

    List<EventStoreJpa> findByStatusAndOccurredOnBefore(String status, LocalDate cutoffDate);

    List<EventStoreJpa> findByStatus(String status);
}
