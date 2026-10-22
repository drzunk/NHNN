package org.example.nhnn.session;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.example.nhnn.config.EnvConfig;
import org.example.nhnn.ocr.CaptchaOcrHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Chuong trinh chay TAY (khong phai JUnit test) de dang nhap mot lan vao he
 * thong va luu lai storageState cho cac lan chay test sau tai su dung.
 *
 * Cach chay (tai khoan Cuc CNTT hoac Vu TCCB - cung 1 trang dang nhap tabid=38):
 *   mvn exec:java -Dexec.mainClass="org.example.nhnn.session.LoginBootstrapperApp" -Dexec.args="dvc-nhnn"
 *   mvn exec:java -Dexec.mainClass="org.example.nhnn.session.LoginBootstrapperApp" -Dexec.args="dvc-nhnn-tccb"
 *
 * He thong DVC-NHNN yeu cau captcha anh 3 chu so. Chuong trinh thu tu dong
 * doc ma bang {@link CaptchaOcrHelper} (OCR tu xay dung, xem Javadoc class
 * do ve do chinh xac va quyet dinh bo rang buoc "khong OCR" truoc day).
 * Neu OCR khong chac chan (anh captcha khac thuong, bi nhieu nang...), roi
 * ve co che cu:
 *   1. Luu anh captcha ra config/state/captcha/dvc-nhnn-captcha.png
 *   2. CHO (poll) mot file tra loi config/state/captcha/dvc-nhnn-captcha-answer.txt
 *      xuat hien, chua dung 3 chu so captcha (khong xuong dong).
 *   3. Doc, xoa file tra loi, dien toan bo form (ten dang nhap/mat khau/captcha)
 *      NGAY TRUOC KHI bam dang nhap, roi submit.
 *
 * Luu y quan trong: form dang nhap nam trong mot DNN UpdatePanel, va thuc te
 * da quan sat duoc o ban that: neu dien o Mat khau roi CHO lau (vai phut, vi
 * du trong luc doi nguoi/agent doc anh captcha) thi gia tri bi xoa truoc khi
 * submit (co the do mot co che bao mat tu xoa truong mat khau sau mot khoang
 * thoi gian). Vi vay thu tu dung: CHUP anh captcha va CHO tra loi TRUOC, chi
 * dien toan bo form (bao gom mat khau) SAU KHI da co ma captcha, ngay truoc
 * khi bam nut dang nhap.
 */
public final class LoginBootstrapperApp {

    private static final int MAX_CAPTCHA_ATTEMPTS = 3;
    private static final int MAX_STALE_CAPTCHA_RETRIES = 5;
    private static final long ANSWER_WAIT_TIMEOUT_MS = 5 * 60 * 1000L;
    private static final long ANSWER_POLL_INTERVAL_MS = 1000L;
    private static final double DEFAULT_WAIT_TIMEOUT_MS = 30_000;

