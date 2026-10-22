package org.example.nhnn.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.example.nhnn.helpers.DownloadHelper;
import org.example.nhnn.helpers.RadGridHelper;
import com.microsoft.playwright.BrowserContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Man hinh "Thu ly ho so" (ThuLyHoSoForm, tabid=54), dung chung cho nhieu
 * linh vuc nhu TiepNhanHoSoForm. Mo tu danh sach bang RadContextMenu (Bay 2,
 * xem RadGridHelper) roi thao tac khoi "Xu ly chuyen nganh" (CTS).
 *
 * Selector lay tu bao cao nghiem thu task DVC-NHNN-4045/4053 va script
 * Playwright da chay that cua agent Antigravity truoc day (khong doan).
 */
public final class ThuLyHoSoPage extends BasePage {

    private static final String FORM = "ThuLyHoSoForm";

    public ThuLyHoSoPage(Page page) {
        super(page);
    }

    public void moHoSoTuDanhSach(String maHoSo) {
        RadGridHelper.openViaContextMenu(page, maHoSo, "Xử lý");
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30_000));
    }

    /**
     * Doc danh sach "Buoc N: ..." trong khoi "Xu ly chuyen nganh". Tra ve
     * danh sach rong neu khoi khong xuat hien tren trang (truong hop Nhom 3 -
     * thu tuc khong dung XML, khoi phai an 100%).
     *
     * Luu y: ranh gioi phan tu cha (fieldset/div.box) duoc suy doan hop ly tu
     * cach trinh bay chung cua he thong, vi script kham pha cu khong luu lai
     * class CSS chinh xac cua khoi nay - co the can chinh lai sau lan chay
     * that dau tien.
     */
    public List<String> danhSachBuocXuLyChuyenNganh() {
        Locator heading = page.locator(
                "legend:has-text(\"Xử lý chuyên ngành\"), "
                        + "h3:has-text(\"Xử lý chuyên ngành\"), "
                        + "h4:has-text(\"Xử lý chuyên ngành\")").first();
        if (heading.count() == 0) {
            return List.of();
        }
        Locator block = heading.locator(
                "xpath=ancestor::fieldset[1] | ancestor::div[contains(@class,'box')][1]").first();
        String text = (block.count() > 0 ? block : heading).innerText();
        return Arrays.stream(text.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> line.matches("^Bước\\s*\\d+.*"))
                .toList();
    }

    /**
     * Bam "Xem Form kiem soat" (buoc 3 nhom 1, buoc 2 nhom 2). Tra ve
     * Locator cua ket qua de test tu assert - co the la toast loi thieu
     * eform, HOAC RadWindow "THONG TIN CHUNG THU SO" neu co eform.
     */
    public Locator xemFormKiemSoat() {
        page.locator("button.xlcn-btn:has-text(\"Xem Form kiểm soát\"), "
                + "button[id*='btnKiemTraToKhaiCTS'], "
                + "button:has-text(\"Xem Form kiểm soát\")").first().click();
        return page.locator(
                "text=THÔNG TIN CHỨNG THƯ SỐ, "
                        + "text=Chưa có thông tin tờ khai cho hồ sơ này").first();
    }

    /**
     * Bam "Xem mau thong bao" (buoc 4), cho modal rwCanCuPhapLy mo, tim lien
     * ket/nut co text "Tai ban Word" (khong hard-code selector cu the vi
     * server khong dam bao attribute co dinh), doc href roi tai ve bang
     * DownloadHelper (ke thua cookie phien dang nhap).
     */
    public void xemMauThongBaoVaTaiWord(BrowserContext context, Path saveTo) throws IOException {
        page.locator("button:has-text(\"Xem mẫu thông báo\")").first().click();
        page.locator("div[id*='" + FORM + "_rwCanCuPhapLy']").first()
                .waitFor(new Locator.WaitForOptions().setTimeout(30_000));

        Object href = page.evaluate(
                "() => { "
                        + "const el = Array.from(document.querySelectorAll('a,button,input[type=button]'))"
                        + "  .find(e => (e.innerText || e.value || '').includes('Tải bản Word')); "
                        + "return el ? (el.getAttribute('href') || el.dataset.href || null) : null; "
                        + "}");
        if (href == null) {
            throw new IllegalStateException("Khong tim thay lien ket 'Tai ban Word' trong modal rwCanCuPhapLy");
        }
        DownloadHelper.downloadAuthenticated(context, String.valueOf(href), saveTo);
    }
}
