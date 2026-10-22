# NHNN Automation Test Framework (Phase 1 — Khung)

Framework automation test bằng Java + Maven + Playwright, kiểm thử hệ thống
DVC-NHNN (ASP.NET WebForms / DotNetNuke / Telerik RadControls) và QLCV.

Tài liệu nguồn kiến trúc: `G:\My Drive\SOP_Automation_Testing_QLCV_NHNN_Cho_Opus.md`
(được "dịch" từ kiến trúc Node.js/Playwright sang Java).

## Ràng buộc an toàn cứng (BẮT BUỘC, không được vi phạm)

1. **Chỉ dùng Chromium độc lập do Playwright khởi chạy** (`headless(true)`).
   Không bao giờ đính kèm/điều khiển/kill trình duyệt Chrome thật của người
   dùng, không dùng Chrome DevTools MCP cho mục đích test.
2. **Không bao giờ ghi/sửa/xoá dữ liệu trực tiếp vào bất kỳ database nào**
   (không `INSERT/UPDATE/DELETE/DROP`...). Mọi hành vi nghiệp vụ phải được
   mô phỏng qua giao diện Web. Nếu cần đối chiếu dữ liệu, chỉ đọc (`SELECT`)
   qua kết nối read-only.
3. **Không OCR captcha.** Đăng nhập hệ thống DVC-NHNN cần nhập captcha thủ
   công (xem mục "Thiết lập session" bên dưới).
4. **Không hard-code bí mật** (tài khoản, mật khẩu, URL nội bộ nhạy cảm)
   trong source code — luôn đọc từ `config/.env`.

## Cấu trúc

```
src/main/java/org/example/nhnn/
  config/   EnvConfig — đọc config/.env
  session/  BrowserFactory, SessionManager, LoginBootstrapperApp
  pages/    BasePage — lớp cha Page Object (Phase 2 mở rộng)
  helpers/  PostbackHelper, RadGridHelper, EformFrameHelper, DownloadHelper
            — xử lý các đặc thù ASP.NET WebForms / Telerik RadControls
  verify/   WordVerifier (Apache POI), WordRenderHelper (Word COM, tuỳ chọn)
  report/   ReportEntry, ReportWriter, ScreenshotHelper

src/test/java/org/example/nhnn/
  testsupport/ BaseTest, BaseDvcNhnnTest, BaseQlcvTest, ScreenshotOnFailureExtension
  smoke/       FrameworkSmokeTest
```

## Thiết lập lần đầu

1. Cài Chromium cho Playwright (chạy một lần):
   ```
   mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install chromium"
   ```
2. Sao chép `config/.env.example` thành `config/.env`, điền tài khoản thật.
   File này nằm trong `.gitignore`, không bao giờ commit.

## Thiết lập session (storageState) cho DVC-NHNN / QLCV

Hệ thống DVC-NHNN yêu cầu captcha ảnh 3 chữ số khi đăng nhập và không có
OCR tự động trong framework này. Thay vào đó, đăng nhập một lần và lưu lại
phiên đăng nhập (`storageState`) để các lần chạy test sau tái sử dụng:

```
mvn exec:java -Dexec.mainClass="org.example.nhnn.session.LoginBootstrapperApp" -Dexec.args="dvc-nhnn"
mvn exec:java -Dexec.mainClass="org.example.nhnn.session.LoginBootstrapperApp" -Dexec.args="qlcv"
```

Chương trình sẽ lưu ảnh captcha vào `config/state/captcha/dvc-nhnn-captcha.png`
rồi **chờ một file trả lời** xuất hiện tại
`config/state/captcha/dvc-nhnn-captcha-answer.txt` (chờ tối đa 5 phút, mỗi
giây kiểm tra 1 lần). Bạn chỉ cần mở ảnh captcha, đọc 3 chữ số, rồi tạo file
`.txt` đó với đúng nội dung mã captcha (không cần xuống dòng). Cách làm này
thay cho việc gõ trực tiếp vào console, vì nhiều môi trường chạy agent không
có stdin tương tác. Sau khi đăng nhập thành công, file
`config/state/<system>.storageState.json` được tạo — các lần `mvn test`
sau sẽ tự nạp lại, không cần đăng nhập lại cho tới khi session hết hạn.

Nếu một bài test tự động phát hiện session đã hết hạn, nó sẽ dừng lại với
thông báo yêu cầu chạy lại `LoginBootstrapperApp` — các suite test tự động
(`mvn test`) sẽ không bao giờ tự mở luồng đăng nhập tương tác.

## Chạy test

```
mvn -q compile
mvn -q test -Dtest=FrameworkSmokeTest
```

`FrameworkSmokeTest` chỉ kiểm tra khung framework hoạt động được (mở
browser, chụp ảnh, ghi báo cáo) — không đăng nhập hệ thống thật, không cần
`config/.env` có giá trị thật.

Báo cáo Markdown + screenshot của mỗi lần chạy được ghi vào `reports/<runId>/`
(thư mục này nằm trong `.gitignore`).

## Phạm vi Phase 1 (khung) — chưa bao gồm

- Test case nghiệp vụ cụ thể (cấu hình XML Chứng thư số, mẫu thông báo Word,
  luồng Thi đua khen thưởng đa cấp...).
- Tự động đọc task từ QLCV.
- Đồng bộ báo cáo lên Google Drive.
- Xác nhận selector DOM thật của DVC-NHNN/QLCV (các helper hiện dùng selector
  placeholder, đánh dấu `// TODO` trong code).

Xem `docs/TEST_GUIDELINES.md` để biết checklist khi viết test case ở Phase 2.
