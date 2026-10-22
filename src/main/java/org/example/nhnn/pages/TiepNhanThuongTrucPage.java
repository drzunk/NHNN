package org.example.nhnn.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.example.nhnn.helpers.RadDropDownTreeHelper;
import org.example.nhnn.helpers.RadGridHelper;

/**
 * Man hinh "Tiep nhan thuong truc" (tabid=237) - diem vao Luong 2 cua TDKT
 * (Thi dua khen thuong), thay the cho tabid=37 o Luong 1. Theo du lieu da
 * xac nhan tu script Playwright thuc te cua agent Antigravity, man hinh nay
 * la mot hang doi ho so can chuyen tiep sang Phan cong (tabid=238), dung
 * chung co che grid + checkbox + RadDropDownTree + nut chuyen voi 238/239/
 * 240/241.
 *
 * Luu y: ctl/mid chinh xac cho URL man hinh nay chua duoc xac nhan (script
 * kham pha cu dieu huong bang menu, khong phai URL truc tiep) - dung
 * tabid don gian, co the can bo sung ctl/mid neu DotNetNuke khong tu nap
 * dung module mac dinh.
 */
public final class TiepNhanThuongTrucPage extends BasePage {

    private static final int TABID = 237;
    private static final String CHECKBOX_CLASS = "chkHSChuyen";
    private static final String DROPDOWN_ID_SUBSTRING = "ddtNguoiNhan";
    private static final String SUBMIT_BUTTON_ID_SUBSTRING = "cmdChuyen";

    public TiepNhanThuongTrucPage(Page page) {
        super(page);
    }

    public void moDanhSach() {
        page.navigate("http://10.14.242.16:9292/Default.aspx?tabid=" + TABID);
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    public void chuyenToiPhanCong(String maHoSo, String tenNguoiNhan) {
        RadGridHelper.tickRowCheckbox(page, maHoSo, CHECKBOX_CLASS);
        RadDropDownTreeHelper.chonNguoiNhanVaChuyen(page, DROPDOWN_ID_SUBSTRING, tenNguoiNhan, SUBMIT_BUTTON_ID_SUBSTRING);
    }
}
