package org.example.nhnn.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Sinh bao cao Markdown doi chieu Yeu cau vs Ket qua cho mot kich ban test,
 * kem link toi anh minh chung (xem {@link ScreenshotHelper}).
 */
public final class ReportWriter {

    private static final DateTimeFormatter RUN_ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path runDir;
    private final Path screenshotsDir;
    private final String scenarioName;
    private final List<ReportEntry> entries = new ArrayList<>();

    private ReportWriter(Path runDir, Path screenshotsDir, String scenarioName) {
        this.runDir = runDir;
        this.screenshotsDir = screenshotsDir;
        this.scenarioName = scenarioName;
    }

    public static ReportWriter startReport(Path reportBaseDir, String scenarioName) throws IOException {
        String runId = RUN_ID_FORMAT.format(LocalDateTime.now());
        Path runDir = reportBaseDir.resolve(runId + "_" + scenarioName);
        Path screenshotsDir = runDir.resolve("screenshots");
        Files.createDirectories(screenshotsDir);
        return new ReportWriter(runDir, screenshotsDir, scenarioName);
    }

    public Path screenshotsDir() {
        return screenshotsDir;
    }

    public Path runDir() {
        return runDir;
    }

    public void addEntry(String requirement, String actualResult, boolean passed, Path screenshotAbsolutePath) {
        Path relative = screenshotAbsolutePath == null ? null : runDir.relativize(screenshotAbsolutePath);
        entries.add(new ReportEntry(requirement, actualResult, passed, relative));
    }

    public Path writeMarkdown() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Bao cao: ").append(scenarioName).append("\n\n");
        sb.append("| Yêu cầu | Kết quả | Pass/Fail | Ảnh |\n");
        sb.append("|---|---|---|---|\n");
        for (ReportEntry entry : entries) {
            String image = entry.screenshotRelativePath() == null
                    ? ""
                    : "![screenshot](" + entry.screenshotRelativePath().toString().replace('\\', '/') + ")";
            sb.append("| ").append(entry.requirement())
                    .append(" | ").append(entry.actualResult())
                    .append(" | ").append(entry.passed() ? "Pass" : "Fail")
                    .append(" | ").append(image)
                    .append(" |\n");
        }
        Path reportFile = runDir.resolve("report.md");
        Files.writeString(reportFile, sb.toString());
        return reportFile;
    }
}
