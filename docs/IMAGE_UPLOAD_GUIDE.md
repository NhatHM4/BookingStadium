# Image Upload - Hướng dẫn sử dụng

## Tổng quan

Hệ thống upload và lưu trữ ảnh cho các sân bóng. Ảnh được lưu trữ trên server trong thư mục `uploads/` và serve như static resources.

## Cấu trúc thư mục

```
uploads/
└── stadiums/
    ├── temp/                    # Ảnh tạm khi chưa có stadiumId
    │   └── uuid-filename.jpg
    ├── 1/                       # Ảnh của sân ID=1
    │   └── uuid-filename.jpg
    ├── 2/                       # Ảnh của sân ID=2
    │   └── uuid-filename.jpg
    └── ...
```

## API Endpoints

### 1. Upload ảnh

**Endpoint:** `POST /api/v1/images/upload`  
**Auth:** OWNER  
**Content-Type:** `multipart/form-data`

**Parameters:**
- `file` (required): File ảnh (JPG, PNG, max 5MB)
- `stadiumId` (optional): ID của sân (null → lưu vào temp)

**Response:**
```json
{
  "success": true,
  "message": "Upload ảnh thành công",
  "data": {
    "path": "stadiums/temp/550e8400-e29b-41d4-a716-446655440000.jpg",
    "url": "/uploads/stadiums/temp/550e8400-e29b-41d4-a716-446655440000.jpg"
  }
}
```

### 2. Truy cập ảnh

**URL Pattern:** `http://localhost:8080/uploads/{path}`

**Ví dụ:**
```
http://localhost:8080/uploads/stadiums/1/550e8400-e29b-41d4-a716-446655440000.jpg
```

> Ảnh được serve công khai, không cần authentication

## Flow tạo sân với ảnh

### Cách 1: Upload trước, tạo sân sau (Recommended)

```
1. Upload ảnh
   POST /api/v1/images/upload
   Form-data: file=stadium.jpg
   → Response: { path: "stadiums/temp/uuid.jpg", url: "/uploads/stadiums/temp/uuid.jpg" }

2. Tạo sân
   POST /api/v1/stadiums
   Body: {
     "name": "Sân ABC",
     "address": "123 Street",
     "imageUrl": "stadiums/temp/uuid.jpg"  // Dùng path từ bước 1
   }
   → Backend tự động move ảnh từ temp/ sang stadiums/{stadiumId}/
```

### Cách 2: Tạo sân trước, upload sau

```
1. Tạo sân
   POST /api/v1/stadiums
   Body: { "name": "Sân ABC", ... }
   → Response: { id: 1, ... }

2. Upload ảnh với stadiumId
   POST /api/v1/images/upload
   Form-data: file=stadium.jpg, stadiumId=1
   → Ảnh lưu trực tiếp vào stadiums/1/

3. Cập nhật sân với imageUrl
   PUT /api/v1/stadiums/1
   Body: { ..., "imageUrl": "stadiums/1/uuid.jpg" }
```

## Validation Rules

- **File type:** Chỉ chấp nhận image/* (JPG, PNG, GIF, WebP, v.v.)
- **File size:** Tối đa 5MB
- **Filename:** Tự động generate UUID để tránh trùng lặp

## Configuration

**File:** `application.yml`

```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 5MB
      max-request-size: 10MB

app:
  upload:
    dir: uploads  # Thư mục lưu trữ
```

## Testing với cURL

### 1. Upload ảnh (temp)

```bash
curl -X POST http://localhost:8080/api/v1/images/upload \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -F "file=@/path/to/stadium.jpg"
```

### 2. Upload ảnh với stadiumId

```bash
curl -X POST http://localhost:8080/api/v1/images/upload \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -F "file=@/path/to/stadium.jpg" \
  -F "stadiumId=1"
```

### 3. Truy cập ảnh

```bash
curl http://localhost:8080/uploads/stadiums/1/uuid-filename.jpg \
  --output downloaded.jpg
```

## Testing với Postman

1. **Method:** POST
2. **URL:** `http://localhost:8080/api/v1/images/upload`
3. **Headers:**
   - `Authorization: Bearer YOUR_ACCESS_TOKEN`
4. **Body:** form-data
   - Key: `file`, Type: File, Value: Browse và chọn file ảnh
   - Key: `stadiumId`, Type: Text, Value: `1` (optional)
5. Click **Send**

## Error Handling

### File không phải ảnh
```json
{
  "success": false,
  "message": "Chỉ chấp nhận file ảnh",
  "data": null
}
```

### File quá lớn
```json
{
  "success": false,
  "message": "Kích thước file không được vượt quá 5MB",
  "data": null
}
```

### File rỗng
```json
{
  "success": false,
  "message": "File không được để trống",
  "data": null
}
```

## Notes

- Thư mục `uploads/` đã được thêm vào `.gitignore`
- Ảnh được lưu vĩnh viễn trên server, cần backup định kỳ
- Khi xóa stadium (soft delete), ảnh vẫn được giữ lại
- Hỗ trợ UTF-8 encoding cho tên file gốc
- UUID được dùng để tránh conflict khi nhiều file cùng tên

## Security

- Upload chỉ dành cho role OWNER
- Truy cập ảnh (GET /uploads/**) là public
- File type validation để chặn file độc hại
- File size limit để tránh DoS attack
