# 📋 Bản Tóm Tắt Cập Nhật API - Cho FE Team

> **Ngày cập nhật:** 08/03/2026  
> **Mục đích:** Tóm tắt các thay đổi API và business logic cho tính năng **Sân Ghép (Grouped Fields)**

---

## 🔄 Changelog 14/04/2026 (Team + Match Response)

### 1) Team API thay đổi

- `TeamRequest` bắt buộc có `phone`
  - Áp dụng cho:
    - `POST /api/v1/teams`
    - `PUT /api/v1/teams/{id}`
- `POST /api/v1/teams/{id}/members`
  - Body đổi từ `email` sang:
  ```json
  {
    "name": "Nguyen Van A",
    "phone": "0901234567"
  }
  ```
  - `phone` optional
  - Member không bắt buộc phải là user
- Endpoint quản lý member đổi path param:
  - `PUT /api/v1/teams/{id}/members/{memberId}/remove`
  - `PUT /api/v1/teams/{id}/members/{memberId}/captain`
  - `memberId` = id bản ghi member, không phải `userId`

### 2) Team response thay đổi

- `TeamResponse` có thêm `phone`
- `TeamMemberResponse` có thêm `name`, `phone`
- Giữ `userId`, `userName`, `userEmail` để backward compatible (có thể `null`)

```json
{
  "id": 1,
  "teamId": 10,
  "teamName": "FC Sao Vang",
  "name": "Nguyen Van A",
  "phone": "0901234567",
  "userId": null,
  "userName": null,
  "userEmail": null,
  "role": "MEMBER",
  "status": "ACTIVE",
  "joinedAt": null,
  "createdAt": "..."
}
```

### 3) Match response hỗ trợ nhận kèo cá nhân

- Endpoint: `POST /api/v1/match-requests/{id}/responses`
- Request mới:
```json
{
  "joinType": "TEAM",
  "teamId": 2,
  "contactPhone": "0901234567",
  "message": "Đội/cháu muốn nhận kèo"
}
```

- Quy tắc:
  - `joinType=TEAM`:
    - bắt buộc `teamId`
    - `contactPhone` optional (BE tự fallback team phone/user phone nếu trống)
  - `joinType=INDIVIDUAL`:
    - không cần `teamId`
    - `contactPhone` optional nếu user đã có `phone`
    - nếu user chưa có `phone` thì FE phải nhập `contactPhone`

- Response có thêm:
  - `joinType`
  - `contactPhone`
  - `responderUserId`
  - `responderUserName`
  - `teamId/teamName/teamLogoUrl` có thể `null` nếu cá nhân

### 4) Checklist FE cần update

- Form tạo/sửa đội:
  - thêm field bắt buộc `phone`
- Form thêm thành viên:
  - đổi từ `email` -> `name` + `phone`
- Action remove/captain member:
  - dùng `memberId` thay vì `userId`
- Form nhận kèo:
  - thêm lựa chọn `TEAM` / `INDIVIDUAL`
  - khi `INDIVIDUAL`: thêm option lấy `session.user.phone` hoặc nhập SĐT mới
- Model dữ liệu response:
  - update theo field mới của `TeamResponse`, `TeamMemberResponse`, `MatchResponseResponse`

---

## 🎯 Tổng Quan Thay Đổi

### Tính năng mới: GROUPED FIELDS (Sân Ghép)

**Business Requirement:**
- Nhiều sân nhỏ có thể ghép thành 1 sân lớn
- VD: 2 sân 5 người → 1 sân 7 người
- VD: 3 sân 5 người → 1 sân 11 người

**Logic:**
- ❌ Đặt sân con → Sân cha **KHÔNG** đặt được cùng khung giờ
- ❌ Đặt sân cha → **TẤT CẢ** sân con **KHÔNG** đặt được cùng khung giờ

---

## 📊 Thay Đổi API Response

### 1. GET /api/v1/stadiums/{stadiumId}/fields

<table>
<tr>
<th width="50%">❌ BEFORE (Cũ)</th>
<th width="50%">✅ AFTER (Mới)</th>
</tr>
<tr>
<td valign="top">

```json
{
  "id": 1,
  "stadiumId": 1,
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000,
  "isActive": true
}
```

**KHÔNG có:**
- `parentFieldId`
- `childFieldCount`

</td>
<td valign="top">

```json
{
  "id": 1,
  "stadiumId": 1,
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000,
  "isActive": true,
  "parentFieldId": 3,      // ⭐ MỚI
  "childFieldCount": 0     // ⭐ MỚI
}
```

