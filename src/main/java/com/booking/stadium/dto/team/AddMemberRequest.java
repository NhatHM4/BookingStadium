package com.booking.stadium.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    @NotBlank(message = "Tên thành viên không được để trống")
    @Size(max = 100, message = "Tên thành viên tối đa 100 ký tự")
    private String name;

    @Size(max = 20, message = "SĐT thành viên tối đa 20 ký tự")
    private String phone;
}
