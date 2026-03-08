# 📚 Booking Stadium - Documentation Index

> **Hệ thống đặt sân bóng đá online**  
> **Backend:** Spring Boot 3.2.3, Java 17  
> **Last Updated:** 08/03/2026

---

## 🚀 Quick Start Cho FE Team

### 1. Đọc Bản Tóm Tắt Cập Nhật Mới Nhất

**👉 [UPDATE_SUMMARY_FOR_FE.md](./UPDATE_SUMMARY_FOR_FE.md)** ⭐ **ĐỌC ĐẦU TIÊN**

- Tóm tắt thay đổi API mới nhất
- So sánh BEFORE vs AFTER
- Checklist implement cho FE
- FAQs và test cases nhanh

---

## 📖 Tài Liệu Chính

### 1. API Documentation

**👉 [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)**

- **79 endpoints** đầy đủ
- Request/Response examples
- Authentication & JWT
- Error handling
- Pagination
- DTOs reference
- Test accounts

**Dùng khi:**
- Cần xem chi tiết endpoint
- Cần biết request/response format
- Cần xem validation rules
- Cần test accounts

---

### 2. System Design

**👉 [SYSTEM_DESIGN.md](./SYSTEM_DESIGN.md)**

- Tech stack
- Database schema (ERD)
- Business logic tổng quan
- Features overview
- Actors & roles

**Dùng khi:**
- Cần hiểu architecture tổng thể
- Cần xem database structure
- Cần hiểu business requirements

---

### 3. Grouped Fields Guide (Sân Ghép) ⭐ MỚI

**👉 [GROUPED_FIELDS_GUIDE.md](./GROUPED_FIELDS_GUIDE.md)**

- Business requirement chi tiết
- BEFORE vs AFTER comparisons
- UI/UX suggestions
- Code examples (TypeScript)
- Test cases đầy đủ
- FAQs

**Dùng khi:**
- Implement tính năng sân ghép
- Hiển thị parent-child fields
- Handle booking conflicts
- Cần examples cụ thể

---

### 4. Image Upload Guide ⭐ MỚI

**👉 [IMAGE_UPLOAD_GUIDE.md](./IMAGE_UPLOAD_GUIDE.md)**

- Upload flow (temp → permanent)
- API usage examples
- Static resource URL
- Validation rules (size, format)
- Error handling

**Dùng khi:**
- Implement upload ảnh sân
- Cần biết URL format để hiển thị ảnh
- Handle multipart/form-data

---

## 🎯 Navigation Map

### Theo Use Case

<table>
<tr>
<th>Use Case</th>
<th>Tài Liệu</th>
</tr>

<tr>
<td><strong>Tôi mới join project</strong></td>
<td>
1. <a href="./UPDATE_SUMMARY_FOR_FE.md">UPDATE_SUMMARY_FOR_FE.md</a><br>
2. <a href="./SYSTEM_DESIGN.md">SYSTEM_DESIGN.md</a><br>
3. <a href="./API_DOCUMENTATION.md">API_DOCUMENTATION.md</a>
</td>
</tr>

<tr>
<td><strong>Implement tính năng mới</strong></td>
<td>
1. <a href="./UPDATE_SUMMARY_FOR_FE.md">UPDATE_SUMMARY_FOR_FE.md</a> (check latest)<br>
2. <a href="./GROUPED_FIELDS_GUIDE.md">GROUPED_FIELDS_GUIDE.md</a> (sân ghép)<br>
3. <a href="./IMAGE_UPLOAD_GUIDE.md">IMAGE_UPLOAD_GUIDE.md</a> (upload ảnh)
</td>
</tr>

<tr>
<td><strong>Debug API call</strong></td>
<td>
1. <a href="./API_DOCUMENTATION.md">API_DOCUMENTATION.md</a><br>
2. Swagger UI: <code>http://localhost:8080/swagger-ui/index.html</code>
</td>
</tr>

<tr>
<td><strong>Hiểu business logic</strong></td>
<td>
1. <a href="./GROUPED_FIELDS_GUIDE.md">GROUPED_FIELDS_GUIDE.md</a> (sân ghép logic)<br>
2. <a href="./SYSTEM_DESIGN.md">SYSTEM_DESIGN.md</a> (tổng quan)
</td>
</tr>

<tr>
<td><strong>Test API locally</strong></td>
<td>
1. <a href="./API_DOCUMENTATION.md#19-test-accounts">Test Accounts</a><br>
2. Swagger UI: <code>http://localhost:8080/swagger-ui/index.html</code>
</td>
</tr>

</table>

---

## 📋 Danh Sách Tính Năng (Features)

### ✅ Đã Hoàn Thành

