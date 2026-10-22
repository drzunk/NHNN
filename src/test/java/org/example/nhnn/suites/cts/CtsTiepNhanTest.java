package org.example.nhnn.suites.cts;

import org.example.nhnn.pages.TiepNhanHoSoPage;
import org.example.nhnn.testsupport.BaseDvcNhnnTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Suite 1 - Luong nghiep vu Chung thu so (CTS), tai khoan Cuc CNTT.
 * Diem vao: tabid=37 (Tiep nhan ho so truc tiep, mid=795).
 *
 * Gia tri thu tuc (cmbLoaiHoSoID) da xac nhan that qua RadComboBox client API
 * tren he thong test (xem config/state/explore/tabid37_combo_*.json):
 *   22 = Thu tuc cap CTS to chuc       (Nhom 1 - co XML IN, 4 buoc)
 *   44 = Thu tuc cap lai CTS ca nhan   (Nhom 2 - chi XML OUT, 3 buoc)
 */
class CtsTiepNhanTest extends BaseDvcNhnnTest {

    private static final int TABID_TIEP_NHAN = 37;
    private static final String MID_TIEP_NHAN = "795";

    private static final String THU_TUC_CAP_TO_CHUC_XML_IN = "22";
    private static final String THU_TUC_CAP_LAI_CA_NHAN_XML_OUT = "44";

    // "Ngan hang Nha nuoc Viet Nam" khong khop bat ky item nao trong combo
    // 5913 dong vi tren he thong that (da xac nhan bang cach doc truc tiep
    // get_items() qua client API) - doi sang gia tri thuc te ton tai.
    private static final String NGAN_HANG_MAU = "01101001 - Văn phòng Ngân hàng Nhà nước";

    @Test
    void capCtsToChuc_nhomXmlIn_taoHoSoThanhCong() {
        TiepNhanHoSoPage tiepNhan = new TiepNhanHoSoPage(page);
        tiepNhan.mo(TABID_TIEP_NHAN, MID_TIEP_NHAN);

        tiepNhan.chonThuTuc(THU_TUC_CAP_TO_CHUC_XML_IN);
        tiepNhan.chonNganHangVaXacNhan(NGAN_HANG_MAU);
        tiepNhan.dienNguoiNopHoSo(
                "001095012345",
                "CTS TO CHUC - TEST AUTOMATION",
                "0987654321",
                "49 Lý Thái Tổ, Hoàn Kiếm, Hà Nội");
        tiepNhan.luu();

        String maHoSo = tiepNhan.layMaHoSoSauKhiLuu();
        assertThat(maHoSo).as("Ma ho so sinh ra sau khi luu").isNotNull();
    }

    @Test
    void capLaiCtsCaNhan_nhomXmlOut_taoHoSoThanhCong() {
        TiepNhanHoSoPage tiepNhan = new TiepNhanHoSoPage(page);
        tiepNhan.mo(TABID_TIEP_NHAN, MID_TIEP_NHAN);

        tiepNhan.chonThuTuc(THU_TUC_CAP_LAI_CA_NHAN_XML_OUT);
        tiepNhan.chonNganHangVaXacNhan(NGAN_HANG_MAU);
        tiepNhan.dienNguoiNopHoSo(
                "001095999888",
                "CTS CAP LAI CA NHAN - TEST AUTOMATION",
                "0987654322",
                "49 Lý Thái Tổ, Hoàn Kiếm, Hà Nội");
        tiepNhan.luu();

        String maHoSo = tiepNhan.layMaHoSoSauKhiLuu();
        assertThat(maHoSo).as("Ma ho so sinh ra sau khi luu").isNotNull();
    }
}
