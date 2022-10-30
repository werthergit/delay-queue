package org.werther.dq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.werther.dq.ChronosStartup;


public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChronosStartup.class);

    private static ChronosConfig cfg = null;

    public static void initConfig(final String configPath) {
        try {
            final long start = System.currentTimeMillis();
            cfg = ConfigUtils.newConfig(configPath, ChronosConfig.class);
            final long cost = System.currentTimeMillis() - start;
            LOGGER.info("succ init chronos config, cost:{}ms, config:{}, configFilePath:{}", cost, cfg, configPath);
        } catch (Exception e) {
            LOGGER.error("error initConfig, configPath:{}, err:{}", configPath, e.getMessage(), e);
        }
    }

    public static ChronosConfig getConfig() {
        return cfg;
    }
}