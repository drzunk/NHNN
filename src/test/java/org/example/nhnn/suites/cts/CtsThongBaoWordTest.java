package org.example.nhnn.suites.cts;

import org.example.nhnn.pages.ThuLyHoSoPage;
import org.example.nhnn.testsupport.BaseDvcNhnnTest;
import org.example.nhnn.verify.WordVerifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Suite Dang 2 (task DVC-NHNN-4045) - test regression that cho mau thong
 * bao cap CTS xuat ban Word (.docx) tai man hinh Thu ly (tabid=54).
 *
 * Ma ho so CO CHU DICH giu co dinh (khac voi CtsXmlCauHinhTest - noi nhom 1/2
 * tu tao ho so moi): mau thong bao nay chi sinh ra SAU KHI ho so da hoan tat
 * vong doi doi soat XML that voi he thong CA ben ngoai (Gui XML -> cho CA
 * tra Ket qua) - mot qua trinh khong the tu dong hoa/tai tao trong 1 lan
 * chay test (phu thuoc he thong ngoai, co do tre thuc). Ho so
 * G19.019-261019-6003 da duoc xac nhan o trang thai nay that su trong bao
 * cao nghiem thu 4045 - giu lai dung theo khuyen nghi TEST_GUIDELINES.md
 * muc 6 (test tai hien dung kich ban da xac nhan, khong xoa).
 */
class CtsThongBaoWordTest extends BaseDvcNhnnTest {

    private static final String HO_SO = "G19.019-261019-6003";

    @Test
    void xemMauThongBao_taiBanWord_dungChuanTask4045() throws IOException {
        ThuLyHoSoPage thuLy = new ThuLyHoSoPage(page);
        thuLy.moHoSoTuDanhSach(HO_SO);

        Path saveTo = Files.createTempFile("task4045_thongbao_capmoi", ".docx");
        thuLy.xemMauThongBaoVaTaiWord(context, saveTo);

        try (WordVerifier word = WordVerifier.open(saveTo)) {
            assertThat(word.headerContains("Mã hồ sơ: " + HO_SO)).isTrue();
            assertThat(word.tablesContainAll("/TB-CNTT6", "CNTT6")).isTrue();
            assertThat(word.hasNoTimeComponentInDates()).isTrue();
            assertThat(word.hasNoWeekdayPrefix()).isTrue();
            assertThat(word.footerContains("Phòng QLTT")).isTrue();
        } finally {
            Files.deleteIfExists(saveTo);
        }
    }
}
