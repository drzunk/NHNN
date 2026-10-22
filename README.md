# NHNN Automation Test Framework

Framework automation test bằng Java + Maven + Playwright, kiểm thử hệ thống
DVC-NHNN (ASP.NET WebForms / DotNetNuke / Telerik RadControls) — 2 luồng
nghiệp vụ: Chứng thư số (CTS, tài khoản Cục CNTT) và Thi đua khen thưởng
(TDKT, tài khoản Vụ TCCB).

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
3. **Không hard-code bí mật** (tài khoản, mật khẩu, URL nội bộ nhạy cảm)
   trong source code — luôn đọc từ `config/.env`.

> **Lịch sử thay đổi (24/10/2026)**: ràng buộc "không OCR captcha" từng có ở
> đây đã được **gỡ bỏ theo yêu cầu tường minh của người dùng** (xem git log).
> `LoginBootstrapperApp` giờ thử tự đọc captcha bằng `CaptchaOcrHelper` (OCR
> tự xây dựng, xem Javadoc class đó) trước khi rơi về cơ chế chờ người nhập
> tay. Độ chính xác đo được trên 9 mẫu captcha thật: **8/9 (~89%)** — chưa
> tuyệt đối, vẫn còn rủi ro đoán sai (có cơ chế tự dừng sau 3 lần thử để
> tránh khoá tài khoản, xem `LoginBootstrapperApp.MAX_CAPTCHA_ATTEMPTS`). Có
> thể tắt OCR bằng `CAPTCHA_OCR_ENABLED=false` trong `config/.env`.

## Cấu trúc

```
src/main/java/org/example/nhnn/
  config/   EnvConfig — đọc config/.env
  session/  BrowserFactory, SessionManager, LoginBootstrapperApp, DomExplorerApp
  pages/    BasePage, TiepNhanHoSoPage (tabid=37), ThuLyHoSoPage (tabid=54, CTS),
            TiepNhanThuongTrucPage/PhanCongPage/ThuLyTdktPage/
            DuyetPheDuyetTdktPage/TraKetQuaTdktPage (tabid 237-242, TDKT)
  helpers/  PostbackHelper, RadComboHelper, RadGridHelper, RadDropDownTreeHelper,
            EformFrameHelper, DownloadHelper — xử lý các đặc thù ASP.NET
            WebForms / Telerik RadControls
  verify/   WordVerifier (Apache POI), WordRenderHelper (Word COM, tuỳ chọn)
  report/   ReportEntry, ReportWriter, ScreenshotHelper

src/test/java/org/example/nhnn/
  testsupport/ BaseTest, BaseDvcNhnnTest (tài khoản Cục CNTT, CTS),
               BaseTdktTest (tài khoản Vụ TCCB, TDKT), ScreenshotOnFailureExtension
  smoke/       FrameworkSmokeTest
  suites/cts/  CtsTiepNhanTest, CtsXmlCauHinhTest, CtsThongBaoWordTest
  suites/tdkt/ TdktLuong2EndToEndTest
```

## Thiết lập lần đầu

1. Cài Chromium cho Playwright (chạy một lần):
   ```
   mvn exec:java -Dexec.mainClass="com.microsoft.playwright.CLI" -Dexec.args="install chromium"
   ```
2. Sao chép `config/.env.example` thành `config/.env`, điền tài khoản thật.
   File này nằm trong `.gitignore`, không bao giờ commit.

## Thiết lập session (storageState) cho DVC-NHNN

Hệ thống DVC-NHNN yêu cầu captcha ảnh 3 chữ số khi đăng nhập. Framework thử
tự đọc captcha bằng OCR (xem cảnh báo độ chính xác ở mục ràng buộc an toàn
phía trên); nếu OCR không chắc chắn sẽ rơi về chờ người nhập tay. Dù đăng
nhập bằng cách nào, phiên đăng nhập được lưu lại (`storageState`) để các lần
chạy test sau tái sử dụng. Có 2
tài khoản/phiên độc lập trên cùng 1 trang đăng nhập (tabid=38) — Cục CNTT
cho suite CTS, Vụ TCCB cho suite TDKT:

```
mvn exec:java -Dexec.mainClass="org.example.nhnn.session.LoginBootstrapperApp" -Dexec.args="dvc-nhnn"
mvn exec:java -Dexec.mainClass="org.example.nhnn.session.LoginBootstrapperApp" -Dexec.args="dvc-nhnn-tccb"
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

Các suite nghiệp vụ thật cần storageState hợp lệ tương ứng (xem mục trên):

```
mvn -q test -Dtest=CtsTiepNhanTest,CtsXmlCauHinhTest,CtsThongBaoWordTest   # can storageState dvc-nhnn
mvn -q test -Dtest=TdktLuong2EndToEndTest                                 # can storageState dvc-nhnn-tccb
```

Báo cáo Markdown + screenshot của mỗi lần chạy được ghi vào `reports/<runId>/`
(thư mục này nằm trong `.gitignore`).

## Phạm vi hiện tại — chưa bao gồm

- **TDKT Luồng 1** (bắt đầu từ tabid=37 giống CTS) — chưa xác nhận được
  combo value của thủ tục TDKT tại tabid=37 (tương tự lỗ hổng combo "nhóm 3"
  của CTS). Suite TDKT hiện chỉ có Luồng 2 (bắt đầu từ tabid=237).
- **TDKT Luồng 5** (Yêu cầu bổ sung hồ sơ) và **Luồng 7** (Lãnh đạo phòng
  không phê duyệt) — chưa tìm thấy DOM/selector xác nhận cho 2 nhánh nhập lý
  do/từ chối này.
- Hành động "Trả kết quả" thật sự (đóng hồ sơ) ở tabid=242 — chưa xác nhận
  selector, `TraKetQuaTdktPage` hiện chỉ kiểm tra được hồ sơ đã tới danh sách.
- `ThuLyTdktPage.chuyenNhanhToiTraKetQua()` dùng cơ chế chuyển-thẳng đã xác
  nhận chạy được qua script Playwright cũ, nhưng **chưa xác nhận** đây có
  đúng là nghiệp vụ "Luồng 4 — Không giải quyết" mô tả trong SOP hay không
  (SOP mô tả một context-menu + ô nhập lý do riêng) — không gán nhãn luồng
  nghiệp vụ cho tới khi xác nhận lại.
- Đồng bộ báo cáo lên Google Drive.
- `WordRenderHelper.renderToPng()` (rasterize PDF→ảnh) — còn là stub, xem
  TODO trong code.

Xem `docs/TEST_GUIDELINES.md` để biết checklist khi viết test case mới.
