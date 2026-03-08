package com.booking.stadium.repository;

import com.booking.stadium.entity.Booking;
import com.booking.stadium.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);

    Page<Booking> findByCustomerIdAndStatus(Long customerId, BookingStatus status, Pageable pageable);

    List<Booking> findByFieldIdAndBookingDate(Long fieldId, LocalDate bookingDate);

    @Query("SELECT b FROM Booking b WHERE b.field.id = :fieldId " +
            "AND b.timeSlot.id = :timeSlotId " +
            "AND b.bookingDate = :bookingDate " +
            "AND b.status NOT IN ('CANCELLED')")
    List<Booking> findActiveBookings(
            @Param("fieldId") Long fieldId,
            @Param("timeSlotId") Long timeSlotId,
            @Param("bookingDate") LocalDate bookingDate);

    @Query("SELECT b FROM Booking b WHERE b.field.stadium.id = :stadiumId " +
            "AND b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByStadiumAndDateRange(
            @Param("stadiumId") Long stadiumId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE b.field.stadium.owner.id = :ownerId")
    Page<Booking> findByStadiumOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    List<Booking> findByRecurringBookingId(Long recurringBookingId);

    @Query("SELECT b FROM Booking b WHERE b.field.stadium.id = :stadiumId " +
            "AND b.bookingDate = :date")
    List<Booking> findByStadiumIdAndDate(
            @Param("stadiumId") Long stadiumId,
            @Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.status = :status " +
            "AND b.depositStatus = :depositStatus " +
            "AND b.createdAt < :cutoffTime")
    List<Booking> findExpiredDepositBookings(
            @Param("status") BookingStatus status,
            @Param("depositStatus") com.booking.stadium.enums.DepositStatus depositStatus,
            @Param("cutoffTime") java.time.LocalDateTime cutoffTime);

    long countByStatus(BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :start AND b.createdAt < :end")
    long countByCreatedAtBetween(@Param("start") java.time.LocalDateTime start,
                                 @Param("end") java.time.LocalDateTime end);
}
