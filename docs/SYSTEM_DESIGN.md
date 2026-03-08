# Hệ Thống Đặt Sân Bóng - System Design Document

## 1. Tổng Quan

Hệ thống đặt sân bóng đá online, cho phép người dùng tìm kiếm, xem lịch trống và đặt sân bóng. Chủ sân có thể quản lý sân, khung giờ và theo dõi đơn đặt.

### Tech Stack
| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.x (Java 17+) |
| Database | MySQL 8.x |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA / Hibernate |
| API Doc | Swagger / SpringDoc OpenAPI |
| Build Tool | Maven |
| Cache | Redis (optional - phase 2) |

---

## 2. Actors (Vai trò)

| Actor | Mô tả |
|-------|--------|
| **Guest** | Xem danh sách sân, xem lịch trống |
| **Customer** | Đăng ký, đăng nhập, đặt sân, xem lịch sử, hủy đặt |
| **Stadium Owner** | Quản lý sân, khung giờ, giá, xem đơn đặt |
| **Admin** | Quản lý toàn bộ hệ thống, duyệt sân, quản lý user |

---

## 3. Chức Năng Chính (Features)

### 3.1 Authentication & Authorization
- Đăng ký tài khoản (Customer / Owner)
- Đăng nhập (JWT token)
- Refresh token
- Phân quyền theo role

### 3.2 Quản lý Sân (Stadium Management) - Owner
- CRUD sân bóng (tên, địa chỉ, mô tả, ảnh, số sân con)
- **Upload ảnh sân:** ⭐ MỚI
  - API upload ảnh riêng: `POST /api/v1/images/upload`
  - Lưu trữ server-side: `uploads/stadiums/{stadiumId}/`
  - Serve static resource: `/uploads/**` (public access)
  - Chi tiết: [IMAGE_UPLOAD_GUIDE.md](./IMAGE_UPLOAD_GUIDE.md)
- CRUD sân con (field) - sân 5, sân 7, sân 11
- Quản lý khung giờ và giá cho từng sân con
- Bật/tắt trạng thái sân
- **Cấu hình sân ghép (grouped fields):** ⭐ MỚI
  - Set parent-child relationship giữa các sân con
  - VD: 2 sân 5 → 1 sân 7

### 3.3 Đặt Sân (Booking) - Customer
- Tìm kiếm sân theo khu vực, loại sân, ngày giờ
- Xem lịch trống của sân
- Đặt sân (chọn sân con + khung giờ + ngày)
- Xem lịch sử đặt sân
- Hủy đặt sân (trước X giờ)

### 3.4 Quản lý Đơn Đặt - Owner
- Xem danh sách đơn đặt
- Xác nhận / Từ chối đơn đặt
- Đánh dấu hoàn thành

### 3.5 Đánh Giá (Review) - Customer
- Đánh giá sân sau khi sử dụng (1-5 sao + comment)
- Xem đánh giá

### 3.6 Đặt Cọc (Deposit) - Customer
- Đặt cọc khi đặt sân (Owner cấu hình % cọc cho từng sân)
- Xác nhận đã thanh toán cọc (Owner đánh dấu)
- Hoàn cọc khi hủy đặt (theo chính sách hoàn cọc)
- Thanh toán phần còn lại khi đến sân
- Lịch sử giao dịch cọc

### 3.7 Đặt Sân Dài Hạn (Recurring Booking) - Customer
- Đặt sân lặp lại theo tuần (VD: mỗi thứ 3, 19h-20h, trong 1 tháng)
- Đặt sân lặp lại theo tháng (VD: 4 buổi/tháng trong 3 tháng)
- Xem danh sách các buổi trong gói đặt dài hạn
- Hủy từng buổi hoặc hủy toàn bộ gói
- Ưu đãi giá khi đặt dài hạn (Owner cấu hình % giảm giá)

### 3.8 Quản Lý Đội Bóng (Team) - Customer
- Tạo đội bóng (tên đội, logo, mô tả, loại sân ưa thích)
- Thêm / xóa thành viên đội (mời qua email hoặc link)
- Xem danh sách đội của mình
- Chuyển quyền đội trưởng
- Rời đội / Giải tán đội

### 3.9 Ráp Kèo (Match Making) - Customer
- Khi đặt sân, chọn option "Ráp kèo" → tạo kèo chờ đối thủ
- Đăng kèo: chọn đội của mình, loại sân (5/7), trình độ, ghi chú
- Xem danh sách kèo đang mở (filter theo khu vực, loại sân, ngày giờ, trình độ)
- Nhận kèo: chọn đội của mình để nhận kèo
- Hủy kèo (chủ kèo) / Rút kèo (đội nhận)
- Xem lịch sử các trận đã ráp
- Chi phí chia đôi: mỗi đội trả 50% hoặc theo thỏa thuận (chủ kèo cấu hình)

