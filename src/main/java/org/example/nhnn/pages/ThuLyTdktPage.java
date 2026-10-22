package org.example.nhnn.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.example.nhnn.helpers.RadDropDownTreeHelper;
import org.example.nhnn.helpers.RadGridHelper;

/**
 * Man hinh "Thu ly ho so Thi dua khen thuong" (tabid=239) - co 2 man hinh
 * con, da xac nhan qua script Playwright thuc te cua agent Antigravity:
 *
 * 1. Danh sach (DanhSachHoSoThuLyThiDuaKhenThuongForm): cung co che
 *    grid+checkbox+RadDropDownTree+nut chuyen nhu 237/238/240/241, dung de
 *    chuyen tiep ho so sang Duyet lanh dao phong (240) HOAC chuyen thang
 *    sang Tra ket qua (242, xem chuyenNhanhToiTraKetQua).
 * 2. Chi tiet xu ly (ThuLyHoSoThiDuaKhenThuongForm, mo tu link lbSoBienNhan
 *    cua 1 dong trong danh sach): widget rieng (khong phai Telerik chuan)
 *    de "xet danh hieu" va "khen thuong" mot/nhieu ca nhan.
 */
public final class ThuLyTdktPage extends BasePage {

    private static final int TABID = 239;
    private static final String CHECKBOX_CLASS = "chkHSChuyen";
    private static final String DROPDOWN_ID_SUBSTRING = "ddtNguoiNhan";
    private static final String LIST_SUBMIT_BUTTON_ID_SUBSTRING = "cmdChuyen";
    private static final String DETAIL_FORM = "ThuLyHoSoThiDuaKhenThuongForm";

    public ThuLyTdktPage(Page page) {
        super(page);
    }

    public void moDanhSach() {
        page.navigate("http://10.14.242.16:9292/Default.aspx?tabid=" + TABID);
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    /** Mo man hinh chi tiet xu ly (xet danh hieu) tu dong chua ma ho so. */
    public void moHoSoDeXetDuyet(String maHoSo) {
        page.locator(String.format("a[id*='lbSoBienNhan']:has-text(\"%s\")", maHoSo)).first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    public void chonTatCaCaNhan() {
        page.locator(".xk-btn-selall").first().click();
    }

    public void chonLoaiDanhHieu(String tenDanhHieu) {
        page.locator(".xk-loai").first().selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(tenDanhHieu));
    }

    /** Bam "Khen thuong" (chi bam duoc khi nut khong o trang thai .off) roi xac nhan modal. */
    public void khenThuongVaXacNhan() {
        Locator awardButton = page.locator(".xk-actions button.xk-btn-award:not(.off)").first();
        awardButton.click();
        page.locator("button:has-text(\"Xác nhận khen thưởng\")").first().click();
        page.waitForLoadState();
    }

    /** Chuyen ho so da xet danh hieu sang Duyet lanh dao phong (tabid=240). */
    public void chuyenToiDuyetPhong(String tenNguoiNhan) {
        RadDropDownTreeHelper.chonNguoiNhanVaChuyen(page, DROPDOWN_ID_SUBSTRING, tenNguoiNhan, DETAIL_FORM + "_btnChuyen");
    }

    /**
     * Chuyen THANG ho so tu danh sach sang Tra ket qua (tabid=242), bo qua
     * buoc xet danh hieu - co che nay DA xac nhan chay that qua script
     * Playwright (l4_test_transfer_to_242.js), nhung CHUA xac nhan day co
     * dung la nghiep vu "Luong 4 - Khong giai quyet" mo ta trong SOP hay
     * khong (SOP mo ta mot context-menu rieng + o nhap ly do, khac co che
     * checkbox+dropdowntree nay) - vi vay dat ten trung lap, KHONG goi la
     * "khongGiaiQuyet" trong code.
     */
    public void chuyenNhanhToiTraKetQua(String maHoSo) {
        RadGridHelper.tickRowCheckbox(page, maHoSo, CHECKBOX_CLASS);
        RadDropDownTreeHelper.chonNguoiNhanVaChuyen(page, DROPDOWN_ID_SUBSTRING, "Trả kết quả", LIST_SUBMIT_BUTTON_ID_SUBSTRING);
    }
}
