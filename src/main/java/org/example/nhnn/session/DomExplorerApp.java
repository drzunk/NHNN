package org.example.nhnn.session;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.example.nhnn.config.EnvConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Cong cu CHAY TAY (khong phai test) de do DOM song tren he thong DVC-NHNN
 * bang storageState da dang nhap san, phuc vu xac nhan lai selector truoc
 * khi viet Page Object/test case that. KHONG phai mot phan cua suite test
 * tu dong - chi la cong cu khao sat.
 *
 * Cach chay (tranh truyen ky tu '&' qua shell bang cach ghep URL trong Java):
 *   mvn exec:java -Dexec.mainClass="org.example.nhnn.session.DomExplorerApp" \
 *       -Dexec.args="<tabid> <ctl> <mid> <ten_file_dump_khong_duoi> [radComboClientId]"
 *
 * Vi du (tabid=37, ctl=TIEPNHAN, mid=795):
 *   -Dexec.args="37 TIEPNHAN 795 tabid37"
 * Kem trich xuat item cua mot RadComboBox (Telerik client API, vi gia tri
 * value khong nam trong HTML tinh):
 *   -Dexec.args="37 TIEPNHAN 795 tabid37 dnn_ctr795_TiepNhanHoSoForm_cmbLoaiHoSoID"
 */
public final class DomExplorerApp {

    private DomExplorerApp() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 4 && args.length != 5) {
            System.err.println("Cach dung: DomExplorerApp <tabid> <ctl> <mid> <ten_file_khong_duoi> [radComboClientId]");
            System.exit(2);
            return;
        }
        String tabid = args[0];
        String ctl = args[1];
        String mid = args[2];
        String fileBaseName = args[3];
        String radComboClientId = args.length == 5 ? args[4] : null;
        String url = "http://10.14.242.16:9292/Default.aspx?tabid=" + tabid
                + "&ctl=" + ctl + "&mid=" + mid;

        EnvConfig config = EnvConfig.load();
        SessionManager sessionManager = new SessionManager(config);
        Path outDir = config.captchaDir().getParent().resolve("explore");
        Files.createDirectories(outDir);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = BrowserFactory.launch(playwright, config);
            BrowserContext context = sessionManager.newContext(browser, KnownSystems.DVC_NHNN);
            Page page = context.newPage();

            page.navigate(url);
            try {
                page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(15_000));
            } catch (Exception e) {
                System.out.println("[Canh bao] networkidle timeout, tiep tuc voi trang thai hien tai: " + e.getMessage());
            }

            Path htmlFile = outDir.resolve(fileBaseName + ".html");
            Path pngFile = outDir.resolve(fileBaseName + ".png");
            Files.writeString(htmlFile, page.content());
            page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setPath(pngFile));

            System.out.println("URL cuoi cung: " + page.url());
            System.out.println("Da luu DOM: " + htmlFile.toAbsolutePath());
            System.out.println("Da luu anh: " + pngFile.toAbsolutePath());

            if (radComboClientId != null) {
                Object items = page.evaluate(
                        "(id) => { "
                                + "const combo = window.$find(id); "
                                + "if (!combo) return 'KHONG_TIM_THAY_COMBO:' + id; "
                                + "const col = combo.get_items(); "
                                + "const out = []; "
                                + "for (let i = 0; i < col.get_count(); i++) { "
                                + "  const it = col.getItem(i); "
                                + "  out.push({ value: it.get_value(), text: it.get_text() }); "
                                + "} "
                                + "return out; "
                                + "}",
                        radComboClientId);
                Path comboFile = outDir.resolve(fileBaseName + "_combo_" + radComboClientId + ".json");
                Files.writeString(comboFile, String.valueOf(items));
                System.out.println("Da luu danh sach item combo: " + comboFile.toAbsolutePath());
                System.out.println(items);
            }
        }
    }
}
