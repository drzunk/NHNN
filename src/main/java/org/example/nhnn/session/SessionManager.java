package org.example.nhnn.session;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.example.nhnn.config.EnvConfig;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Quan ly storageState (phien dang nhap da luu) cho tung he thong, de khong
 * phai dang nhap/giai captcha lai moi lan chay test.
 */
public final class SessionManager {

    private final EnvConfig config;

    public SessionManager(EnvConfig config) {
        this.config = config;
    }

    public Path storageStatePathFor(String systemKey) {
        return config.storageStateDir().resolve(systemKey + ".storageState.json");
    }

    /**
     * Tao BrowserContext moi, tu dong nap storageState da luu neu co.
     */
    public BrowserContext newContext(Browser browser, String systemKey) {
        Path statePath = storageStatePathFor(systemKey);
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080)
                .setIgnoreHTTPSErrors(true);
        if (Files.exists(statePath)) {
            options.setStorageStatePath(statePath);
        }
        return browser.newContext(options);
    }

    public void saveStorageState(BrowserContext context, String systemKey) throws java.io.IOException {
        Path statePath = storageStatePathFor(systemKey);
        Files.createDirectories(statePath.getParent());
        context.storageState(new BrowserContext.StorageStateOptions().setPath(statePath));
    }

    /**
     * Dieu huong toi URL can kiem tra va xac dinh session con hop le hay
     * khong, dua tren su xuat hien cua selector dac trung trang dang nhap.
     *
     * @return true neu session con hop le (KHONG thay form dang nhap).
     */
    public boolean isSessionValid(Page page, LoginIndicator indicator) {
        page.navigate(indicator.checkUrl());
        page.waitForLoadState();
        return page.locator(indicator.loginFormIndicatorCss()).count() == 0;
    }
}
