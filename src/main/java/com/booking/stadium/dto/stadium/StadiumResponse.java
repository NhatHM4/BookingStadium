package com.booking.stadium.dto.stadium;

import com.booking.stadium.entity.Stadium;
import com.booking.stadium.enums.StadiumStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StadiumResponse {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private String name;
    private String address;
    private String district;
    private String city;
    private String description;
    private String imageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalTime openTime;
    private LocalTime closeTime;
    private StadiumStatus status;
    private Double avgRating;
    private Long reviewCount;
    private Integer fieldCount;

    public static StadiumResponse fromEntity(Stadium stadium) {
        return StadiumResponse.builder()
                .id(stadium.getId())
                .ownerId(stadium.getOwner().getId())
                .ownerName(stadium.getOwner().getFullName())
                .name(stadium.getName())
                .address(stadium.getAddress())
                .district(stadium.getDistrict())
                .city(stadium.getCity())
                .description(stadium.getDescription())
                .imageUrl(stadium.getImageUrl())
                .latitude(stadium.getLatitude())
                .longitude(stadium.getLongitude())
                .openTime(stadium.getOpenTime())
                .closeTime(stadium.getCloseTime())
                .status(stadium.getStatus())
                .fieldCount(stadium.getFields() != null ? stadium.getFields().size() : 0)
                .build();
    }
}
