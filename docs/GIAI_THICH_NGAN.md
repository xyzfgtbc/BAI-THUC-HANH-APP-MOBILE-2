# Giải thích ngắn để cả nhóm đọc code

## 1. Luồng chạy

`LoginActivity` đọc form XML → `DBHelper.login()` tìm Account → `SessionManager.save()` lưu ID/username/role → mở `HomeActivity`.

Home có 3 nút chức năng. Admin thấy CRUD nhân viên; Employee không có nút này. BaseActivity kiểm tra tài khoản còn tồn tại trước khi mở màn hình. EmployeeActivity cũng kiểm tra vai trò Admin. Các Activity nội bộ không export ra ứng dụng khác.

Các màn hình dùng `setContentView(R.layout...)`, `findViewById` để lấy ô nhập/nút, `setOnClickListener` để xử lý thao tác. `ArrayAdapter` đưa danh sách lên `ListView`; `toString()` trong Models quy định nội dung mỗi dòng.

## 2. CRUD nhân viên nằm ở đâu?

| CRUD | Giao diện | DBHelper | Lệnh SQLite |
|---|---|---|---|
| Create | Thêm nhân viên → nhập form → Lưu | saveEmployee khi id=0 | insertOrThrow |
| Read | Danh sách; chạm một dòng xem thông tin | employees | SELECT JOIN Account |
| Update | Chạm dòng → sửa thông tin → Lưu | saveEmployee khi id khác 0 | update với id=? |
| Delete | Chạm dòng → Xóa → xác nhận | deleteEmployee | delete Account, tự cascade |
| Search | Nhập mã/tên → Tìm kiếm | employees(keyword) | LIKE ? |

`ContentValues` chứa tên cột và giá trị cần thêm/sửa. Dấu `?` nhận tham số riêng, tránh nối trực tiếp chuỗi người dùng vào SQL.

Thêm nhân viên cần thêm Account trước để lấy accountId, rồi thêm Employee. Dùng transaction để nếu một bước lỗi thì cả hai bước được hủy, không dư tài khoản.

## 3. Bốn bảng liên hệ như thế nào?

- Account: tên đăng nhập, mật khẩu và vai trò.
- Employee.accountId → Account.id: hồ sơ nhân viên thuộc một tài khoản.
- WorkLog.employeeId → Employee.id: các ca làm của nhân viên.
- Salary.employeeId → Employee.id: lương từng tháng/năm.

`UNIQUE(employeeId, workDate)` chặn hai ca trong cùng ngày. `UNIQUE(employeeId, month, year)` chặn hai bảng lương cùng tháng. `ON DELETE CASCADE` xóa dữ liệu liên quan khi xóa tài khoản nhân viên.

## 4. Check-in và checkout

Check-in tìm xem đã có công ngày này hoặc còn ca Open chưa. Nếu có thì báo lỗi; nếu không thì thêm WorkLog có giờ vào, giờ ra null, status Open.

Checkout tìm ca Open hôm nay. `finishLog()` kiểm tra giờ ra sau giờ vào, tính số giây/3600 rồi cập nhật status Complete và gọi calculateSalary. Quên checkout ngày cũ thì Admin vào lịch sử, chạm ca đó và nhập giờ ra đúng thực tế.

## 5. Công thức lương

Ví dụ 08:00 → 17:00 = 9 giờ:

- Giờ thường = 8.
- Giờ tăng ca = 9 − 8 = 1.
- Lương = 8 × 50.000 + 1 × 50.000 × 1,5 = **475.000 VNĐ**.

Hàm calculateSalary lấy từng ca trong tháng, cộng tổng giờ và tăng ca **theo từng ngày**, sau đó ghi Salary. Nếu còn giờ ra null thì từ chối tính, không coi ca thiếu là 0 giờ. Đây là chính sách mẫu của bài thực hành, không tính thuế, nghỉ trưa hoặc phụ cấp.

## 6. Câu hỏi thường gặp

**Vì sao dùng SQLite?** Có sẵn trên Android, lưu được dữ liệu sau khi tắt app, không cần server.

**SharedPreferences khác SQLite thế nào?** Preferences lưu vài giá trị nhỏ cho phiên đăng nhập; SQLite lưu các bảng và quan hệ.

**Sao nhân viên không thấy công/lương của người khác?** Màn hình lấy employeeId từ tài khoản đang đăng nhập, rồi truyền ID đó vào truy vấn. Admin truyền 0 để xem toàn bộ.

**Sao tách XML?** XML mô tả giao diện; Kotlin xử lý sự kiện và dữ liệu. Dễ tìm phần cần sửa hơn.

**Sửa lương giờ thì sao?** Xóa bản lương Chưa trả để không hiển thị số tiền cũ, rồi Admin tính lại. Bài đơn giản dùng một lương giờ hiện tại, chưa có lịch sử thay đổi lương.

**Vì sao code còn các test?** Chúng nằm riêng trong src/test để kiểm tra CRUD và tính tiền, không chạy cùng app và không phải kiến trúc của app.

## 7. Phạm vi bài này

Một ca/ngày, cùng ngày; dữ liệu trên một thiết bị; đồng hồ theo thiết bị. Trạng thái Đã trả chỉ là nhãn, không khóa sửa hay thực hiện chuyển tiền. Mật khẩu dạng văn bản như mẫu lớp, chỉ dùng dữ liệu giả. Không thêm các luồng doanh nghiệp ngoài yêu cầu thực hành.

## 8. Một vài cú pháp Kotlin đang dùng

- `data class`: lớp chứa dữ liệu một dòng, tự có hàm copy để sửa dữ liệu thuận tiện.
- `cursor.use { ... }`: đọc xong tự đóng Cursor.
- `ContentValues().apply { put(...) }`: tạo đối tượng rồi điền các cặp cột/giá trị.
- `old?.id ?: 0`: lấy ID cũ nếu đang sửa; nếu không có thì dùng 0 để thêm mới.
- `sumOf { it.hours }`: cộng trường hours của mọi dòng.
- `require(...)`/`check(...)`: sai điều kiện thì báo lỗi; BaseActivity.action bắt lỗi và hiện Toast.
