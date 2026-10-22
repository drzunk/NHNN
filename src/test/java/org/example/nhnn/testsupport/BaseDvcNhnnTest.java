package org.example.nhnn.testsupport;

import org.example.nhnn.config.EnvConfig;
import org.example.nhnn.session.KnownSystems;
import org.example.nhnn.session.SessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Lop cha cho test tren he thong DVC-NHNN: moi test method nhan mot
 * BrowserContext da nap storageState "dvc-nhnn" (xem LoginBootstrapperApp).
 *
 * Neu storageState khong hop le/het han, test se FAIL ngay voi thong bao ro
 * rang - framework KHONG tu mo luong dang nhap tuong tac giua luc chay
 * `mvn test` khong nguoi theo doi.
 */
public abstract class BaseDvcNhnnTest extends BaseTest {

    private static EnvConfig config;
    private static SessionManager sessionManager;

    @BeforeAll
    static void setUpSession() {
        config = EnvConfig.load();
        sessionManager = new SessionManager(config);
    }

    @BeforeEach
    void openDvcNhnnSession() {
        context = sessionManager.newContext(browser, KnownSystems.DVC_NHNN);
        page = context.newPage();

        if (!sessionManager.isSessionValid(page, KnownSystems.dvcNhnnLoginIndicator(config))) {
            throw new IllegalStateException(
                    "storageState cho DVC-NHNN khong hop le hoac da het han. Hay chay: "
                            + "mvn exec:java -Dexec.mainClass=org.example.nhnn.session.LoginBootstrapperApp "
                            + "-Dexec.args=dvc-nhnn");
        }
    }
}
