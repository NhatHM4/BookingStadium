# Booking Stadium - API Documentation cho Frontend

> **Base URL:** `http://localhost:8080/api/v1`  
> **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`  
> **Content-Type:** `application/json`  
> **Authentication:** JWT Bearer Token  
> **Tổng số endpoints:** 79

---

## Mục Lục

1. [Authentication](#1-authentication)
2. [Thông tin chung](#2-thông-tin-chung)
3. [Auth APIs](#3-auth-apis)
4. [Image Upload APIs](#4-image-upload-apis)
5. [Stadium APIs](#5-stadium-apis)
6. [Field APIs](#6-field-apis)
7. [TimeSlot APIs](#7-timeslot-apis)
8. [Booking APIs](#8-booking-apis)
9. [Deposit APIs](#9-deposit-apis)
10. [Recurring Booking APIs](#10-recurring-booking-apis)
11. [Team APIs](#11-team-apis)
12. [Match Making APIs](#12-match-making-apis)
13. [Review APIs](#13-review-apis)
14. [Admin APIs](#14-admin-apis)
15. [Health API](#15-health-api)
16. [Enums Reference](#16-enums-reference)
17. [DTOs Reference](#17-dtos-reference)
18. [Error Handling](#18-error-handling)
19. [Pagination](#19-pagination)
20. [Test Accounts](#20-test-accounts)

---

## 1. Authentication

### JWT Bearer Token

Tất cả API yêu cầu xác thực cần gửi header:

```
Authorization: Bearer <accessToken>
```

### Flow đăng nhập

```
1. POST /auth/login → nhận accessToken + refreshToken
   (hoặc POST /auth/social-login với thông tin user từ Google/Social IDP)
2. Dùng accessToken trong header Authorization
3. Khi accessToken hết hạn → POST /auth/refresh-token
4. Khi logout → POST /auth/logout
```

### Token Expiration

| Token | Thời hạn |
|-------|---------|
| Access Token | 24 giờ |
| Refresh Token | 7 ngày |

---

## 2. Thông Tin Chung

### Response Format

Tất cả API trả về cùng format:

```json
{
  "success": true,
  "message": "Thành công",
  "data": { ... }
}
```

### Error Response Format

```json
{
  "success": false,
  "message": "Mô tả lỗi",
  "data": null
}
```

### Validation Error Response

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "email": "Email không hợp lệ",
    "password": "Mật khẩu phải có ít nhất 6 ký tự"
  }
}
```

---

## 3. Auth APIs

### 3.1 Đăng ký
```
POST /api/v1/auth/register
```
**Auth:** Public

**Request Body:**
```json
{
  "email": "user@example.com",       // required, @Email
  "password": "123456",              // required, min 6 ký tự
  "fullName": "Nguyễn Văn A",        // required
  "phone": "0901234567",             // optional
  "role": "CUSTOMER"                 // optional, default: CUSTOMER. Values: CUSTOMER | OWNER
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "phone": "0901234567",
      "avatarUrl": null,
      "role": "CUSTOMER",
      "authProvider": "LOCAL",
      "isActive": true
    }
  }
}
```

---

### 3.2 Đăng nhập
```
POST /api/v1/auth/login
```
**Auth:** Public

**Request Body:**
```json
{
  "email": "user@example.com",       // required, @Email
  "password": "123456"               // required, min 6 ký tự
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "Nguyễn Văn A",
      "phone": "0901234567",
      "avatarUrl": null,
      "role": "CUSTOMER",
      "authProvider": "LOCAL",
      "isActive": true
    }
  }
}
```

---

### 3.3 Refresh Token
```
POST /api/v1/auth/refresh-token
```
**Auth:** Public

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."  // required
}
```

**Response:** `200 OK` — Trả về `JwtResponse` mới (cùng format đăng nhập)

---

### 3.4 Đăng xuất
```
POST /api/v1/auth/logout
```
**Auth:** Public

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."  // required
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Đăng xuất thành công",
  "data": null
}
```

---

### 3.5 Đăng nhập bằng Social (Google, v.v.)
```
POST /api/v1/auth/social-login
```
**Auth:** Public

> **Flow:** FE tự xử lý OAuth với Google (hoặc Social IDP khác), lấy thông tin user (email, tên, avatar), sau đó gửi xuống Spring. Spring không tracking với IDP.

**Request Body:**
```json
{
  "email": "user@gmail.com",         // required, @Email
  "fullName": "Nguyễn Văn A",        // required
  "avatarUrl": "https://lh3.googleusercontent.com/...",  // optional
  "phone": "0901234567"              // optional
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Đăng nhập Social thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 6,
      "email": "user@gmail.com",
      "fullName": "Nguyễn Văn A",
      "phone": null,
      "avatarUrl": "https://lh3.googleusercontent.com/...",
      "role": "CUSTOMER",
      "authProvider": "SOCIAL",
      "isActive": true
    }
  }
}
```

> **Lưu ý:**
> - Nếu email đã tồn tại (đăng ký thường trước đó) → cập nhật `authProvider` thành `SOCIAL`, trả JWT
> - Nếu email chưa tồn tại → tạo user mới với role `CUSTOMER`, password default, trả JWT
> - User social login cũng có thể login bằng credential với password default: `Social@123456`

---

### 3.6 Thông tin user hiện tại
```
GET /api/v1/auth/me
```
**Auth:** Authenticated (Bearer Token)

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Thành công",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A",
    "phone": "0901234567",
    "avatarUrl": null,
    "role": "CUSTOMER",
    "authProvider": "LOCAL",
    "isActive": true
  }
}
```

---

## 4. Image Upload APIs

### 4.1 Upload ảnh sân
```
POST /api/v1/images/upload
```
**Auth:** OWNER  
**Content-Type:** `multipart/form-data`

**Form Data:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `file` | File | **Yes** | File ảnh (JPG, PNG, max 5MB) |
| `stadiumId` | Long | No | ID sân (nếu đã có). Nếu null → lưu vào temp |

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Upload ảnh thành công",
  "data": {
    "path": "stadiums/temp/uuid-filename.jpg",
    "url": "/uploads/stadiums/temp/uuid-filename.jpg"
  }
}
```

**Example cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/images/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@stadium.jpg" \
  -F "stadiumId=1"
```

**Flow tạo sân với ảnh:**
```
1. Upload ảnh trước (POST /images/upload) → nhận path từ response
2. Tạo sân (POST /stadiums) với imageUrl = path từ bước 1
3. Backend tự động move ảnh từ temp/ sang stadiums/{stadiumId}/
```

**Lưu ý:**
- Chỉ chấp nhận file ảnh (image/*)
- Max size: 5MB
- File được lưu trong thư mục `uploads/stadiums/{stadiumId}/`
- URL truy cập: `http://localhost:8080/uploads/stadiums/{stadiumId}/{filename}`
- Static resource được serve công khai (không cần auth)

---

## 5. Stadium APIs

### 5.1 Danh sách sân (filter, search, paging)
```
GET /api/v1/stadiums
```
**Auth:** Public

**Query Parameters:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `city` | String | No | Lọc theo thành phố |
| `district` | String | No | Lọc theo quận/huyện |
| `name` | String | No | Tìm theo tên sân |
| `fieldType` | FieldType | No | Lọc theo loại sân: `FIVE_A_SIDE`, `SEVEN_A_SIDE`, `ELEVEN_A_SIDE` |
| `page` | int | No | Trang (0-indexed), default: 0 |
| `size` | int | No | Số item/trang, default: 10 |
| `sort` | String | No | VD: `name,asc` hoặc `createdAt,desc` |

