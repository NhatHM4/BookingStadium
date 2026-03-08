# Hướng Dẫn Tính Năng Sân Ghép (Grouped Fields) - Dành Cho FE Team

> **Ngày cập nhật:** 08/03/2026  
> **Version:** 1.0.0  
> **Mục đích:** Tài liệu hướng dẫn FE team implement business logic cho tính năng sân ghép

---

## 📋 Tổng Quan

### Business Requirement

> "Đang có thêm 1 business là sẽ có trường hợp 2, 3 sân 5 sẽ thành sân 7. Thì nếu đặt sân 5 của 1 trong 2(hay 3) sân đó thì sẽ không đặt được sân 7."

**Tình huống:** 
- Một sân lớn có thể được chia thành nhiều sân nhỏ
- Hoặc ngược lại: nhiều sân nhỏ có thể ghép thành 1 sân lớn
- Khi đặt sân nhỏ (child) → sân lớn (parent) **không đặt được** cùng khung giờ
- Khi đặt sân lớn (parent) → **tất cả** sân nhỏ (children) **không đặt được** cùng khung giờ

### Ví Dụ Thực Tế

#### Ví dụ 1: 2 sân 5 người → 1 sân 7 người
```
Stadium: Sân Thống Nhất
├── Sân A1 (5 người - child)  ──┐
├── Sân A2 (5 người - child)  ──┼──> Sân B1 (7 người - parent)
└── Sân B1 (7 người - parent) ──┘
```

**Quy tắc:**
- Nếu ai đó đặt **Sân A1** vào 19:00-20:30 → **Sân B1** ❌ không thể đặt 19:00-20:30
- Nếu ai đó đặt **Sân A2** vào 19:00-20:30 → **Sân B1** ❌ không thể đặt 19:00-20:30
- Nếu ai đó đặt **Sân B1** vào 19:00-20:30 → **Sân A1 và A2** ❌ không thể đặt 19:00-20:30

#### Ví dụ 2: 3 sân 5 người → 1 sân 11 người
```
Stadium: Sân Gò Vấp
├── Sân 5A (5 người - child)  ──┐
├── Sân 5B (5 người - child)  ──┼──> Sân 11 (11 người - parent)
├── Sân 5C (5 người - child)  ──┘
└── Sân 11 (11 người - parent) ──┘
```

**Quy tắc:**
- Nếu **bất kỳ** sân 5 nào (5A, 5B, 5C) đã đặt → **Sân 11** không đặt được
- Nếu **Sân 11** đã đặt → **tất cả 3** sân 5 (5A, 5B, 5C) đều không đặt được

---

## 🔄 Thay Đổi API Response

### ⚠️ BEFORE vs AFTER - CẦN CHÚ Ý

<table>
<tr>
<th width="50%">❌ BEFORE (CŨ)</th>
<th width="50%">✅ AFTER (MỚI)</th>
</tr>
<tr>
<td valign="top">

**GET /api/v1/stadiums/{stadiumId}/fields**

Response cũ (KHÔNG có thông tin grouped fields):

```json
{
  "success": true,
  "message": "Thành công",
  "data": [
    {
      "id": 1,
      "stadiumId": 1,
      "stadiumName": "Sân Thống Nhất",
      "name": "Sân A1 - 5 người",
      "fieldType": "FIVE_A_SIDE",
      "defaultPrice": 250000,
      "isActive": true
    },
    {
      "id": 2,
      "stadiumId": 1,
      "stadiumName": "Sân Thống Nhất",
      "name": "Sân A2 - 5 người",
      "fieldType": "FIVE_A_SIDE",
      "defaultPrice": 250000,
      "isActive": true
    },
    {
      "id": 3,
      "stadiumId": 1,
      "stadiumName": "Sân Thống Nhất",
      "name": "Sân B1 - 7 người",
      "fieldType": "SEVEN_A_SIDE",
      "defaultPrice": 350000,
      "isActive": true
    }
  ]
}
```

</td>
<td valign="top">

**GET /api/v1/stadiums/{stadiumId}/fields**

Response mới (CÓ thông tin grouped fields):

