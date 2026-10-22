package org.example.nhnn.ocr;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * OCR tu xay dung (khong phu thuoc Tesseract/thu vien native - chi dung
 * java.awt/javax.imageio co san trong JDK) de doc captcha anh 3 chu so cua
 * DVC-NHNN, thay cho viec nguoi dung phai doc va go tay.
 *
 * QUYET DINH CO CHU DICH (24/10/2026): du an nay truoc day co rang buoc
 * "khong OCR captcha" (xem lich su git README/SOP) - nguoi dung da xac nhan
 * ro rang muon bo rang buoc do va tu dong hoa buoc nay. Vi chi co 1 mau
 * captcha that de doi chieu luc xay dung (chua co bo du lieu huan luyen),
 * do chinh xac KHONG duoc dam bao cho moi bien the captcha sinh ra sau nay -
 * {@link org.example.nhnn.session.LoginBootstrapperApp} van giu nguyen co
 * che cho file tra loi thu cong lam phuong an du phong khi OCR khong chac
 * chan (xem {@link #solve(Path)} tra ve rong).
 *
 * Thuat toan: nhi phan hoa anh theo do xam -> AN MON (morphological erosion,
 * giu lai pixel co >=5/8 lang gieng la "muc") de xoa net nhieu cheo mong
 * (thuong chi day 1px nen khong song sot qua an mon) nhung giu lai phan loi
 * cua net chu so (day hon) -> tim vung lien thong tren anh DA AN MON -> loc
 * manh vun nho (cham nhieu) -> con lai dung 3 vung ung voi 3 chu so -> nhan
 * dang tung vung bang so sanh voi template chu so 0-9 (cung duoc an mon
 * giong het cach xu ly anh that, de so sanh "táo voi táo") tu ve bang
 * Graphics2D.
 *
 * Phat hien thuc te khi xay dung: net nhieu cheo trong captcha nay thuong
 * CHAM/DINH LIEN vao dung 1 trong 3 chu so qua phep noi vung 8-huong, khien
 * buoc loc nhieu don gian (chi dua vao mat do/kich thuoc bbox) vo tinh xoa
 * luon ca chu so bi dinh do. An mon truoc khi tim vung lien thong giai
 * quyet dung goc van de nay.
 */
public final class CaptchaOcrHelper {

    private static final int DIGIT_COUNT = 3;
    private static final int GRAY_INK_THRESHOLD = 170;
    private static final int EROSION_MIN_NEIGHBORS = 6;
    private static final int NORM_WIDTH = 20;
    private static final int NORM_HEIGHT = 28;
    private static final int MIN_DIGIT_PIXELS = 15;

    private static final List<DigitTemplate> TEMPLATES = buildTemplates();

    private CaptchaOcrHelper() {
    }

    /** Doc anh captcha va co doan 3 chu so. Rong neu khong chac chan. */
    public static Optional<String> solve(Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                return Optional.empty();
            }
            boolean[][] ink = binarize(image);
            boolean[][] eroded = erode(ink, EROSION_MIN_NEIGHBORS);
            List<Component> components = findComponents(eroded);
            components.removeIf(c -> c.pixelCount < MIN_DIGIT_PIXELS);
            if (components.size() != DIGIT_COUNT) {
                return Optional.empty();
            }
            components.sort(Comparator.comparingInt(c -> c.minX));

            StringBuilder result = new StringBuilder();
            for (Component digit : components) {
                result.append(classify(eroded, digit));
            }
            String answer = result.toString();
            return answer.matches("\\d{" + DIGIT_COUNT + "}") ? Optional.of(answer) : Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static boolean[][] binarize(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        boolean[][] ink = new boolean[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int rgb = image.getRGB(x, y);
                int gray = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                ink[x][y] = gray < GRAY_INK_THRESHOLD;
            }
        }
        return ink;
    }

    private static List<Component> findComponents(boolean[][] ink) {
        int w = ink.length;
        int h = ink[0].length;
        boolean[][] visited = new boolean[w][h];
        List<Component> components = new ArrayList<>();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (!ink[x][y] || visited[x][y]) {
                    continue;
                }
                Component c = new Component();
                Deque<int[]> queue = new ArrayDeque<>();
                queue.add(new int[] {x, y});
                visited[x][y] = true;
                while (!queue.isEmpty()) {
                    int[] p = queue.poll();
                    c.addPixel(p[0], p[1]);
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = p[0] + dx;
                            int ny = p[1] + dy;
                            if (nx >= 0 && nx < w && ny >= 0 && ny < h && ink[nx][ny] && !visited[nx][ny]) {
                                visited[nx][ny] = true;
                                queue.add(new int[] {nx, ny});
                            }
                        }
                    }
                }
                components.add(c);
            }
        }
        return components;
    }

    /**
     * An mon: 1 pixel muc chi "song sot" neu co >= minNeighbors trong so 8
     * lang gieng cung la muc - net mong 1px (duong cheo nhieu) hau nhu luon
     * co it lang gieng nen bi xoa, trong khi loi net chu so (day hon) van
     * con lai mot phan dang ke.
     */
    private static boolean[][] erode(boolean[][] ink, int minNeighbors) {
        int w = ink.length;
        int h = ink[0].length;
        boolean[][] out = new boolean[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (!ink[x][y]) {
                    continue;
                }
                int neighbors = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h && ink[nx][ny]) {
                            neighbors++;
                        }
                    }
                }
                out[x][y] = neighbors >= minNeighbors;
            }
        }
        return out;
    }

    private static char classify(boolean[][] ink, Component digit) {
        boolean[][] normalized = normalize(ink, digit);
        DigitTemplate best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (DigitTemplate template : TEMPLATES) {
            int distance = hammingDistance(normalized, template.pixels);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = template;
            }
        }
        return best == null ? '?' : best.digit;
    }

    /** Cat theo bounding box roi phong/thu ve luoi co dinh NORM_WIDTH x NORM_HEIGHT. */
    private static boolean[][] normalize(boolean[][] ink, Component c) {
        int bboxW = c.maxX - c.minX + 1;
        int bboxH = c.maxY - c.minY + 1;
        boolean[][] out = new boolean[NORM_WIDTH][NORM_HEIGHT];
        for (int nx = 0; nx < NORM_WIDTH; nx++) {
            for (int ny = 0; ny < NORM_HEIGHT; ny++) {
                int srcX = c.minX + nx * bboxW / NORM_WIDTH;
                int srcY = c.minY + ny * bboxH / NORM_HEIGHT;
                out[nx][ny] = ink[srcX][srcY];
            }
        }
        return out;
    }

    private static int hammingDistance(boolean[][] a, boolean[][] b) {
        int distance = 0;
        for (int x = 0; x < NORM_WIDTH; x++) {
            for (int y = 0; y < NORM_HEIGHT; y++) {
                if (a[x][y] != b[x][y]) {
                    distance++;
                }
            }
        }
        return distance;
    }

    /** Ve san 0-9 bang Graphics2D lam template doi chieu (khong co mau that de huan luyen). */
    private static List<DigitTemplate> buildTemplates() {
        List<DigitTemplate> templates = new ArrayList<>();
        String[] fonts = {Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED};
        for (char digit = '0'; digit <= '9'; digit++) {
            for (String fontName : fonts) {
                templates.add(renderTemplate(digit, fontName));
            }
        }
        return templates;
    }

    private static DigitTemplate renderTemplate(char digit, String fontName) {
        int size = 40;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLACK);
        g.setFont(new Font(fontName, Font.BOLD, 32));
        g.drawString(String.valueOf(digit), 8, 32);
        g.dispose();

        boolean[][] rawInk = new boolean[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int rgb = img.getRGB(x, y);
                int gray = (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3;
                rawInk[x][y] = gray < GRAY_INK_THRESHOLD;
            }
        }
        // An mon giong het anh captcha that, de so sanh template tren cung
        // mot "khong gian" voi anh da an mon (xem Javadoc dau class).
        boolean[][] ink = erode(rawInk, EROSION_MIN_NEIGHBORS);
        int minX = size, minY = size, maxX = 0, maxY = 0;
        boolean any = false;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (ink[x][y]) {
                    any = true;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (!any) {
            return new DigitTemplate(digit, new boolean[NORM_WIDTH][NORM_HEIGHT]);
        }
        Component bbox = new Component();
        bbox.minX = minX;
        bbox.minY = minY;
        bbox.maxX = maxX;
        bbox.maxY = maxY;
        bbox.pixelCount = 1;
        return new DigitTemplate(digit, normalize(ink, bbox));
    }

    private static final class Component {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int pixelCount = 0;

        void addPixel(int x, int y) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            pixelCount++;
        }
    }

    private static final class DigitTemplate {
        final char digit;
        final boolean[][] pixels;

        DigitTemplate(char digit, boolean[][] pixels) {
            this.digit = digit;
            this.pixels = pixels;
        }
    }
}
