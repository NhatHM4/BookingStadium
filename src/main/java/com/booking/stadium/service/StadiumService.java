package com.booking.stadium.service;

import com.booking.stadium.dto.stadium.StadiumRequest;
import com.booking.stadium.dto.stadium.StadiumResponse;
import com.booking.stadium.entity.Stadium;
import com.booking.stadium.entity.User;
import com.booking.stadium.enums.FieldType;
import com.booking.stadium.enums.StadiumStatus;
import com.booking.stadium.exception.BadRequestException;
import com.booking.stadium.exception.ResourceNotFoundException;
import com.booking.stadium.exception.UnauthorizedException;
import com.booking.stadium.repository.ReviewRepository;
import com.booking.stadium.repository.StadiumRepository;
import com.booking.stadium.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StadiumService {

    private final StadiumRepository stadiumRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;

    public StadiumService(StadiumRepository stadiumRepository,
                          UserRepository userRepository,
                          ReviewRepository reviewRepository,
                          FileStorageService fileStorageService) {
        this.stadiumRepository = stadiumRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.fileStorageService = fileStorageService;
    }

    // ========== PUBLIC ==========

    @Transactional(readOnly = true)
    public Page<StadiumResponse> getApprovedStadiums(String city, String district, String name, FieldType fieldType, Pageable pageable) {
        Page<Stadium> stadiums;
        if (fieldType != null) {
            stadiums = stadiumRepository.searchStadiumsWithFieldType(
                    StadiumStatus.APPROVED, city, district, name, fieldType, pageable);
        } else {
            stadiums = stadiumRepository.searchStadiums(
                    StadiumStatus.APPROVED, city, district, name, pageable);
        }
        return stadiums.map(this::toResponseWithRating);
    }

    @Transactional(readOnly = true)
    public StadiumResponse getStadiumById(Long id) {
        Stadium stadium = stadiumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stadium", "id", id));
        return toResponseWithRating(stadium);
    }

    @Transactional(readOnly = true)
    public List<StadiumResponse> getNearbyStadiums(Double lat, Double lng, Double radius) {
        return stadiumRepository.findNearbyStadiums(lat, lng, radius)
                .stream().map(this::toResponseWithRating).toList();
    }

    // ========== OWNER ==========

    @Transactional
    public StadiumResponse createStadium(StadiumRequest request) {
        User owner = getCurrentUser();

        Stadium stadium = Stadium.builder()
                .owner(owner)
                .name(request.getName())
                .address(request.getAddress())
                .district(request.getDistrict())
                .city(request.getCity())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .status(StadiumStatus.PENDING)
                .build();

        stadium = stadiumRepository.save(stadium);
        
        // Move image from temp to stadium folder if exists
        if (stadium.getImageUrl() != null && stadium.getImageUrl().startsWith("stadiums/temp/")) {
            String newImagePath = fileStorageService.moveFromTempToStadium(stadium.getImageUrl(), stadium.getId());
            stadium.setImageUrl(newImagePath);
            stadium = stadiumRepository.save(stadium);
        }
        
        return StadiumResponse.fromEntity(stadium);
    }

    @Transactional
    public StadiumResponse updateStadium(Long id, StadiumRequest request) {
        Stadium stadium = getOwnedStadium(id);

        // Delete old image if changing to new one
        String oldImageUrl = stadium.getImageUrl();
        if (request.getImageUrl() != null && !request.getImageUrl().equals(oldImageUrl)) {
            fileStorageService.deleteFile(oldImageUrl);
        }

        stadium.setName(request.getName());
        stadium.setAddress(request.getAddress());
        stadium.setDistrict(request.getDistrict());
        stadium.setCity(request.getCity());
        stadium.setDescription(request.getDescription());
        stadium.setImageUrl(request.getImageUrl());
        stadium.setLatitude(request.getLatitude());
        stadium.setLongitude(request.getLongitude());
        stadium.setOpenTime(request.getOpenTime());
        stadium.setCloseTime(request.getCloseTime());

        stadium = stadiumRepository.save(stadium);
        return toResponseWithRating(stadium);
    }

    @Transactional
    public void deleteStadium(Long id) {
        Stadium stadium = getOwnedStadium(id);
        stadium.setStatus(StadiumStatus.INACTIVE);
        stadiumRepository.save(stadium);
        // Delete all images for this stadium
        fileStorageService.deleteStadiumImages(id);
    }

    @Transactional(readOnly = true)
    public List<StadiumResponse> getMyStadiums() {
        User owner = getCurrentUser();
        return stadiumRepository.findByOwnerId(owner.getId())
                .stream().map(this::toResponseWithRating).toList();
    }

    // ========== ADMIN ==========

    @Transactional(readOnly = true)
    public Page<StadiumResponse> getPendingStadiums(Pageable pageable) {
        return stadiumRepository.findByStatus(StadiumStatus.PENDING, pageable)
                .map(StadiumResponse::fromEntity);
    }

    @Transactional
    public StadiumResponse approveStadium(Long id) {
        Stadium stadium = stadiumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stadium", "id", id));
        if (stadium.getStatus() != StadiumStatus.PENDING) {
            throw new BadRequestException("Sân không ở trạng thái chờ duyệt");
        }
        stadium.setStatus(StadiumStatus.APPROVED);
        stadium = stadiumRepository.save(stadium);
        return StadiumResponse.fromEntity(stadium);
    }

    @Transactional
    public StadiumResponse rejectStadium(Long id) {
        Stadium stadium = stadiumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stadium", "id", id));
        if (stadium.getStatus() != StadiumStatus.PENDING) {
            throw new BadRequestException("Sân không ở trạng thái chờ duyệt");
        }
        stadium.setStatus(StadiumStatus.REJECTED);
        stadium = stadiumRepository.save(stadium);
        return StadiumResponse.fromEntity(stadium);
    }

    // ========== HELPERS ==========

    private Stadium getOwnedStadium(Long stadiumId) {
        User owner = getCurrentUser();
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Stadium", "id", stadiumId));
        if (!stadium.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Bạn không có quyền quản lý sân này");
        }
        return stadium;
    }

    public Stadium getOwnedStadiumEntity(Long stadiumId) {
        return getOwnedStadium(stadiumId);
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
    }

    private StadiumResponse toResponseWithRating(Stadium stadium) {
        StadiumResponse response = StadiumResponse.fromEntity(stadium);
        response.setAvgRating(reviewRepository.getAverageRatingByStadiumId(stadium.getId()));
        response.setReviewCount(reviewRepository.countByStadiumId(stadium.getId()));
        return response;
    }
}
