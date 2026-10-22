package org.example.nhnn.helpers;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.BrowserContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Xu ly "Bay 4" trong SOP: bam the &lt;a&gt; tai file (.docx/.pdf/.xml) de bung
 * popup trinh duyet hoac lam mat event download cua Playwright.
 *
 * Cach xu ly chuan: dung context.request().get(url) de ke thua 100% cookie
 * phien dang nhap, nhan buffer nhi phan va ghi ra dia truc tiep.
 */
public final class DownloadHelper {

    private DownloadHelper() {
    }

    public static void downloadAuthenticated(BrowserContext context, String url, Path savePath) throws IOException {
        APIResponse response = context.request().get(url);
        if (!response.ok()) {
            throw new IllegalStateException(
                    "Tai file that bai: " + url + " -> HTTP " + response.status());
        }
        Path parent = savePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(savePath, response.body());
    }
}