**Response:** `200 OK` — `Page<StadiumResponse>`
```json
{
  "success": true,
  "message": "Thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "ownerId": 2,
        "ownerName": "Trần Văn B",
        "name": "Sân bóng ABC",
        "address": "123 Nguyễn Huệ",
        "district": "Quận 1",
        "city": "TP.HCM",
        "description": "Sân đẹp, cỏ nhân tạo",
        "imageUrl": "https://example.com/image.jpg",
        "latitude": 10.762622,
        "longitude": 106.660172,
        "openTime": "06:00",
        "closeTime": "22:00",
        "status": "APPROVED",
        "avgRating": 4.5,
        "reviewCount": 12,
        "fieldCount": 3
      }
    ],
    "pageable": { ... },
    "totalPages": 5,
    "totalElements": 48,
    "size": 10,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

---

### 4.2 Chi tiết sân
```
GET /api/v1/stadiums/{id}
```
**Auth:** Public

**Path Params:** `id` (Long) — ID của sân

**Response:** `200 OK` — `StadiumResponse` (cùng format trên)

---

### 4.3 Tìm sân gần đây
```
GET /api/v1/stadiums/nearby
```
**Auth:** Public

**Query Parameters:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `lat` | Double | **Yes** | Vĩ độ hiện tại |
| `lng` | Double | **Yes** | Kinh độ hiện tại |
| `radius` | Double | No | Bán kính tìm kiếm (km), default: 5 |

**Response:** `200 OK` — `List<StadiumResponse>`

---

### 4.4 Tạo sân mới
```
POST /api/v1/stadiums
```
**Auth:** OWNER

**Request Body:**
```json
{
  "name": "Sân bóng XYZ",                           // required
  "address": "456 Lê Lợi, Q1",                      // required
  "district": "Quận 1",                             // optional
  "city": "TP.HCM",                                 // optional
  "description": "Sân cỏ nhân tạo",                 // optional
  "imageUrl": "stadiums/temp/uuid-filename.jpg",    // optional (path từ API upload)
  "latitude": 10.762622,                            // optional
  "longitude": 106.660172,                          // optional
  "openTime": "06:00",                              // optional (HH:mm)
  "closeTime": "22:00"                              // optional (HH:mm)
}
```

**Response:** `201 Created` — `StadiumResponse`

> **Lưu ý:** 
> - Sân mới tạo sẽ có `status: "PENDING"`, cần Admin duyệt
> - **Để upload ảnh:** Gọi `POST /api/v1/images/upload` trước, nhận `path` từ response, sau đó dùng `path` làm `imageUrl`
> - Backend tự động move ảnh từ `temp/` sang `stadiums/{stadiumId}/` sau khi tạo sân

---

### 4.5 Cập nhật sân
```
PUT /api/v1/stadiums/{id}
```
**Auth:** OWNER (chỉ owner của sân)

**Path Params:** `id` (Long)  
**Request Body:** Cùng format `StadiumRequest` (4.4)

**Response:** `200 OK` — `StadiumResponse`

---

### 4.6 Xóa sân
```
DELETE /api/v1/stadiums/{id}
```
**Auth:** OWNER (chỉ owner của sân)

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Xóa sân thành công",
  "data": null
}
```

---

### 4.7 Danh sách sân của tôi (Owner)
```
GET /api/v1/owner/stadiums
```
**Auth:** OWNER

**Response:** `200 OK` — `List<StadiumResponse>`

---

### 4.8 DS sân chờ duyệt (Admin)
```
GET /api/v1/admin/stadiums/pending
```
**Auth:** ADMIN

**Query Parameters:** Pagination (`page`, `size`, `sort`)

**Response:** `200 OK` — `Page<StadiumResponse>`

---

### 4.9 Duyệt sân (Admin)
```
PUT /api/v1/admin/stadiums/{id}/approve
```
**Auth:** ADMIN

**Response:** `200 OK` — `StadiumResponse` (status: `APPROVED`)

---

### 4.10 Từ chối sân (Admin)
```
PUT /api/v1/admin/stadiums/{id}/reject
```
**Auth:** ADMIN

**Response:** `200 OK` — `StadiumResponse` (status: `REJECTED`)

---

## 5. Field APIs (Sân con)

> **⭐ MỚI: Grouped Fields (Sân Ghép)**  
> Từ version mới, hỗ trợ sân ghép: nhiều sân nhỏ có thể ghép thành 1 sân lớn.  
> **Chi tiết:** Xem [GROUPED_FIELDS_GUIDE.md](./GROUPED_FIELDS_GUIDE.md)

### 5.1 DS sân con của 1 sân
```
GET /api/v1/stadiums/{stadiumId}/fields
```
**Auth:** Public

**Path Params:** `stadiumId` (Long)

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Thành công",
  "data": [
    {
      "id": 1,
      "stadiumId": 1,
      "stadiumName": "Sân bóng ABC",
      "name": "Sân A1 - 5 người",
      "fieldType": "FIVE_A_SIDE",
      "defaultPrice": 250000,
      "isActive": true,
      "parentFieldId": 3,           // ⭐ MỚI: ID sân cha (null nếu là sân cha hoặc độc lập)
      "childFieldCount": 0          // ⭐ MỚI: Số sân con (0 nếu là sân con hoặc độc lập)
    },
    {
      "id": 2,
      "stadiumId": 1,
      "stadiumName": "Sân bóng ABC",
      "name": "Sân A2 - 5 người",
      "fieldType": "FIVE_A_SIDE",
      "defaultPrice": 250000,
      "isActive": true,
      "parentFieldId": 3,
      "childFieldCount": 0
    },
    {
      "id": 3,
      "stadiumId": 1,
      "stadiumName": "Sân bóng ABC",
      "name": "Sân B1 - 7 người (Ghép)",
      "fieldType": "SEVEN_A_SIDE",
      "defaultPrice": 350000,
      "isActive": true,
      "parentFieldId": null,        // null = đây là sân cha
      "childFieldCount": 2          // có 2 sân con (A1, A2)
    }
  ]
}
```

**Field Description:**
| Field | Type | Mô tả |
|-------|------|-------|
| `parentFieldId` | `Long \| null` | **⭐ MỚI:** ID của sân cha. `null` = sân này là sân cha hoặc sân độc lập |
| `childFieldCount` | `Integer` | **⭐ MỚI:** Số sân con. `0` = không có sân con (là sân con hoặc độc lập), `> 0` = là sân cha |

**Grouped Fields Logic:**
- Khi đặt sân con → sân cha **không thể đặt** cùng khung giờ
- Khi đặt sân cha → **tất cả** sân con **không thể đặt** cùng khung giờ
- API `GET /fields/{id}/available-slots` tự động check conflict
- API `POST /bookings` tự động validate conflict

---

### 5.2 Tạo sân con
```
POST /api/v1/stadiums/{stadiumId}/fields
```
**Auth:** OWNER

**Request Body:**
```json
{
  "name": "Sân A1 - 5 người",         // required
  "fieldType": "FIVE_A_SIDE",         // required: FIVE_A_SIDE | SEVEN_A_SIDE | ELEVEN_A_SIDE
  "defaultPrice": 250000,             // required, > 0
  "imageUrl": "stadiums/1/field-a1.jpg", // optional (path từ API upload)
  "parentFieldId": 3                  // ⭐ MỚI: optional, ID sân cha (nếu sân này là sân con)
}
```

**Response:** `201 Created` — `FieldResponse`

**Lưu ý:**
- Nếu truyền `parentFieldId`, sân cha phải thuộc cùng stadium
- Sân con không thể có sân con (không hỗ trợ multi-level)

---

### 5.3 Cập nhật sân con
```
PUT /api/v1/fields/{id}
```
**Auth:** OWNER

**Request Body:**
```json
{
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000,
  "imageUrl": "stadiums/1/field-a1-v2.jpg", // optional
  "parentFieldId": 3                  // ⭐ MỚI: có thể update parent relationship
}
```

**Response:** `200 OK` — `FieldResponse`

---

### 5.4 Xóa sân con
```
DELETE /api/v1/fields/{id}
```
**Auth:** OWNER

**Response:** `200 OK` — `ApiResponse<Void>`

---

## 6. TimeSlot APIs (Khung giờ)

### 6.1 DS khung giờ của sân con
```
GET /api/v1/fields/{fieldId}/time-slots
```
**Auth:** Public

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Thành công",
  "data": [
    {
      "id": 1,
      "fieldId": 1,
      "fieldName": "Sân 1",
      "startTime": "06:00",
      "endTime": "07:30",
      "price": 350000,
      "isActive": true
    }
  ]
}
```

---

### 6.2 Tạo khung giờ
```
POST /api/v1/fields/{fieldId}/time-slots
```
**Auth:** OWNER

**Request Body:**
```json
{
  "startTime": "06:00",               // required (HH:mm)
  "endTime": "07:30",                 // required (HH:mm)
  "price": 350000                     // required, > 0
}
```

**Response:** `201 Created` — `TimeSlotResponse`

---

### 6.3 Cập nhật khung giờ
```
PUT /api/v1/time-slots/{id}
```
**Auth:** OWNER

