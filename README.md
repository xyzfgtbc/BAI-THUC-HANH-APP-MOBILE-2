# BAI-THUC-HANH-APP-MOBILE-2

## Phân công công việc

- **Kiều Đức Nguyên**: Database, đăng nhập, lưu phiên, menu và điều hướng theo vai trò
- **Lê Hải Đăng**: CRUD nhân viên: thêm, xem, sửa, xóa, tìm kiếm
- **Lê Ngọc Minh**: Chấm công, lịch sử, tính và quản lý lương; kiểm tra các luồng chính

---

# Bài thực hành: Quản lý chấm công và lương

**Kotlin + XML + SQLite**, tổ chức gần mẫu MPTest2Complete: Activity gọi thẳng DBHelper, lưu phiên bằng SharedPreferences. Giao diện dùng form và ListView; không dùng Compose, MVVM, Repository hay server.

## Mở và chạy

1. Giải nén, mở thư mục **PayrollSimple** bằng Android Studio.
2. Dùng Gradle JDK **17**, cài Android SDK Platform 35 và Build-Tools khi Studio yêu cầu, chờ Sync.
3. Mở emulator/điện thoại Android **8.0 (API 26)** trở lên, chọn **app → Run**.

Có đủ `gradlew`, `gradlew.bat`, `gradle-wrapper.jar`, cấu hình Gradle 8.9. Không cần cài Gradle riêng. Lần Sync đầu cần Internet; JDK và Android SDK không nằm trong ZIP.

APK có sẵn: `apk/PayrollSimple-debug.apk`. Dùng applicationId riêng nên có thể cài song song với bản Java cũ.

## Tài khoản mẫu

| Vai trò | Tài khoản | Mật khẩu |
|---|---|---|
| Admin | admin | 123456 |
| Nhân viên | nv01 | 123456 |

Dữ liệu mẫu: một nhân viên, một ca **08:00–17:00 ngày 01 của tháng trước**. Admin vào Lương tháng, chọn tháng trước rồi bấm Tính lương: 9 giờ làm, 1 giờ tăng ca, **475.000 VNĐ**. Tháng hiện tại để trống để thử check-in/out.

## Chức năng

- Đăng nhập Admin/Employee, ghi nhớ bằng SharedPreferences; đăng xuất xóa phiên.
- Admin: **thêm, xem, sửa, xóa, tìm nhân viên**, gồm lương giờ và tài khoản đăng nhập.
- Nhân viên: check-in/out; tự tính giờ và lương khi checkout; xem công/lương của mình.
- Admin: xem công theo tháng, sửa giờ/hoàn tất ca quên checkout, tính lương tháng, đổi trạng thái hoặc xóa bản lương.
- Chặn trùng mã/tài khoản, check-in trùng, giờ ra không sau giờ vào, tính lương khi ca chưa hoàn tất.

## Cách demo ngắn

1. Admin → Nhân viên → thêm NV02, sửa họ tên/lương, tìm NV02, chạm dòng để xem rồi xóa.
2. Đăng xuất → nv01 → Chấm công → Check-in → chờ vài giây → Check-out → Xem tháng.
3. Lương tháng → xem số giờ, tăng ca và tiền vừa được tính.
4. Admin → Chấm công → chọn tháng trước để xem ca mẫu; Lương tháng → tính và đổi trạng thái.

## Quy tắc dễ giải thích

- Một ca/ngày, giờ vào/ra cùng ngày; tổng giờ = số giây giữa hai thời điểm / 3600.
- Giờ thường/ngày tối đa 8; tăng ca/ngày = max(giờ làm − 8, 0), hệ số **1,5**.
- Lương tháng = giờ thường × lương giờ + tăng ca × lương giờ × 1,5; làm tròn tiền cuối.
- Ca chưa checkout chặn chấm công tiếp; nhờ Admin nhập giờ ra đúng ngày cũ.
- Tính lương lại theo lương giờ hiện tại. Trạng thái “Đã trả” chỉ là nhãn quản lý, không khóa dữ liệu hay chuyển tiền.
- Sửa nhân viên xóa lương “Chưa trả” của người đó để tránh hiện số liệu cũ; Admin tính lại. Xóa nhân viên xóa cả tài khoản, công và lương qua khóa ngoại.
- Giống mẫu lớp: mật khẩu lưu dạng văn bản trong SQLite để dễ theo dõi bài thực hành, chỉ dùng tài khoản giả. Không dùng cách lưu này cho ứng dụng thực tế.

## File cần đọc

- `docs/PHAN_CONG_3_NGUOI.md`: phân công đề xuất, khoảng 1/3 công việc/người.
- `docs/GIAI_THICH_NGAN.md`: luồng chạy, CRUD và các câu hỏi cơ bản.
- `docs/KIEM_TRA.md`: kết quả build/test của bản ZIP.

## Chạy test và Git

Sau khi Sync, chạy `RUN_TESTS.bat` (Windows), `sh RUN_TESTS.sh` (Linux/macOS), hoặc:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Test nằm riêng ở `app/src/test`, không cần học Robolectric để giải thích source app. Báo cáo sau khi chạy: `app/build/reports/tests/testDebugUnitTest/index.html`.

Đề còn yêu cầu GitHub/GitLab. Nhóm tạo repo của mình rồi chạy:

```bash
git init
git add .
git commit -m "Bai thuc hanh Kotlin XML SQLite"
git branch -M main
git remote add origin URL_REPO_CUA_NHOM
git push -u origin main
```

Mỗi người làm một branch theo phân công rồi merge. Chưa có repo đích nên bản ZIP không tự đăng lên GitHub/GitLab. Không commit `local.properties`, `.gradle` hoặc `build`.
