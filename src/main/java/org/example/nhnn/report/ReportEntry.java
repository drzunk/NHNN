package org.example.nhnn.report;

import java.nio.file.Path;

/**
 * Mot dong doi chieu Yeu cau vs Ket qua trong bao cao Markdown.
 *
 * @param screenshotRelativePath co the null neu dong nay khong gan anh minh chung.
 */
public record ReportEntry(String requirement, String actualResult, boolean passed, Path screenshotRelativePath) {
}
