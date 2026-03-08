package com.booking.stadium.service;

import com.booking.stadium.dto.booking.AvailableSlotResponse;
import com.booking.stadium.dto.booking.BookingRequest;
import com.booking.stadium.dto.booking.BookingResponse;
import com.booking.stadium.entity.*;
import com.booking.stadium.enums.BookingStatus;
import com.booking.stadium.enums.DepositStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FieldRepository fieldRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final DepositPolicyRepository depositPolicyRepository;

    public BookingService(BookingRepository bookingRepository,
                          FieldRepository fieldRepository,
                          TimeSlotRepository timeSlotRepository,
                          UserRepository userRepository,
                          DepositPolicyRepository depositPolicyRepository) {
        this.bookingRepository = bookingRepository;
        this.fieldRepository = fieldRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.userRepository = userRepository;
        this.depositPolicyRepository = depositPolicyRepository;
    }

    // ========== PUBLIC ==========

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getAvailableSlots(Long fieldId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Không thể xem slot của ngày trong quá khứ");
        }

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", fieldId));

        List<TimeSlot> allSlots = timeSlotRepository.findByFieldIdOrderByStartTimeAsc(fieldId);

        // Lấy các booking đang active cho ngày đó
        List<Booking> existingBookings = bookingRepository.findByFieldIdAndBookingDate(fieldId, date);
        Set<Long> bookedSlotIds = existingBookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .map(b -> b.getTimeSlot().getId())
                .collect(Collectors.toSet());

        // Check grouped fields (parent và children)
        Set<Long> groupedBookedSlotIds = getGroupedFieldBookedSlots(field, date);
        bookedSlotIds.addAll(groupedBookedSlotIds);

        return allSlots.stream()
                .filter(TimeSlot::getIsActive)
                .map(slot -> AvailableSlotResponse.builder()
                        .timeSlotId(slot.getId())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .price(slot.getPrice())
                        .isAvailable(!bookedSlotIds.contains(slot.getId()))
                        .build())
                .toList();
    }

    // ========== CUSTOMER ==========

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
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

        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Không thể đặt sân cho ngày trong quá khứ");
        }

        // Check trùng lịch
        List<Booking> existing = bookingRepository.findActiveBookings(
                field.getId(), timeSlot.getId(), request.getBookingDate());
        if (!existing.isEmpty()) {
            throw new BadRequestException("Khung giờ này đã được đặt cho ngày " + request.getBookingDate());
        }

        // Check conflict với grouped fields (parent-child relationship)
        checkGroupedFieldConflicts(field, timeSlot.getId(), request.getBookingDate());

        // Tính giá và cọc
        BigDecimal totalPrice = timeSlot.getPrice();
        BigDecimal depositAmount = BigDecimal.ZERO;
        BigDecimal remainingAmount = totalPrice;
        DepositStatus depositStatus = DepositStatus.NOT_REQUIRED;

        DepositPolicy policy = depositPolicyRepository.findByStadiumId(field.getStadium().getId())
                .orElse(null);

        if (policy != null && policy.getIsDepositRequired()) {
            depositAmount = totalPrice.multiply(policy.getDepositPercent())
                    .divide(new BigDecimal("100"), 0, RoundingMode.CEILING);
            remainingAmount = totalPrice.subtract(depositAmount);
            depositStatus = DepositStatus.PENDING;
        }

        // Generate booking code: BK + yyyyMMdd + 3 digits
        String bookingCode = generateBookingCode();

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .customer(customer)
                .field(field)
                .timeSlot(timeSlot)
                .bookingDate(request.getBookingDate())
                .isMatchRequest(request.getIsMatchRequest() != null ? request.getIsMatchRequest() : false)
                .totalPrice(totalPrice)
                .depositAmount(depositAmount)
                .remainingAmount(remainingAmount)
                .depositStatus(depositStatus)
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);
        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(BookingStatus status, Pageable pageable) {
        User customer = getCurrentUser();
        Page<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findByCustomerIdAndStatus(customer.getId(), status, pageable);
        } else {
            bookings = bookingRepository.findByCustomerId(customer.getId(), pageable);
        }
        return bookings.map(BookingResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return BookingResponse.fromEntity(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long id) {
        User customer = getCurrentUser();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedException("Bạn không có quyền hủy booking này");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking đã bị hủy trước đó");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy booking đã hoàn thành");
        }

        // Kiểm tra hủy trước 2 tiếng
        LocalDateTime bookingDateTime = booking.getBookingDate()
                .atTime(booking.getTimeSlot().getStartTime());
        if (LocalDateTime.now().plusHours(2).isAfter(bookingDateTime)) {
            throw new BadRequestException("Chỉ được hủy trước giờ đá ít nhất 2 tiếng");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason("Khách hàng hủy");

        booking = bookingRepository.save(booking);
        return BookingResponse.fromEntity(booking);
    }

    // ========== OWNER ==========

    @Transactional(readOnly = true)
    public Page<BookingResponse> getOwnerBookings(Pageable pageable) {
        User owner = getCurrentUser();
        return bookingRepository.findByStadiumOwnerId(owner.getId(), pageable)
                .map(BookingResponse::fromEntity);
    }

    @Transactional
    public BookingResponse confirmBooking(Long id) {
        Booking booking = getOwnerBooking(id);

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.DEPOSIT_PAID) {
            throw new BadRequestException("Booking không ở trạng thái có thể xác nhận");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        return BookingResponse.fromEntity(booking);
    }

    @Transactional
    public BookingResponse rejectBooking(Long id) {
        Booking booking = getOwnerBooking(id);

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Không thể từ chối booking này");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason("Chủ sân từ chối");
        booking = bookingRepository.save(booking);
        return BookingResponse.fromEntity(booking);
    }

    @Transactional
    public BookingResponse completeBooking(Long id) {
        Booking booking = getOwnerBooking(id);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Chỉ có thể hoàn thành booking đã xác nhận");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);
        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByStadiumAndDate(Long stadiumId, LocalDate date) {
        return bookingRepository.findByStadiumIdAndDate(stadiumId, date)
                .stream().map(BookingResponse::fromEntity).toList();
    }

    // ========== HELPERS ==========

    private Booking getOwnerBooking(Long bookingId) {
        User owner = getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        if (!booking.getField().getStadium().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý booking này");
        }
        return booking;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }

    private String generateBookingCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("BK%s%s", dateStr, uuid);
    }

    /**
     * Check conflict với grouped fields (parent-child)
     * VD: Nếu đặt sân 5 (child), check xem sân 7 (parent) có booking không
     * VD: Nếu đặt sân 7 (parent), check xem các sân 5 (children) có booking không
     */
    private void checkGroupedFieldConflicts(Field field, Long timeSlotId, LocalDate date) {
        // Check parent field
        if (field.getParentField() != null) {
            Field parentField = field.getParentField();
            List<Booking> parentBookings = bookingRepository.findByFieldIdAndBookingDate(
                    parentField.getId(), date);
            boolean hasParentConflict = parentBookings.stream()
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .anyMatch(b -> b.getTimeSlot().getId().equals(timeSlotId));
            
            if (hasParentConflict) {
                throw new BadRequestException(
                    "Không thể đặt sân này vì sân ghép lớn hơn (" + parentField.getName() + 
                    ") đã được đặt trong khung giờ này");
            }
        }

        // Check child fields
        if (field.getChildFields() != null && !field.getChildFields().isEmpty()) {
            for (Field childField : field.getChildFields()) {
                List<Booking> childBookings = bookingRepository.findByFieldIdAndBookingDate(
                        childField.getId(), date);
                boolean hasChildConflict = childBookings.stream()
                        .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                        .anyMatch(b -> b.getTimeSlot().getId().equals(timeSlotId));
                
                if (hasChildConflict) {
                    throw new BadRequestException(
                        "Không thể đặt sân ghép này vì sân con (" + childField.getName() + 
                        ") đã được đặt trong khung giờ này");
                }
            }
        }
    }

    /**
     * Lấy danh sách slot IDs đã được book ở grouped fields (parent + children)
     */
    private Set<Long> getGroupedFieldBookedSlots(Field field, LocalDate date) {
        Set<Long> bookedSlotIds = new HashSet<>();

        // Check parent field bookings
        if (field.getParentField() != null) {
            List<Booking> parentBookings = bookingRepository.findByFieldIdAndBookingDate(
                    field.getParentField().getId(), date);
            Set<Long> parentSlotIds = parentBookings.stream()
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                    .map(b -> b.getTimeSlot().getId())
                    .collect(Collectors.toSet());
            bookedSlotIds.addAll(parentSlotIds);
        }

        // Check child fields bookings
        if (field.getChildFields() != null && !field.getChildFields().isEmpty()) {
            for (Field childField : field.getChildFields()) {
                List<Booking> childBookings = bookingRepository.findByFieldIdAndBookingDate(
                        childField.getId(), date);
                Set<Long> childSlotIds = childBookings.stream()
                        .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                        .map(b -> b.getTimeSlot().getId())
                        .collect(Collectors.toSet());
                bookedSlotIds.addAll(childSlotIds);
            }
        }

        return bookedSlotIds;
    }
}
