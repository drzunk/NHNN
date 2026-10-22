package org.example.nhnn.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.example.nhnn.helpers.RadDropDownTreeHelper;
import org.example.nhnn.helpers.RadGridHelper;

/**
 * Man hinh "Lanh dao phong phan cong" (tabid=238) - TDKT. Cung co che
 * grid + checkbox + RadDropDownTree + nut chuyen nhu 237/239/240/241 (xem
 * RadDropDownTreeHelper).
 */
public final class PhanCongPage extends BasePage {

    private static final int TABID = 238;
    private static final String CHECKBOX_CLASS = "chkHSChuyen";
    private static final String DROPDOWN_ID_SUBSTRING = "ddtNguoiNhan";
    private static final String SUBMIT_BUTTON_ID_SUBSTRING = "cmdChuyen";

    public PhanCongPage(Page page) {
        super(page);
    }

    public void moDanhSach() {
        page.navigate("http://10.14.242.16:9292/Default.aspx?tabid=" + TABID);
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    public void chuyenToiThuLy(String maHoSo, String tenNguoiNhan) {
        RadGridHelper.tickRowCheckbox(page, maHoSo, CHECKBOX_CLASS);
        RadDropDownTreeHelper.chonNguoiNhanVaChuyen(page, DROPDOWN_ID_SUBSTRING, tenNguoiNhan, SUBMIT_BUTTON_ID_SUBSTRING);
    }
}
