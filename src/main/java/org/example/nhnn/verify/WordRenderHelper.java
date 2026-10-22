package org.example.nhnn.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Render file .docx ra anh that de kiem tra bang mat, boi vi thuoc tinh XML
 * doi khi dung nhung hien thi thuc te sai (bai hoc tu skill "eform" cua
 * nguoi dung). KHONG chay mac dinh trong moi lan test (cham, can cai
 * Microsoft Word tren may chay) - chi goi tuong minh khi can review truc
 * quan.
 *
 * Yeu cau: may chay phai cai Microsoft Word (dung Word COM qua PowerShell).
 */
public final class WordRenderHelper {

    private static final Path CONVERT_SCRIPT = Path.of("scripts", "ConvertDocxToPdf.ps1");

    private WordRenderHelper() {
    }

    /**
     * Chuyen file .docx sang .pdf bang Word COM (qua scripts/ConvertDocxToPdf.ps1).
     *
     * @return duong dan file .pdf da tao.
     */
    public static Path renderToPdf(Path docxPath, Path outDir) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass",
                "-File", CONVERT_SCRIPT.toAbsolutePath().toString(),
                "-DocxPath", docxPath.toAbsolutePath().toString(),
                "-OutDir", outDir.toAbsolutePath().toString());
        builder.redirectErrorStream(true);
        Process process = builder.start();

        String output;
        try (var reader = process.inputReader()) {
            output = reader.lines().reduce("", (a, b) -> a + b + System.lineSeparator());
        }
        boolean finished = process.waitFor(120, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("ConvertDocxToPdf.ps1 qua thoi gian cho (120s).");
        }
        if (process.exitValue() != 0) {
            throw new IllegalStateException("ConvertDocxToPdf.ps1 that bai (exit=" + process.exitValue()
                    + "):" + System.lineSeparator() + output);
        }

        String baseName = docxPath.getFileName().toString().replaceFirst("\\.docx$", "");
        return outDir.resolve(baseName + ".pdf");
    }

    /**
     * TODO (Phase 2): rasterize PDF sang PNG tung trang de so sanh truc quan.
     * Chua them thu vien rasterize PDF (vi du Apache PDFBox) vao pom.xml o
     * Phase 1 vi chua duoc duyet trong plan khung framework - can bo sung
     * dependency va trien khai khi co nhu cau thuc te o Phase 2.
     */
    public static Path renderToPng(Path pdfPath, Path outDir) {
        throw new UnsupportedOperationException(
                "renderToPng() chua duoc trien khai - can bo sung thu vien rasterize PDF o Phase 2.");
    }
}
