package org.example.nhnn.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.example.nhnn.helpers.EformFrameHelper;
import org.example.nhnn.helpers.RadComboHelper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Man hinh "Tiep nhan ho so truc tiep" (TiepNhanHoSoForm), dung chung cho
 * nhieu linh vuc (vi du Chung thu so, truy cap qua Default.aspx?tabid=37
 * &amp;ctl=TIEPNHAN&amp;mid=795). Ten User Control "TiepNhanHoSoForm" la on dinh
 * giua cac session, nhung so ctrXXX phia truoc thay doi - moi selector o day
 * dung dang id*=... (chua chuoi on dinh) thay vi id co dinh.
 */
public final class TiepNhanHoSoPage extends BasePage {

    private static final String FORM = "TiepNhanHoSoForm";
    private static final Pattern MA_HO_SO_PATTERN = Pattern.compile("G\\d{2}\\.\\d{3}-\\d{6}-\\d{4}");

    public TiepNhanHoSoPage(Page page) {
        super(page);
    }

    public void mo(int tabid, String mid) {
        page.navigate("http://10.14.242.16:9292/Default.aspx?tabid=" + tabid + "&ctl=TIEPNHAN&mid=" + mid);
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    public void chonThuTuc(String loaiHoSoValue) {
        RadComboHelper.selectByValue(page, FORM + "_cmbLoaiHoSoID", loaiHoSoValue);
    }

    public void chonNganHangVaXacNhan(String tenNganHang) {
        RadComboHelper.selectByText(page, FORM + "_cboDonViNganHang", tenNganHang);
        // Nut nay la Telerik RadButton (rbLinkButton) render ra <span>, KHONG
        // phai <input> - dung id$=... (ket thuc dung bang, khong phai chua)
        // de khop chinh xac span that, loai tru ca <input ..._ClientState>
        // di kem (da xac nhan qua khao sat DOM that).
        page.locator("[id$='" + FORM + "_btnLuuDonViNganHang']").first().click();
    }

    public void dienNguoiNopHoSo(String soGiayToTuyThan, String tenKhachHang, String diDong, String diaChiThuongTru) {
        fillGridField("tbSoGiayToTuyThan", soGiayToTuyThan);
        fillGridField("tbTenKhachHang", tenKhachHang);
        fillGridField("tbDiDong", diDong);
        fillGridField("tbDiaChiThuongTru", diaChiThuongTru);
    }

    /**
     * Dien va luu to khai dien tu E-form (Bay 3) truoc khi bam luu() chinh -
     * can cho nhom 2 CTS "co eform" (SOP: Nhap to khai online tai Tiep nhan
     * -> Luu ho so -> Thu ly -> Xem Form kiem soat).
     */
    public void dienEformVaLuu(String hoTen, String soCccd) {
        EformFrameHelper.fillAndSave(page, "input[name*='Eform_CongChuc']",
                Map.of("hoTen", hoTen, "cccd", soCccd), "Lưu tờ khai");
    }

    public void luu() {
        // Cung la Telerik RadButton render ra <span>, khong phai <input>
        // (da xac nhan qua khao sat DOM that, giong btnLuuDonViNganHang).
        // id$=... (ket thuc dung bang) de tranh khop nham btnSaveWord/
        // btnSavePanel (ca 2 deu CHUA "_btnSave" nhung khong KET THUC bang
        // dung "_btnSave").
        page.locator("[id$='" + FORM + "_btnSave']").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    /**
     * Doc ma ho so vua sinh ra tu noi dung trang (dang G19.xxx-DDMMYY-NNNN).
     * Tra ve null neu khong tim thay (vi du luu that bai).
     */
    public String layMaHoSoSauKhiLuu() {
        String bodyText = page.locator("body").innerText();
        Matcher matcher = MA_HO_SO_PATTERN.matcher(bodyText);
        return matcher.find() ? matcher.group() : null;
    }

    private void fillGridField(String fieldName, String value) {
        String selector = "input[id*='" + FORM + "_RadGrid1_ctl00_ctl04_" + fieldName + "']:not([id$='_ClientState'])";
        page.fill(selector, value);
    }
}
