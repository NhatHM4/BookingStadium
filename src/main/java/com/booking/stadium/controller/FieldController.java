package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.dto.field.FieldRequest;
import com.booking.stadium.dto.field.FieldResponse;
import com.booking.stadium.service.FieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Field", description = "APIs quản lý sân con")
public class FieldController {

    private final FieldService fieldService;

    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @GetMapping("/stadiums/{stadiumId}/fields")
    @Operation(summary = "Danh sách sân con của stadium")
    public ResponseEntity<ApiResponse<List<FieldResponse>>> getFields(@PathVariable Long stadiumId) {
        return ResponseEntity.ok(ApiResponse.success(fieldService.getFieldsByStadium(stadiumId)));
    }

    @PostMapping("/stadiums/{stadiumId}/fields")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Tạo sân con (Owner)")
    public ResponseEntity<ApiResponse<FieldResponse>> createField(
            @PathVariable Long stadiumId,
            @Valid @RequestBody FieldRequest request) {
        FieldResponse response = fieldService.createField(stadiumId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo sân con thành công", response));
    }

    @PutMapping("/fields/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Cập nhật sân con (Owner)")
    public ResponseEntity<ApiResponse<FieldResponse>> updateField(
            @PathVariable Long id,
            @Valid @RequestBody FieldRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", fieldService.updateField(id, request)));
    }

    @DeleteMapping("/fields/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Xóa sân con (Owner)")
    public ResponseEntity<ApiResponse<Void>> deleteField(@PathVariable Long id) {
        fieldService.deleteField(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sân con thành công"));
    }
}
