package com.booking.stadium.service;

import com.booking.stadium.dto.deposit.DepositRequest;
import com.booking.stadium.dto.deposit.DepositResponse;
import com.booking.stadium.dto.deposit.RefundRequest;
import com.booking.stadium.entity.*;
import com.booking.stadium.enums.*;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.exception.UnauthorizedException;
import com.booking.stadium.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepositService {

    private final DepositRepository depositRepository;
    private final BookingRepository bookingRepository;
    private final DepositPolicyRepository depositPolicyRepository;
    private final UserRepository userRepository;

    public DepositService(DepositRepository depositRepository,
                          BookingRepository bookingRepository,
                          DepositPolicyRepository depositPolicyRepository,
                          UserRepository userRepository) {
        this.depositRepository = depositRepository;
        this.bookingRepository = bookingRepository;
        this.depositPolicyRepository = depositPolicyRepository;
        this.userRepository = userRepository;
    }

    // ========== CUSTOMER ==========

    /**
     * Customer tạo giao dịch đặt cọc cho booking
     * Flow: Customer đặt sân → Chuyển cọc → Owner xác nhận → Booking DEPOSIT_PAID
     */
    @Transactional
    public DepositResponse createDeposit(Long bookingId, DepositRequest request) {
        User customer = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Kiểm tra quyền
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("Bạn không có quyền thao tác booking này");
        }

        // Kiểm tra trạng thái booking
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể đặt cọc cho booking đang ở trạng thái PENDING");
        }

        // Kiểm tra deposit status
        if (booking.getDepositStatus() != DepositStatus.PENDING) {
            throw new BadRequestException("Booking không yêu cầu đặt cọc hoặc đã đặt cọc");
        }

        // Kiểm tra đã có giao dịch cọc PENDING chưa
        List<Deposit> pendingDeposits = depositRepository.findByBookingIdAndStatus(
                bookingId, DepositTransactionStatus.PENDING);
        if (!pendingDeposits.isEmpty()) {
            throw new BadRequestException("Đã có giao dịch cọc đang chờ xác nhận");
        }

        Deposit deposit = Deposit.builder()
                .booking(booking)
                .amount(booking.getDepositAmount())
                .depositType(DepositType.DEPOSIT)
                .paymentMethod(request.getPaymentMethod())
                .transactionCode(request.getTransactionCode())
                .note(request.getNote())
                .status(DepositTransactionStatus.PENDING)
                .build();

        deposit = depositRepository.save(deposit);
        return DepositResponse.fromEntity(deposit);
    }

    /**
     * Xem lịch sử giao dịch cọc của booking
     */
    @Transactional(readOnly = true)
    public List<DepositResponse> getDepositsByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Kiểm tra quyền: customer hoặc owner
        User currentUser = getCurrentUser();
        boolean isCustomer = booking.getCustomer().getId().equals(currentUser.getId());
        boolean isOwner = booking.getField().getStadium().getOwner().getId().equals(currentUser.getId());

        if (!isCustomer && !isOwner) {
            throw new UnauthorizedException("Bạn không có quyền xem giao dịch cọc này");
        }

        return depositRepository.findByBookingId(bookingId)
                .stream().map(DepositResponse::fromEntity).toList();
    }

    // ========== OWNER ==========

    /**
     * Owner xác nhận đã nhận tiền cọc → Booking chuyển sang DEPOSIT_PAID
     */
    @Transactional
    public DepositResponse confirmDeposit(Long depositId) {
        User owner = getCurrentUser();

        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit", "id", depositId));

        // Kiểm tra quyền owner
        Booking booking = deposit.getBooking();
        if (!booking.getField().getStadium().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý giao dịch này");
        }

        if (deposit.getStatus() != DepositTransactionStatus.PENDING) {
            throw new BadRequestException("Giao dịch không ở trạng thái chờ xác nhận");
        }

        if (deposit.getDepositType() != DepositType.DEPOSIT) {
            throw new BadRequestException("Chỉ có thể xác nhận giao dịch đặt cọc");
        }

        // Cập nhật deposit
        deposit.setStatus(DepositTransactionStatus.CONFIRMED);
        deposit.setConfirmedBy(owner);
        deposit.setConfirmedAt(LocalDateTime.now());
        depositRepository.save(deposit);

        // Cập nhật booking status
        booking.setDepositStatus(DepositStatus.PAID);
        booking.setStatus(BookingStatus.DEPOSIT_PAID);
        bookingRepository.save(booking);

        return DepositResponse.fromEntity(deposit);
    }

    /**
     * Owner từ chối giao dịch cọc
     */
    @Transactional
    public DepositResponse rejectDeposit(Long depositId) {
        User owner = getCurrentUser();

        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit", "id", depositId));

        Booking booking = deposit.getBooking();
        if (!booking.getField().getStadium().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý giao dịch này");
        }

        if (deposit.getStatus() != DepositTransactionStatus.PENDING) {
            throw new BadRequestException("Giao dịch không ở trạng thái chờ xác nhận");
        }

        deposit.setStatus(DepositTransactionStatus.REJECTED);
        deposit.setConfirmedBy(owner);
        deposit.setConfirmedAt(LocalDateTime.now());
        depositRepository.save(deposit);

        return DepositResponse.fromEntity(deposit);
    }

    /**
     * Owner hoàn cọc khi customer hủy booking
     * Tính toán số tiền hoàn dựa trên DepositPolicy:
     * - Hủy trước refund_before_hours → hoàn refund_percent %
     * - Hủy trễ → hoàn late_cancel_refund_percent %
     */
    @Transactional
    public DepositResponse refundDeposit(Long bookingId, RefundRequest request) {
        User owner = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getField().getStadium().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý booking này");
        }

        // Booking phải đã bị hủy
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new BadRequestException("Chỉ hoàn cọc cho booking đã bị hủy");
        }

        // Kiểm tra đã trả cọc chưa
        if (booking.getDepositStatus() != DepositStatus.PAID) {
            throw new BadRequestException("Booking chưa đặt cọc, không thể hoàn");
        }

        // Kiểm tra đã hoàn cọc chưa
        List<Deposit> existingRefunds = depositRepository.findByBookingIdAndDepositType(
                bookingId, DepositType.REFUND);
        if (!existingRefunds.isEmpty()) {
            throw new BadRequestException("Đã hoàn cọc cho booking này rồi");
        }

        // Tính số tiền hoàn
        BigDecimal refundAmount = calculateRefundAmount(booking);

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Không đủ điều kiện hoàn cọc (số tiền hoàn = 0)");
        }

        Deposit refund = Deposit.builder()
                .booking(booking)
                .amount(refundAmount)
                .depositType(DepositType.REFUND)
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote() != null ? request.getNote() : "Hoàn cọc do hủy booking")
                .confirmedBy(owner)
                .confirmedAt(LocalDateTime.now())
                .status(DepositTransactionStatus.CONFIRMED)
                .build();

        depositRepository.save(refund);

        // Cập nhật deposit status
        booking.setDepositStatus(DepositStatus.REFUNDED);
        bookingRepository.save(booking);

        return DepositResponse.fromEntity(refund);
    }

    // ========== HELPERS ==========

    /**
     * Tính số tiền hoàn cọc dựa trên thời gian hủy và chính sách
     */
    private BigDecimal calculateRefundAmount(Booking booking) {
        DepositPolicy policy = depositPolicyRepository.findByStadiumId(
                        booking.getField().getStadium().getId())
                .orElse(null);

        if (policy == null) {
            return booking.getDepositAmount(); // Không có policy → hoàn 100%
        }

        BigDecimal depositAmount = booking.getDepositAmount();

        // Tính thời gian từ lúc hủy đến giờ đá
        LocalDateTime bookingDateTime = booking.getBookingDate()
                .atTime(booking.getTimeSlot().getStartTime());
        LocalDateTime cancelledAt = booking.getCancelledAt() != null
                ? booking.getCancelledAt() : LocalDateTime.now();

        long hoursBeforeMatch = java.time.Duration.between(cancelledAt, bookingDateTime).toHours();

        BigDecimal refundPercent;
        if (hoursBeforeMatch >= policy.getRefundBeforeHours()) {
            // Hủy đúng hạn → hoàn theo refund_percent
            refundPercent = policy.getRefundPercent();
        } else {
            // Hủy trễ → hoàn theo late_cancel_refund_percent
            refundPercent = policy.getLateCancelRefundPercent();
        }

        return depositAmount.multiply(refundPercent)
                .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR);
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }
}
