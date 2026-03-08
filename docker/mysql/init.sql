-- =============================================
-- Booking Stadium - Database Initialization
-- =============================================

-- Ensure proper charset
ALTER DATABASE booking_stadium CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges
GRANT ALL PRIVILEGES ON booking_stadium.* TO 'booking_user'@'%';
FLUSH PRIVILEGES;
