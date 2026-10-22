package org.example.nhnn.helpers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Xu ly Telerik RadDropDownTree, dung xuyen suot cac man hinh chuyen ho so
 * TDKT (Thi dua khen thuong): tabid 238/239/240/241. Khac RadComboBox (Bay 1
 * trong SOP), control nay khong can qua "bay" strict-mode vi hanh dong chon
 * node la 1 click that (Playwright .click()) chu khong phai goi .select()
 * dong bo trong page.evaluate().
 *
 * Selector lay tu script Playwright da chay that cua agent Antigravity
 * truoc day (id*='ddtNguoiNhan', class .RadTreeView), khong phai doan.
 */
public final class RadDropDownTreeHelper {

    private RadDropDownTreeHelper() {
    }

    /**
     * Mo RadDropDownTree bang client API (an toan, chi la thao tac UI mo
     * khung, khong kich hoat postback), roi click that vao node co text
     * khop, cuoi cung click nut chuyen (id chua submitButtonIdSubstring).
     */
    public static void chonNguoiNhanVaChuyen(
            Page page, String dropDownIdSubstring, String tenNguoiNhan, String submitButtonIdSubstring) {
        String clientId = resolveClientId(page, dropDownIdSubstring);
        page.evaluate("(id) => { const ddt = window.$find(id); if (ddt) { ddt.openDropDown(); } }", clientId);

        Locator node = page.locator(".RadTreeView label:has-text(\"" + tenNguoiNhan + "\")").first();
        node.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        node.click();

        page.locator("[id*='" + submitButtonIdSubstring + "']").first().click();
        page.waitForLoadState();
    }

    private static String resolveClientId(Page page, String stableIdSubstring) {
        Object id = page.evaluate(
                "(sub) => { "
                        + "const el = document.querySelector(`[id*='${sub}']`); "
                        + "return el ? el.id : null; "
                        + "}",
                stableIdSubstring);
        if (id == null) {
            throw new IllegalStateException("Khong tim thay RadDropDownTree voi id chua: " + stableIdSubstring);
        }
        return String.valueOf(id);
    }
}
