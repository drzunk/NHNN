package org.example.nhnn.session;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.example.nhnn.config.EnvConfig;

/**
 * Noi DUY NHAT trong framework duoc phep goi playwright.chromium().launch(...).
 *
 * Rang buoc an toan cung (xem README.md): chi khoi chay mot Chromium doc lap
 * do Playwright quan ly - KHONG bao gio dieu khien/kill trinh duyet Chrome
 * that cua nguoi dung. Che do headless/headed (co the thay doi de quan sat
 * truc tiep khi debug) duoc dieu khien qua config/.env (PW_HEADLESS), mac
 * dinh headless=true neu khong cau hinh.
 */
public final class BrowserFactory {

    private BrowserFactory() {
    }

    public static Browser launch(Playwright playwright, EnvConfig config) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(config.headless())
                .setSlowMo(config.headless() ? 0 : 250));
    }
}
