package com.booking.stadium.repository;

import com.booking.stadium.entity.RecurringBooking;
import com.booking.stadium.enums.RecurringBookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecurringBookingRepository extends JpaRepository<RecurringBooking, Long> {

    Optional<RecurringBooking> findByRecurringCode(String recurringCode);

    Page<RecurringBooking> findByCustomerId(Long customerId, Pageable pageable);

    Page<RecurringBooking> findByCustomerIdAndStatus(Long customerId, RecurringBookingStatus status, Pageable pageable);

    Page<RecurringBooking> findByFieldStadiumOwnerId(Long ownerId, Pageable pageable);
}
