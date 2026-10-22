package org.example.nhnn.testsupport;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.example.nhnn.config.EnvConfig;
import org.example.nhnn.report.ReportWriter;
import org.example.nhnn.session.BrowserFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

/**
 * Lop cha chung cho moi test class: giu MOT Playwright/Browser song xuyen
 * suot ca class test (khong mo/dong browser moi test method), dung tinh
 * than "khong tat/mo browser moi lenh" cua SOP. Moi test method tu tao
 * BrowserContext/Page rieng (xem BaseDvcNhnnTest/BaseQlcvTest) de co su co
 * lap can thiet ma khong phai tra gia khoi dong lai Chromium.
 */
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseTest {

    protected static Playwright playwright;
    protected static Browser browser;
    protected static ReportWriter reportWriter;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser(TestInfo testInfo) throws IOException {
        EnvConfig config = EnvConfig.load();
        playwright = Playwright.create();
        browser = BrowserFactory.launch(playwright, config);

        String scenarioName = testInfo.getTestClass()
                .map(Class::getSimpleName)
                .orElse("UnknownScenario");
        reportWriter = ReportWriter.startReport(config.reportDir(), scenarioName);
    }

    @AfterAll
    static void shutdownBrowser() throws IOException {
        if (reportWriter != null) {
            reportWriter.writeMarkdown();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
            context = null;
            page = null;
        }
    }
}
