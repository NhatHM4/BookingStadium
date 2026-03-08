package com.booking.stadium.controller;

import com.booking.stadium.dto.ApiResponse;
import com.booking.stadium.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "Image", description = "APIs quản lý upload ảnh")
public class ImageController {

    private final FileStorageService fileStorageService;

    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Upload ảnh sân (Owner)")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadStadiumImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "stadiumId", required = false) Long stadiumId) {
        
        String imagePath = fileStorageService.saveStadiumImage(file, stadiumId);
        String imageUrl = "/uploads/" + imagePath;
        
        ImageUploadResponse response = new ImageUploadResponse(imagePath, imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Upload ảnh thành công", response));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUploadResponse {
        private String path;      // Đường dẫn lưu trong DB
        private String url;       // URL để truy cập từ client
    }
}
