package com.booking.stadium.repository;

import com.booking.stadium.entity.Deposit;
import com.booking.stadium.enums.DepositTransactionStatus;
import com.booking.stadium.enums.DepositType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findByBookingId(Long bookingId);

    List<Deposit> findByBookingIdAndDepositType(Long bookingId, DepositType depositType);

    List<Deposit> findByBookingIdAndStatus(Long bookingId, DepositTransactionStatus status);
}
