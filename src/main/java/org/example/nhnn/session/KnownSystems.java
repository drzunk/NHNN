package org.example.nhnn.session;

import org.example.nhnn.config.EnvConfig;

/**
 * Hang so dinh danh he thong + selector dang nhap.
 *
 * Selector cua DVC-NHNN lay truc tiep tu SOP_Automation_Testing_QLCV_NHNN_Cho_Opus.md
 * (da duoc xac nhan chay thanh cong thuc te boi agent Antigravity truoc day).
 * Selector cua QLCV CHUA duoc xac nhan trong SOP - danh dau TODO, can kiem tra
 * lai DOM that truoc khi dung LoginBootstrapperApp cho QLCV.
 */
public final class KnownSystems {

    public static final String DVC_NHNN = "dvc-nhnn";
    public static final String QLCV = "qlcv";

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

    /**
     * TODO: SOP khong mo ta chi tiet DOM trang dang nhap QLCV. Can xac nhan
     * lai selector o nhap username/password that truoc khi dung cho QLCV.
     */
    public static LoginIndicator qlcvLoginIndicator(EnvConfig config) {
        return new LoginIndicator(config.qlcvBaseUrl(), "input[type='password']");
    }
}