### 3.10 Admin
- Quản lý users
- Duyệt sân mới đăng ký
- Dashboard thống kê

### 3.11 Sân Ghép (Grouped Fields) - Owner ⭐ MỚI
- Cấu hình nhiều sân nhỏ ghép thành 1 sân lớn (parent-child relationship)
  - VD: 2-3 sân 5 người → 1 sân 7 người hoặc 11 người
- Khi tạo/cập nhật field, chọn `parentFieldId` để set sân cha
- **Business Logic tự động:**
  - Booking sân con → Sân cha không thể đặt cùng khung giờ
  - Booking sân cha → Tất cả sân con không thể đặt cùng khung giờ
  - API available slots tự động filter theo conflict
  - API create booking tự động validate conflict
- Chi tiết: [GROUPED_FIELDS_GUIDE.md](./GROUPED_FIELDS_GUIDE.md)

---

## 4. Database Design (ERD)

### 4.1 Bảng `users`
```
users
├── id (BIGINT, PK, AUTO_INCREMENT)
├── email (VARCHAR 255, UNIQUE, NOT NULL)
├── password (VARCHAR 255, NOT NULL)
├── full_name (VARCHAR 255, NOT NULL)
├── phone (VARCHAR 20)
├── avatar_url (VARCHAR 500)
├── role (ENUM: CUSTOMER, OWNER, ADMIN)
├── auth_provider (ENUM: LOCAL, SOCIAL, DEFAULT 'LOCAL')
├── is_active (BOOLEAN, DEFAULT true)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.2 Bảng `stadiums`
```
stadiums
├── id (BIGINT, PK, AUTO_INCREMENT)
├── owner_id (BIGINT, FK -> users.id)
├── name (VARCHAR 255, NOT NULL)
├── address (VARCHAR 500, NOT NULL)
├── district (VARCHAR 100)
├── city (VARCHAR 100)
├── description (TEXT)
├── image_url (VARCHAR 500)
├── latitude (DECIMAL 10,8)
├── longitude (DECIMAL 11,8)
├── open_time (TIME)
├── close_time (TIME)
├── status (ENUM: PENDING, APPROVED, REJECTED, INACTIVE)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.3 Bảng `fields` (Sân con)
```
fields
├── id (BIGINT, PK, AUTO_INCREMENT)
├── stadium_id (BIGINT, FK -> stadiums.id)
├── name (VARCHAR 100, NOT NULL)  -- VD: "Sân 1", "Sân A"
├── field_type (ENUM: FIVE_A_SIDE, SEVEN_A_SIDE, ELEVEN_A_SIDE)
├── default_price (DECIMAL 10,2)
├── parent_field_id (BIGINT, FK -> fields.id, NULLABLE) ⭐ MỚI: Self-reference cho sân ghép
├── is_active (BOOLEAN, DEFAULT true)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

**⭐ Grouped Fields (Sân Ghép):**
- `parent_field_id = NULL` → Sân này là sân cha (hoặc sân độc lập)
- `parent_field_id = <id>` → Sân này là sân con, thuộc sân cha có ID `<id>`
- **Business Logic:**
  - Đặt sân con → Sân cha **không khả dụng** cùng khung giờ
  - Đặt sân cha → **Tất cả** sân con **không khả dụng** cùng khung giờ
  - Backend tự động check conflict khi:
    - `GET /fields/{id}/available-slots` → filter slots theo grouped fields
    - `POST /bookings` → validate conflict trước khi tạo booking
- **Chi tiết:** Xem [GROUPED_FIELDS_GUIDE.md](./GROUPED_FIELDS_GUIDE.md)

### 4.4 Bảng `time_slots` (Khung giờ - giá riêng)
```
time_slots
├── id (BIGINT, PK, AUTO_INCREMENT)
├── field_id (BIGINT, FK -> fields.id)
├── start_time (TIME, NOT NULL)
├── end_time (TIME, NOT NULL)
├── price (DECIMAL 10,2, NOT NULL)
├── is_active (BOOLEAN, DEFAULT true)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.5 Bảng `bookings`
```
bookings
├── id (BIGINT, PK, AUTO_INCREMENT)
├── booking_code (VARCHAR 20, UNIQUE)
├── customer_id (BIGINT, FK -> users.id)
├── field_id (BIGINT, FK -> fields.id)
├── time_slot_id (BIGINT, FK -> time_slots.id)
├── recurring_booking_id (BIGINT, FK -> recurring_bookings.id, NULLABLE)
├── booking_date (DATE, NOT NULL)
├── is_match_request (BOOLEAN, DEFAULT false)  -- Có ráp kèo không
├── total_price (DECIMAL 10,2)
├── deposit_amount (DECIMAL 10,2, DEFAULT 0)
├── remaining_amount (DECIMAL 10,2)
├── deposit_status (ENUM: NOT_REQUIRED, PENDING, PAID, REFUNDED)
├── note (TEXT)
├── status (ENUM: PENDING, DEPOSIT_PAID, CONFIRMED, CANCELLED, COMPLETED)
├── cancelled_at (DATETIME)
├── cancel_reason (VARCHAR 500)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.6 Bảng `reviews`
```
reviews
├── id (BIGINT, PK, AUTO_INCREMENT)
├── booking_id (BIGINT, FK -> bookings.id, UNIQUE)
├── customer_id (BIGINT, FK -> users.id)
├── stadium_id (BIGINT, FK -> stadiums.id)
├── rating (INT, 1-5)
├── comment (TEXT)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.7 Bảng `deposits` (Lịch sử đặt cọc)
```
deposits
├── id (BIGINT, PK, AUTO_INCREMENT)
├── booking_id (BIGINT, FK -> bookings.id)
├── amount (DECIMAL 10,2, NOT NULL)
├── deposit_type (ENUM: DEPOSIT, REFUND)
├── payment_method (ENUM: CASH, TRANSFER, MOMO, ZALOPAY)
├── transaction_code (VARCHAR 50)
├── note (TEXT)
├── confirmed_by (BIGINT, FK -> users.id)  -- Owner xác nhận
├── confirmed_at (DATETIME)
├── status (ENUM: PENDING, CONFIRMED, REJECTED)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.8 Bảng `recurring_bookings` (Đặt sân dài hạn)
```
recurring_bookings
├── id (BIGINT, PK, AUTO_INCREMENT)
├── recurring_code (VARCHAR 20, UNIQUE)
├── customer_id (BIGINT, FK -> users.id)
├── field_id (BIGINT, FK -> fields.id)
├── time_slot_id (BIGINT, FK -> time_slots.id)
├── recurrence_type (ENUM: WEEKLY, MONTHLY)
├── day_of_week (INT, 1-7)                -- Cho WEEKLY: thứ mấy
├── start_date (DATE, NOT NULL)
├── end_date (DATE, NOT NULL)
├── total_sessions (INT)                  -- Tổng số buổi
├── completed_sessions (INT, DEFAULT 0)
├── cancelled_sessions (INT, DEFAULT 0)
├── discount_percent (DECIMAL 5,2, DEFAULT 0)  -- % giảm giá dài hạn
├── original_price_per_session (DECIMAL 10,2)
├── discounted_price_per_session (DECIMAL 10,2)
├── total_price (DECIMAL 12,2)
├── total_deposit (DECIMAL 12,2)
├── deposit_status (ENUM: PENDING, PAID, PARTIALLY_REFUNDED, REFUNDED)
├── status (ENUM: ACTIVE, CANCELLED, COMPLETED, EXPIRED)
├── note (TEXT)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.9 Bảng `teams` (Đội bóng)
```
teams
├── id (BIGINT, PK, AUTO_INCREMENT)
├── name (VARCHAR 100, NOT NULL)
├── logo_url (VARCHAR 500)
├── description (TEXT)
├── preferred_field_type (ENUM: FIVE_A_SIDE, SEVEN_A_SIDE)
├── skill_level (ENUM: BEGINNER, INTERMEDIATE, ADVANCED, PRO)
├── captain_id (BIGINT, FK -> users.id)  -- Đội trưởng
├── member_count (INT, DEFAULT 1)
├── city (VARCHAR 100)
├── district (VARCHAR 100)
├── is_active (BOOLEAN, DEFAULT true)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.10 Bảng `team_members` (Thành viên đội)
```
team_members
├── id (BIGINT, PK, AUTO_INCREMENT)
├── team_id (BIGINT, FK -> teams.id)
├── user_id (BIGINT, FK -> users.id)
├── role (ENUM: CAPTAIN, MEMBER)
├── joined_at (DATETIME)
├── status (ENUM: PENDING, ACTIVE, LEFT, KICKED)
├── created_at (DATETIME)
└── updated_at (DATETIME)
-- UNIQUE(team_id, user_id)
```

### 4.11 Bảng `match_requests` (Kèo ráp)
```
match_requests
├── id (BIGINT, PK, AUTO_INCREMENT)
├── match_code (VARCHAR 20, UNIQUE)
├── booking_id (BIGINT, FK -> bookings.id)          -- Booking gốc (chủ kèo đã đặt sân)
├── host_team_id (BIGINT, FK -> teams.id)            -- Đội chủ kèo
├── opponent_team_id (BIGINT, FK -> teams.id, NULLABLE) -- Đội nhận kèo
├── field_type (ENUM: FIVE_A_SIDE, SEVEN_A_SIDE)
├── required_skill_level (ENUM: ANY, BEGINNER, INTERMEDIATE, ADVANCED, PRO)
├── cost_sharing (ENUM: EQUAL_SPLIT, HOST_PAY, OPPONENT_PAY, CUSTOM)
├── host_share_percent (DECIMAL 5,2, DEFAULT 50)     -- % chủ kèo trả
├── opponent_share_percent (DECIMAL 5,2, DEFAULT 50)  -- % đối thủ trả
├── opponent_amount (DECIMAL 10,2)                    -- Số tiền đối thủ cần trả
├── message (TEXT)                                    -- Lời nhắn/ghi chú kèo
├── contact_phone (VARCHAR 20)                        -- SĐT liên hệ
├── status (ENUM: OPEN, ACCEPTED, CANCELLED, EXPIRED, COMPLETED)
├── accepted_at (DATETIME)
├── expired_at (DATETIME)                             -- Tự hết hạn trước giờ đá X giờ
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.12 Bảng `match_responses` (Lượt nhận kèo)
```
match_responses
├── id (BIGINT, PK, AUTO_INCREMENT)
├── match_request_id (BIGINT, FK -> match_requests.id)
├── team_id (BIGINT, FK -> teams.id)
├── message (TEXT)                    -- Lời nhắn từ đội muốn nhận kèo
├── status (ENUM: PENDING, ACCEPTED, REJECTED, WITHDRAWN)
├── responded_at (DATETIME)
├── created_at (DATETIME)
└── updated_at (DATETIME)
-- UNIQUE(match_request_id, team_id)   -- Mỗi đội chỉ gửi 1 lần/kèo
```

