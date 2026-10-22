package org.example.nhnn.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.example.nhnn.helpers.RadDropDownTreeHelper;
import org.example.nhnn.helpers.RadGridHelper;

/**
 * Man hinh duyet TDKT - dung CHUNG 1 form (DanhSachPheDuyetThiDuaKhenThuongForm)
 * o ca tabid=240 (Lanh dao phong phe duyet) va tabid=241 (Lanh dao don vi
 * phe duyet), da xac nhan qua script Playwright thuc te (chi khac prefix
 * dnn_ctrNN theo session, dung quy uoc id*='...' nhu moi noi khac trong
 * project). Truyen tabid qua constructor de tai dung 1 class cho ca 2 man
 * hinh thay vi 2 class gan nhu giong het nhau.
 */
public final class DuyetPheDuyetTdktPage extends BasePage {

    private static final String CHECKBOX_CLASS = "chkHSChuyen";
    private static final String DROPDOWN_ID_SUBSTRING = "ddtNguoiNhan";
    private static final String SUBMIT_BUTTON_ID_SUBSTRING = "cmdChuyen";

    private final int tabid;

    public DuyetPheDuyetTdktPage(Page page, int tabid) {
        super(page);
        this.tabid = tabid;
    }

    public void moDanhSach() {
        page.navigate("http://10.14.242.16:9292/Default.aspx?tabid=" + tabid);
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    public void duyet(String maHoSo, String tenNguoiNhanTiepTheo) {
        RadGridHelper.tickRowCheckbox(page, maHoSo, CHECKBOX_CLASS);
        RadDropDownTreeHelper.chonNguoiNhanVaChuyen(page, DROPDOWN_ID_SUBSTRING, tenNguoiNhanTiepTheo, SUBMIT_BUTTON_ID_SUBSTRING);
    }
}