```json
{
  "success": true,
  "message": "Thành công",
  "data": [
    {
      "id": 1,
      "stadiumId": 1,
      "stadiumName": "Sân Thống Nhất",
      "name": "Sân A1 - 5 người",
      "fieldType": "FIVE_A_SIDE",
      "defaultPrice": 250000,
      "isActive": true,
      "parentFieldId": 3,      // ⭐ MỚI: ID sân cha
      "childFieldCount": 0     // ⭐ MỚI: Số sân con
    },
    {
      "id": 2,
      "stadiumId": 1,
      "stadiumName": "Sân Thống Nhất",
      "name": "Sân A2 - 5 người",
      "fieldType": "FIVE_A_SIDE",
      "defaultPrice": 250000,
      "isActive": true,
      "parentFieldId": 3,      // ⭐ MỚI: ID sân cha
      "childFieldCount": 0     // ⭐ MỚI: Số sân con
    },
    {
      "id": 3,
      "stadiumId": 1,
      "stadiumName": "Sân Thống Nhất",
      "name": "Sân B1 - 7 người (Ghép)",
      "fieldType": "SEVEN_A_SIDE",
      "defaultPrice": 350000,
      "isActive": true,
      "parentFieldId": null,   // ⭐ MỚI: null = là sân cha
      "childFieldCount": 2     // ⭐ MỚI: có 2 sân con
    }
  ]
}
```

</td>
</tr>
</table>

### 🔑 Giải Thích Các Field Mới

| Field | Type | Mô tả | Logic |
|-------|------|-------|-------|
| **`parentFieldId`** | `Long \| null` | ID của sân cha (nếu có) | - `null` = sân này là sân cha (hoặc độc lập)<br>- `number` = sân này là sân con, thuộc sân cha có ID này |
| **`childFieldCount`** | `Integer` | Số lượng sân con | - `0` = không có sân con (là sân con hoặc sân độc lập)<br>- `> 0` = là sân cha, có N sân con |

---

## 🎯 Cách Hiển Thị Trên FE

### 1. Phân Loại Sân

```typescript
interface Field {
  id: number;
  name: string;
  fieldType: 'FIVE_A_SIDE' | 'SEVEN_A_SIDE' | 'ELEVEN_A_SIDE';
  parentFieldId: number | null;  // ⭐ MỚI
  childFieldCount: number;       // ⭐ MỚI
  // ... các field khác
}

// Kiểm tra loại sân
function getFieldRole(field: Field) {
  if (field.parentFieldId === null && field.childFieldCount > 0) {
    return 'PARENT'; // Sân cha (ghép)
  } else if (field.parentFieldId !== null) {
    return 'CHILD'; // Sân con
  } else {
    return 'INDEPENDENT'; // Sân độc lập
  }
}
```

### 2. UI/UX Gợi Ý Hiển Thị

#### Option A: Hiển thị phẳng với badge

```
📋 Danh sách sân:
┌─────────────────────────────────────┐
│ ⚽ Sân A1 - 5 người    [Sân con]    │
│    250,000đ/90 phút                 │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ ⚽ Sân A2 - 5 người    [Sân con]    │
│    250,000đ/90 phút                 │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ ⚽ Sân B1 - 7 người    [Sân ghép]   │
│    350,000đ/90 phút   (2 sân con)  │
└─────────────────────────────────────┘
```

#### Option B: Hiển thị theo nhóm (nested)

```
📋 Danh sách sân:
┌─────────────────────────────────────┐
│ ⚽ Sân B1 - 7 người (Ghép)          │
│    350,000đ/90 phút                 │
│    └─ Ghép từ 2 sân:               │
│       • Sân A1 - 5 người            │
│       • Sân A2 - 5 người            │
└─────────────────────────────────────┘
```

#### Option C: Thêm tooltip/info icon

```
⚽ Sân A1 - 5 người  (i) ← click hiển thị:
   "Sân này có thể ghép với Sân A2 thành Sân B1 (7 người)"
```

### 3. Logic Lọc và Nhóm Sân