### 4.13 Bảng `deposit_policies` (Chính sách đặt cọc)
```
deposit_policies
├── id (BIGINT, PK, AUTO_INCREMENT)
├── stadium_id (BIGINT, FK -> stadiums.id)
├── deposit_percent (DECIMAL 5,2, NOT NULL)       -- % cọc (VD: 30%)
├── refund_before_hours (INT, DEFAULT 24)          -- Hoàn cọc nếu hủy trước X giờ
├── refund_percent (DECIMAL 5,2, DEFAULT 100)      -- % hoàn lại khi hủy đúng hạn
├── late_cancel_refund_percent (DECIMAL 5,2, DEFAULT 0)  -- % hoàn khi hủy trễ
├── recurring_discount_percent (DECIMAL 5,2, DEFAULT 0)  -- % giảm giá đặt dài hạn
├── min_recurring_sessions (INT, DEFAULT 4)        -- Số buổi tối thiểu để được giảm giá
├── is_deposit_required (BOOLEAN, DEFAULT true)
├── created_at (DATETIME)
└── updated_at (DATETIME)
```

### 4.14 Quan hệ (Relationships)
```
users (1) ──────── (N) stadiums              : 1 owner có nhiều stadium
stadiums (1) ────── (N) fields               : 1 stadium có nhiều sân con
stadiums (1) ────── (1) deposit_policies      : 1 stadium có 1 chính sách cọc
fields (1) ──────── (N) time_slots            : 1 sân con có nhiều khung giờ
users (1) ──────── (N) bookings              : 1 customer có nhiều booking
fields (1) ──────── (N) bookings              : 1 sân con có nhiều booking
time_slots (1) ──── (N) bookings              : 1 khung giờ có nhiều booking (khác ngày)
bookings (1) ────── (N) deposits              : 1 booking có nhiều giao dịch cọc
bookings (1) ────── (1) reviews               : 1 booking chỉ 1 review
recurring_bookings (1)── (N) bookings         : 1 gói dài hạn có nhiều booking con
users (1) ──────── (N) recurring_bookings    : 1 customer có nhiều gói dài hạn
users (1) ──────── (N) teams (captain)        : 1 user làm đội trưởng nhiều đội
teams (1) ──────── (N) team_members           : 1 đội có nhiều thành viên
users (1) ──────── (N) team_members           : 1 user tham gia nhiều đội
bookings (1) ────── (0..1) match_requests      : 1 booking có thể có 1 kèo ráp
teams (1) ──────── (N) match_requests (host)  : 1 đội tạo nhiều kèo
teams (1) ──────── (N) match_requests (opponent): 1 đội nhận nhiều kèo
match_requests (1)── (N) match_responses      : 1 kèo có nhiều lượt xin nhận
```

