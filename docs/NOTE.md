1. trường hợp nhận kèo có sẵn thì sẽ có option cho khách (không cần tạo đội)

2. Lỗi đang không có popup ở màn hình /register

3. Thêm 1 tab Trận đấu ở header nữa 

4. ✅ **[DONE]** Khi đặt sân nếu có isMatchRequest = true thì tự động tạo một trận ráp kèo ngay
   - Đã thêm các fields vào `BookingRequest`: teamId, requiredSkillLevel, costSharing, hostSharePercent, opponentSharePercent, matchMessage, contactPhone
   - Đã implement logic tự động tạo match request trong `BookingService.createBooking()`
   - Khi isMatchRequest = true:
     * **Bắt buộc** phải có teamId (đội chủ nhà)
     * User phải là đội trưởng của đội đó
     * Tự động tạo match request với status OPEN
     * Match request tự động hết hạn 2 giờ trước giờ đá
     * Mặc định costSharing = EQUAL_SPLIT nếu không truyền
   - Chi tiết xem thêm tại: `/docs/API_DOCUMENTATION.md` - Section 7.2

   ### ⭐ UPDATE MỚI - 3 Options khi tạo trận ráp kèo:
   
   **✅ OPTION 1: Dùng đội có sẵn**
   - Truyền `teamId` vào request
   - User phải là đội trưởng của đội đó
   - Logic giống như trước
   
   **✅ OPTION 2: Tạo đội nhanh tại màn hình**
   - Set `createQuickTeam = true`
   - Truyền `quickTeamName` (bắt buộc)
   - Truyền `quickTeamSkillLevel` (optional)
   - Hệ thống tự động tạo team mới với user làm captain
   - Implementation: `BookingService.createQuickTeam()`
   
   **✅ OPTION 3: Không cần đội - chỉ tên + SĐT**
   - Không truyền `teamId` và `createQuickTeam = false/null`
   - Truyền `hostName` (bắt buộc) - tên người chơi
   - Truyền `contactPhone` (bắt buộc) - SĐT liên hệ
   - Hệ thống tự động tạo team tạm thời với tên người chơi
   - Implementation: `BookingService.createTemporaryTeam()`
   
   **✅ CÁCH CHIA PHÍ MỚI:**
   - Đã thêm `WIN_LOSE` vào enum `CostSharing`
   - `WIN_LOSE`: Đội thắng trả 70%, đội thua trả 30% - **MẶC ĐỊNH**
   - Default cost sharing đã thay đổi từ `EQUAL_SPLIT` sang `WIN_LOSE`
   - Các option khác: `EQUAL_SPLIT`, `HOST_PAY`, `OPPONENT_PAY`, `CUSTOM`
   
   **Files đã update:**
   - `/src/main/java/com/booking/stadium/dto/booking/BookingRequest.java`
   - `/src/main/java/com/booking/stadium/enums/CostSharing.java`
   - `/src/main/java/com/booking/stadium/service/BookingService.java`
   - `/docs/API_DOCUMENTATION.md` - Section 7.2, 11.1, 16 (Enums)
   
   **Chi tiết API xem tại:**
   - Booking API: `/docs/API_DOCUMENTATION.md` - Section 7.2
   - Match Request API: `/docs/API_DOCUMENTATION.md` - Section 11.1
   - Enums Reference: `/docs/API_DOCUMENTATION.md` - Section 16




   ### UPDATE:
   mình muốn update thêm thì khi booking sân bóng thì có tùy chọn cho người chưa đăng nhập (Khách). Update tài liệu nhé

   ### ✅ OPTION 4: Đặt sân cho Khách (Guest Booking - Không cần đăng nhập)
   
   **Endpoint mới:** `POST /api/v1/bookings/guest` (Public - không cần Auth)
   
   **Cách hoạt động:**
   - Khách chưa đăng nhập có thể đặt sân trực tiếp
   - Chỉ cần cung cấp: `guestName` (bắt buộc), `guestPhone` (bắt buộc), `guestEmail` (optional)
   - Khách **KHÔNG** thể tạo trận ráp kèo (`isMatchRequest` bị từ chối)
   - Booking khách có `customerId = null`, `isGuestBooking = true`
   
   **Request mẫu:**
   ```json
   {
     "fieldId": 1,
     "timeSlotId": 1,
     "bookingDate": "2025-03-15",
     "guestName": "Trần Văn B",
     "guestPhone": "0912345678",
     "guestEmail": "guest@example.com",
     "note": "Đặt sân cho buổi giao lưu"
   }
   ```
   
   **Files đã update:**
   - `/src/main/java/com/booking/stadium/entity/Booking.java` — Thêm `guestName`, `guestPhone`, `guestEmail`, `customer` nullable
   - `/src/main/java/com/booking/stadium/dto/booking/BookingRequest.java` — Thêm fields cho guest
   - `/src/main/java/com/booking/stadium/dto/booking/BookingResponse.java` — Thêm `isGuestBooking`, `guestName`, `guestPhone`, `guestEmail`
   - `/src/main/java/com/booking/stadium/service/BookingService.java` — Thêm `createGuestBooking()`, refactor `processBooking()` dùng chung
   - `/src/main/java/com/booking/stadium/controller/BookingController.java` — Thêm endpoint `POST /bookings/guest`
   - `/src/main/java/com/booking/stadium/config/SecurityConfig.java` — Cho phép POST `/bookings/guest` public
   - `/docs/API_DOCUMENTATION.md` — Thêm Section 7.3



### UPDATE 

tạo thêm cho tôi một màn hình để search mã đặt sân: hiện thị thông tin đặt sân (vì book qua khách thì không có lịch sử)

### ✅ Tra cứu đơn đặt sân theo mã booking

**Endpoint mới:** `GET /api/v1/bookings/lookup?bookingCode=BK...` (Public - không cần Auth)

**Cách hoạt động:**
- Khách nhập mã booking code được cấp khi đặt sân
- Hệ thống trả về đầy đủ thông tin: sân, giờ, ngày, giá, trạng thái...
- Phù hợp cho khách (guest) không có tài khoản để xem lịch sử

**Files đã update:**
- `/src/main/java/com/booking/stadium/service/BookingService.java` — Thêm `getBookingByCode()`
- `/src/main/java/com/booking/stadium/controller/BookingController.java` — Thêm endpoint `GET /bookings/lookup`
- `/src/main/java/com/booking/stadium/config/SecurityConfig.java` — Cho phép GET `/bookings/lookup` public
- `/docs/API_DOCUMENTATION.md` — Thêm Section 7.4
