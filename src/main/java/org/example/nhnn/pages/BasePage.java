package org.example.nhnn.pages;

import com.microsoft.playwright.Page;

/**
 * Lop cha toi gian cho Page Object Model. Phase 2 se mo rong cac lop con cu
 * the cho tung man hinh nghiep vu (vi du TiepNhanPage, ThuLyPage...).
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    public Page page() {
        return page;
    }
}
