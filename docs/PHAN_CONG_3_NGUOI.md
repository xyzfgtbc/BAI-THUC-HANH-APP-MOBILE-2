# Phân công đề xuất cho nhóm 3 người

Điền tên thành viên và điều chỉnh theo phần mỗi người thực sự triển khai, kiểm tra và giải thích. Phân công theo khối lượng chức năng, không chia máy móc theo số file.

| Thành viên | Phần việc chính (khoảng 1/3/người) | File / phạm vi | Nội dung cần trình bày |
|---|---|---|---|
| Người 1: … | Khung project, tạo 4 bảng, đăng nhập, lưu phiên, menu; giao diện xem/quản lý lương | Cấu hình Gradle, DBHelper: onCreate/onConfigure/login/account/employeeId; LoginActivity, HomeActivity, SessionManager, BaseActivity, SalaryActivity và XML tương ứng | Khóa chính/khóa ngoại; đăng nhập phân vai; lưu phiên; danh sách lương và cập nhật trạng thái |
| Người 2: … | Toàn bộ CRUD, tìm kiếm và kiểm tra dữ liệu nhân viên; kiểm thử luồng thêm/sửa/xóa | Models; DBHelper: employees/saveEmployee/deleteEmployee; EmployeeActivity; activity_employee.xml, dialog_employee.xml | SELECT/INSERT/UPDATE/DELETE; click dòng đổ lên form; tạo tài khoản cùng nhân viên; kiểm tra trùng; xóa liên quan |
| Người 3: … | Check-in/out, lịch sử và tổng giờ tháng, sửa ca thiếu, thuật toán tính lương; kiểm thử giờ và tiền | DBHelper: logs/checkIn/checkOut/finishLog/calculateSalary/salaries/updateSalaryStatus/deleteSalary; AttendanceActivity và XML tương ứng | Cách chống chấm công trùng; tính chênh lệch giờ; kiểm tra ca thiếu; ngưỡng 8h và hệ số tăng ca 1,5 |

Người 1 phụ trách **giao diện quản lý lương**, người 3 phụ trách **truy vấn và công thức lương** để cân đối khối lượng. DBHelper có chú thích PHẦN 1, 2, 3; thống nhất chữ ký hàm trước khi ghép code.

## Cách ghép bài đơn giản

1. Người 1 tạo khung, schema, tài khoản mẫu và màn đăng nhập/menu.
2. Người 2 làm branch `employee-crud`, người 3 làm branch `attendance-salary` từ khung chung.
3. Người 1 bổ sung SalaryActivity; người 3 thống nhất hàm trả danh sách Salary cho màn hình này.
4. Cả nhóm ghép các hàm DBHelper, cùng chạy demo từ đầu đến cuối. Mỗi người đọc phần của mình và hiểu đầu vào/đầu ra của phần liên quan.

## Mỗi người tự kiểm tra trước khi báo cáo

- Người 1: sai/đúng mật khẩu; mở lại app còn phiên; đăng xuất; Admin/Employee có menu khác nhau; xem/đổi trạng thái lương.
- Người 2: thêm đủ thông tin, nhập thiếu, mã/tài khoản trùng, tìm theo tên/mã, sửa giữ mật khẩu cũ, xóa và kiểm tra tài khoản mất theo.
- Người 3: chưa check-in mà checkout; check-in hai lần; giờ ra sai; ca còn thiếu; tính 9 giờ × 50.000 với một giờ tăng ca ra 475.000 VNĐ.
