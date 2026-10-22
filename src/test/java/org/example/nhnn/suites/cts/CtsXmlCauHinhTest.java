package org.example.nhnn.suites.cts;

import org.example.nhnn.pages.ThuLyHoSoPage;
import org.example.nhnn.pages.TiepNhanHoSoPage;
import org.example.nhnn.testsupport.BaseDvcNhnnTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Suite Dang 1 (task DVC-NHNN-4053) - kiem tra cau hinh buoc XML chuyen
 * nganh Chung thu so (CTS) tai man hinh Thu ly (tabid=54), theo 3 nhom thu
 * tuc mo ta trong SOP.
 *
 * Nhom 1 va Nhom 2 TU TAO ho so moi moi lan chay (qua TiepNhanHoSoPage,
 * giong CtsTiepNhanTest) roi doc dung ma ho so vua sinh ra - KHONG hardcode
 * ma ho so co san, vi do la du lieu se thay doi/bien mat theo thoi gian tren
 * he thong DEV dung chung, khong phai nguon xac nhan on dinh lau dai.
 *
 * Nhom 3 la NGOAI LE CO CHU DICH: hien chua xac nhan duoc gia tri combo
 * cmbLoaiHoSoID nao tao ra 1 ho so CTS thuoc nhom "khong dung XML" (gia han/
 * tam dung/ma ngan hang) - bao cao nghiem thu 4053 goc cung phai dung tam 1
 * ho so CO SAN thuoc linh vuc KHAC (ma ngan hang) de chung minh khoi bi an
 * 100%, vi ly do tuong tu. Giu nguyen fixture do cho toi khi tim duoc combo
 * value that de tu tao.
 */
class CtsXmlCauHinhTest extends BaseDvcNhnnTest {

    private static final String THU_TUC_XML_IN = "22";
    private static final String THU_TUC_XML_OUT = "44";
    private static final String NGAN_HANG_MAU = "01101001 - Văn phòng Ngân hàng Nhà nước";
    private static final String NHOM3_KHONG_XML_HO_SO_CO_SAN = "G19.019-260722-6005";

    @Test
    void nhom1_xmlIn_duDuBonBuoc() {
        String maHoSo = taoHoSoCts(THU_TUC_XML_IN, "CTS TO CHUC - XML CAU HINH N1", false);

        ThuLyHoSoPage thuLy = new ThuLyHoSoPage(page);
        thuLy.moHoSoTuDanhSach(maHoSo);

        List<String> buoc = thuLy.danhSachBuocXuLyChuyenNganh();
        assertThat(buoc).hasSize(4);
        assertThat(buoc.get(0)).contains("Gửi XML sang CA");
        assertThat(buoc.get(3)).contains("Xem mẫu thông báo");
    }

    @Test
    void nhom2_thieuEform_hienThiToastLoi() {
        String maHoSo = taoHoSoCts(THU_TUC_XML_OUT, "CTS CAP LAI - XML CAU HINH N2 THIEU EFORM", false);

        ThuLyHoSoPage thuLy = new ThuLyHoSoPage(page);
        thuLy.moHoSoTuDanhSach(maHoSo);

        List<String> buoc = thuLy.danhSachBuocXuLyChuyenNganh();
        assertThat(buoc).hasSize(3);

        assertThat(thuLy.xemFormKiemSoat().innerText())
                .contains("Chưa có thông tin tờ khai cho hồ sơ này");
    }

    @Test
    void nhom2_coEform_moDuocFormKiemSoat() {
        String maHoSo = taoHoSoCts(THU_TUC_XML_OUT, "CTS CAP LAI - XML CAU HINH N2 CO EFORM", true);

        ThuLyHoSoPage thuLy = new ThuLyHoSoPage(page);
        thuLy.moHoSoTuDanhSach(maHoSo);

        assertThat(thuLy.xemFormKiemSoat().innerText())
                .contains("THÔNG TIN CHỨNG THƯ SỐ");
    }

    @Test
    void nhom3_khongDungXml_khoiXuLyChuyenNganhAnHoanToan() {
        ThuLyHoSoPage thuLy = new ThuLyHoSoPage(page);
        thuLy.moHoSoTuDanhSach(NHOM3_KHONG_XML_HO_SO_CO_SAN);

        assertThat(thuLy.danhSachBuocXuLyChuyenNganh()).isEmpty();
    }

    private String taoHoSoCts(String thuTucValue, String tenKhachHang, boolean dienEform) {
        TiepNhanHoSoPage tiepNhan = new TiepNhanHoSoPage(page);
        tiepNhan.mo(37, "795");
        tiepNhan.chonThuTuc(thuTucValue);
        tiepNhan.chonNganHangVaXacNhan(NGAN_HANG_MAU);
        tiepNhan.dienNguoiNopHoSo(
                "001095012345", tenKhachHang, "0987654321", "49 Lý Thái Tổ, Hoàn Kiếm, Hà Nội");
        if (dienEform) {
            tiepNhan.dienEformVaLuu(tenKhachHang, "001095012345");
        }
        tiepNhan.luu();

        String maHoSo = tiepNhan.layMaHoSoSauKhiLuu();
        assertThat(maHoSo).as("Mã hồ sơ sinh ra sau khi lưu").isNotNull();
        return maHoSo;
    }
}
