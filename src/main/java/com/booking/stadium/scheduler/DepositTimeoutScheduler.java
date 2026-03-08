package com.booking.stadium.scheduler;

import com.booking.stadium.entity.Booking;
import com.booking.stadium.enums.BookingStatus;
import com.booking.stadium.enums.DepositStatus;
import com.booking.stadium.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler tự động hủy booking nếu không đặt cọc trong 30 phút
 * Business Rule #10: Timeout cọc
 */
@Component
public class DepositTimeoutScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DepositTimeoutScheduler.class);
    private static final int DEPOSIT_TIMEOUT_MINUTES = 30;

    private final BookingRepository bookingRepository;

    public DepositTimeoutScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Chạy mỗi 5 phút, kiểm tra booking PENDING có deposit_status = PENDING
     * mà đã quá 30 phút kể từ khi tạo → tự động hủy
     */
    @Scheduled(fixedRate = 300000) // 5 phút
    @Transactional
    public void cancelUnpaidBookings() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(DEPOSIT_TIMEOUT_MINUTES);

        List<Booking> expiredBookings = bookingRepository.findExpiredDepositBookings(
                BookingStatus.PENDING, DepositStatus.PENDING, cutoffTime);

        if (!expiredBookings.isEmpty()) {
            logger.info("Tìm thấy {} booking quá hạn đặt cọc, tiến hành hủy...", expiredBookings.size());

            for (Booking booking : expiredBookings) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setCancelledAt(LocalDateTime.now());
                booking.setCancelReason("Tự động hủy do không đặt cọc trong " + DEPOSIT_TIMEOUT_MINUTES + " phút");
                bookingRepository.save(booking);

                logger.info("Đã hủy booking {} (code: {}) do quá hạn cọc",
                        booking.getId(), booking.getBookingCode());
            }
        }
    }
}
