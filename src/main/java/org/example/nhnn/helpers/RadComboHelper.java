package org.example.nhnn.helpers;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Chon item trong mot Telerik RadComboBox bang client-side API
 * (window.$find(id).findItemByValue(value).select()) thay vi click UI,
 * vi gia tri that cua tung item khong nam trong HTML tinh (phai lay qua
 * RadComboBoxItemCollection luc runtime). Day la pattern da duoc agent
 * Antigravity dung thanh cong truoc day (xem scratch/pw/cntt_create_dossier.js).
 *
 * Vi id control DNN thay doi theo tung session (dnn_ctr&lt;N&gt;_...), ham nay
 * tu do tim clientId chinh xac dua tren mot doan id on dinh (ten User Control
 * + ten field, vi du "TiepNhanHoSoForm_cmbLoaiHoSoID").
 */
public final class RadComboHelper {

    private RadComboHelper() {
    }

    public static void selectByValue(Page page, String stableIdSubstring, String value) {
        select(page, stableIdSubstring, value, "findItemByValue");
    }

    /** Dung khi chua biet ma (value) that cua item, chi biet text hien thi. */
    public static void selectByText(Page page, String stableIdSubstring, String text) {
        select(page, stableIdSubstring, text, "findItemByText");
    }

    /**
     * "Bay 1" (SOP muc 4): item.select() kich hoat __doPostBack dong bo.
     * Ben trong, ma Telerik/ASP.NET AJAX da bien dich truy cap
     * arguments/caller khi duyet call stack cua postback - neu stack do bat
     * nguon tu mot closure trong page.evaluate() (luon strict mode), V8 nem
     * loi "'caller','callee','arguments' properties may not be accessed on
     * strict mode functions". Boc trong setTimeout(..., 0) KHONG du, vi
     * callback cua setTimeout van la closure duoc dinh nghia ben trong
     * evaluate() - van la ham strict mode, chi doi thoi diem goi chu khong
     * doi ngu canh goi.
     *
     * Cach xu ly: tach lam hai buoc. (1) resolve + kiem tra combo/item ton
     * tai bang mot evaluate() thuan tuy, khong goi select(). (2) kich hoat
     * that su .select() tu mot the &lt;script&gt; duoc chen vao trang - chay
     * nhu mot script toan cuc (khong strict), cat dut hoan toan lien ket
     * closure ve evaluate(), nen _doPostBack khong con dung phai ancestor
     * strict-mode nua.
     */
    private static void select(Page page, String stableIdSubstring, String criterion, String finderMethod) {
        String clientId = resolveClientId(page, stableIdSubstring);

        Object lookup = page.evaluate(
                "([id, criterion, finderMethod]) => { "
                        + "const combo = window.$find(id); "
                        + "if (!combo) { return 'KHONG_TIM_THAY_COMBO'; } "
                        + "const item = combo[finderMethod](criterion); "
                        + "if (!item) { return 'KHONG_TIM_THAY_ITEM'; } "
                        + "return 'OK'; "
                        + "}",
                new Object[] {clientId, criterion, finderMethod});
        if (!"OK".equals(lookup)) {
            throw new IllegalStateException(
                    "Chon RadComboBox that bai (" + stableIdSubstring + "=" + criterion + "): " + lookup);
        }

        page.evaluate(
                "([id, criterion, finderMethod]) => { "
                        + "const s = document.createElement('script'); "
                        + "s.textContent = 'window.$find(' + JSON.stringify(id) + ')'"
                        + "        + '[' + JSON.stringify(finderMethod) + '](' + JSON.stringify(criterion) + ').select();'; "
                        + "document.head.appendChild(s); "
                        + "s.remove(); "
                        + "}",
                new Object[] {clientId, criterion, finderMethod});

        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    private static String resolveClientId(Page page, String stableIdSubstring) {
        Object id = page.evaluate(
                "(sub) => { "
                        + "const el = document.querySelector("
                        + "  `div.RadComboBox[id*='${sub}']:not([id$='_DropDown'])`); "
                        + "return el ? el.id : null; "
                        + "}",
                stableIdSubstring);
        if (id == null) {
            throw new IllegalStateException("Khong tim thay RadComboBox voi id chua: " + stableIdSubstring);
        }
        return String.valueOf(id);
    }
}