```typescript
// Group fields theo parent-child relationship
function groupFields(fields: Field[]) {
  const groups: Record<string, { parent: Field | null, children: Field[] }> = {};
  
  fields.forEach(field => {
    if (field.parentFieldId === null && field.childFieldCount > 0) {
      // Đây là sân cha
      if (!groups[field.id]) {
        groups[field.id] = { parent: field, children: [] };
      }
    } else if (field.parentFieldId !== null) {
      // Đây là sân con
      const parentId = field.parentFieldId;
      if (!groups[parentId]) {
        groups[parentId] = { parent: null, children: [] };
      }
      groups[parentId].children.push(field);
    } else {
      // Sân độc lập
      groups[`independent_${field.id}`] = { parent: field, children: [] };
    }
  });
  
  return Object.values(groups);
}
```

---

## 🚨 Logic Kiểm Tra Availability

### Backend Đã Xử Lý

Backend đã implement logic kiểm tra conflict tự động trong các API sau:

1. **GET /api/v1/fields/{fieldId}/available-slots?date=2026-03-15**
   - Backend tự động check parent + children bookings
   - Trả về `isAvailable: false` nếu có conflict

2. **POST /api/v1/bookings**
   - Backend tự động validate conflict trước khi tạo booking
   - Throw error nếu có conflict

### ⚠️ BEFORE vs AFTER - Available Slots Logic

<table>
<tr>
<th width="50%">❌ BEFORE (CŨ)</th>
<th width="50%">✅ AFTER (MỚI)</th>
</tr>
<tr>
<td valign="top">

**Logic cũ (chỉ check sân hiện tại):**

```typescript
// Check available slots cho Sân A1
GET /api/v1/fields/1/available-slots
  ?date=2026-03-15

// Backend chỉ check:
// - Sân A1 có booking không?
// 
// KHÔNG check:
// - Sân B1 (parent) có booking không?
// - Sân A2 (sibling) có booking không?
```

**Vấn đề:**
- Có thể đặt Sân A1 trong khi Sân B1 đã được đặt
- Gây conflict logic!

</td>
<td valign="top">

**Logic mới (check cả parent và children):**

```typescript
// Check available slots cho Sân A1
GET /api/v1/fields/1/available-slots
  ?date=2026-03-15

// Backend tự động check:
// ✅ Sân A1 có booking không?
// ✅ Sân B1 (parent) có booking không?
// ✅ Các sân anh em (A2) KHÔNG được check
//    (vì có thể đặt A1 khi A2 trống)
```

**Khi nào isAvailable = false:**
1. Sân A1 đã được đặt
2. HOẶC Sân B1 (parent) đã được đặt

```typescript
// Check available slots cho Sân B1 (parent)
GET /api/v1/fields/3/available-slots
  ?date=2026-03-15

// Backend check:
// ✅ Sân B1 có booking không?
// ✅ Sân A1 có booking không?
// ✅ Sân A2 có booking không?
```

**Khi nào isAvailable = false:**
1. Sân B1 đã được đặt
2. HOẶC **BẤT KỲ** sân con (A1 hoặc A2) đã được đặt

</td>
</tr>
</table>

### FE Chỉ Cần Hiển Thị Kết Quả

```typescript
// Gọi API
const response = await fetch(
  `${API_BASE}/fields/${fieldId}/available-slots?date=${date}`
);
const { data: slots } = await response.json();

// Hiển thị slots
slots.forEach(slot => {
  if (slot.isAvailable) {
    // ✅ Hiển thị slot có thể đặt (màu xanh/enabled)
  } else {
    // ❌ Hiển thị slot không khả dụng (màu xám/disabled)
  }
});

// KHÔNG cần implement logic check parent/child ở FE
// Backend đã xử lý hết!
```

---

## ⚠️ Error Handling

### BEFORE vs AFTER - Booking Errors

<table>
<tr>
<th width="50%">❌ BEFORE (CŨ)</th>
<th width="50%">✅ AFTER (MỚI)</th>
</tr>
<tr>
<td valign="top">

**Error cũ:**

```json
{
  "success": false,
  "message": "Khung giờ này đã được đặt",
  "data": null
}
```

</td>
<td valign="top">

**Error mới (conflict với grouped field):**

```json
{
  "success": false,
  "message": "Không thể đặt sân A1 vào khung giờ 19:00-20:30 ngày 2026-03-15 vì sân cha Sân B1 - 7 người (Ghép) đã được đặt",
  "data": null
}
```