| # | Feature | Guide | Endpoints |
|---|---------|-------|-----------|
| 1 | Authentication (JWT) | [API Doc](./API_DOCUMENTATION.md#3-auth-apis) | 6 |
| 2 | Stadium Management | [API Doc](./API_DOCUMENTATION.md#5-stadium-apis) | 10 |
| 3 | Field Management | [API Doc](./API_DOCUMENTATION.md#6-field-apis) | 4 |
| 4 | Time Slots | [API Doc](./API_DOCUMENTATION.md#7-timeslot-apis) | 4 |
| 5 | Booking | [API Doc](./API_DOCUMENTATION.md#8-booking-apis) | 10 |
| 6 | Deposit (Đặt cọc) | [API Doc](./API_DOCUMENTATION.md#9-deposit-apis) | 7 |
| 7 | Recurring Booking | [API Doc](./API_DOCUMENTATION.md#10-recurring-booking-apis) | 7 |
| 8 | Team Management | [API Doc](./API_DOCUMENTATION.md#11-team-apis) | 11 |
| 9 | Match Making (Ráp kèo) | [API Doc](./API_DOCUMENTATION.md#12-match-making-apis) | 10 |
| 10 | Review & Rating | [API Doc](./API_DOCUMENTATION.md#13-review-apis) | 4 |
| 11 | Admin Dashboard | [API Doc](./API_DOCUMENTATION.md#14-admin-apis) | 4 |
| 12 | **⭐ Image Upload** | **[IMAGE_UPLOAD_GUIDE.md](./IMAGE_UPLOAD_GUIDE.md)** | **1** |
| 13 | **⭐ Grouped Fields (Sân ghép)** | **[GROUPED_FIELDS_GUIDE.md](./GROUPED_FIELDS_GUIDE.md)** | **0** (logic update) |

**Tổng:** **79 endpoints**

---

## 🔧 Local Development

### Backend URL
```
http://localhost:8080
```

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### Test Accounts

| Email | Password | Role |
|-------|----------|------|
| `customer@example.com` | `123456` | CUSTOMER |
| `owner@example.com` | `123456` | OWNER |
| `admin@example.com` | `123456` | ADMIN |

### Quick Test Command

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "customer@example.com", "password": "123456"}'

# Get fields (with grouped info)
curl http://localhost:8080/api/v1/stadiums/1/fields

# Check available slots
curl "http://localhost:8080/api/v1/fields/1/available-slots?date=2026-03-15"
```

---

## 🆕 Latest Updates (08/03/2026)

### 1. Grouped Fields (Sân Ghép) ⭐

**Business Logic:**
- Nhiều sân nhỏ → 1 sân lớn (parent-child)
- Đặt sân con → sân cha không khả dụng
- Đặt sân cha → tất cả sân con không khả dụng

**API Changes:**
- `FieldResponse` có thêm: `parentFieldId`, `childFieldCount`
- `FieldRequest` có thêm: `parentFieldId` (optional)
- `GET /fields/{id}/available-slots` tự động check grouped conflicts
- `POST /bookings` tự động validate grouped conflicts

**📖 Chi tiết:** [GROUPED_FIELDS_GUIDE.md](./GROUPED_FIELDS_GUIDE.md)

---

### 2. Image Upload ⭐

**Features:**
- Upload ảnh sân, lưu server-side
- Static resource serving: `/uploads/**`
- Temp folder cho upload trước khi tạo sân
- Auto move từ temp → permanent sau khi tạo sân

**Endpoint mới:**
- `POST /api/v1/images/upload`

**📖 Chi tiết:** [IMAGE_UPLOAD_GUIDE.md](./IMAGE_UPLOAD_GUIDE.md)

---

## 📞 Support

### Khi Cần Hỗ Trợ

1. **Check tài liệu:**
   - [UPDATE_SUMMARY_FOR_FE.md](./UPDATE_SUMMARY_FOR_FE.md) - Latest changes
   - [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - API reference
   - Feature guides - GROUPED_FIELDS_GUIDE.md, IMAGE_UPLOAD_GUIDE.md

2. **Test trên Swagger UI:**
   - http://localhost:8080/swagger-ui/index.html
   - Try out endpoints directly

3. **Liên hệ Backend Team:**
   - Khi business logic không rõ
   - Khi API response không đúng expected
   - Khi cần thêm endpoint mới

---

## 🔄 Document Structure

```
docs/
├── README.md                    ← 📍 You are here
├── UPDATE_SUMMARY_FOR_FE.md     ← 🎯 Start here (latest updates)
├── API_DOCUMENTATION.md         ← 📋 All 79 endpoints
├── SYSTEM_DESIGN.md            ← 🏗️  Architecture & DB schema
├── GROUPED_FIELDS_GUIDE.md     ← ⚽ Sân ghép feature (NEW)
└── IMAGE_UPLOAD_GUIDE.md       ← 📸 Upload ảnh sân (NEW)
```

---

## 📊 Statistics

- **Total Endpoints:** 79
- **Total Features:** 13
- **Authentication:** JWT (Bearer Token)
- **Database:** MySQL 8.0
- **Cache:** Redis 7
- **Documentation:** 5 files

---

**Maintained by:** Backend Team  
**Project:** Booking Stadium System  
**Version:** 1.0.0  
**Last Updated:** 08/03/2026
