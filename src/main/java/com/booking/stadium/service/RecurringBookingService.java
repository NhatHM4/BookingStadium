package com.booking.stadium.service;

import com.booking.stadium.dto.booking.BookingResponse;
import com.booking.stadium.dto.recurring.RecurringBookingRequest;
import com.booking.stadium.dto.recurring.RecurringBookingResponse;
import com.booking.stadium.entity.*;
import com.booking.stadium.enums.*;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.exception.UnauthorizedException;
import com.booking.stadium.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecurringBookingService {

    private final RecurringBookingRepository recurringBookingRepository;
    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final DepositPolicyRepository depositPolicyRepository;

    public RecurringBookingService(RecurringBookingRepository recurringBookingRepository,
                                   BookingRepository bookingRepository,
                                   FieldRepository fieldRepository,
                                   TimeSlotRepository timeSlotRepository,
                                   UserRepository userRepository,
                                   DepositPolicyRepository depositPolicyRepository) {
        this.recurringBookingRepository = recurringBookingRepository;
        this.bookingRepository = bookingRepository;
        this.fieldRepository = fieldRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.depositPolicyRepository = depositPolicyRepository;
    }

    // ========== CUSTOMER ==========

    /**
     * Tạo gói đặt sân dài hạn
     * - Tính toán các ngày booking dựa trên recurrenceType
     * - Kiểm tra trùng lịch cho tất cả các ngày
     * - Áp dụng giảm giá nếu đủ điều kiện
     * - Tạo N bookings con
     */
    @Transactional
    public RecurringBookingResponse createRecurringBooking(RecurringBookingRequest request) {
        User customer = getCurrentUser();

        Field field = fieldRepository.findById(request.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", request.getFieldId()));

        if (!field.getIsActive()) {
            throw new BadRequestException("Sân con này hiện không hoạt động");
        }

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", request.getTimeSlotId()));

        if (!timeSlot.getField().getId().equals(field.getId())) {
            throw new BadRequestException("Khung giờ không thuộc sân này");
        }

        if (!timeSlot.getIsActive()) {
            throw new BadRequestException("Khung giờ này hiện không hoạt động");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Ngày bắt đầu không thể trong quá khứ");
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Tính các ngày booking
        List<LocalDate> bookingDates = calculateBookingDates(
                request.getRecurrenceType(), request.getStartDate(), request.getEndDate());

        if (bookingDates.isEmpty()) {
            throw new BadRequestException("Không có ngày nào phù hợp trong khoảng thời gian đã chọn");
        }

        // Kiểm tra trùng lịch cho tất cả các ngày
        List<LocalDate> conflictDates = new ArrayList<>();
        for (LocalDate date : bookingDates) {
            List<Booking> existing = bookingRepository.findActiveBookings(
                    field.getId(), timeSlot.getId(), date);
            if (!existing.isEmpty()) {
                conflictDates.add(date);
            }
        }

        if (!conflictDates.isEmpty()) {
            throw new BadRequestException("Trùng lịch vào các ngày: " + conflictDates);
        }

        // Tính giá
        BigDecimal originalPrice = timeSlot.getPrice();
        BigDecimal discountPercent = BigDecimal.ZERO;
        BigDecimal discountedPrice = originalPrice;
        int totalSessions = bookingDates.size();

        // Áp dụng giảm giá dài hạn
        DepositPolicy policy = depositPolicyRepository.findByStadiumId(field.getStadium().getId())
                .orElse(null);

        if (policy != null && totalSessions >= policy.getMinRecurringSessions()
                && policy.getRecurringDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            discountPercent = policy.getRecurringDiscountPercent();
            discountedPrice = originalPrice.subtract(
                    originalPrice.multiply(discountPercent).divide(new BigDecimal("100"), 0, RoundingMode.FLOOR));
        }

        BigDecimal totalPrice = discountedPrice.multiply(BigDecimal.valueOf(totalSessions));

        // Tính tiền cọc
        BigDecimal totalDeposit = BigDecimal.ZERO;
        RecurringDepositStatus depositStatus = RecurringDepositStatus.PENDING;
        DepositStatus bookingDepositStatus = DepositStatus.NOT_REQUIRED;

        if (policy != null && policy.getIsDepositRequired()) {
            totalDeposit = totalPrice.multiply(policy.getDepositPercent())
                    .divide(new BigDecimal("100"), 0, RoundingMode.CEILING);
            bookingDepositStatus = DepositStatus.PENDING;
        }

        // Tính day of week từ startDate
        int dayOfWeek = request.getStartDate().getDayOfWeek().getValue();

        // Generate recurring code
        String recurringCode = generateRecurringCode();

        // Tạo RecurringBooking
        RecurringBooking recurringBooking = RecurringBooking.builder()
                .recurringCode(recurringCode)
                .customer(customer)
                .field(field)
                .timeSlot(timeSlot)
                .recurrenceType(request.getRecurrenceType())
                .dayOfWeek(dayOfWeek)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalSessions(totalSessions)
                .completedSessions(0)
                .cancelledSessions(0)
                .discountPercent(discountPercent)
                .originalPricePerSession(originalPrice)
                .discountedPricePerSession(discountedPrice)
                .totalPrice(totalPrice)
                .totalDeposit(totalDeposit)
                .depositStatus(depositStatus)
                .status(RecurringBookingStatus.ACTIVE)
                .note(request.getNote())
                .build();

        recurringBooking = recurringBookingRepository.save(recurringBooking);

        // Tạo N bookings con
        BigDecimal depositPerSession = BigDecimal.ZERO;
        if (totalDeposit.compareTo(BigDecimal.ZERO) > 0) {
            depositPerSession = totalDeposit.divide(BigDecimal.valueOf(totalSessions), 0, RoundingMode.CEILING);
        }

        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (LocalDate date : bookingDates) {
            String bookingCode = generateBookingCode();
            BigDecimal remainingAmount = discountedPrice.subtract(depositPerSession);

            Booking booking = Booking.builder()
                    .bookingCode(bookingCode)
                    .customer(customer)
                    .field(field)
                    .timeSlot(timeSlot)
                    .recurringBooking(recurringBooking)
                    .bookingDate(date)
                    .isMatchRequest(false)
                    .totalPrice(discountedPrice)
                    .depositAmount(depositPerSession)
                    .remainingAmount(remainingAmount)
                    .depositStatus(bookingDepositStatus)
                    .note("Gói dài hạn: " + recurringCode)
                    .status(BookingStatus.PENDING)
                    .build();

            booking = bookingRepository.save(booking);
            bookingResponses.add(BookingResponse.fromEntity(booking));
        }

        return RecurringBookingResponse.fromEntity(recurringBooking, bookingResponses);
    }

    /**
     * DS gói dài hạn của customer
     */
    @Transactional(readOnly = true)
    public Page<RecurringBookingResponse> getMyRecurringBookings(RecurringBookingStatus status, Pageable pageable) {
        User customer = getCurrentUser();
        Page<RecurringBooking> page;
        if (status != null) {
            page = recurringBookingRepository.findByCustomerIdAndStatus(customer.getId(), status, pageable);
        } else {
            page = recurringBookingRepository.findByCustomerId(customer.getId(), pageable);
        }
        return page.map(RecurringBookingResponse::fromEntity);
    }

    /**
     * Chi tiết gói kèm danh sách các buổi
     */
    @Transactional(readOnly = true)
    public RecurringBookingResponse getRecurringBookingDetail(Long id) {
        RecurringBooking rb = recurringBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringBooking", "id", id));

        // Kiểm tra quyền: customer hoặc owner
        User currentUser = getCurrentUser();
        boolean isCustomer = rb.getCustomer().getId().equals(currentUser.getId());
        boolean isOwner = rb.getField().getStadium().getOwner().getId().equals(currentUser.getId());

        if (!isCustomer && !isOwner) {
            throw new UnauthorizedException("Bạn không có quyền xem gói này");
        }

        List<BookingResponse> bookings = bookingRepository.findByRecurringBookingId(id)
                .stream().map(BookingResponse::fromEntity).toList();

        return RecurringBookingResponse.fromEntity(rb, bookings);
    }

    /**
     * Hủy toàn bộ gói - hoàn cọc các buổi chưa diễn ra
     */
    @Transactional
    public RecurringBookingResponse cancelRecurringBooking(Long id) {
        User customer = getCurrentUser();

        RecurringBooking rb = recurringBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringBooking", "id", id));

        if (!rb.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("Bạn không có quyền hủy gói này");
        }

        if (rb.getStatus() == RecurringBookingStatus.CANCELLED) {
            throw new BadRequestException("Gói đã bị hủy trước đó");
        }

        if (rb.getStatus() == RecurringBookingStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy gói đã hoàn thành");
        }

        // Hủy tất cả booking chưa COMPLETED và chưa CANCELLED
        List<Booking> bookings = bookingRepository.findByRecurringBookingId(id);
        int cancelledCount = 0;
        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatus.COMPLETED
                    && booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setCancelledAt(LocalDateTime.now());
                booking.setCancelReason("Hủy toàn bộ gói dài hạn");
                bookingRepository.save(booking);
                cancelledCount++;
            }
        }

        rb.setCancelledSessions(rb.getCancelledSessions() + cancelledCount);
        rb.setStatus(RecurringBookingStatus.CANCELLED);
        recurringBookingRepository.save(rb);

        List<BookingResponse> bookingResponses = bookingRepository.findByRecurringBookingId(id)
                .stream().map(BookingResponse::fromEntity).toList();

        return RecurringBookingResponse.fromEntity(rb, bookingResponses);
    }

    /**
     * Hủy 1 buổi trong gói
     */
    @Transactional
    public RecurringBookingResponse cancelSingleBooking(Long recurringId, Long bookingId) {
        User customer = getCurrentUser();

        RecurringBooking rb = recurringBookingRepository.findById(recurringId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringBooking", "id", recurringId));

        if (!rb.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("Bạn không có quyền thao tác gói này");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getRecurringBooking() == null
                || !booking.getRecurringBooking().getId().equals(recurringId)) {
            throw new BadRequestException("Booking không thuộc gói dài hạn này");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Buổi này đã bị hủy trước đó");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy buổi đã hoàn thành");
        }

        // Kiểm tra hủy trước 2 tiếng
        LocalDateTime bookingDateTime = booking.getBookingDate()
                .atTime(booking.getTimeSlot().getStartTime());
        if (LocalDateTime.now().plusHours(2).isAfter(bookingDateTime)) {
            throw new BadRequestException("Chỉ được hủy trước giờ đá ít nhất 2 tiếng");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason("Hủy 1 buổi trong gói dài hạn");
        bookingRepository.save(booking);

        // Cập nhật recurring booking
        rb.setCancelledSessions(rb.getCancelledSessions() + 1);
        recurringBookingRepository.save(rb);

        List<BookingResponse> bookingResponses = bookingRepository.findByRecurringBookingId(recurringId)
                .stream().map(BookingResponse::fromEntity).toList();

        return RecurringBookingResponse.fromEntity(rb, bookingResponses);
    }

    // ========== OWNER ==========

    /**
     * DS gói dài hạn của sân mình
     */
    @Transactional(readOnly = true)
    public Page<RecurringBookingResponse> getOwnerRecurringBookings(Pageable pageable) {
        User owner = getCurrentUser();
        return recurringBookingRepository.findByFieldStadiumOwnerId(owner.getId(), pageable)
                .map(RecurringBookingResponse::fromEntity);
    }

    /**
     * Owner xác nhận gói dài hạn → confirm tất cả bookings con
     */
    @Transactional
    public RecurringBookingResponse confirmRecurringBooking(Long id) {
        User owner = getCurrentUser();

        RecurringBooking rb = recurringBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringBooking", "id", id));

        if (!rb.getField().getStadium().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý gói này");
        }

        if (rb.getStatus() != RecurringBookingStatus.ACTIVE) {
            throw new BadRequestException("Gói không ở trạng thái có thể xác nhận");
        }

        // Confirm tất cả booking con PENDING hoặc DEPOSIT_PAID
        List<Booking> bookings = bookingRepository.findByRecurringBookingId(id);
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.PENDING
                    || booking.getStatus() == BookingStatus.DEPOSIT_PAID) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
            }
        }

        List<BookingResponse> bookingResponses = bookingRepository.findByRecurringBookingId(id)
                .stream().map(BookingResponse::fromEntity).toList();

        return RecurringBookingResponse.fromEntity(rb, bookingResponses);
    }

    // ========== HELPERS ==========

    /**
     * Tính các ngày booking dựa trên recurrence type
     */
    private List<LocalDate> calculateBookingDates(RecurrenceType type, LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();

        if (type == RecurrenceType.WEEKLY) {
            // Lặp mỗi tuần vào cùng thứ
            DayOfWeek targetDay = startDate.getDayOfWeek();
            LocalDate current = startDate;

            while (!current.isAfter(endDate)) {
                dates.add(current);
                current = current.plusWeeks(1);
            }
        } else if (type == RecurrenceType.MONTHLY) {
            // Lặp mỗi tháng vào cùng ngày
            int targetDayOfMonth = startDate.getDayOfMonth();
            LocalDate current = startDate;

            while (!current.isAfter(endDate)) {
                dates.add(current);
                // Chuyển sang tháng tiếp theo, xử lý trường hợp tháng không có ngày đó
                current = current.plusMonths(1);
                if (current.getDayOfMonth() != targetDayOfMonth) {
                    // Tháng tiếp theo không có ngày đó (VD: ngày 31)
                    current = current.withDayOfMonth(
                            Math.min(targetDayOfMonth, current.lengthOfMonth()));
                }
            }
        }

        return dates;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }

    private String generateRecurringCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("RC%s%s", dateStr, uuid);
    }

    private String generateBookingCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("BK%s%s", dateStr, uuid);
    }
}