**HOẶC:**

```json
{
  "success": false,
  "message": "Không thể đặt sân B1 - 7 người (Ghép) vào khung giờ 19:00-20:30 ngày 2026-03-15 vì sân con Sân A1 - 5 người đã được đặt",
  "data": null
}
```

</td>
</tr>
</table>

### Hiển Thị Error Trên FE

```typescript
try {
  const response = await createBooking(bookingData);
  // Success
} catch (error) {
  // Hiển thị error message
  alert(error.message);
  
  // Hoặc hiển thị modal với thông tin chi tiết:
  // "❌ Không thể đặt sân
  //  
  //  Lý do: Sân cha "Sân B1 - 7 người (Ghép)" đã được đặt 
  //         vào khung giờ này.
  //  
  //  Gợi ý: Chọn khung giờ khác hoặc đặt sân khác."
}
```

---

## 🔧 API Endpoints Hỗ Trợ Grouped Fields

### 1. Lấy danh sách fields với thông tin grouped
```http
GET /api/v1/stadiums/{stadiumId}/fields
Authorization: không cần (public)
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Sân A1 - 5 người",
      "parentFieldId": 3,     // ⭐ MỚI
      "childFieldCount": 0    // ⭐ MỚI
    }
  ]
}
```

### 2. Tạo field với parent relationship
```http
POST /api/v1/stadiums/{stadiumId}/fields
Authorization: Bearer <owner-token>
Content-Type: application/json

{
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000,
  "parentFieldId": 3        // ⭐ MỚI: Optional, ID của sân cha
}
```

### 3. Cập nhật parent relationship
```http
PUT /api/v1/fields/{id}
Authorization: Bearer <owner-token>
Content-Type: application/json

{
  "name": "Sân A1 - 5 người",
  "fieldType": "FIVE_A_SIDE",
  "defaultPrice": 250000,
  "parentFieldId": 3        // ⭐ Có thể update
}
```

### 4. Check available slots (auto check grouped fields)
```http
GET /api/v1/fields/{fieldId}/available-slots?date=2026-03-15
Authorization: không cần (public)
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "timeSlotId": 1,
      "startTime": "19:00:00",
      "endTime": "20:30:00",
      "price": 250000,
      "isAvailable": false    // false vì parent/child có booking
    }
  ]
}
```

### 5. Create booking (auto validate grouped fields)
```http
POST /api/v1/bookings
Authorization: Bearer <customer-token>
Content-Type: application/json

{
  "fieldId": 1,
  "timeSlotId": 1,
  "bookingDate": "2026-03-15"
}
```

**Error nếu conflict:**
```json
{
  "success": false,
  "message": "Không thể đặt sân A1 vào khung giờ 19:00-20:30 ngày 2026-03-15 vì sân cha Sân B1 - 7 người (Ghép) đã được đặt"
}
```

---

## 🧪 Test Cases Cho FE

### Test Case 1: Hiển thị đúng parent-child relationship

| Sân | parentFieldId | childFieldCount | Hiển thị |
|-----|---------------|-----------------|----------|
| Sân A1 | 3 | 0 | Badge "Sân con" hoặc icon |
| Sân A2 | 3 | 0 | Badge "Sân con" hoặc icon |
| Sân B1 | null | 2 | Badge "Sân ghép (2 sân con)" |

### Test Case 2: Check available slots

**Setup:**
- Sân A1 (child, id=1) thuộc Sân B1 (parent, id=3)
- Booking: Sân B1, 2026-03-15, 19:00-20:30 đã được đặt

**Test:**
1. Gọi `GET /fields/1/available-slots?date=2026-03-15`
2. Kiểm tra slot 19:00-20:30 có `isAvailable: false`
3. Hiển thị disabled cho slot đó

### Test Case 3: Booking conflict

**Setup:**
- Sân A1 (child, id=1) thuộc Sân B1 (parent, id=3)
- Booking: Sân A1, 2026-03-15, 19:00-20:30 đã được đặt

**Test:**
1. User cố đặt Sân B1, 2026-03-15, 19:00-20:30
2. POST /bookings → nhận error 400
3. Hiển thị error message: "...vì sân con Sân A1 đã được đặt"

