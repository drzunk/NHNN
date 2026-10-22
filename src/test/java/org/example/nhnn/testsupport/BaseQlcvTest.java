package org.example.nhnn.testsupport;

import org.example.nhnn.config.EnvConfig;
import org.example.nhnn.session.KnownSystems;
import org.example.nhnn.session.SessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Lop cha cho test tren QLCV: moi test method nhan mot BrowserContext da nap
 * storageState "qlcv" (xem LoginBootstrapperApp).
 */
public abstract class BaseQlcvTest extends BaseTest {

    private static EnvConfig config;
    private static SessionManager sessionManager;

    @BeforeAll
    static void setUpSession() {
        config = EnvConfig.load();
        sessionManager = new SessionManager(config);
    }

    @BeforeEach
    void openQlcvSession() {
        context = sessionManager.newContext(browser, KnownSystems.QLCV);
        page = context.newPage();

        if (!sessionManager.isSessionValid(page, KnownSystems.qlcvLoginIndicator(config))) {
            throw new IllegalStateException(
                    "storageState cho QLCV khong hop le hoac da het han. Hay chay: "
                            + "mvn exec:java -Dexec.mainClass=org.example.nhnn.session.LoginBootstrapperApp "
                            + "-Dexec.args=qlcv");
        }
    }
}
