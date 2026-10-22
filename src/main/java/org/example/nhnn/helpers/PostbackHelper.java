package org.example.nhnn.helpers;

import com.microsoft.playwright.Page;

import java.util.Map;

/**
 * Xu ly "Bay 1" trong SOP: ASP.NET AJAX (Sys.WebForms.PageRequestManager) co
 * the nem loi "'caller', 'callee', and 'arguments' properties may not be
 * accessed on strict mode functions" neu goi __doPostBack truc tiep (dong bo)
 * trong page.evaluate(), vi no duyet callstack bang arguments.callee.caller
 * trong moi truong ES6 strict mode.
 *
 * Cach xu ly chuan: boc loi goi postback trong setTimeout(..., 0).
 *
 * Uu tien dung Locator.click() native cua Playwright khi co the - chi dung
 * helper nay khi khong co phan tu clickable tuong ung (vi du postback duoc
 * kich hoat tu mot su kien JS khong gan truc tiep vao phan tu nao ro rang).
 */
public final class PostbackHelper {

    private PostbackHelper() {
    }

    public static void doPostBack(Page page, String target, String argument) {
        page.evaluate(
                "(args) => { setTimeout(() => { __doPostBack(args.target, args.argument); }, 0); }",
                Map.of("target", target, "argument", argument == null ? "" : argument));
    }
}