**CÓ thêm:**
- **`parentFieldId`**: ID sân cha (null = sân cha/độc lập)
- **`childFieldCount`**: Số sân con (0 = sân con/độc lập)

</td>
</tr>
</table>

---

### 2. POST /api/v1/stadiums/{stadiumId}/fields (Tạo sân)

<table>
<tr>
<th width="50%">❌ BEFORE (Cũ)</th>
<th width="50%">✅ AFTER (Mới)</th>
</tr>
<tr>
<td valign="top">

**Request Body:**
```json
{
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000
}
```

**KHÔNG có:**
- `parentFieldId`

</td>
<td valign="top">

**Request Body:**
```json
{
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000,
  "parentFieldId": 3        // ⭐ MỚI (optional)
}
```

**CÓ thêm:**
- **`parentFieldId`**: Optional, ID sân cha nếu sân này là sân con

</td>
</tr>
</table>

---

### 3. PUT /api/v1/fields/{id} (Cập nhật sân)

<table>
<tr>
<th width="50%">❌ BEFORE (Cũ)</th>
<th width="50%">✅ AFTER (Mới)</th>
</tr>
<tr>
<td valign="top">

**Request Body:**
```json
{
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000
}
```

</td>
<td valign="top">

**Request Body:**
```json
{
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000,
  "parentFieldId": 3        // ⭐ MỚI (optional)
}
```

**Có thể update parent relationship**

</td>
</tr>
</table>

---

### 4. GET /api/v1/fields/{fieldId}/available-slots

<table>
<tr>
<th width="50%">❌ BEFORE (Cũ)</th>
<th width="50%">✅ AFTER (Mới)</th>
</tr>
<tr>
<td valign="top">

**Logic cũ:**
- Chỉ check sân hiện tại có booking không

**Vấn đề:**
- Có thể đặt sân con khi sân cha đã đặt
- Gây conflict!

</td>
<td valign="top">

**Logic mới:**
- ✅ Check sân hiện tại
- ✅ Check **sân cha** (nếu là sân con)
- ✅ Check **TẤT CẢ sân con** (nếu là sân cha)

**Response không đổi:**
```json
{
  "timeSlotId": 1,
  "isAvailable": false  // false = có conflict
}
```

**FE chỉ cần hiển thị theo `isAvailable`**
- Backend đã xử lý logic!

</td>
</tr>
</table>

---

### 5. POST /api/v1/bookings

<table>
<tr>
<th width="50%">❌ BEFORE (Cũ)</th>
<th width="50%">✅ AFTER (Mới)</th>
</tr>
<tr>
<td valign="top">

**Error cũ:**
```json
{
  "success": false,
  "message": "Khung giờ này đã được đặt"
}
```

**Generic error**

</td>
<td valign="top">

**Error mới (conflict với grouped field):**
```json
{
  "success": false,
  "message": "Không thể đặt sân A1 vào khung giờ 19:00-20:30 ngày 2026-03-15 vì sân cha Sân B1 - 7 người (Ghép) đã được đặt"
}
```

**Chi tiết lý do conflict**
- Nêu rõ sân nào bị conflict
- Nêu rõ lý do (sân cha/con đã đặt)

</td>
</tr>
</table>

---

## 🔑 Giải Thích Các Field Mới

| Field | Type | Mô tả | Khi nào = null? | Khi nào > 0? |
|-------|------|-------|----------------|--------------|
| **`parentFieldId`** | `Long \| null` | ID của sân cha | Sân này là **sân cha** hoặc **sân độc lập** | Sân này là **sân con** |
| **`childFieldCount`** | `Integer` | Số sân con | Sân này là **sân con** hoặc **sân độc lập** | Sân này là **sân cha** |

### Ví dụ phân loại

```typescript
function getFieldRole(field) {
  if (field.parentFieldId === null && field.childFieldCount > 0) {
    return 'PARENT';      // Sân cha (ghép)
  } else if (field.parentFieldId !== null) {
    return 'CHILD';       // Sân con
  } else {
    return 'INDEPENDENT'; // Sân độc lập
  }
}
```

---

## ✅ Checklist Cho FE Team

### Phase 1: Hiển thị thông tin
- [ ] Parse `parentFieldId` và `childFieldCount` từ API response
- [ ] Hiển thị badge/icon cho:
  - Sân con: `parentFieldId !== null`
  - Sân cha: `childFieldCount > 0`