**Request Body:** Cùng `TimeSlotRequest`

**Response:** `200 OK` — `TimeSlotResponse`

---

### 6.4 Xóa khung giờ
```
DELETE /api/v1/time-slots/{id}
```
**Auth:** OWNER

**Response:** `200 OK` — `ApiResponse<Void>`

---

## 7. Booking APIs (Đặt sân)

### 7.1 Xem slot trống theo ngày
```
GET /api/v1/fields/{fieldId}/available-slots?date=2025-03-15
```
**Auth:** Public

**Query Parameters:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `date` | LocalDate | **Yes** | Ngày cần xem (ISO format: `yyyy-MM-dd`) |

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Thành công",
  "data": [
    {
      "timeSlotId": 1,
      "startTime": "06:00",
      "endTime": "07:30",
      "price": 350000,
      "isAvailable": true
    },
    {
      "timeSlotId": 2,
      "startTime": "07:30",
      "endTime": "09:00",
      "price": 350000,
      "isAvailable": false
    }
  ]
}
```

---

### 7.2 Đặt sân
```
POST /api/v1/bookings
```
**Auth:** CUSTOMER

**Request Body:**
```json
{
  "fieldId": 1,                        // required
  "timeSlotId": 1,                     // required
  "bookingDate": "2025-03-15",         // required (yyyy-MM-dd)
  "note": "Đặt cho team ABC",          // optional
  "isMatchRequest": false,             // optional, default: false
  
  // ========== Fields cho Match Request (chỉ dùng khi isMatchRequest = true) ==========
  
  // === OPTION 1: Dùng đội có sẵn ===
  "teamId": 5,                         // optional: ID đội có sẵn (nếu đã có đội)
  
  // === OPTION 2: Tạo đội nhanh ===
  "createQuickTeam": true,             // optional: true = tạo đội nhanh tại màn hình
  "quickTeamName": "Team ABC",         // required nếu createQuickTeam = true: Tên đội mới
  "quickTeamSkillLevel": "INTERMEDIATE", // optional: Trình độ đội (ANY | BEGINNER | INTERMEDIATE | ADVANCED | EXPERT)
  
  // === OPTION 3: Không cần đội - chỉ tên + SĐT ===
  "hostName": "Nguyễn Văn A",          // required nếu không có teamId và createQuickTeam = false: Tên người chơi
  "contactPhone": "0987654321",        // required nếu không có team: SĐT liên hệ
  
  // === Thông tin chung ===
  "requiredSkillLevel": "INTERMEDIATE",// optional: Yêu cầu trình độ đối thủ
  "costSharing": "WIN_LOSE",           // optional: WIN_LOSE (70/30 mặc định) | EQUAL_SPLIT | HOST_PAY | OPPONENT_PAY | CUSTOM
  "hostSharePercent": 70.00,           // optional: % tiền chủ nhà trả (chỉ dùng khi costSharing = CUSTOM)
  "opponentSharePercent": 30.00,       // optional: % tiền đối thủ trả (chỉ dùng khi costSharing = CUSTOM)
  "matchMessage": "Tìm đối ráp kèo"    // optional: Lời nhắn cho đối thủ
}
```

**⭐ CÁC OPTION KHI TẠO TRẬN RÁP KÈO:**

**Option 1 - Dùng đội có sẵn:**
```json
{
  "isMatchRequest": true,
  "teamId": 5,
  "contactPhone": "0987654321"
}
```
- Dùng đội đã tạo trước đó
- Bạn phải là **đội trưởng** của đội đó

**Option 2 - Tạo đội nhanh:**
```json
{
  "isMatchRequest": true,
  "createQuickTeam": true,
  "quickTeamName": "Team ABC",
  "quickTeamSkillLevel": "INTERMEDIATE",
  "contactPhone": "0987654321"
}
```
- Hệ thống tự động tạo đội mới với bạn là đội trưởng
- Không cần vào màn hình quản lý đội

**Option 3 - Không cần đội (chỉ tên + SĐT):**
```json
{
  "isMatchRequest": true,
  "hostName": "Nguyễn Văn A",
  "contactPhone": "0987654321"
}
```
- Không cần tạo đội
- Hệ thống tạo team tạm thời với tên người chơi
- Phù hợp cho người chơi cá nhân

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Đặt sân thành công",
  "data": {
    "id": 1,
    "bookingCode": "BK20250315001",
    "customerId": 1,
    "customerName": "Nguyễn Văn A",
    "fieldId": 1,
    "fieldName": "Sân 1",
    "stadiumId": 1,
    "stadiumName": "Sân bóng ABC",
    "timeSlotId": 1,
    "startTime": "06:00",
    "endTime": "07:30",
    "bookingDate": "2025-03-15",
    "isMatchRequest": true,
    "totalPrice": 350000,
    "depositAmount": 105000,
    "remainingAmount": 245000,
    "depositStatus": "PENDING",
    "note": "Đặt cho team ABC",
    "status": "PENDING",
    "cancelledAt": null,
    "cancelReason": null,
    "recurringBookingId": null,
    "createdAt": "2025-03-10T10:30:00"
  }
}
```

**⭐ CÁCH CHIA PHÍ MỚI:**
- **WIN_LOSE** (mặc định): Đội thắng trả 70%, đội thua trả 30%
  - Tỷ lệ này được set mặc định khi tạo match request
  - Sau khi trận đấu kết thúc, hệ thống sẽ cập nhật lại dựa trên kết quả
- **EQUAL_SPLIT**: Chia đều 50/50
- **HOST_PAY**: Chủ nhà trả 100%
- **OPPONENT_PAY**: Đối thủ trả 100%
- **CUSTOM**: Tự định nghĩa tỷ lệ (hostSharePercent + opponentSharePercent = 100%)

**⭐ LƯU Ý:**
- Match request được tạo với status `OPEN`, cho phép các đội khác vào nhận kèo
- Match request tự động hết hạn **2 giờ trước** giờ đá
- Nếu không truyền `costSharing`, mặc định là `WIN_LOSE` (70/30)
- Tham khảo thêm **Match Making APIs** để biết cách quản lý và nhận kèo

---

### 7.3 Đặt sân cho Khách (không cần đăng nhập)
```
POST /api/v1/bookings/guest
```
**Auth:** Không yêu cầu (Public)

**Mô tả:** Cho phép khách chưa đăng nhập đặt sân bóng. Khách chỉ cần cung cấp tên, số điện thoại và email (optional). Khách **không thể** tạo trận ráp kèo (`isMatchRequest` phải là `false`).

**Request Body:**
```json
{
  "fieldId": 1,                        // required
  "timeSlotId": 1,                     // required
  "bookingDate": "2025-03-15",         // required (yyyy-MM-dd)
  "note": "Đặt sân cho buổi giao lưu", // optional
  "guestName": "Trần Văn B",          // required: Tên khách
  "guestPhone": "0912345678",         // required: SĐT khách
  "guestEmail": "guest@example.com"   // optional: Email khách
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Đặt sân thành công (Khách)",
  "data": {
    "id": 10,
    "bookingCode": "BK20250315A1B2C3",
    "customerId": null,
    "customerName": "Trần Văn B",
    "isGuestBooking": true,
    "guestName": "Trần Văn B",
    "guestPhone": "0912345678",
    "guestEmail": "guest@example.com",
    "fieldId": 1,
    "fieldName": "Sân 1",
    "stadiumId": 1,
    "stadiumName": "Sân bóng ABC",
    "timeSlotId": 1,
    "startTime": "06:00",
    "endTime": "07:30",
    "bookingDate": "2025-03-15",
    "isMatchRequest": false,
    "totalPrice": 350000,
    "depositAmount": 105000,
    "remainingAmount": 245000,
    "depositStatus": "PENDING",
    "note": "Đặt sân cho buổi giao lưu",
    "status": "PENDING",
    "cancelledAt": null,
    "cancelReason": null,
    "recurringBookingId": null,
    "createdAt": "2025-03-10T10:30:00"
  }
}
```

**⭐ LƯU Ý CHO BOOKING KHÁCH:**
- `guestName` và `guestPhone` là **bắt buộc**
- `guestEmail` là **optional**
- Khách **không thể** tạo trận ráp kèo (`isMatchRequest` sẽ bị từ chối)
- Booking khách có `customerId = null` và `isGuestBooking = true`
- Owner vẫn thấy booking khách trong danh sách quản lý và có thể xác nhận/từ chối bình thường

