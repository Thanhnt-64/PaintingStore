package com.vn.ManageStore.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.ManageStore.domain.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    /**
     * Find events starting after the given date (upcoming events)
     */
    List<Event> findByStartDateAfterOrderByStartDateAsc(LocalDateTime startDate);
}
