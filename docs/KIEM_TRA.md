# Kết quả kiểm tra bản Kotlin + XML

Ngày 24/10/2026. JDK 17, AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9. Compile SDK 35, target SDK 34, min SDK 26.

- Build `assembleDebug`: **THÀNH CÔNG**.
- `testDebugUnitTest`: **9/9 ĐẠT**, không lỗi, không bỏ qua.
- `lintDebug`: **0 lỗi**, có các cảnh báo khuyến nghị như tách chuỗi XML sang strings.xml, autofill, icon/backup và target API. Bản lớp học giữ giao diện và cấu hình cơ bản; không tắt kiểm tra lỗi của Lint.
- Source app: **422 dòng Kotlin**, 9 file Kotlin và 7 layout XML. Các test nằm riêng, không thuộc luồng chạy app.
- Kiểm tra trên JVM bằng Robolectric API 28; chưa kiểm tra trên điện thoại thật. APK nằm ở `apk/PayrollSimple-debug.apk`.

## 9 test đã chạy

1. Đăng nhập sai/đúng, tham số SQL và SharedPreferences.
2. Thêm–xem–sửa–tìm–xóa nhân viên và tài khoản tương ứng.
3. Mã trùng thì rollback, không dư tài khoản.
4. Kiểm tra email, mật khẩu, lương giờ.
5. Check-in/out, chặn trùng/sai giờ, tự tính lương.
6. Ca còn thiếu chặn lương/check-in tiếp; Admin hoàn tất được ca.
7. Tính lương mẫu, đổi trạng thái, xóa bản lương.
8. Xóa nhân viên kéo theo chấm công và lương.
9. Mở layout chấm công/lương/CRUD; nhân viên không thấy công người khác; mở được form thêm nhân viên.

Báo cáo có sẵn: `test-report/index.html`; log build: `build-log.txt`.

SHA-256 của APK:

```text
9ac30514d7a172f9d28698f2b27b9bc6bd827e267f811d2ea12cf517fc39fdff
```