---

### 7.4 Tra cứu đơn đặt sân theo mã (không cần đăng nhập)
```
GET /api/v1/bookings/lookup?bookingCode=BK20250315A1B2C3
```
**Auth:** Không yêu cầu (Public)

**Mô tả:** Cho phép khách tra cứu thông tin đơn đặt sân bằng mã booking code. Phù hợp cho khách đặt sân không đăng nhập (guest) vì họ không có lịch sử đặt sân.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| bookingCode | String | ✅ | Mã đặt sân (VD: BK20250315A1B2C3) |

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 10,
    "bookingCode": "BK20250315A1B2C3",
    "customerId": null,
    "customerName": "Trần Văn B",
    "isGuestBooking": true,
    "guestName": "Trần Văn B",
    "guestPhone": "0912345678",
    "guestEmail": "guest@example.com",
    "fieldId": 1,
    "fieldName": "Sân 1",
    "stadiumId": 1,
    "stadiumName": "Sân bóng ABC",
    "timeSlotId": 1,
    "startTime": "06:00",
    "endTime": "07:30",
    "bookingDate": "2025-03-15",
    "isMatchRequest": false,
    "totalPrice": 350000,
    "depositAmount": 105000,
    "remainingAmount": 245000,
    "depositStatus": "PENDING",
    "note": "Đặt sân cho buổi giao lưu",
    "status": "PENDING",
    "cancelledAt": null,
    "cancelReason": null,
    "recurringBookingId": null,
    "createdAt": "2025-03-10T10:30:00"
  }
}
```

**Error Response:** `404 Not Found`
```json
{
  "success": false,
  "message": "Booking not found with bookingCode: BK20250315XXXXXX"
}
```

---

### 7.5 Lịch sử đặt sân của tôi
```
GET /api/v1/bookings/my
```
**Auth:** CUSTOMER

**Query Parameters:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `status` | BookingStatus | No | Lọc: `PENDING`, `DEPOSIT_PAID`, `CONFIRMED`, `CANCELLED`, `COMPLETED` |
| `page` | int | No | default: 0 |
| `size` | int | No | default: 10 |

**Response:** `200 OK` — `Page<BookingResponse>`

---

### 7.6 Chi tiết đơn đặt
```
GET /api/v1/bookings/{id}
```
**Auth:** Authenticated (Customer hoặc Owner đều xem được)

**Response:** `200 OK` — `BookingResponse`

---

### 7.7 Hủy đặt sân
```
PUT /api/v1/bookings/{id}/cancel
```
**Auth:** CUSTOMER

**Response:** `200 OK` — `BookingResponse` (status: `CANCELLED`)

> **Lưu ý:** Chỉ được hủy trước giờ đá ít nhất 2 tiếng. Hoàn cọc theo chính sách.

---

### 7.8 DS đơn đặt (Owner)
```
GET /api/v1/owner/bookings
```
**Auth:** OWNER

**Query Parameters:** Pagination (`page`, `size`, `sort`)

**Response:** `200 OK` — `Page<BookingResponse>`

---

### 7.9 Xác nhận đơn đặt
```
PUT /api/v1/owner/bookings/{id}/confirm
```
**Auth:** OWNER

**Response:** `200 OK` — `BookingResponse` (status: `CONFIRMED`)

---

### 7.10 Từ chối đơn đặt
```
PUT /api/v1/owner/bookings/{id}/reject
```
**Auth:** OWNER

**Response:** `200 OK` — `BookingResponse` (status: `CANCELLED`)

---

### 7.11 Hoàn thành đơn đặt
```
PUT /api/v1/owner/bookings/{id}/complete
```
**Auth:** OWNER

**Response:** `200 OK` — `BookingResponse` (status: `COMPLETED`)

---

### 7.12 DS đơn theo sân + ngày (Owner)
```
GET /api/v1/owner/stadiums/{stadiumId}/bookings?date=2025-03-15
```
**Auth:** OWNER

**Query Parameters:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `date` | LocalDate | **Yes** | Ngày cần xem (`yyyy-MM-dd`) |

**Response:** `200 OK` — `List<BookingResponse>`

---

## 8. Deposit APIs (Đặt cọc)

### 8.1 Xem chính sách đặt cọc
```
GET /api/v1/stadiums/{stadiumId}/deposit-policy
```
**Auth:** Public

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Thành công",
  "data": {
    "id": 1,
    "stadiumId": 1,
    "stadiumName": "Sân bóng ABC",
    "depositPercent": 30.00,
    "refundBeforeHours": 24,
    "refundPercent": 100.00,
    "lateCancelRefundPercent": 0.00,
    "recurringDiscountPercent": 10.00,
    "minRecurringSessions": 4,
    "isDepositRequired": true
  }
}
```

---

### 8.2 Cập nhật chính sách đặt cọc
```
PUT /api/v1/stadiums/{stadiumId}/deposit-policy
```
**Auth:** OWNER

**Request Body:**
```json
{
  "depositPercent": 30,                // required, 0-100
  "refundBeforeHours": 24,             // optional, default: 24
  "refundPercent": 100,                // optional, 0-100, default: 100
  "lateCancelRefundPercent": 0,        // optional, 0-100, default: 0
  "recurringDiscountPercent": 10,      // optional, 0-100, default: 0
  "minRecurringSessions": 4,           // optional, default: 4
  "isDepositRequired": true            // optional, default: true
}
```

**Response:** `200 OK` — `DepositPolicyResponse`

---

### 8.3 Tạo giao dịch đặt cọc
```
POST /api/v1/bookings/{bookingId}/deposits
```
**Auth:** CUSTOMER

**Request Body:**
```json
{
  "paymentMethod": "TRANSFER",         // required: CASH | TRANSFER | MOMO | ZALOPAY
  "transactionCode": "TXN123456",      // optional
  "note": "Đã chuyển khoản"            // optional
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Đặt cọc thành công",
  "data": {
    "id": 1,
    "bookingId": 1,
    "bookingCode": "BK20250315001",
    "amount": 105000,
    "depositType": "DEPOSIT",
    "paymentMethod": "TRANSFER",
    "transactionCode": "TXN123456",
    "note": "Đã chuyển khoản",
    "confirmedById": null,
    "confirmedByName": null,
    "confirmedAt": null,
    "status": "PENDING",
    "createdAt": "2025-03-10T10:35:00"
  }
}
```

> **Lưu ý:** Nếu sau 30 phút không đặt cọc, booking tự động bị hủy.

---

### 8.4 Xác nhận đã nhận cọc
```
PUT /api/v1/owner/deposits/{id}/confirm
```
**Auth:** OWNER / ADMIN

**Response:** `200 OK` — `DepositResponse` (status: `CONFIRMED`)

---

### 8.5 Từ chối giao dịch cọc
```
PUT /api/v1/owner/deposits/{id}/reject
```
**Auth:** OWNER / ADMIN

**Response:** `200 OK` — `DepositResponse` (status: `REJECTED`)

---

### 8.6 Lịch sử cọc của booking
```
GET /api/v1/bookings/{bookingId}/deposits
```
**Auth:** Authenticated

**Response:** `200 OK` — `List<DepositResponse>`

---

### 8.7 Hoàn cọc khi hủy
```
POST /api/v1/owner/bookings/{bookingId}/refund
```
**Auth:** OWNER / ADMIN

**Request Body:** (optional)
```json
{
  "paymentMethod": "TRANSFER",         // optional
  "note": "Hoàn cọc do hủy sân"        // optional
}
```

**Response:** `200 OK` — `DepositResponse` (depositType: `REFUND`)

---

## 9. Recurring Booking APIs (Đặt sân dài hạn)

### 9.1 Tạo gói đặt sân dài hạn
```
POST /api/v1/recurring-bookings
```
**Auth:** CUSTOMER

