-- ============================================================
-- SEED DATA - Booking Stadium
-- Chạy tự động khi khởi động ứng dụng
-- Sử dụng INSERT IGNORE để tránh lỗi duplicate khi chạy lại
-- ============================================================

-- ========================
-- MIGRATION: Cho phép guest booking (customer_id nullable)
-- ========================
SET @col_nullable = (SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookings' AND COLUMN_NAME = 'customer_id');
SET @alter_sql = IF(@col_nullable = 'NO',
    'ALTER TABLE bookings MODIFY COLUMN customer_id BIGINT NULL',
    'SELECT 1');
PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ========================
-- 1. USERS (password: 123456 - bcrypt encoded)
-- ========================
INSERT IGNORE INTO users (id, email, password, full_name, phone, role, auth_provider, is_active, created_at, updated_at)
VALUES
    (1, 'customer@example.com', '$2a$10$jSIDF.3mUiOiDweSSt92C.X/YAvzLTCV73Ve1x2x8C7OC.vvTVA/m', 'Nguyễn Văn Khách', '0901234567', 'CUSTOMER', 'LOCAL', true, NOW(), NOW()),
    (2, 'owner@example.com', '$2a$10$jSIDF.3mUiOiDweSSt92C.X/YAvzLTCV73Ve1x2x8C7OC.vvTVA/m', 'Trần Thị Chủ Sân', '0912345678', 'OWNER', 'LOCAL', true, NOW(), NOW()),
    (3, 'admin@example.com', '$2a$10$jSIDF.3mUiOiDweSSt92C.X/YAvzLTCV73Ve1x2x8C7OC.vvTVA/m', 'Admin System', '0909090909', 'ADMIN', 'LOCAL', true, NOW(), NOW()),
    (4, 'customer2@example.com', '$2a$10$jSIDF.3mUiOiDweSSt92C.X/YAvzLTCV73Ve1x2x8C7OC.vvTVA/m', 'Lê Minh Khách 2', '0903456789', 'CUSTOMER', 'LOCAL', true, NOW(), NOW()),
    (5, 'owner2@example.com', '$2a$10$jSIDF.3mUiOiDweSSt92C.X/YAvzLTCV73Ve1x2x8C7OC.vvTVA/m', 'Phạm Văn Chủ Sân 2', '0914567890', 'OWNER', 'LOCAL', true, NOW(), NOW());

-- ========================
-- 2. STADIUMS
-- ========================
INSERT IGNORE INTO stadiums (id, owner_id, name, address, district, city, description, latitude, longitude, open_time, close_time, status, created_at, updated_at)
VALUES
    (1, 2, 'Sân Bóng Thống Nhất', '138 Đào Duy Từ, Phường 6', 'Quận 10', 'TP. Hồ Chí Minh',
     'Sân bóng đá mini chất lượng cao, mặt cỏ nhân tạo, hệ thống đèn chiếu sáng hiện đại. Có chỗ đậu xe rộng rãi và canteen phục vụ đồ uống.',
     10.77250000, 106.66820000, '06:00:00', '23:00:00', 'APPROVED', NOW(), NOW()),

    (2, 2, 'Sân Bóng Tân Phú', '300 Lũy Bán Bích, Phường Hòa Thạnh', 'Quận Tân Phú', 'TP. Hồ Chí Minh',
     'Sân bóng đá 7 người tiêu chuẩn, có phòng thay đồ và tắm rửa. Wifi miễn phí.',
     10.79320000, 106.63150000, '05:30:00', '22:30:00', 'APPROVED', NOW(), NOW()),

    (3, 5, 'Sân Bóng Phú Nhuận', '56 Hoàng Minh Giám, Phường 9', 'Quận Phú Nhuận', 'TP. Hồ Chí Minh',
     'Sân bóng đá 5 và 7 người, cỏ nhân tạo thế hệ mới. Giá rẻ, vị trí thuận tiện gần trung tâm.',
     10.79880000, 106.68120000, '06:00:00', '22:00:00', 'APPROVED', NOW(), NOW()),

    (4, 5, 'Sân Bóng Gò Vấp Sport', '120 Nguyễn Oanh, Phường 17', 'Quận Gò Vấp', 'TP. Hồ Chí Minh',
     'Khu liên hợp thể thao với sân bóng 5, 7 và 11 người. Có khu giải khát và xem trực tiếp bóng đá.',
     10.83450000, 106.67230000, '05:00:00', '23:30:00', 'PENDING', NOW(), NOW());

-- ========================
-- 3. FIELDS (sân con)
-- parent_field_id: Sân con có thể ghép thành sân lớn
-- VD: Sân A1 + A2 (5 người) ghép thành Sân B1 (7 người)
-- NOTE: Insert parent fields first, then child fields
-- ========================

-- Insert parent fields first (no parent_field_id)
INSERT IGNORE INTO fields (id, stadium_id, name, field_type, default_price, parent_field_id, is_active, created_at, updated_at)
VALUES
    (3, 1, 'Sân B1 - 7 người (Ghép)', 'SEVEN_A_SIDE', 500000.00, NULL, true, NOW(), NOW()),
    (4, 2, 'Sân 7A', 'SEVEN_A_SIDE', 450000.00, NULL, true, NOW(), NOW()),
    (5, 2, 'Sân 7B', 'SEVEN_A_SIDE', 450000.00, NULL, true, NOW(), NOW()),
    (6, 3, 'Sân Mini 1', 'FIVE_A_SIDE', 250000.00, NULL, true, NOW(), NOW()),
    (7, 3, 'Sân 7 người', 'SEVEN_A_SIDE', 400000.00, NULL, true, NOW(), NOW()),
    (10, 4, 'Sân 11 (Ghép)', 'ELEVEN_A_SIDE', 1200000.00, NULL, true, NOW(), NOW()),
    (9, 4, 'Sân 7A', 'SEVEN_A_SIDE', 480000.00, NULL, true, NOW(), NOW());

-- Insert child fields with parent_field_id
INSERT IGNORE INTO fields (id, stadium_id, name, field_type, default_price, parent_field_id, is_active, created_at, updated_at)
VALUES
    (1, 1, 'Sân A1 - 5 người', 'FIVE_A_SIDE', 300000.00, 3, true, NOW(), NOW()),
    (2, 1, 'Sân A2 - 5 người', 'FIVE_A_SIDE', 300000.00, 3, true, NOW(), NOW()),
    (8, 4, 'Sân 5A', 'FIVE_A_SIDE', 280000.00, 10, true, NOW(), NOW()),
    (11, 4, 'Sân 5B', 'FIVE_A_SIDE', 280000.00, 10, true, NOW(), NOW()),
    (12, 4, 'Sân 5C', 'FIVE_A_SIDE', 280000.00, 10, true, NOW(), NOW());

-- ========================
-- 4. TIME SLOTS (khung giờ)
-- ========================
INSERT IGNORE INTO time_slots (id, field_id, start_time, end_time, price, is_active, created_at, updated_at)
VALUES
    -- Sân A1 (field_id=1) - Sân 5 người Thống Nhất (có parent)
    (1,  1, '06:00:00', '07:30:00', 250000.00, true, NOW(), NOW()),
    (2,  1, '07:30:00', '09:00:00', 250000.00, true, NOW(), NOW()),
    (3,  1, '09:00:00', '10:30:00', 200000.00, true, NOW(), NOW()),
    (4,  1, '15:00:00', '16:30:00', 300000.00, true, NOW(), NOW()),
    (5,  1, '16:30:00', '18:00:00', 350000.00, true, NOW(), NOW()),
    (6,  1, '18:00:00', '19:30:00', 400000.00, true, NOW(), NOW()),
    (7,  1, '19:30:00', '21:00:00', 400000.00, true, NOW(), NOW()),
    (8,  1, '21:00:00', '22:30:00', 350000.00, true, NOW(), NOW()),

    -- Sân A2 (field_id=2) - Sân 5 người Thống Nhất (có parent)
    (9,  2, '06:00:00', '07:30:00', 250000.00, true, NOW(), NOW()),
    (10, 2, '07:30:00', '09:00:00', 250000.00, true, NOW(), NOW()),
    (11, 2, '16:30:00', '18:00:00', 350000.00, true, NOW(), NOW()),
    (12, 2, '18:00:00', '19:30:00', 400000.00, true, NOW(), NOW()),
    (13, 2, '19:30:00', '21:00:00', 400000.00, true, NOW(), NOW()),

    -- Sân B1 (field_id=3) - Sân 7 người Thống Nhất (là parent của A1, A2)
    (14, 3, '06:00:00', '07:30:00', 400000.00, true, NOW(), NOW()),
    (15, 3, '07:30:00', '09:00:00', 400000.00, true, NOW(), NOW()),
    (16, 3, '17:00:00', '18:30:00', 550000.00, true, NOW(), NOW()),
    (17, 3, '18:30:00', '20:00:00', 600000.00, true, NOW(), NOW()),
    (18, 3, '20:00:00', '21:30:00', 600000.00, true, NOW(), NOW()),

    -- Sân 7A - Tân Phú (field_id=4)
    (19, 4, '06:00:00', '07:30:00', 350000.00, true, NOW(), NOW()),
    (20, 4, '07:30:00', '09:00:00', 350000.00, true, NOW(), NOW()),
    (21, 4, '17:00:00', '18:30:00', 500000.00, true, NOW(), NOW()),
    (22, 4, '18:30:00', '20:00:00', 550000.00, true, NOW(), NOW()),
    (23, 4, '20:00:00', '21:30:00', 500000.00, true, NOW(), NOW()),

    -- Sân 7B - Tân Phú (field_id=5)
    (24, 5, '06:00:00', '07:30:00', 350000.00, true, NOW(), NOW()),
    (25, 5, '18:00:00', '19:30:00', 550000.00, true, NOW(), NOW()),
    (26, 5, '19:30:00', '21:00:00', 550000.00, true, NOW(), NOW()),

    -- Sân Mini 1 - Phú Nhuận (field_id=6)
    (27, 6, '06:00:00', '07:30:00', 200000.00, true, NOW(), NOW()),
    (28, 6, '18:00:00', '19:30:00', 300000.00, true, NOW(), NOW()),
    (29, 6, '19:30:00', '21:00:00', 300000.00, true, NOW(), NOW()),

    -- Sân 7 - Phú Nhuận (field_id=7)
    (30, 7, '06:00:00', '07:30:00', 350000.00, true, NOW(), NOW()),
    (31, 7, '18:00:00', '19:30:00', 500000.00, true, NOW(), NOW()),
    (32, 7, '19:30:00', '21:00:00', 500000.00, true, NOW(), NOW()),

    -- Sân 5A - Gò Vấp (field_id=8, có parent là sân 11)
    (33, 8, '06:00:00', '07:30:00', 250000.00, true, NOW(), NOW()),
    (34, 8, '18:00:00', '19:30:00', 350000.00, true, NOW(), NOW()),
    (35, 8, '19:30:00', '21:00:00', 350000.00, true, NOW(), NOW()),

    -- Sân 7A - Gò Vấp (field_id=9)
    (36, 9, '06:00:00', '07:30:00', 400000.00, true, NOW(), NOW()),
    (37, 9, '18:00:00', '19:30:00', 600000.00, true, NOW(), NOW()),
    (38, 9, '19:30:00', '21:00:00', 600000.00, true, NOW(), NOW()),

    -- Sân 11 - Gò Vấp (field_id=10, là parent của 8, 11, 12)
    (39, 10, '06:00:00', '08:00:00', 1000000.00, true, NOW(), NOW()),
    (40, 10, '08:00:00', '10:00:00', 1000000.00, true, NOW(), NOW()),
    (41, 10, '18:00:00', '20:00:00', 1500000.00, true, NOW(), NOW()),
    (42, 10, '20:00:00', '22:00:00', 1500000.00, true, NOW(), NOW()),

    -- Sân 5B - Gò Vấp (field_id=11, có parent là sân 11)
    (43, 11, '06:00:00', '07:30:00', 250000.00, true, NOW(), NOW()),
    (44, 11, '18:00:00', '19:30:00', 350000.00, true, NOW(), NOW()),
    (45, 11, '19:30:00', '21:00:00', 350000.00, true, NOW(), NOW()),

    -- Sân 5C - Gò Vấp (field_id=12, có parent là sân 11)
    (46, 12, '06:00:00', '07:30:00', 250000.00, true, NOW(), NOW()),
    (47, 12, '18:00:00', '19:30:00', 350000.00, true, NOW(), NOW()),
    (48, 12, '19:30:00', '21:00:00', 350000.00, true, NOW(), NOW());

-- ========================
-- 5. DEPOSIT POLICIES (chính sách đặt cọc)
-- ========================
INSERT IGNORE INTO deposit_policies (id, stadium_id, deposit_percent, refund_before_hours, refund_percent, late_cancel_refund_percent, recurring_discount_percent, min_recurring_sessions, is_deposit_required, created_at, updated_at)
VALUES
    (1, 1, 30.00, 24, 100.00, 0.00, 10.00, 4, true, NOW(), NOW()),
    (2, 2, 50.00, 12, 80.00, 0.00, 15.00, 8, true, NOW(), NOW()),
    (3, 3, 30.00, 24, 100.00, 50.00, 5.00, 4, false, NOW(), NOW());
