package com.booking.stadium.dto.field;

import com.booking.stadium.enums.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldRequest {

    @NotBlank(message = "Tên sân con không được để trống")
    private String name;

    @NotNull(message = "Loại sân không được để trống")
    private FieldType fieldType;

    @NotNull(message = "Giá mặc định không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal defaultPrice;

    private Long parentFieldId;  // ID của sân cha (nếu là sân con trong nhóm)
}