**Request Body:**
```json
{
  "fieldId": 1,                        // required
  "timeSlotId": 1,                     // required
  "recurrenceType": "WEEKLY",          // required: WEEKLY | MONTHLY
  "startDate": "2025-03-15",           // required
  "endDate": "2025-06-15",             // required
  "note": "Đặt sân hàng tuần"          // optional
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Tạo gói đặt sân dài hạn thành công",
  "data": {
    "id": 1,
    "recurringCode": "RC20250315001",
    "customerId": 1,
    "customerName": "Nguyễn Văn A",
    "fieldId": 1,
    "fieldName": "Sân 1",
    "stadiumId": 1,
    "stadiumName": "Sân bóng ABC",
    "timeSlotId": 1,
    "timeSlotRange": "06:00-07:30",
    "recurrenceType": "WEEKLY",
    "dayOfWeek": 6,
    "startDate": "2025-03-15",
    "endDate": "2025-06-15",
    "totalSessions": 14,
    "completedSessions": 0,
    "cancelledSessions": 0,
    "discountPercent": 10.00,
    "originalPricePerSession": 350000,
    "discountedPricePerSession": 315000,
    "totalPrice": 4410000,
    "totalDeposit": 1323000,
    "depositStatus": "PENDING",
    "status": "ACTIVE",
    "note": "Đặt sân hàng tuần",
    "createdAt": "2025-03-10T10:30:00",
    "bookings": [ ... ]
  }
}
```

---

### 9.2 DS gói dài hạn của tôi
```
GET /api/v1/recurring-bookings/my
```
**Auth:** CUSTOMER

**Query Parameters:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `status` | RecurringBookingStatus | No | Lọc: `ACTIVE`, `CANCELLED`, `COMPLETED`, `EXPIRED` |
| `page` | int | No | default: 0 |
| `size` | int | No | default: 10 |
| `sort` | String | No | default: `createdAt,desc` |

**Response:** `200 OK` — `Page<RecurringBookingResponse>`

---

### 9.3 Chi tiết gói dài hạn
```
GET /api/v1/recurring-bookings/{id}
```
**Auth:** Authenticated

**Response:** `200 OK` — `RecurringBookingResponse` (kèm danh sách bookings con)

---

### 9.4 Hủy toàn bộ gói
```
PUT /api/v1/recurring-bookings/{id}/cancel
```
**Auth:** CUSTOMER

**Response:** `200 OK` — `RecurringBookingResponse` (status: `CANCELLED`)

> **Lưu ý:** Các buổi đã COMPLETED giữ nguyên. Các buổi chưa diễn ra sẽ bị hủy + hoàn cọc theo chính sách.

---

### 9.5 Hủy 1 buổi trong gói
```
PUT /api/v1/recurring-bookings/{id}/bookings/{bookingId}/cancel
```
**Auth:** CUSTOMER

**Response:** `200 OK` — `RecurringBookingResponse`

---

### 9.6 DS gói dài hạn (Owner)
```
GET /api/v1/owner/recurring-bookings
```
**Auth:** OWNER / ADMIN

**Query Parameters:** Pagination (`page`, `size`, `sort`)

**Response:** `200 OK` — `Page<RecurringBookingResponse>`

---

### 9.7 Xác nhận gói dài hạn
```
PUT /api/v1/owner/recurring-bookings/{id}/confirm
```
**Auth:** OWNER / ADMIN

**Response:** `200 OK` — `RecurringBookingResponse`

---

## 10. Team APIs (Đội bóng)

### 10.1 Tạo đội bóng
```
POST /api/v1/teams
```
**Auth:** CUSTOMER / OWNER

**Request Body:**
```json
{
  "name": "FC Thunder",                // required, max 100 ký tự
  "phone": "0901234567",               // required, max 20 ký tự
  "logoUrl": "https://...",            // optional
  "description": "Đội bóng vui vẻ",    // optional
  "preferredFieldType": "FIVE_A_SIDE", // optional: FIVE_A_SIDE | SEVEN_A_SIDE | ELEVEN_A_SIDE
  "skillLevel": "INTERMEDIATE",        // optional: ANY | BEGINNER | INTERMEDIATE | ADVANCED | PRO
  "city": "TP.HCM",                   // optional, max 100
  "district": "Quận 1"                // optional, max 100
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Tạo đội thành công",
  "data": {
    "id": 1,
    "name": "FC Thunder",
    "phone": "0901234567",
    "logoUrl": "https://...",
    "description": "Đội bóng vui vẻ",
    "preferredFieldType": "FIVE_A_SIDE",
    "skillLevel": "INTERMEDIATE",
    "captainId": 1,
    "captainName": "Nguyễn Văn A",
    "memberCount": 1,
    "city": "TP.HCM",
    "district": "Quận 1",
    "isActive": true,
    "createdAt": "2025-03-10T10:30:00",
    "members": [
      {
        "id": 1,
        "teamId": 1,
        "teamName": "FC Thunder",
        "name": "Nguyễn Văn A",
        "phone": "0901234567",
        "userId": 1,
        "userName": "Nguyễn Văn A",
        "userEmail": "user@example.com",
        "role": "CAPTAIN",
        "status": "ACTIVE",
        "joinedAt": "2025-03-10T10:30:00",
        "createdAt": "2025-03-10T10:30:00"
      }
    ]
  }
}
```

---

### 10.2 DS đội của tôi
```
GET /api/v1/teams/my
```
**Auth:** Authenticated

**Response:** `200 OK` — `List<TeamResponse>`

---

### 10.3 Chi tiết đội (kèm DS thành viên)
```
GET /api/v1/teams/{id}
```
**Auth:** Authenticated

**Response:** `200 OK` — `TeamResponse`

---

### 10.4 Cập nhật thông tin đội
```
PUT /api/v1/teams/{id}
```
**Auth:** Authenticated (chỉ Captain)

**Request Body:** Cùng `TeamRequest` (10.1)

**Response:** `200 OK` — `TeamResponse`

---

### 10.5 Giải tán đội
```
DELETE /api/v1/teams/{id}
```
**Auth:** Authenticated (chỉ Captain)

**Response:** `200 OK` — `ApiResponse<Void>`

---

### 10.6 Thêm thành viên
```
POST /api/v1/teams/{id}/members
```
**Auth:** Authenticated (chỉ Captain)

**Request Body:**
```json
{
  "name": "Nguyễn Văn B",              // required, max 100
  "phone": "0908889999"                // optional, max 20
}
```

**Response:** `201 Created` — `TeamMemberResponse` (status: `ACTIVE`)

---

### 10.7 Xóa thành viên
```
PUT /api/v1/teams/{id}/members/{memberId}/remove
```
**Auth:** Authenticated (chỉ Captain)

**Response:** `200 OK` — `ApiResponse<Void>`

---

### 10.8 Chuyển quyền đội trưởng
```
PUT /api/v1/teams/{id}/members/{memberId}/captain
```
**Auth:** Authenticated (chỉ Captain hiện tại)

**Response:** `200 OK` — `TeamResponse`

---

### 10.9 Rời đội
```
PUT /api/v1/teams/{id}/leave
```
**Auth:** Authenticated (member, không phải captain)

**Response:** `200 OK` — `ApiResponse<Void>`

---

### 10.10 Chấp nhận lời mời vào đội
```
PUT /api/v1/team-invites/{id}/accept
```
**Auth:** Authenticated (người được mời)

**Response:** `200 OK` — `TeamMemberResponse` (status: `ACTIVE`)

---

### 10.11 Từ chối lời mời vào đội
```
PUT /api/v1/team-invites/{id}/reject
```
**Auth:** Authenticated (người được mời)

**Response:** `200 OK` — `TeamMemberResponse`

---

## 11. Match Making APIs (Ráp kèo)

> **⭐ Lưu ý:** Bạn có thể tạo match request trực tiếp khi đặt sân bằng cách set `isMatchRequest = true` trong API `POST /api/v1/bookings`. Xem thêm tại **Section 7.2**.

### 11.1 Tạo kèo ráp đối
```
POST /api/v1/match-requests
```
**Auth:** Authenticated