### Test Case 4: Sân độc lập (không grouped)

**Setup:**
- Sân C1: parentFieldId=null, childFieldCount=0

**Test:**
1. Hiển thị bình thường, không có badge đặc biệt
2. Booking logic không khác gì trước đây

---

## 📝 Checklist Cho FE Team

### Phase 1: Hiển thị thông tin (UI/UX)
- [ ] Parse `parentFieldId` và `childFieldCount` từ API response
- [ ] Hiển thị badge/icon cho sân con (`parentFieldId !== null`)
- [ ] Hiển thị badge/icon cho sân cha (`childFieldCount > 0`)
- [ ] (Optional) Hiển thị grouped fields theo nhóm thay vì flat list

### Phase 2: Available slots
- [ ] Gọi API `GET /fields/{id}/available-slots?date=...`
- [ ] Hiển thị slots với `isAvailable: true` enabled
- [ ] Hiển thị slots với `isAvailable: false` disabled
- [ ] KHÔNG cần implement logic check parent/child (backend đã xử lý)

### Phase 3: Booking flow
- [ ] User chọn sân → gọi available slots → hiển thị
- [ ] User chọn slot enabled → gọi `POST /bookings`
- [ ] Nếu success → redirect/hiển thị success
- [ ] Nếu error → parse error message và hiển thị cho user

### Phase 4: Error handling
- [ ] Catch error 400 khi booking conflict
- [ ] Hiển thị error message chi tiết
- [ ] (Optional) Highlight sân/slot conflict để user dễ hiểu

### Phase 5: Owner management (nếu có màn hình quản lý)
- [ ] Khi tạo/update field, thêm dropdown chọn `parentFieldId`
- [ ] Dropdown chỉ hiển thị các sân cùng stadium
- [ ] (Optional) Validate fieldType phù hợp (ví dụ: parent phải >= children)

---

## 🎓 FAQs

### Q1: FE có cần validate logic grouped fields không?

**A:** ❌ KHÔNG CẦN. Backend đã:
- Validate khi create/update field (parent phải cùng stadium)
- Check conflict khi booking
- Tự động filter `isAvailable` trong available slots API

FE chỉ cần:
- Hiển thị thông tin `parentFieldId` và `childFieldCount`
- Hiển thị slots theo `isAvailable`
- Hiển thị error message khi booking fail

### Q2: Nếu muốn hiển thị "Sân này thuộc nhóm sân nào", phải gọi API thêm không?

**A:** ✅ CÓ. Nếu muốn hiển thị tên sân cha:
1. Lấy `parentFieldId` từ field hiện tại
2. Tìm field có `id === parentFieldId` trong danh sách fields

```typescript
const parentField = fields.find(f => f.id === currentField.parentFieldId);
console.log(`Sân này thuộc nhóm: ${parentField.name}`);
```

### Q3: Có API nào trả về list children của 1 parent không?

**A:** ❌ KHÔNG CÓ API riêng. Nhưng có thể filter từ danh sách fields:

```typescript
const children = fields.filter(f => f.parentFieldId === parentFieldId);
```

### Q4: Nếu user đặt sân con, có cần show warning "Sân cha sẽ không khả dụng" không?

**A:** ⚠️ TÙY CHỌN. Đây là enhance UX, không bắt buộc:

```typescript
if (selectedField.parentFieldId !== null) {
  const parentField = fields.find(f => f.id === selectedField.parentFieldId);
  showWarning(`Lưu ý: Nếu đặt sân này, sân "${parentField.name}" 
               sẽ không khả dụng cùng khung giờ.`);
}
```

### Q5: Backend có tự động tạo parent-child relationship không?

**A:** ❌ KHÔNG. Owner phải tự config qua API:
- Khi tạo field mới, truyền `parentFieldId` vào request body
- Hoặc update field existing để set `parentFieldId`

---

## 📞 Support

Nếu có vấn đề khi implement, liên hệ:
- Backend team để clarify business logic
- Check [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) để xem chi tiết API
- Check Swagger UI: http://localhost:8080/swagger-ui/index.html

---

**Created by:** Backend Team  
**Last Updated:** 08/03/2026  
**Version:** 1.0.0
