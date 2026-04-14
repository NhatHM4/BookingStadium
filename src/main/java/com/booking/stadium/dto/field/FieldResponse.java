package com.booking.stadium.dto.field;

import com.booking.stadium.entity.Field;
import com.booking.stadium.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldResponse {

    private Long id;
    private Long stadiumId;
    private String stadiumName;
    private String name;
    private String imageUrl;
    private FieldType fieldType;
    private BigDecimal defaultPrice;
    private Boolean isActive;
    private Long parentFieldId;  // ID của sân cha (nếu có)
    private Integer childFieldCount;  // Số lượng sân con (nếu là sân cha)

    public static FieldResponse fromEntity(Field field) {
        return FieldResponse.builder()
                .id(field.getId())
                .stadiumId(field.getStadium().getId())
                .stadiumName(field.getStadium().getName())
                .name(field.getName())
                .imageUrl(field.getImageUrl())
                .fieldType(field.getFieldType())
                .defaultPrice(field.getDefaultPrice())
                .isActive(field.getIsActive())
                .parentFieldId(field.getParentField() != null ? field.getParentField().getId() : null)
                .childFieldCount(field.getChildFields() != null ? field.getChildFields().size() : 0)
                .build();
    }
}