**Request Body:**
```json
{
  "bookingId": 1,                      // required — booking đã CONFIRMED/DEPOSIT_PAID
  "teamId": 1,                        // required — đội của bạn
  "requiredSkillLevel": "INTERMEDIATE",// optional: ANY | BEGINNER | INTERMEDIATE | ADVANCED | PRO
  "costSharing": "WIN_LOSE",          // optional: WIN_LOSE (70/30 mặc định) | EQUAL_SPLIT | HOST_PAY | OPPONENT_PAY | CUSTOM
  "hostSharePercent": 70,             // optional (dùng khi CUSTOM)
  "opponentSharePercent": 30,         // optional (dùng khi CUSTOM)
  "message": "Tìm đối 5 người",       // optional
  "contactPhone": "0901234567"         // optional
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Tạo kèo thành công",
  "data": {
    "id": 1,
    "matchCode": "MR1A2B3C4D",
    "bookingId": 1,
    "bookingCode": "BK20250315001",
    "bookingDate": "2025-03-15",
    "startTime": "06:00",
    "endTime": "07:30",
    "stadiumId": 1,
    "stadiumName": "Sân bóng ABC",
    "stadiumAddress": "123 Nguyễn Huệ",
    "fieldId": 1,
    "fieldName": "Sân 1",
    "hostTeamId": 1,
    "hostTeamName": "FC Thunder",
    "hostTeamLogoUrl": "https://...",
    "opponentTeamId": null,
    "opponentTeamName": null,
    "opponentTeamLogoUrl": null,
    "fieldType": "FIVE_A_SIDE",
    "requiredSkillLevel": "INTERMEDIATE",
    "costSharing": "WIN_LOSE",
    "hostSharePercent": 70,
    "opponentSharePercent": 30,
    "totalPrice": 350000,
    "opponentAmount": 105000,
    "message": "Tìm đối 5 người",
    "contactPhone": "0901234567",
    "status": "OPEN",
    "acceptedAt": null,
    "expiredAt": "2025-03-15T02:00:00",
    "createdAt": "2025-03-10T10:30:00",
    "responseCount": 0,
    "responses": []
  }
}
```

---

### 11.2 DS kèo đang mở (Public, filter)
```
GET /api/v1/match-requests
```
**Auth:** Public / Authenticated

**Query Parameters:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `fieldType` | FieldType | No | `FIVE_A_SIDE`, `SEVEN_A_SIDE`, `ELEVEN_A_SIDE` |
| `skillLevel` | SkillLevel | No | `ANY`, `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `PRO` |
| `excludeUserId` | Long | No | Loại trừ kèo do user này tạo |
| `page` | int | No | default: 0 |
| `size` | int | No | default: 10 |
| `sort` | String | No | default: `createdAt,desc` |

**Response:** `200 OK` — `Page<MatchRequestResponse>`

---

### 11.3 DS kèo tôi đã tạo
```
GET /api/v1/match-requests/my
```
**Auth:** Authenticated

**Response:** `200 OK` — `List<MatchRequestResponse>`

---

### 11.4 DS kèo tôi đã nhận
```
GET /api/v1/match-requests/my-matches
```
**Auth:** Authenticated

**Response:** `200 OK` — `List<MatchRequestResponse>`

---

### 11.5 Chi tiết kèo
```
GET /api/v1/match-requests/{id}
```
**Auth:** Public / Authenticated

**Response:** `200 OK` — `MatchRequestResponse` (kèm responses nếu là host)

---

### 11.6 Hủy kèo
```
PUT /api/v1/match-requests/{id}/cancel
```
**Auth:** Authenticated (chỉ host captain)

**Response:** `200 OK` — `MatchRequestResponse` (status: `CANCELLED`)

---

### 11.7 Gửi yêu cầu nhận kèo
```
POST /api/v1/match-requests/{id}/responses
```
**Auth:** Authenticated

**Request Body:**
```json
{
  "joinType": "TEAM",                 // optional: TEAM | INDIVIDUAL (default: TEAM nếu có teamId, ngược lại INDIVIDUAL)
  "teamId": 2,                        // required khi joinType=TEAM
  "contactPhone": "0901234567",       // optional; với INDIVIDUAL nếu bỏ trống sẽ lấy từ user.phone
  "message": "Đội chúng tôi muốn nhận kèo" // optional
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Gửi yêu cầu nhận kèo thành công",
  "data": {
    "id": 1,
    "matchRequestId": 1,
    "teamId": 2,
    "teamName": "FC Storm",
    "teamLogoUrl": "https://...",
    "responderUserId": 5,
    "responderUserName": "Nguyễn Văn B",
    "joinType": "TEAM",
    "contactPhone": "0901234567",
    "message": "Đội chúng tôi muốn nhận kèo",
    "status": "PENDING",
    "respondedAt": "2025-03-10T11:00:00",
    "createdAt": "2025-03-10T11:00:00"
  }
}
```

---

### 11.8 Chấp nhận đội nhận kèo
```
PUT /api/v1/match-requests/{id}/responses/{responseId}/accept
```
**Auth:** Authenticated (chỉ host captain)

**Response:** `200 OK` — `MatchRequestResponse` (status: `ACCEPTED`, có opponentTeam)

> **Lưu ý:** Khi accept 1 đội, tất cả đội khác tự động bị REJECTED.

---

### 11.9 Từ chối đội nhận kèo
```
PUT /api/v1/match-requests/{id}/responses/{responseId}/reject
```
**Auth:** Authenticated (chỉ host captain)

**Response:** `200 OK` — `MatchResponseResponse` (status: `REJECTED`)

---

### 11.10 Rút yêu cầu nhận kèo
```
PUT /api/v1/match-requests/{id}/responses/{responseId}/withdraw
```
**Auth:** Authenticated (chỉ captain nếu nhận theo TEAM, hoặc chính user gửi nếu nhận theo INDIVIDUAL)

**Response:** `200 OK` — `MatchResponseResponse` (status: `WITHDRAWN`)

---

## 12. Review APIs (Đánh giá)

### 12.1 Đánh giá sân
```
POST /api/v1/reviews
```
**Auth:** CUSTOMER / OWNER

**Request Body:**
```json
{
  "bookingId": 1,                      // required — booking đã COMPLETED
  "rating": 5,                        // required, 1-5
  "comment": "Sân đẹp, cỏ tốt"        // optional
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Đánh giá thành công",
  "data": {
    "id": 1,
    "bookingId": 1,
    "bookingCode": "BK20250315001",
    "customerId": 1,
    "customerName": "Nguyễn Văn A",
    "customerAvatarUrl": null,
    "stadiumId": 1,
    "stadiumName": "Sân bóng ABC",
    "rating": 5,
    "comment": "Sân đẹp, cỏ tốt",
    "createdAt": "2025-03-15T22:00:00"
  }
}
```

---

### 12.2 DS đánh giá của sân
```
GET /api/v1/stadiums/{stadiumId}/reviews
```
**Auth:** Public

**Query Parameters:** Pagination (`page`, `size`, `sort` — default: `createdAt,desc`)

**Response:** `200 OK` — `Page<ReviewResponse>`

---

### 12.3 DS đánh giá của tôi
```
GET /api/v1/reviews/my
```
**Auth:** Authenticated

**Query Parameters:** Pagination (`page`, `size`, `sort` — default: `createdAt,desc`)

**Response:** `200 OK` — `Page<ReviewResponse>`

---

### 12.4 Xóa đánh giá
```
DELETE /api/v1/reviews/{id}
```
**Auth:** Authenticated (chỉ người đã đánh giá)

**Response:** `200 OK` — `ApiResponse<Void>`

---

## 13. Admin APIs

### 13.1 Danh sách users
```
GET /api/v1/admin/users
```
**Auth:** ADMIN

**Query Parameters:**
| Param | Type | Required | Mô tả |
|-------|------|----------|-------|
| `role` | Role | No | `CUSTOMER`, `OWNER`, `ADMIN` |
| `search` | String | No | Tìm theo email hoặc tên |
| `page` | int | No | default: 0 |
| `size` | int | No | default: 10 |
| `sort` | String | No | default: `createdAt,desc` |

**Response:** `200 OK` — `Page<UserResponse>`

---

### 13.2 Chi tiết user
```
GET /api/v1/admin/users/{id}
```
**Auth:** ADMIN

**Response:** `200 OK` — `UserResponse`

---

### 13.3 Bật/tắt user
```
PUT /api/v1/admin/users/{id}/toggle-active
```
**Auth:** ADMIN

**Response:** `200 OK` — `UserResponse` (isActive toggled)

---

### 13.4 Dashboard thống kê
```
GET /api/v1/admin/dashboard
```
**Auth:** ADMIN

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Thành công",
  "data": {
    "totalUsers": 150,
    "totalCustomers": 120,
    "totalOwners": 25,
    "totalStadiums": 40,
    "approvedStadiums": 35,
    "pendingStadiums": 5,
    "totalBookings": 500,
    "completedBookings": 380,
    "cancelledBookings": 45,
    "totalTeams": 30,
    "totalMatchRequests": 80,
    "totalReviews": 200,
    "averageRating": 4.3,
    "recentBookings": {
      "2025-03-10": 15,
      "2025-03-09": 12,
      "2025-03-08": 18
    }
  }
}
```

