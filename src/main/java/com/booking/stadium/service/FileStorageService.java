package com.booking.stadium.service;

import com.booking.stadium.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
            log.info("Upload directory created at: {}", this.uploadPath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    /**
     * Lưu file vào thư mục uploads/stadiums/{stadiumId}/
     * @param file File upload
     * @param stadiumId ID của sân (null nếu tạo mới)
     * @return Đường dẫn tương đối để lưu vào DB
     */
    public String saveStadiumImage(MultipartFile file, Long stadiumId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Chỉ chấp nhận file ảnh");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Kích thước file không được vượt quá 5MB");
        }

        try {
            // Clean filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Create directory structure: uploads/stadiums/{stadiumId}/
            String stadiumFolder = stadiumId != null ? stadiumId.toString() : "temp";
            Path stadiumPath = this.uploadPath.resolve("stadiums").resolve(stadiumFolder);
            Files.createDirectories(stadiumPath);

            // Save file
            Path targetLocation = stadiumPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path
            String relativePath = "stadiums/" + stadiumFolder + "/" + newFilename;
            log.info("File saved successfully: {}", relativePath);
            return relativePath;

        } catch (IOException ex) {
            log.error("Failed to store file", ex);
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    /**
     * Di chuyển ảnh từ thư mục temp sang thư mục stadium thực tế
     */
    public String moveFromTempToStadium(String tempPath, Long stadiumId) {
        if (tempPath == null || !tempPath.startsWith("stadiums/temp/")) {
            return tempPath; // Không phải temp file
        }

        try {
            Path sourcePath = this.uploadPath.resolve(tempPath);
            if (!Files.exists(sourcePath)) {
                log.warn("Source file not found: {}", tempPath);
                return tempPath;
            }

            String filename = sourcePath.getFileName().toString();
            Path targetDir = this.uploadPath.resolve("stadiums").resolve(stadiumId.toString());
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(filename);
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            String newPath = "stadiums/" + stadiumId + "/" + filename;
            log.info("File moved from {} to {}", tempPath, newPath);
            return newPath;

        } catch (IOException ex) {
            log.error("Failed to move file from temp", ex);
            return tempPath; // Return original path if move fails
        }
    }

    /**
     * Xóa file
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }

        try {
            Path path = this.uploadPath.resolve(filePath);
            Files.deleteIfExists(path);
            log.info("File deleted: {}", filePath);
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", filePath, ex);
        }
    }

    /**
     * Xóa tất cả ảnh của stadium
     */
    public void deleteStadiumImages(Long stadiumId) {
        try {
            Path stadiumDir = this.uploadPath.resolve("stadiums").resolve(stadiumId.toString());
            if (Files.exists(stadiumDir)) {
                Files.walk(stadiumDir)
                        .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.error("Failed to delete: {}", path, e);
                            }
                        });
                log.info("Deleted all images for stadium: {}", stadiumId);
            }
        } catch (IOException ex) {
            log.error("Failed to delete stadium images", ex);
        }
    }
}