### Phase 2: Available slots
- [ ] Gọi `GET /fields/{id}/available-slots?date=...`
- [ ] Hiển thị slots theo `isAvailable`
  - `true` → enabled (màu xanh)
  - `false` → disabled (màu xám)
- [ ] **KHÔNG** cần implement logic check parent/child
  - Backend đã xử lý!

### Phase 3: Booking flow
- [ ] User chọn sân → available slots → hiển thị
- [ ] User chọn slot → `POST /bookings`
- [ ] Nếu error → hiển thị error message chi tiết

### Phase 4: Owner management (optional)
- [ ] Khi tạo/update field, thêm dropdown `parentFieldId`
- [ ] Dropdown chỉ hiển thị sân cùng stadium

---

## 📚 Tài Liệu Chi Tiết

| Tài liệu | Nội dung |
|----------|----------|
| **[GROUPED_FIELDS_GUIDE.md](./GROUPED_FIELDS_GUIDE.md)** | 📖 **ĐỌC ĐẦU TIÊN** - Hướng dẫn chi tiết, examples, UI/UX suggestions, test cases |
| [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) | 📋 API endpoints đầy đủ (79 endpoints) |
| [SYSTEM_DESIGN.md](./SYSTEM_DESIGN.md) | 🏗️ Database schema, business logic tổng quan |
| [IMAGE_UPLOAD_GUIDE.md](./IMAGE_UPLOAD_GUIDE.md) | 📸 Hướng dẫn upload ảnh sân |

---

## 🧪 Test Cases Nhanh

### Test 1: Hiển thị grouped fields
```bash
# Lấy danh sách fields của stadium 1
curl http://localhost:8080/api/v1/stadiums/1/fields

# Kiểm tra response có parentFieldId và childFieldCount
```

### Test 2: Check available slots với conflict
```bash
# Setup: Sân A1 (id=1) là con của Sân B1 (id=3)
# Giả sử Sân B1 đã đặt 19:00-20:30 ngày 2026-03-15

# Gọi API available slots cho Sân A1
curl "http://localhost:8080/api/v1/fields/1/available-slots?date=2026-03-15"

# Kiểm tra: slot 19:00-20:30 có isAvailable: false
```

### Test 3: Booking conflict
```bash
# Setup: Sân A1 (id=1) đã đặt 19:00-20:30 ngày 2026-03-15

# Thử đặt Sân B1 (parent) cùng khung giờ
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "fieldId": 3,
    "timeSlotId": 1,
    "bookingDate": "2026-03-15"
  }'

# Kiểm tra: Nhận error 400 với message chi tiết
# "...vì sân con Sân A1 đã được đặt"
```

---

## ❓ FAQs Nhanh

### Q: FE có cần validate logic grouped fields không?
**A:** ❌ KHÔNG. Backend đã xử lý tất cả:
- Filter `isAvailable` trong available slots
- Validate conflict khi booking
- FE chỉ hiển thị kết quả

### Q: Có API nào trả về list children của parent không?
**A:** ❌ KHÔNG có API riêng. Filter từ danh sách fields:
```typescript
const children = fields.filter(f => f.parentFieldId === parentId);
```

### Q: Nếu muốn hiển thị tên sân cha?
**A:** ✅ Tìm trong danh sách fields:
```typescript
const parent = fields.find(f => f.id === field.parentFieldId);
console.log(`Thuộc nhóm: ${parent.name}`);
```

---

## 📞 Support

**Vấn đề khi implement?**
- Check [GROUPED_FIELDS_GUIDE.md](./GROUPED_FIELDS_GUIDE.md) (hướng dẫn đầy đủ)
- Check Swagger UI: http://localhost:8080/swagger-ui/index.html
- Liên hệ Backend team

---

## 📝 Summary Checklist

### Backend ✅ Đã Hoàn Thành
- [x] Thêm `parent_field_id` vào database
- [x] Update Entity với parent-child relationship
- [x] Update DTOs với `parentFieldId`, `childFieldCount`
- [x] Implement conflict checking logic
- [x] Update available slots API
- [x] Update booking API với validation
- [x] Seed data với examples
- [x] Viết tài liệu đầy đủ

### Frontend ⏳ Cần Làm
- [ ] Update type definitions cho Field DTO
- [ ] Hiển thị parent-child info trong UI
- [ ] Hiển thị available slots theo `isAvailable`
- [ ] Handle error messages chi tiết
- [ ] (Optional) UI/UX enhancements

---

**Created by:** Backend Team  
**Last Updated:** 08/03/2026  
**Version:** 1.0.0  
**App Status:** ✅ Running on http://localhost:8080 (79 endpoints)