> **Lưu ý:** Các API duyệt sân (pending/approve/reject) nằm ở mục [4.8-4.10](#48-ds-sân-chờ-duyệt-admin)

---

## 14. Health API

### 14.1 Health Check
```
GET /api/v1/health
```
**Auth:** Public

**Response:** `200 OK`
```json
{
  "status": "UP",
  "mysql": {
    "status": "UP",
    "database": "booking_stadium"
  },
  "redis": {
    "status": "UP",
    "ping": "PONG",
    "read_write": "ok"
  }
}
```

---

## 15. Enums Reference

### Role
| Value | Mô tả |
|-------|--------|
| `CUSTOMER` | Khách hàng đặt sân |
| `OWNER` | Chủ sân |
| `ADMIN` | Quản trị viên |

### FieldType
| Value | Mô tả |
|-------|--------|
| `FIVE_A_SIDE` | Sân 5 người |
| `SEVEN_A_SIDE` | Sân 7 người |
| `ELEVEN_A_SIDE` | Sân 11 người |

### StadiumStatus
| Value | Mô tả |
|-------|--------|
| `PENDING` | Chờ duyệt |
| `APPROVED` | Đã duyệt |
| `REJECTED` | Bị từ chối |
| `INACTIVE` | Ngừng hoạt động |

### BookingStatus
| Value | Mô tả |
|-------|--------|
| `PENDING` | Chờ đặt cọc |
| `DEPOSIT_PAID` | Đã đặt cọc |
| `CONFIRMED` | Đã xác nhận |
| `CANCELLED` | Đã hủy |
| `COMPLETED` | Hoàn thành |

### DepositStatus
| Value | Mô tả |
|-------|--------|
| `NOT_REQUIRED` | Không yêu cầu cọc |
| `PENDING` | Chờ đặt cọc |
| `PAID` | Đã đặt cọc |
| `REFUNDED` | Đã hoàn cọc |

### DepositType
| Value | Mô tả |
|-------|--------|
| `DEPOSIT` | Giao dịch đặt cọc |
| `REFUND` | Giao dịch hoàn cọc |

### DepositTransactionStatus
| Value | Mô tả |
|-------|--------|
| `PENDING` | Chờ xác nhận |
| `CONFIRMED` | Đã xác nhận |
| `REJECTED` | Bị từ chối |

### PaymentMethod
| Value | Mô tả |
|-------|--------|
| `CASH` | Tiền mặt |
| `TRANSFER` | Chuyển khoản ngân hàng |
| `MOMO` | Ví MoMo |
| `ZALOPAY` | ZaloPay |

### RecurrenceType
| Value | Mô tả |
|-------|--------|
| `WEEKLY` | Lặp hàng tuần |
| `MONTHLY` | Lặp hàng tháng |

### RecurringBookingStatus
| Value | Mô tả |
|-------|--------|
| `ACTIVE` | Đang hoạt động |
| `CANCELLED` | Đã hủy |
| `COMPLETED` | Hoàn thành |
| `EXPIRED` | Hết hạn |

### RecurringDepositStatus
| Value | Mô tả |
|-------|--------|
| `PENDING` | Chờ đặt cọc |
| `PAID` | Đã đặt cọc |
| `PARTIALLY_REFUNDED` | Hoàn một phần |
| `REFUNDED` | Đã hoàn toàn bộ |

### SkillLevel
| Value | Mô tả |
|-------|--------|
| `ANY` | Bất kỳ |
| `BEGINNER` | Mới chơi |
| `INTERMEDIATE` | Trung bình |
| `ADVANCED` | Nâng cao |
| `PRO` | Chuyên nghiệp |

### CostSharing
| Value | Mô tả |
|-------|--------|
| `WIN_LOSE` | Đội thắng 70%, đội thua 30% (Mặc định) |
| `EQUAL_SPLIT` | Chia đều 50/50 |
| `HOST_PAY` | Chủ kèo trả hết |
| `OPPONENT_PAY` | Đội khách trả hết |
| `CUSTOM` | Tùy chỉnh % |

### MatchStatus
| Value | Mô tả |
|-------|--------|
| `OPEN` | Đang mở, chờ đối thủ |
| `ACCEPTED` | Đã có đối thủ |
| `CANCELLED` | Đã hủy |
| `EXPIRED` | Hết hạn |
| `COMPLETED` | Hoàn thành |

### MatchResponseStatus
| Value | Mô tả |
|-------|--------|
| `PENDING` | Chờ xét duyệt |
| `ACCEPTED` | Được chấp nhận |
| `REJECTED` | Bị từ chối |
| `WITHDRAWN` | Đã rút |

### TeamMemberRole
| Value | Mô tả |
|-------|--------|
| `CAPTAIN` | Đội trưởng |
| `MEMBER` | Thành viên |

### TeamMemberStatus
| Value | Mô tả |
|-------|--------|
| `PENDING` | Chờ xác nhận lời mời |
| `ACTIVE` | Đã vào đội |
| `LEFT` | Đã rời đội |
| `KICKED` | Bị đuổi khỏi đội |

---

## 16. DTOs Reference

### Request DTOs

| DTO | Dùng cho | Fields |
|-----|----------|--------|
| **RegisterRequest** | `POST /auth/register` | `email`\*, `password`\* (min 6), `fullName`\*, `phone`, `role` |
| **LoginRequest** | `POST /auth/login` | `email`\*, `password`\* (min 6) |
| **SocialLoginRequest** | `POST /auth/social-login` | `email`\*, `fullName`\*, `avatarUrl`, `phone` |
| **RefreshTokenRequest** | `POST /auth/refresh-token`, `POST /auth/logout` | `refreshToken`\* |
| **StadiumRequest** | `POST /stadiums`, `PUT /stadiums/{id}` | `name`\*, `address`\*, `district`, `city`, `description`, `imageUrl`, `latitude`, `longitude`, `openTime`, `closeTime` |
| **FieldRequest** | `POST /stadiums/{id}/fields`, `PUT /fields/{id}` | `name`\*, `fieldType`\* (FieldType), `defaultPrice`\* (>0), `imageUrl`, `parentFieldId` ⭐ |
| **TimeSlotRequest** | `POST /fields/{id}/time-slots`, `PUT /time-slots/{id}` | `startTime`\* (HH:mm), `endTime`\* (HH:mm), `price`\* (>0) |
| **BookingRequest** | `POST /bookings` | `fieldId`\*, `timeSlotId`\*, `bookingDate`\* (yyyy-MM-dd), `note`, `isMatchRequest` (default: false) |
| **DepositRequest** | `POST /bookings/{id}/deposits` | `paymentMethod`\* (PaymentMethod), `transactionCode`, `note` |
| **RefundRequest** | `POST /owner/bookings/{id}/refund` | `paymentMethod` (PaymentMethod), `note` |
| **DepositPolicyRequest** | `PUT /stadiums/{id}/deposit-policy` | `depositPercent`\* (0-100), `refundBeforeHours` (default 24), `refundPercent` (0-100), `lateCancelRefundPercent` (0-100), `recurringDiscountPercent` (0-100), `minRecurringSessions` (default 4), `isDepositRequired` (default true) |
| **RecurringBookingRequest** | `POST /recurring-bookings` | `fieldId`\*, `timeSlotId`\*, `recurrenceType`\* (RecurrenceType), `startDate`\* (yyyy-MM-dd), `endDate`\* (yyyy-MM-dd), `note` |
| **TeamRequest** | `POST /teams`, `PUT /teams/{id}` | `name`\* (max 100), `phone`\* (max 20), `logoUrl`, `description`, `preferredFieldType` (FieldType), `skillLevel` (SkillLevel), `city` (max 100), `district` (max 100) |
| **AddMemberRequest** | `POST /teams/{id}/members` | `name`\* (max 100), `phone` (max 20) |
| **MatchRequestRequest** | `POST /match-requests` | `bookingId`\*, `teamId`\*, `requiredSkillLevel` (SkillLevel), `costSharing` (CostSharing), `hostSharePercent`, `opponentSharePercent`, `message`, `contactPhone` |
| **MatchResponseRequest** | `POST /match-requests/{id}/responses` | `joinType` (MatchJoinType: TEAM/INDIVIDUAL), `teamId` (required khi TEAM), `contactPhone` (required khi INDIVIDUAL nếu user.phone trống), `message` |
| **ReviewRequest** | `POST /reviews` | `bookingId`\*, `rating`\* (1-5), `comment` |

> \* = required field

### Response DTOs

| DTO | Fields |
|-----|--------|
| **UserResponse** | `id`, `email`, `fullName`, `phone`, `avatarUrl`, `role`, `authProvider` (LOCAL/SOCIAL), `isActive` |
| **JwtResponse** | `accessToken`, `refreshToken`, `tokenType` ("Bearer"), `user` (UserResponse) |
| **StadiumResponse** | `id`, `ownerId`, `ownerName`, `name`, `address`, `district`, `city`, `description`, `imageUrl`, `latitude`, `longitude`, `openTime`, `closeTime`, `status`, `avgRating`, `reviewCount`, `fieldCount` |
| **FieldResponse** | `id`, `stadiumId`, `stadiumName`, `name`, `imageUrl`, `fieldType`, `defaultPrice`, `isActive`, `parentFieldId` ⭐, `childFieldCount` ⭐ |
| **TimeSlotResponse** | `id`, `fieldId`, `fieldName`, `startTime`, `endTime`, `price`, `isActive` |
| **AvailableSlotResponse** | `timeSlotId`, `startTime`, `endTime`, `price`, `isAvailable` (⭐ tự động check grouped fields) |
| **BookingResponse** | `id`, `bookingCode`, `customerId`, `customerName`, `fieldId`, `fieldName`, `stadiumId`, `stadiumName`, `timeSlotId`, `startTime`, `endTime`, `bookingDate`, `isMatchRequest`, `totalPrice`, `depositAmount`, `remainingAmount`, `depositStatus`, `note`, `status`, `cancelledAt`, `cancelReason`, `recurringBookingId`, `createdAt` |
| **DepositResponse** | `id`, `bookingId`, `bookingCode`, `amount`, `depositType`, `paymentMethod`, `transactionCode`, `note`, `confirmedById`, `confirmedByName`, `confirmedAt`, `status`, `createdAt` |
| **DepositPolicyResponse** | `id`, `stadiumId`, `stadiumName`, `depositPercent`, `refundBeforeHours`, `refundPercent`, `lateCancelRefundPercent`, `recurringDiscountPercent`, `minRecurringSessions`, `isDepositRequired` |
| **RecurringBookingResponse** | `id`, `recurringCode`, `customerId`, `customerName`, `fieldId`, `fieldName`, `stadiumId`, `stadiumName`, `timeSlotId`, `timeSlotRange`, `recurrenceType`, `dayOfWeek`, `startDate`, `endDate`, `totalSessions`, `completedSessions`, `cancelledSessions`, `discountPercent`, `originalPricePerSession`, `discountedPricePerSession`, `totalPrice`, `totalDeposit`, `depositStatus`, `status`, `note`, `createdAt`, `bookings` (List\<BookingResponse\>) |
| **TeamResponse** | `id`, `name`, `phone`, `logoUrl`, `description`, `preferredFieldType`, `skillLevel`, `captainId`, `captainName`, `memberCount`, `city`, `district`, `isActive`, `createdAt`, `members` (List\<TeamMemberResponse\>) |
| **TeamMemberResponse** | `id`, `teamId`, `teamName`, `name`, `phone`, `userId`, `userName`, `userEmail`, `role`, `status`, `joinedAt`, `createdAt` |
| **MatchRequestResponse** | `id`, `matchCode`, `bookingId`, `bookingCode`, `bookingDate`, `startTime`, `endTime`, `stadiumId`, `stadiumName`, `stadiumAddress`, `fieldId`, `fieldName`, `hostTeamId`, `hostTeamName`, `hostTeamLogoUrl`, `opponentTeamId`, `opponentTeamName`, `opponentTeamLogoUrl`, `fieldType`, `requiredSkillLevel`, `costSharing`, `hostSharePercent`, `opponentSharePercent`, `totalPrice`, `opponentAmount`, `message`, `contactPhone`, `status`, `acceptedAt`, `expiredAt`, `createdAt`, `responseCount`, `responses` (List\<MatchResponseResponse\>) |
| **MatchResponseResponse** | `id`, `matchRequestId`, `teamId`, `teamName`, `teamLogoUrl`, `responderUserId`, `responderUserName`, `joinType`, `contactPhone`, `message`, `status`, `respondedAt`, `createdAt` |
| **ReviewResponse** | `id`, `bookingId`, `bookingCode`, `customerId`, `customerName`, `customerAvatarUrl`, `stadiumId`, `stadiumName`, `rating`, `comment`, `createdAt` |
| **DashboardResponse** | `totalUsers`, `totalCustomers`, `totalOwners`, `totalStadiums`, `approvedStadiums`, `pendingStadiums`, `totalBookings`, `completedBookings`, `cancelledBookings`, `totalTeams`, `totalMatchRequests`, `totalReviews`, `averageRating`, `recentBookings` (Map\<String, Long\>) |
| **ApiResponse\<T\>** | `success` (boolean), `message` (String), `data` (T) |

---

## 17. Error Handling

### HTTP Status Codes

| Status | Ý nghĩa |
|--------|---------|
| `200` | Thành công |
| `201` | Tạo mới thành công |
| `400` | Dữ liệu không hợp lệ / Lỗi business logic |
| `401` | Chưa đăng nhập / Token hết hạn |
| `403` | Không có quyền truy cập |
| `404` | Không tìm thấy tài nguyên |
| `405` | Method not allowed |
| `409` | Xung đột dữ liệu (duplicate) |
| `500` | Lỗi server |

### Error Response Examples

**401 - Chưa đăng nhập:**
```json
{
  "success": false,
  "message": "Token không hợp lệ hoặc đã hết hạn",
  "data": null
}
```

**403 - Không có quyền:**
```json
{
  "success": false,
  "message": "Bạn không có quyền truy cập tài nguyên này",
  "data": null
}
```

**404 - Không tìm thấy:**
```json
{
  "success": false,
  "message": "Stadium not found with id: 999",
  "data": null
}
```

**400 - Validation error:**
```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "email": "Email không hợp lệ",
    "password": "size must be between 6 and 2147483647",
    "fullName": "must not be blank"
  }
}
```

**400 - Business logic error:**
```json
{
  "success": false,
  "message": "Khung giờ đã được đặt vào ngày này",
  "data": null
}
```

---

## 18. Pagination

Các API trả về `Page<T>` hỗ trợ các query params:

| Param | Type | Default | Mô tả |
|-------|------|---------|-------|
| `page` | int | 0 | Số trang (bắt đầu từ 0) |
| `size` | int | 10 | Số item mỗi trang |
| `sort` | String | varies | Cột + hướng sắp xếp. VD: `createdAt,desc`, `name,asc` |

### Page Response Structure

```json
{
  "content": [ ... ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": { "sorted": true, "direction": "DESC" }
  },
  "totalPages": 5,
  "totalElements": 48,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false,
  "numberOfElements": 10,
  "empty": false
}
```

---

## 19. Test Accounts

| Email | Password | Role | Mô tả |
|-------|----------|------|--------|
| `test@example.com` | `123456` | CUSTOMER | Khách hàng test |
| `customer@example.com` | `123456` | CUSTOMER | Khách hàng 2 |
| `owner@example.com` | `123456` | OWNER | Chủ sân test |
| `owner2@example.com` | `123456` | OWNER | Chủ sân 2 |
| `admin@example.com` | `123456` | ADMIN | Admin |

### Quick Login Test

```bash
# Đăng nhập
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "123456"}'

# Sử dụng token
curl http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <accessToken>"
```

---

## Tổng Kết Endpoints

| Module | Số endpoints | Auth |
|--------|-------------|------|
| Health | 1 | Public |
| Auth | 6 | Public / Authenticated |
| Stadium | 10 | Public / OWNER / ADMIN |
| Field | 4 | Public / OWNER |
| TimeSlot | 4 | Public / OWNER |
| Booking | 10 | Public / CUSTOMER / OWNER |
| Deposit | 7 | CUSTOMER / OWNER / ADMIN |
| Recurring Booking | 7 | CUSTOMER / OWNER / ADMIN |
| Team | 11 | Authenticated |
| Match Making | 10 | Public / Authenticated |
| Review | 4 | Public / Authenticated |
| Admin | 4 | ADMIN |
| **Tổng** | **78** | |
