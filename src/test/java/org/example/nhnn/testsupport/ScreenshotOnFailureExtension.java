package org.example.nhnn.testsupport;

import org.example.nhnn.report.ScreenshotHelper;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.IOException;

/**
 * Tu dong chup anh full-page khi mot test fail, phuc vu dieu tra sau (xem
 * skill "qa-investigation").
 */
public final class ScreenshotOnFailureExtension implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        context.getTestInstance().ifPresent(instance -> {
            if (!(instance instanceof BaseTest baseTest)) {
                return;
            }
            if (baseTest.page == null || BaseTest.reportWriter == null) {
                return;
            }
            try {
                ScreenshotHelper.capture(baseTest.page, BaseTest.reportWriter.screenshotsDir(),
                        "FAILURE_" + context.getDisplayName());
            } catch (IOException e) {
                System.err.println("Khong chup duoc screenshot khi test fail: " + e.getMessage());
            }
        });
    }
}
