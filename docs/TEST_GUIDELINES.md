# Checklist khi viết test case (Phase 2 trở đi)

Tổng hợp từ skill `test-guard` (Antigravity) — áp dụng cho mọi test JUnit
viết trong framework này.

1. **Test hành vi, không test implementation.** Assert trên những gì người
   dùng thấy/hệ thống trả về (trạng thái UI, nội dung file tải về, toast
   thông báo), không assert vào chi tiết nội bộ không ai quan tâm.
2. **Mock phải có lý do rõ ràng.** Framework này ưu tiên test thật trên
   môi trường DVC-NHNN qua UI thật — chỉ mock khi có lý do kỹ thuật cụ
   thể (ví dụ cô lập một bước chậm/không ổn định), ghi lý do trong comment.
3. **Một kịch bản cho một test.** Tránh nhồi nhiều luồng nghiệp vụ vào một
   `@Test` — mỗi nhánh (ví dụ "có XML" / "thiếu XML") nên là một test riêng.
4. **Xoá ngay test không còn ý nghĩa**, đừng giữ lại "cho chắc".
5. **Đặt tên test theo kịch bản nghiệp vụ**, ví dụ
   `capCtsToChuc_thieuEform_hienThiToastLoi()` thay vì `test1()`.
6. **Giữ lại test regression thật** — khi một bug đã từng xảy ra trên
   DVC-NHNN, thêm test tái hiện đúng kịch bản đó, không xoá sau khi fix.
7. **Không test lại chính framework** (ví dụ không viết test kiểm tra
   `WordVerifier.headerContains()` hoạt động đúng với Apache POI — đó là
   trách nhiệm của thư viện, hãy tin tưởng framework, chỉ test nghiệp vụ).
8. **Không mock các đối tượng trạng thái** (`Page`, `BrowserContext`) —
   những thứ này phải là thật vì bản chất của automation test là xác nhận
   hành vi trên trình duyệt thật.
9. **Dùng hạ tầng thật cho test hạ tầng** — ví dụ khi test chính
   `SessionManager`/`ScreenshotHelper` (nếu có viết test cho framework), dùng
   Playwright thật thay vì giả lập.

Hai ràng buộc an toàn cứng (xem `README.md`) luôn áp dụng, không có ngoại lệ:
không chạm Chrome thật của người dùng, không ghi/sửa/xoá dữ liệu DB.
