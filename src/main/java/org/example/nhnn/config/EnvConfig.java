package org.example.nhnn.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Doc bi mat/cau hinh tu config/.env (dinh dang KEY=VALUE don gian, dong bat
 * dau bang '#' la comment, dong rong bo qua). File nay KHONG duoc commit vao
 * git (xem .gitignore) - xem config/.env.example de biet danh sach key.
 */
public final class EnvConfig {

    private static final Path DEFAULT_ENV_FILE = Path.of("config", ".env");
    private static final Path STATE_DIR = Path.of("config", "state");
    private static final Path CAPTCHA_DIR = STATE_DIR.resolve("captcha");
    private static final Path REPORT_DIR = Path.of("reports");

    private final Map<String, String> values;

    private EnvConfig(Map<String, String> values) {
        this.values = values;
    }

    public static EnvConfig load() {
        return load(DEFAULT_ENV_FILE);
    }

    public static EnvConfig load(Path envFile) {
        Map<String, String> parsed = new LinkedHashMap<>();
        if (Files.exists(envFile)) {
            try {
                for (String line : Files.readAllLines(envFile)) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    int eq = trimmed.indexOf('=');
                    if (eq <= 0) {
                        continue;
                    }
                    String key = trimmed.substring(0, eq).trim();
                    String value = trimmed.substring(eq + 1).trim();
                    parsed.put(key, value);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Khong doc duoc file config: " + envFile, e);
            }
        }
        return new EnvConfig(parsed);
    }

    public String get(String key) {
        return values.get(key);
    }

    public String required(String key) {
        String value = values.get(key);
        if (value == null || value.isBlank() || "CHANGE_ME".equals(value)) {
            throw new IllegalStateException(
                    "Thieu cau hinh bat buoc '" + key + "' trong " + DEFAULT_ENV_FILE
                            + ". Hay sao chep config/.env.example thanh config/.env va dien gia tri that.");
        }
        return value;
    }

    public String dvcNhnnBaseUrl() {
        return required("DVC_NHNN_BASE_URL");
    }

    public String dvcNhnnCucCnttUsername() {
        return required("DVC_NHNN_CUCCNTT_USERNAME");
    }

    public String dvcNhnnCucCnttPassword() {
        return required("DVC_NHNN_CUCCNTT_PASSWORD");
    }

    public String dvcNhnnVuTccbUsername() {
        return required("DVC_NHNN_VUTCCB_USERNAME");
    }

    public String dvcNhnnVuTccbPassword() {
        return required("DVC_NHNN_VUTCCB_PASSWORD");
    }

    public Path storageStateDir() {
        return STATE_DIR;
    }

    public Path captchaDir() {
        return CAPTCHA_DIR;
    }

    /**
     * true (mac dinh) = Chromium chay an (headless) - phu hop CI/chay hang
     * loat. false = hien cua so trinh duyet that de quan sat truc tiep luc
     * debug (xem config/.env: PW_HEADLESS=false).
     */
    public boolean headless() {
        String value = get("PW_HEADLESS");
        return value == null || !value.equalsIgnoreCase("false");
    }

    public Path reportDir() {
        return REPORT_DIR;
    }

    /**
     * true (mac dinh) = thu tu dong doc captcha bang CaptchaOcrHelper truoc
     * khi cho nguoi/agent dien tay. false = luon cho tra loi thu cong (vo
     * hieu hoa OCR, vi du: CAPTCHA_OCR_ENABLED=false trong config/.env).
     */
    public boolean captchaOcrEnabled() {
        String value = get("CAPTCHA_OCR_ENABLED");
        return value == null || !value.equalsIgnoreCase("false");
    }
}
