package org.example.nhnn.session;

import org.example.nhnn.config.EnvConfig;

/**
 * Hang so dinh danh he thong + selector dang nhap.
 *
 * Selector cua DVC-NHNN lay truc tiep tu SOP_Automation_Testing_QLCV_NHNN_Cho_Opus.md
 * (da duoc xac nhan chay thanh cong thuc te boi agent Antigravity truoc day).
 * Hai he thong con (DVC_NHNN, DVC_NHNN_TCCB) deu dang nhap qua CUNG mot trang
 * (tabid=38), chi khac tai khoan dien vao - nen dung chung 1 bo selector.
 */
public final class KnownSystems {

    public static final String DVC_NHNN = "dvc-nhnn";
    public static final String DVC_NHNN_TCCB = "dvc-nhnn-tccb";

    /** Selector o nhap ten dang nhap tren form login DVC-NHNN. Nguon: SOP muc 3.1. */
    public static final String DVC_NHNN_USERNAME_INPUT = "input[id*='Login_A']";
    /** Selector o nhap mat khau tren form login DVC-NHNN. Nguon: SOP muc 3.1. */
    public static final String DVC_NHNN_PASSWORD_INPUT = "input[id*='Login_B']";
    /** Selector anh captcha tren form login DVC-NHNN. Nguon: SOP muc 3.1. */
    public static final String DVC_NHNN_CAPTCHA_IMAGE = "img[id*='CaptchaImageUP'], img[id*='Captcha']";
    /** Selector o nhap ma captcha tren form login DVC-NHNN. Nguon: SOP muc 3.1. */
    public static final String DVC_NHNN_CAPTCHA_INPUT = "input[id*='CaptchaTextBox']";
    /** Selector nut dang nhap tren form login DVC-NHNN. Nguon: SOP muc 3.1. */
    public static final String DVC_NHNN_LOGIN_BUTTON = "input[id*='btnLogin'], input[value*='Đăng nhập']";

    private KnownSystems() {
    }

    public static LoginIndicator dvcNhnnLoginIndicator(EnvConfig config) {
        return new LoginIndicator(config.dvcNhnnBaseUrl(), DVC_NHNN_USERNAME_INPUT);
    }

    /** Tai khoan Vu TCCB (Thi dua khen thuong) - cung trang dang nhap tabid=38. */
    public static LoginIndicator dvcNhnnTccbLoginIndicator(EnvConfig config) {
        return new LoginIndicator(config.dvcNhnnBaseUrl(), DVC_NHNN_USERNAME_INPUT);
    }
}