---

## 5. API Design

### 5.1 Auth APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| POST | `/api/v1/auth/register` | Đăng ký | Public |
| POST | `/api/v1/auth/login` | Đăng nhập | Public |
| POST | `/api/v1/auth/refresh-token` | Refresh token | Authenticated |
| POST | `/api/v1/auth/logout` | Đăng xuất (blacklist token) | Authenticated |
| POST | `/api/v1/auth/social-login` | Đăng nhập Social (FE gửi thông tin user) | Public |
| GET | `/api/v1/auth/me` | Thông tin user hiện tại | Authenticated |

### 5.2 Stadium APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| GET | `/api/v1/stadiums` | Danh sách sân (filter, search, paging) | Public |
| GET | `/api/v1/stadiums/{id}` | Chi tiết sân | Public |
| GET | `/api/v1/stadiums/nearby` | Tìm sân gần đây (lat, lng, radius) | Public |
| POST | `/api/v1/stadiums` | Tạo sân mới | OWNER |
| PUT | `/api/v1/stadiums/{id}` | Cập nhật sân | OWNER |
| DELETE | `/api/v1/stadiums/{id}` | Xóa sân | OWNER |
| GET | `/api/v1/owner/stadiums` | DS sân của owner | OWNER |

### 5.3 Field APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| GET | `/api/v1/stadiums/{stadiumId}/fields` | Danh sách sân con | Public |
| POST | `/api/v1/stadiums/{stadiumId}/fields` | Tạo sân con | OWNER |
| PUT | `/api/v1/fields/{id}` | Cập nhật sân con | OWNER |
| DELETE | `/api/v1/fields/{id}` | Xóa sân con | OWNER |

