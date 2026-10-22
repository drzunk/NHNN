package org.example.nhnn.session;

/**
 * Mo ta cach nhan biet mot he thong dang o trang dang nhap (tuc la session/
 * storageState khong con hop le).
 *
 * @param checkUrl               URL can dieu huong toi de kiem tra session.
 * @param loginFormIndicatorCss  CSS selector dac trung CHI xuat hien khi dang
 *                               o trang dang nhap (vi du o nhap username).
 */
public record LoginIndicator(String checkUrl, String loginFormIndicatorCss) {
}
