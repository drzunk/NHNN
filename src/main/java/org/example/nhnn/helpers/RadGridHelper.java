package org.example.nhnn.helpers;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Xu ly "Bay 2" trong SOP: click thang vao link ma ho so (lbSoBienNhan) tren
 * RadGrid (vi du tabid=54) hay bi timeout "element is not visible" vi Telerik
 * freeze column hoac dat trong the span long nhau.
 *
 * Cach xu ly chuan: click vao span/mã ho so de bung RadContextMenu, roi click
 * vao item menu mong muon (vi du "Xu ly").
 */
public final class RadGridHelper {

    private RadGridHelper() {
    }

    public static void openViaContextMenu(Page page, String maHoSoOrRowText, String menuItemText) {
        Locator row = page.locator(
                String.format("span:has-text(\"%s\"), a:has-text(\"%s\")", maHoSoOrRowText, maHoSoOrRowText)
        ).first();
        row.click();

        Locator menuItem = page.locator(
                String.format(".rmItem:has-text(\"%s\"), .rmLink:has-text(\"%s\")", menuItemText, menuItemText)
        ).first();
        menuItem.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        menuItem.click();
        page.waitForLoadState();
    }
}
