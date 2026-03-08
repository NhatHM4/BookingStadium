package com.booking.stadium.dto.team;

import com.booking.stadium.enums.FieldType;
import com.booking.stadium.enums.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {

    @NotBlank(message = "Tên đội không được để trống")
    @Size(max = 100, message = "Tên đội tối đa 100 ký tự")
    private String name;

    private String logoUrl;

    private String description;

    private FieldType preferredFieldType;

    private SkillLevel skillLevel;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String district;
}
