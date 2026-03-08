package com.booking.stadium.service;

import com.booking.stadium.dto.field.FieldRequest;
import com.booking.stadium.dto.field.FieldResponse;
import com.booking.stadium.entity.Field;
import com.booking.stadium.entity.Stadium;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.exception.UnauthorizedException;
import com.booking.stadium.repository.FieldRepository;
import com.booking.stadium.repository.StadiumRepository;
import com.booking.stadium.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FieldService {

    private final FieldRepository fieldRepository;
    private final StadiumRepository stadiumRepository;
    private final UserRepository userRepository;

    public FieldService(FieldRepository fieldRepository,
                        StadiumRepository stadiumRepository,
                        UserRepository userRepository) {
        this.fieldRepository = fieldRepository;
        this.stadiumRepository = stadiumRepository;
        this.userRepository = userRepository;
    }

    // ========== PUBLIC ==========

    @Transactional(readOnly = true)
    public List<FieldResponse> getFieldsByStadium(Long stadiumId) {
        return fieldRepository.findByStadiumIdAndIsActiveTrue(stadiumId)
                .stream().map(FieldResponse::fromEntity).toList();
    }

    // ========== OWNER ==========

    @Transactional
    public FieldResponse createField(Long stadiumId, FieldRequest request) {
        Stadium stadium = getOwnedStadium(stadiumId);

        Field.FieldBuilder builder = Field.builder()
                .stadium(stadium)
                .name(request.getName())
                .fieldType(request.getFieldType())
                .defaultPrice(request.getDefaultPrice())
                .isActive(true);

        // Set parent field if provided
        if (request.getParentFieldId() != null) {
            Field parentField = fieldRepository.findById(request.getParentFieldId())
                    .orElseThrow(() -> new ResourceNotFoundException("Field", "id", request.getParentFieldId()));
            // Verify parent field belongs to same stadium
            if (!parentField.getStadium().getId().equals(stadiumId)) {
                throw new UnauthorizedException("Sân cha phải thuộc cùng stadium");
            }
            builder.parentField(parentField);
        }

        Field field = builder.build();
        field = fieldRepository.save(field);
        return FieldResponse.fromEntity(field);
    }

    @Transactional
    public FieldResponse updateField(Long id, FieldRequest request) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", id));
        verifyOwnership(field.getStadium());

        field.setName(request.getName());
        field.setFieldType(request.getFieldType());
        field.setDefaultPrice(request.getDefaultPrice());

        // Update parent field if provided
        if (request.getParentFieldId() != null) {
            Field parentField = fieldRepository.findById(request.getParentFieldId())
                    .orElseThrow(() -> new ResourceNotFoundException("Field", "id", request.getParentFieldId()));
            if (!parentField.getStadium().getId().equals(field.getStadium().getId())) {
                throw new UnauthorizedException("Sân cha phải thuộc cùng stadium");
            }
            field.setParentField(parentField);
        } else {
            field.setParentField(null);
        }

        field = fieldRepository.save(field);
        return FieldResponse.fromEntity(field);
    }

    @Transactional
    public void deleteField(Long id) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", id));
        verifyOwnership(field.getStadium());

        field.setIsActive(false);
        fieldRepository.save(field);
    }

    // ========== HELPERS ==========

    private Stadium getOwnedStadium(Long stadiumId) {
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Stadium", "id", stadiumId));
        verifyOwnership(stadium);
        return stadium;
    }

    private void verifyOwnership(Stadium stadium) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        if (!stadium.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý sân này");
        }
    }
}
