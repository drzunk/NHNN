package org.example.nhnn.helpers;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.Map;
import java.util.Optional;

/**
 * Xu ly "Bay 3" trong SOP: popup To khai dien tu E-form la mot iframe tro
 * toi service E-form doc lap (tokhaidientu...), long ben trong mot Telerik
 * RadWindow (wndCollectEform).
 *
 * Viec do frame xuat hien van phai polling (page.frames() khong co su kien
 * "frame appeared" de Playwright tu dong cho), nhung day van la mot vong
 * lap CHO CO DIEU KIEN (kiem tra dieu kien moi 300ms, co gioi han thoi
 * gian), khong phai mot khoang cho cung co dinh.
 */
public final class EformFrameHelper {

    private static final double FRAME_WAIT_TIMEOUT_MS = 30_000;
    private static final long FRAME_POLL_INTERVAL_MS = 300;

    private EformFrameHelper() {
    }

    /**
     * @param openIconSelector      selector cua icon mo Eform (vi du
     *                               "input[name*='Eform_CongChuc']").
     * @param fieldsByNameContains  map tu doan chuoi chua trong thuoc tinh
     *                               name/placeholder cua input (vi du "hoTen")
     *                               sang gia tri can dien.
     * @param saveButtonText        text hien thi tren nut luu (vi du "Lưu tờ khai").
     */
    public static void fillAndSave(Page page, String openIconSelector,
                                    Map<String, String> fieldsByNameContains, String saveButtonText) {
        page.locator(openIconSelector).first().click();

        Frame eformFrame = waitForEformFrame(page);
        if (eformFrame == null) {
            throw new IllegalStateException(
                    "Khong tim thay Eform frame (url chua 'tokhaidientu') sau "
                            + (long) FRAME_WAIT_TIMEOUT_MS + "ms.");
        }

        for (Map.Entry<String, String> field : fieldsByNameContains.entrySet()) {
            String selector = String.format(
                    "input[name*='%s'], input[placeholder*='%s']", field.getKey(), field.getKey());
            eformFrame.fill(selector, field.getValue());
        }

        eformFrame.locator(String.format("button:has-text(\"%s\")", saveButtonText)).click();
        // Cho dieu kien: mang (XHR luu to khai) tro ve trang thai idle, thay
        // vi cho cung mot khoang thoi gian co dinh.
        eformFrame.waitForLoadState(LoadState.NETWORKIDLE,
                new Frame.WaitForLoadStateOptions().setTimeout(FRAME_WAIT_TIMEOUT_MS));
    }

    private static Frame waitForEformFrame(Page page) {
        long deadline = System.currentTimeMillis() + (long) FRAME_WAIT_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            Optional<Frame> found = page.frames().stream()
                    .filter(f -> f.url().contains("tokhaidientu"))
                    .findFirst();
            if (found.isPresent()) {
                return found.get();
            }
            page.waitForTimeout(FRAME_POLL_INTERVAL_MS);
        }
        return null;
    }
}
