package com.example.detection.drools;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * DroolsEngineProvider
 * Singleton cung cấp engine cho toàn hệ thống
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DroolsEngineProvider {
    private static class DroolsEngineHelper {
        private static final DefaultDroolsEngine INSTANCE = new DefaultDroolsEngine();
    }

    public static synchronized DroolsEngine getInstance() {
        return DroolsEngineHelper.INSTANCE;
    }
}