### 5.4 TimeSlot APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| GET | `/api/v1/fields/{fieldId}/time-slots` | DS khung giờ | Public |
| POST | `/api/v1/fields/{fieldId}/time-slots` | Tạo khung giờ | OWNER |
| PUT | `/api/v1/time-slots/{id}` | Cập nhật khung giờ | OWNER |
| DELETE | `/api/v1/time-slots/{id}` | Xóa khung giờ | OWNER |

### 5.5 Booking APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| GET | `/api/v1/fields/{fieldId}/available-slots?date=` | Xem slot trống theo ngày | Public |
| POST | `/api/v1/bookings` | Đặt sân | CUSTOMER |
| GET | `/api/v1/bookings/my` | Lịch sử đặt sân của tôi | CUSTOMER |
| GET | `/api/v1/bookings/{id}` | Chi tiết đơn đặt | CUSTOMER/OWNER |
| PUT | `/api/v1/bookings/{id}/cancel` | Hủy đặt | CUSTOMER |
| GET | `/api/v1/owner/bookings` | DS đơn đặt (owner) | OWNER |
| PUT | `/api/v1/owner/bookings/{id}/confirm` | Xác nhận đơn | OWNER |
| PUT | `/api/v1/owner/bookings/{id}/reject` | Từ chối đơn | OWNER |
| PUT | `/api/v1/owner/bookings/{id}/complete` | Hoàn thành | OWNER |
| GET | `/api/v1/owner/stadiums/{stadiumId}/bookings` | DS đơn theo sân + ngày | OWNER |

### 5.6 Deposit APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| GET | `/api/v1/stadiums/{stadiumId}/deposit-policy` | Xem chính sách cọc | Public |
| PUT | `/api/v1/stadiums/{stadiumId}/deposit-policy` | Cập nhật chính sách cọc | OWNER |
| POST | `/api/v1/bookings/{bookingId}/deposits` | Tạo giao dịch đặt cọc | CUSTOMER |
| PUT | `/api/v1/owner/deposits/{id}/confirm` | Xác nhận đã nhận cọc | OWNER |
| PUT | `/api/v1/owner/deposits/{id}/reject` | Từ chối giao dịch cọc | OWNER |
| GET | `/api/v1/bookings/{bookingId}/deposits` | Lịch sử cọc của booking | CUSTOMER/OWNER |
| POST | `/api/v1/owner/bookings/{bookingId}/refund` | Hoàn cọc khi hủy | OWNER |

### 5.7 Recurring Booking APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| POST | `/api/v1/recurring-bookings` | Đặt sân dài hạn | CUSTOMER |
| GET | `/api/v1/recurring-bookings/my` | DS gói dài hạn của tôi | CUSTOMER |
| GET | `/api/v1/recurring-bookings/{id}` | Chi tiết gói (kèm DS các buổi) | CUSTOMER/OWNER |
| PUT | `/api/v1/recurring-bookings/{id}/cancel` | Hủy toàn bộ gói | CUSTOMER |
| PUT | `/api/v1/recurring-bookings/{id}/bookings/{bookingId}/cancel` | Hủy 1 buổi trong gói | CUSTOMER |
| GET | `/api/v1/owner/recurring-bookings` | DS gói dài hạn (owner) | OWNER |
| PUT | `/api/v1/owner/recurring-bookings/{id}/confirm` | Xác nhận gói | OWNER |

