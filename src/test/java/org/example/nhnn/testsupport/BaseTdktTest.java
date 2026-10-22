package org.example.nhnn.testsupport;

import org.example.nhnn.config.EnvConfig;
import org.example.nhnn.session.KnownSystems;
import org.example.nhnn.session.SessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Lop cha cho test luong TDKT (Thi dua khen thuong) tren DVC-NHNN: moi test
 * method nhan mot BrowserContext da nap storageState "dvc-nhnn-tccb" (tai
 * khoan Vu TCCB, xem LoginBootstrapperApp) - khac BaseDvcNhnnTest (tai khoan
 * Cuc CNTT dung cho CTS) vi 2 luong nghiep vu dung 2 tai khoan khac nhau tren
 * cung 1 he thong.
 */
public abstract class BaseTdktTest extends BaseTest {

    private static EnvConfig config;
    private static SessionManager sessionManager;

    @BeforeAll
    static void setUpSession() {
        config = EnvConfig.load();
        sessionManager = new SessionManager(config);
    }

    @BeforeEach
    void openTccbSession() {
        context = sessionManager.newContext(browser, KnownSystems.DVC_NHNN_TCCB);
        page = context.newPage();

        if (!sessionManager.isSessionValid(page, KnownSystems.dvcNhnnTccbLoginIndicator(config))) {
            throw new IllegalStateException(
                    "storageState cho DVC-NHNN (Vu TCCB) khong hop le hoac da het han. Hay chay: "
                            + "mvn exec:java -Dexec.mainClass=org.example.nhnn.session.LoginBootstrapperApp "
                            + "-Dexec.args=dvc-nhnn-tccb");
        }
    }
}
