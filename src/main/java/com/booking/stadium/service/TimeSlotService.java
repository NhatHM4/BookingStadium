package com.booking.stadium.service;

import com.booking.stadium.dto.timeslot.TimeSlotRequest;
import com.booking.stadium.dto.timeslot.TimeSlotResponse;
import com.booking.stadium.entity.Field;
import com.booking.stadium.entity.TimeSlot;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.exception.UnauthorizedException;
import com.booking.stadium.repository.FieldRepository;
import com.booking.stadium.repository.TimeSlotRepository;
import com.booking.stadium.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;

    public TimeSlotService(TimeSlotRepository timeSlotRepository,
                           FieldRepository fieldRepository,
                           UserRepository userRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
    }

    // ========== PUBLIC ==========

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getTimeSlotsByField(Long fieldId) {
        return timeSlotRepository.findByFieldIdOrderByStartTimeAsc(fieldId)
                .stream()
                .filter(ts -> ts.getIsActive())
                .map(TimeSlotResponse::fromEntity)
                .toList();
    }

    // ========== OWNER ==========

    @Transactional
    public TimeSlotResponse createTimeSlot(Long fieldId, TimeSlotRequest request) {
        Field field = getOwnedField(fieldId);

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Giờ bắt đầu phải trước giờ kết thúc");
        }

        TimeSlot timeSlot = TimeSlot.builder()
                .field(field)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .isActive(true)
                .build();

        timeSlot = timeSlotRepository.save(timeSlot);
        return TimeSlotResponse.fromEntity(timeSlot);
    }

    @Transactional
    public TimeSlotResponse updateTimeSlot(Long id, TimeSlotRequest request) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", id));
        verifyOwnership(timeSlot.getField());

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Giờ bắt đầu phải trước giờ kết thúc");
        }

        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setPrice(request.getPrice());

        timeSlot = timeSlotRepository.save(timeSlot);
        return TimeSlotResponse.fromEntity(timeSlot);
    }

    @Transactional
    public void deleteTimeSlot(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", id));
        verifyOwnership(timeSlot.getField());

        timeSlot.setIsActive(false);
        timeSlotRepository.save(timeSlot);
    }

    // ========== HELPERS ==========

    private Field getOwnedField(Long fieldId) {
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", fieldId));
        verifyOwnership(field);
        return field;
    }

    private void verifyOwnership(Field field) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        if (!field.getStadium().getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý khung giờ này");
        }
    }
}