    private LoginBootstrapperApp() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1
                || (!KnownSystems.DVC_NHNN.equals(args[0]) && !KnownSystems.DVC_NHNN_TCCB.equals(args[0]))) {
            System.err.println("Cach dung: LoginBootstrapperApp <dvc-nhnn|dvc-nhnn-tccb>");
            System.exit(2);
            return;
        }
        String systemKey = args[0];
        EnvConfig config = EnvConfig.load();
        SessionManager sessionManager = new SessionManager(config);

        try (Playwright playwright = Playwright.create()) {
            Browser browser = BrowserFactory.launch(playwright, config);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setIgnoreHTTPSErrors(true));
            Page page = context.newPage();

            String username = KnownSystems.DVC_NHNN.equals(systemKey)
                    ? config.dvcNhnnCucCnttUsername()
                    : config.dvcNhnnVuTccbUsername();
            String password = KnownSystems.DVC_NHNN.equals(systemKey)
                    ? config.dvcNhnnCucCnttPassword()
                    : config.dvcNhnnVuTccbPassword();
            boolean loggedIn = loginDvcNhnn(page, config, username, password);

            if (!loggedIn) {
                System.err.println("Dang nhap that bai sau " + MAX_CAPTCHA_ATTEMPTS + " lan thu (hoac het thoi gian cho ma captcha). Dung lai.");
                System.exit(1);
                return;
            }

            sessionManager.saveStorageState(context, systemKey);
            System.out.println("Dang nhap thanh cong. Da luu storageState: "
                    + sessionManager.storageStatePathFor(systemKey).toAbsolutePath());
        }
    }

    private static boolean loginDvcNhnn(Page page, EnvConfig config, String username, String password) throws IOException {
        Path captchaDir = config.captchaDir();
        Files.createDirectories(captchaDir);
        Path captchaFile = captchaDir.resolve("dvc-nhnn-captcha.png");
        Path answerFile = captchaDir.resolve("dvc-nhnn-captcha-answer.txt");
        Files.deleteIfExists(answerFile);

        for (int attempt = 1; attempt <= MAX_CAPTCHA_ATTEMPTS; attempt++) {
            page.navigate(config.dvcNhnnBaseUrl());

            // Cho dieu kien: anh captcha hien thi ra (xac nhan DNN UpdatePanel
            // cua form dang nhap da render xong) thay vi cho cung mot khoang
            // thoi gian co dinh.
            Locator captchaImage = page.locator(KnownSystems.DVC_NHNN_CAPTCHA_IMAGE).first();
            captchaImage.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(DEFAULT_WAIT_TIMEOUT_MS));

            if (attempt == 1) {
                Path domDumpFile = captchaDir.resolve("dvc-nhnn-login-page.html");
                Files.writeString(domDumpFile, page.content());
                System.out.println("[Debug] Da luu DOM trang dang nhap: " + domDumpFile.toAbsolutePath());
            }

            byte[] captchaBytes = captchaImage.screenshot();
            Files.write(captchaFile, captchaBytes);

            String code = tryOcr(config, captchaFile);
            if (code != null) {
                System.out.println("[Lan thu " + attempt + "/" + MAX_CAPTCHA_ATTEMPTS + "] "
                        + "OCR tu doc duoc ma captcha: " + code);
            }
            for (int staleRetry = 1; code == null && staleRetry <= MAX_STALE_CAPTCHA_RETRIES; staleRetry++) {
                System.out.println("[Lan thu " + attempt + "/" + MAX_CAPTCHA_ATTEMPTS + "] "
                        + "Anh captcha da luu tai: " + captchaFile.toAbsolutePath());
                System.out.println("Hay ghi ma captcha (3 chu so) vao file (khong xuong dong thua): "
                        + answerFile.toAbsolutePath());
                System.out.println("Dang cho toi da " + (ANSWER_WAIT_TIMEOUT_MS / 1000) + " giay...");

                String candidateCode = waitForAnswerFile(answerFile);
                if (candidateCode == null) {
                    System.err.println("Het thoi gian cho file tra loi captcha.");
                    return false;
                }

                // He thong thuc te duoc quan sat la TU DOI anh captcha sau
                // mot khoang thoi gian (co the vi ly do bao mat). Chup lai
                // ngay truoc khi dien de xac nhan anh chua doi - neu da doi,
                // luu anh moi va xin lai ma thay vi nop ma cu (tranh sai lien
                // tuc co the dan toi khoa tai khoan).
                byte[] freshBytes = captchaImage.screenshot();
                if (Arrays.equals(freshBytes, captchaBytes)) {
                    code = candidateCode;
                    break;
                }
                System.out.println("Captcha da tu doi anh trong luc cho tra loi - da luu anh moi, "
                        + "hay doc lai va ghi ma moi.");
                captchaBytes = freshBytes;
                Files.write(captchaFile, captchaBytes);
                Files.deleteIfExists(answerFile);
            }
            if (code == null) {
                System.err.println("Captcha lien tuc tu doi anh truoc khi kip dien, dung lai.");
                return false;
            }

            // Dien toan bo form (bao gom mat khau) NGAY TRUOC KHI submit, de
            // tranh bi mot co che nao do tren trang tu xoa truong mat khau
            // neu dien qua som va cho lau (xem Javadoc dau class).
            page.fill(KnownSystems.DVC_NHNN_USERNAME_INPUT, username);
            page.fill(KnownSystems.DVC_NHNN_PASSWORD_INPUT, password);
            page.fill(KnownSystems.DVC_NHNN_CAPTCHA_INPUT, code.trim());

            Locator loginButton = page.locator(KnownSystems.DVC_NHNN_LOGIN_BUTTON).first();
            loginButton.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(DEFAULT_WAIT_TIMEOUT_MS));
            loginButton.click();

            // Form nam trong DNN UpdatePanel (co the la postback AJAX khong
            // dieu huong trang) - cho networkidle thay vi 'load' de bat duoc
            // ca truong hop nay.
            page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions()
                    .setTimeout(DEFAULT_WAIT_TIMEOUT_MS));

            Path debugShot = captchaDir.resolve("dvc-nhnn-login-result-" + attempt + ".png");
            page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setPath(debugShot));
            System.out.println("[Debug] url-sau-submit=" + page.url() + " screenshot=" + debugShot.toAbsolutePath());

            boolean stillOnLoginPage = page.locator(KnownSystems.DVC_NHNN_USERNAME_INPUT).count() > 0;
            if (!stillOnLoginPage) {
                return true;
            }
            System.out.println("Captcha sai hoac dang nhap that bai, thu lai.");
        }
        return false;
    }

    /** Tra ve null neu OCR bi tat (config) hoac khong chac chan - goi fallback thu cong. */
    private static String tryOcr(EnvConfig config, Path captchaFile) {
        if (!config.captchaOcrEnabled()) {
            return null;
        }
        Optional<String> result = CaptchaOcrHelper.solve(captchaFile);
        return result.orElse(null);
    }

    /**
     * Cho (poll) mot file tra loi xuat hien va khong rong, doc noi dung roi
     * xoa file di. Day la cho PHAN HOI CUA NGUOI/AGENT (co the mat vai phut
     * de xem anh captcha), khac voi cho dieu kien mot phan tu UI xuat hien -
     * nen khong dung timeout mac dinh 30s ma dung rieng ANSWER_WAIT_TIMEOUT_MS.
     * Tra ve null neu het thoi gian cho.
     */
    private static String waitForAnswerFile(Path answerFile) {
        long deadline = System.currentTimeMillis() + ANSWER_WAIT_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (Files.exists(answerFile)) {
                    String content = Files.readString(answerFile).trim();
                    if (!content.isEmpty()) {
                        Files.deleteIfExists(answerFile);
                        return content;
                    }
                }
            } catch (IOException e) {
                // File co the dang duoc ghi do, bo qua va thu lai o vong lap sau.
            }
            try {
                Thread.sleep(ANSWER_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }
}
