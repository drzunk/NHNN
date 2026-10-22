package org.example.nhnn.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Man hinh "Tra ket qua" TDKT (tabid=242) - diem cuoi cua chuoi duyet. Hanh
 * dong "tra ket qua" that su (dong ho so) CHUA duoc xac nhan selector qua
 * script Playwright da doc duoc, nen chi cung cap kha nang kiem tra ho so
 * da toi noi, khong gia dinh them hanh dong nao khac.
 */
public final class TraKetQuaTdktPage extends BasePage {

    private static final int TABID = 242;

    public TraKetQuaTdktPage(Page page) {
        super(page);
    }

    public void moDanhSach() {
        page.navigate("http://10.14.242.16:9292/Default.aspx?tabid=" + TABID);
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    public boolean hoSoCoTrongDanhSach(String maHoSo) {
        return page.locator(String.format("tr:has-text(\"%s\")", maHoSo)).count() > 0;
    }
}
