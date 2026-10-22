package org.example.nhnn.report;

import com.microsoft.playwright.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Chup anh full-page va dat ten file tuan tu (001_xxx.png, 002_xxx.png...)
 * trong thu muc screenshots cua mot lan chay test.
 */
public final class ScreenshotHelper {

    private ScreenshotHelper() {
    }

    public static Path capture(Page page, Path screenshotsDir, String label) throws IOException {
        Files.createDirectories(screenshotsDir);
        int nextSeq = countExistingFiles(screenshotsDir) + 1;
        String fileName = String.format("%03d_%s.png", nextSeq, sanitize(label));
        Path path = screenshotsDir.resolve(fileName);
        page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setPath(path));
        return path;
    }

    private static int countExistingFiles(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return (int) stream.filter(Files::isRegularFile).count();
        }
    }

    private static String sanitize(String label) {
        return label.replaceAll("[^a-zA-Z0-9-_]", "-");
    }
}
