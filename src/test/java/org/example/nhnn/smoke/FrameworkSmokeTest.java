package org.example.nhnn.smoke;

import com.microsoft.playwright.BrowserContext;
import org.example.nhnn.config.EnvConfig;
import org.example.nhnn.report.ScreenshotHelper;
import org.example.nhnn.session.SessionManager;
import org.example.nhnn.testsupport.BaseTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test cho KHUNG framework (Phase 1) - KHONG dang nhap he thong that,
 * khong can config/.env co gia tri that. Chi xac nhan: mo duoc Chromium
 * headless, chup duoc screenshot, sinh duoc bao cao Markdown, va
 * SessionManager khong nem loi khi storageState chua ton tai.
 */
class FrameworkSmokeTest extends BaseTest {

    @Test
    void frameworkMoBrowserChupAnhVaGhiBaoCao() throws IOException {
        context = browser.newContext();
        page = context.newPage();
        page.navigate("about:blank");

        Path screenshot = ScreenshotHelper.capture(page, reportWriter.screenshotsDir(), "blank-page");
        assertThat(Files.exists(screenshot)).isTrue();

        reportWriter.addEntry(
                "Framework mo duoc Chromium headless va chup anh full-page",
                "Da chup anh thanh cong: " + screenshot.getFileName(),
                true,
                screenshot);
    }

    @Test
    void sessionManagerKhongNemLoiKhiChuaCoStorageState() {
        EnvConfig config = EnvConfig.load();
        SessionManager sessionManager = new SessionManager(config);

        BrowserContext newContext = sessionManager.newContext(browser, "he-thong-chua-ton-tai");
        context = newContext;
        page = newContext.newPage();

        assertThat(sessionManager.storageStatePathFor("he-thong-chua-ton-tai"))
                .hasFileName("he-thong-chua-ton-tai.storageState.json");
    }

    @Test
    void reportWriterSinhDuocBaoCaoMarkdown() throws IOException {
        reportWriter.addEntry(
                "Framework sinh duoc bao cao Markdown doi chieu Yeu cau vs Ket qua",
                "Da ghi file report.md",
                true,
                null);

        Path reportFile = reportWriter.writeMarkdown();

        assertThat(Files.exists(reportFile)).isTrue();
        assertThat(Files.readString(reportFile)).contains("FrameworkSmokeTest");
    }
}
