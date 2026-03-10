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

