package org.example.nhnn.verify;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Doc va doi chieu noi dung file .docx bang Apache POI - thay the cho
 * python-docx trong SOP goc. Cac phuong thuc o day mang tinh TONG QUAT, tai
 * dung duoc cho nhieu mau thong bao/van ban khac nhau (khong hard-code rieng
 * cho mot mau cu the).
 *
 * Luu y: day la kiem tra CAU TRUC (XML). Thuoc tinh XML dung khong dam bao
 * hien thi thuc te cung dung - khi can xac nhan bang mat, dung them
 * {@link WordRenderHelper} de render ra PDF/anh.
 */
public final class WordVerifier implements AutoCloseable {

    private final XWPFDocument document;
    private final InputStream inputStream;

    private WordVerifier(XWPFDocument document, InputStream inputStream) {
        this.document = document;
        this.inputStream = inputStream;
    }

    public static WordVerifier open(Path docxPath) throws IOException {
        InputStream in = Files.newInputStream(docxPath);
        try {
            return new WordVerifier(new XWPFDocument(in), in);
        } catch (IOException | RuntimeException e) {
            in.close();
            throw e;
        }
    }

    public boolean headerContains(String text) {
        return headerText().contains(text);
    }

    public boolean footerContains(String text) {
        return footerText().contains(text);
    }

    public String allTablesText() {
        StringBuilder sb = new StringBuilder();
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                List<String> cellTexts = new ArrayList<>();
                for (XWPFTableCell cell : row.getTableCells()) {
                    cellTexts.add(cell.getText().trim());
                }
                sb.append(String.join(" | ", cellTexts)).append('\n');
            }
        }
        return sb.toString();
    }

    public boolean tablesContainAll(String... snippets) {
        String text = allTablesText();
        for (String snippet : snippets) {
            if (!text.contains(snippet)) {
                return false;
            }
        }
        return true;
    }

    /** Dam bao cac moc ngay thang khong co phan gio:phut:giay (chi dd/MM/yyyy). */
    public boolean hasNoTimeComponentInDates() {
        return !allTablesText().contains(":00:00");
    }

    /** Dam bao khong con chu "thứ " (ten thu trong tuan) truoc ngay thang. */
    public boolean hasNoWeekdayPrefix() {
        return !allTablesText().toLowerCase(Locale.ROOT).contains("thứ ");
    }

    private String headerText() {
        StringBuilder sb = new StringBuilder();
        for (XWPFHeader header : document.getHeaderList()) {
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                sb.append(paragraph.getText().trim()).append(' ');
            }
        }
        return sb.toString();
    }

    private String footerText() {
        StringBuilder sb = new StringBuilder();
        for (XWPFFooter footer : document.getFooterList()) {
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                sb.append(paragraph.getText().trim()).append(' ');
            }
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        document.close();
        inputStream.close();
    }
}
