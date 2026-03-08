package com.booking.stadium.repository;

import com.booking.stadium.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByFieldId(Long fieldId);

    List<TimeSlot> findByFieldIdAndIsActiveTrue(Long fieldId);

    List<TimeSlot> findByFieldIdOrderByStartTimeAsc(Long fieldId);
}