### 5.8 Team APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| POST | `/api/v1/teams` | Tạo đội bóng | CUSTOMER |
| GET | `/api/v1/teams/my` | DS đội của tôi (captain + member) | CUSTOMER |
| GET | `/api/v1/teams/{id}` | Chi tiết đội (kèm DS thành viên) | CUSTOMER |
| PUT | `/api/v1/teams/{id}` | Cập nhật thông tin đội | CUSTOMER (captain) |
| DELETE | `/api/v1/teams/{id}` | Giải tán đội | CUSTOMER (captain) |
| POST | `/api/v1/teams/{id}/members` | Thêm thành viên (mời) | CUSTOMER (captain) |
| PUT | `/api/v1/teams/{id}/members/{userId}/remove` | Xóa thành viên | CUSTOMER (captain) |
| PUT | `/api/v1/teams/{id}/members/{userId}/captain` | Chuyển quyền đội trưởng | CUSTOMER (captain) |
| PUT | `/api/v1/teams/{id}/leave` | Rời đội | CUSTOMER (member) |
| PUT | `/api/v1/team-invites/{id}/accept` | Chấp nhận lời mời vào đội | CUSTOMER |
| PUT | `/api/v1/team-invites/{id}/reject` | Từ chối lời mời | CUSTOMER |

### 5.9 Match Making APIs (Ráp Kèo)
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| POST | `/api/v1/match-requests` | Tạo kèo (kèm booking_id + team_id) | CUSTOMER |
| GET | `/api/v1/match-requests` | DS kèo đang mở (filter: khu vực, loại sân, ngày, trình độ) | Public |
| GET | `/api/v1/match-requests/{id}` | Chi tiết kèo | Public |
| PUT | `/api/v1/match-requests/{id}/cancel` | Hủy kèo | CUSTOMER (host) |
| GET | `/api/v1/match-requests/my` | DS kèo tôi đã tạo | CUSTOMER |
| GET | `/api/v1/match-requests/my-matches` | DS kèo tôi đã nhận | CUSTOMER |
| POST | `/api/v1/match-requests/{id}/responses` | Gửi yêu cầu nhận kèo | CUSTOMER |
| PUT | `/api/v1/match-requests/{id}/responses/{responseId}/accept` | Chấp nhận đội nhận kèo | CUSTOMER (host) |
| PUT | `/api/v1/match-requests/{id}/responses/{responseId}/reject` | Từ chối đội nhận kèo | CUSTOMER (host) |
| PUT | `/api/v1/match-requests/{id}/responses/{responseId}/withdraw` | Rút yêu cầu nhận kèo | CUSTOMER |

### 5.10 Review APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| POST | `/api/v1/reviews` | Đánh giá | CUSTOMER/OWNER |
| GET | `/api/v1/stadiums/{stadiumId}/reviews` | DS đánh giá của sân | Public |
| GET | `/api/v1/reviews/my` | DS đánh giá của tôi | Authenticated |
| DELETE | `/api/v1/reviews/{id}` | Xóa đánh giá | Authenticated |

### 5.11 Admin APIs
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| GET | `/api/v1/admin/users` | DS users | ADMIN |
| GET | `/api/v1/admin/users/{id}` | Chi tiết user | ADMIN |
| PUT | `/api/v1/admin/users/{id}/toggle-active` | Bật/tắt user | ADMIN |
| GET | `/api/v1/admin/stadiums/pending` | DS sân chờ duyệt | ADMIN |
| PUT | `/api/v1/admin/stadiums/{id}/approve` | Duyệt sân | ADMIN |
| PUT | `/api/v1/admin/stadiums/{id}/reject` | Từ chối sân | ADMIN |
| GET | `/api/v1/admin/dashboard` | Thống kê | ADMIN |

### 5.12 Health API
| Method | Endpoint | Mô tả | Role |
|--------|----------|--------|------|
| GET | `/api/v1/health` | Health check (MySQL + Redis) | Public |

---

## 6. Project Structure

