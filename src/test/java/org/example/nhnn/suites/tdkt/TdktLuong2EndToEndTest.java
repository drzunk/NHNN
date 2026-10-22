package org.example.nhnn.suites.tdkt;

import org.example.nhnn.pages.DuyetPheDuyetTdktPage;
import org.example.nhnn.pages.PhanCongPage;
import org.example.nhnn.pages.TiepNhanThuongTrucPage;
import org.example.nhnn.testsupport.BaseTdktTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Suite Luong 2 TDKT (Thi dua khen thuong) - "Tiep nhan thuong truc"
 * (tabid=237) thay the tabid=37, giu nguyen phan con lai nhu Luong 1. Dung
 * cac ho so hoi quy that da duoc agent Antigravity xac nhan chuyen thanh
 * cong qua script Playwright truoc day.
 *
 * LUU Y QUAN TRONG ve tinh lap lai duoc: khac voi test CTS (xem/tai file -
 * hanh dong doc, lap lai duoc), hanh dong "chuyen ho so" o day la MOT CHIEU
 * (khong co co che nao trong framework de day ho so nguoc lai, vi vi pham
 * rang buoc an toan "khong ghi/sua/xoa DB truc tiep"). Vi vay moi test o day
 * kiem tra TRANG THAI HIEN TAI truoc: neu ho so da o dich (lan chay truoc
 * da chuyen roi), coi nhu buoc do da PASS va bo qua hanh dong chuyen, chi
 * assert su co mat o man hinh dich - tranh that bai gia do chay lai nhieu
 * lan tren cung 1 ho so that.
 */
class TdktLuong2EndToEndTest extends BaseTdktTest {

    private static final String HO_SO_TIEP_NHAN_THUONG_TRUC = "G19.002-261019-6006";
    private static final String HO_SO_DUYET = "G19.002-261020-6004";
    private static final String NGUOI_NHAN_PHAN_CONG = "Quản trị Vụ tổ chức cán bộ";

    @Test
    void tiepNhanThuongTruc_chuyenToiPhanCong() {
        PhanCongPage phanCong = new PhanCongPage(page);
        phanCong.moDanhSach();
        if (hoSoCoTrongTrang(HO_SO_TIEP_NHAN_THUONG_TRUC)) {
            return;
        }

        TiepNhanThuongTrucPage tiepNhan = new TiepNhanThuongTrucPage(page);
        tiepNhan.moDanhSach();
        tiepNhan.chuyenToiPhanCong(HO_SO_TIEP_NHAN_THUONG_TRUC, NGUOI_NHAN_PHAN_CONG);

        phanCong.moDanhSach();
        assertThat(hoSoCoTrongTrang(HO_SO_TIEP_NHAN_THUONG_TRUC)).isTrue();
    }

    @Test
    void duyetLanhDaoPhong_denDuyetLanhDaoDonVi() {
        DuyetPheDuyetTdktPage duyetDonVi = new DuyetPheDuyetTdktPage(page, 241);
        duyetDonVi.moDanhSach();
        if (hoSoCoTrongTrang(HO_SO_DUYET)) {
            return;
        }

        DuyetPheDuyetTdktPage duyetPhong = new DuyetPheDuyetTdktPage(page, 240);
        duyetPhong.moDanhSach();
        duyetPhong.duyet(HO_SO_DUYET, "Lãnh đạo đơn vị");

        duyetDonVi.moDanhSach();
        assertThat(hoSoCoTrongTrang(HO_SO_DUYET)).isTrue();
    }

    private boolean hoSoCoTrongTrang(String maHoSo) {
        return page.locator(String.format("tr:has-text(\"%s\")", maHoSo)).count() > 0;
    }
}