```
booking-stadium/
├── src/main/java/com/booking/stadium/
│   ├── BookingStadiumApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── CorsConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── StadiumController.java
│   │   ├── FieldController.java
│   │   ├── TimeSlotController.java
│   │   ├── BookingController.java
│   │   ├── DepositController.java
│   │   ├── DepositPolicyController.java
│   │   ├── RecurringBookingController.java
│   │   ├── TeamController.java
│   │   ├── MatchRequestController.java
│   │   ├── ReviewController.java
│   │   ├── AdminController.java
│   │   └── HealthController.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── StadiumRequest.java
│   │   │   ├── FieldRequest.java
│   │   │   ├── TimeSlotRequest.java
│   │   │   ├── BookingRequest.java
│   │   │   ├── DepositRequest.java
│   │   │   ├── DepositPolicyRequest.java
│   │   │   │   ├── RecurringBookingRequest.java
│   │   │   ├── TeamRequest.java
│   │   │   ├── TeamMemberRequest.java
│   │   │   ├── MatchRequestRequest.java
│   │   │   ├── MatchResponseRequest.java
│   │   │   └── ReviewRequest.java
│   │   └── response/
│   │       ├── ApiResponse.java
│   │       ├── JwtResponse.java
│   │       ├── UserResponse.java
│   │       ├── StadiumResponse.java
│   │       ├── FieldResponse.java
│   │       ├── TimeSlotResponse.java
│   │       ├── BookingResponse.java
│   │       ├── DepositResponse.java
│   │       ├── DepositPolicyResponse.java
│   │       ├── RecurringBookingResponse.java
│   │       ├── TeamResponse.java
│   │       ├── TeamMemberResponse.java
│   │       ├── MatchRequestResponse.java
│   │       ├── MatchResponseResponse.java
│   │       └── ReviewResponse.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Stadium.java
│   │   ├── Field.java
│   │   ├── TimeSlot.java
│   │   ├── Booking.java
│   │   ├── Deposit.java
│   │   ├── DepositPolicy.java
│   │   ├── RecurringBooking.java
│   │   ├── Team.java
│   │   ├── TeamMember.java
│   │   ├── MatchRequest.java
│   │   ├── MatchResponse.java
│   │   └── Review.java
│   ├── enums/
│   │   ├── Role.java
│   │   ├── FieldType.java
│   │   ├── StadiumStatus.java
│   │   ├── BookingStatus.java
│   │   ├── DepositStatus.java
│   │   ├── DepositType.java
│   │   ├── PaymentMethod.java
│   │   ├── RecurrenceType.java
│   │   ├── SkillLevel.java
│   │   ├── CostSharing.java
│   │   ├── MatchStatus.java
│   │   ├── TeamMemberRole.java
│   │   └── TeamMemberStatus.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   └── UnauthorizedException.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── StadiumRepository.java
│   │   ├── FieldRepository.java
│   │   ├── TimeSlotRepository.java
│   │   ├── BookingRepository.java
│   │   ├── DepositRepository.java
│   │   ├── DepositPolicyRepository.java
│   │   ├── RecurringBookingRepository.java
│   │   ├── TeamRepository.java
│   │   ├── TeamMemberRepository.java
│   │   ├── MatchRequestRepository.java
│   │   ├── MatchResponseRepository.java
│   │   └── ReviewRepository.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   └── service/
│       ├── AuthService.java
│       ├── StadiumService.java
│       ├── FieldService.java
│       ├── TimeSlotService.java
│       ├── BookingService.java
│       ├── DepositService.java
│       ├── RecurringBookingService.java
│       ├── TeamService.java
│       ├── MatchRequestService.java
│       ├── ReviewService.java
│       └── AdminService.java
├── src/main/resources/
│   ├── application.yml
│   └── data.sql (seed data)
├── src/test/java/
├── pom.xml
└── docs/
    └── SYSTEM_DESIGN.md
```

---

## 7. Business Rules

### 7.1 Đặt sân cơ bản
1. **Đặt sân**: Không cho phép đặt trùng (cùng field + cùng time_slot + cùng ngày)
2. **Hủy đặt**: Chỉ được hủy trước giờ đá ít nhất 2 tiếng
3. **Đánh giá**: Chỉ đánh giá được sau khi booking COMPLETED
4. **Sân mới**: Owner tạo sân → status = PENDING → Admin duyệt
5. **Booking code**: Tự sinh mã unique (VD: BK20260307001)

### 7.2 Đặt cọc (Deposit)
6. **Cọc bắt buộc**: Owner cấu hình % cọc cho sân (mặc định 30%)
7. **Flow đặt cọc**: Customer đặt sân → Chuyển cọc → Owner xác nhận → Booking CONFIRMED
8. **Hoàn cọc đúng hạn**: Hủy trước `refund_before_hours` giờ → hoàn theo `refund_percent`
9. **Hoàn cọc trễ**: Hủy trong vòng `refund_before_hours` giờ → hoàn theo `late_cancel_refund_percent`
10. **Timeout cọc**: Nếu sau 30 phút không đặt cọc → tự động hủy booking
11. **Thanh toán**: Đến sân thanh toán phần còn lại = `total_price - deposit_amount`

### 7.3 Đặt sân dài hạn (Recurring)
12. **Tạo gói**: Customer chọn sân + khung giờ + ngày bắt đầu/kết thúc + kiểu lặp → hệ thống tự tạo N bookings con
13. **Giảm giá**: Đặt >= `min_recurring_sessions` buổi → được giảm giá `recurring_discount_percent`%
14. **Cọc gói dài hạn**: Cọc = % × giá 1 buổi × tổng số buổi (sau giảm giá)
15. **Kiểm tra trùng lịch**: Tất cả các ngày trong gói phải không trùng booking khác
16. **Hủy 1 buổi**: Cho phép hủy từng buổi, hoàn cọc theo chính sách, các buổi còn lại không ảnh hưởng
17. **Hủy toàn bộ gói**: Hoàn cọc các buổi chưa diễn ra theo chính sách, các buổi đã COMPLETED giữ nguyên
18. **Ưu tiên dài hạn**: Booking dài hạn được ưu tiên giữ slot hơn booking lẻ

### 7.4 Đội bóng & Ráp kèo (Team & Match Making)
19. **Tạo đội**: 1 user có thể tạo nhiều đội, mỗi đội có 1 đội trưởng
20. **Thành viên**: Đội trưởng mời thành viên qua email, thành viên xác nhận mới vào đội
21. **Tạo kèo**: Chỉ tạo kèo khi đã đặt sân + booking đang ở trạng thái CONFIRMED/DEPOSIT_PAID
22. **Loại sân**: Kèo chỉ cho sân 5 và sân 7 (FIVE_A_SIDE, SEVEN_A_SIDE)
23. **Nhận kèo**: Đội nhận kèo không được trùng với đội chủ kèo, 1 đội chỉ gửi 1 lần/kèo
24. **Chọn đối thủ**: Chủ kèo xem DS đội muốn nhận → chọn 1 đội → status = ACCEPTED
25. **Chia chi phí**: Mặc định 50/50, chủ kèo có thể cấu hình tỷ lệ khác
26. **Hết hạn kèo**: Kèo tự hết hạn trước giờ đá 4 tiếng nếu chưa có đội nhận
27. **Hủy kèo**: Chủ kèo hủy bất cứ lúc nào khi chưa ACCEPTED, booking vẫn giữ nguyên
28. **Rút kèo**: Đội đã nhận kèo (ACCEPTED) rút → kèo quay về OPEN nếu chưa hết hạn
29. **Hoàn thành**: Khi booking COMPLETED → match_request cũng COMPLETED
30. **Filter kèo**: Tìm kèo theo khu vực, ngày giờ, loại sân, trình độ

---

## 8. Kế Hoạch Triển Khai (Implementation Phases)

### Phase 1 - Foundation ✅
- [x] Khởi tạo project Spring Boot
- [x] Cấu hình MySQL, JPA
- [x] Tạo entities & repositories
- [x] Cấu hình Security + JWT
- [x] Auth APIs (register, login, refresh-token, me, logout)

### Phase 2 - Core Features ✅
- [x] Stadium CRUD APIs
- [x] Field CRUD APIs
- [x] TimeSlot CRUD APIs
- [x] Booking APIs (đặt lẻ)
- [x] Available slots logic
- [x] Deposit Policy CRUD

### Phase 3 - Deposit & Recurring ✅
- [x] Deposit flow (tạo cọc, xác nhận, từ chối, hoàn cọc)
- [x] Deposit timeout auto-cancel (DepositTimeoutScheduler - 30 phút)
- [x] Recurring Booking APIs (tạo, xem, chi tiết, owner confirm)
- [x] Auto-generate bookings cho gói dài hạn
- [x] Hủy 1 buổi / hủy toàn bộ gói
- [x] Giảm giá dài hạn (discount logic based on deposit policy)

### Phase 4 - Team & Match Making ✅
- [x] Team CRUD APIs
- [x] Team member management (mời, xóa, rời đội)
- [x] Match Request APIs (tạo kèo, DS kèo)
- [x] Match Response APIs (nhận kèo, chọn đối thủ)
- [x] Cost sharing logic
- [x] Auto-expire kèo

### Phase 5 - Extended Features ✅
- [x] Review APIs
- [x] Admin APIs
- [x] Search & Filter
- [x] Pagination

### Phase 6 - Polish ✅
- [x] Swagger documentation
- [x] Validation & Error handling
- [x] Seed data
- [x] Testing

---

## 9. Cấu Hình Môi Trường

### Docker Services
```bash
# Start tất cả services
docker compose up -d

# Kiểm tra status
docker compose ps

# Stop
docker compose down

# Stop + xóa data
docker compose down -v
```

| Service | Container | Host Port | Password |
|---------|-----------|-----------|----------|
| MySQL 8.0 | booking-stadium-mysql | 3307 | booking_user / booking_pass |
| Redis 7 | booking-stadium-redis | 6380 | redis123 |

### MySQL
```
Database: booking_stadium
Host: localhost
Port: 3307 (host) → 3306 (container)
Username: booking_user
Password: booking_pass
Root Password: root123
Charset: utf8mb4
```

### Redis
```
Host: localhost
Port: 6380 (host) → 6379 (container)
Password: redis123
Max Memory: 256mb
Eviction Policy: allkeys-lru
```

### Application
```
Server port: 8080
JWT Secret: (configure in application.yml)
JWT Expiration: 24h
Refresh Token Expiration: 7d
```

### Health Check API
```
GET http://localhost:8080/api/v1/health

Response:
{
  "status": "UP",
  "mysql": { "status": "UP", "database": "booking_stadium" },
  "redis": { "status": "UP", "ping": "PONG", "read_write": "ok" }
}
```

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```
