package org.werther.dq;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.werther.dq.config.ConfigManager;
import org.werther.dq.ha.MasterElection;
import org.werther.dq.rocksdb.RDB;
import org.werther.dq.service.MetaService;
import org.werther.dq.utils.ZkUtils;

import java.util.concurrent.CountDownLatch;


public class ChronosStartup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChronosStartup.class);

    private CountDownLatch waitForShutdown;
    private String configFilePath = "chronos.yaml";

    ChronosStartup(final String configFilePath) {
        if (StringUtils.isNotBlank(configFilePath)) {
            this.configFilePath = configFilePath;
        }
    }

    public void start() throws Exception {
        LOGGER.info("start to launch chronos...");
        final long start = System.currentTimeMillis();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    LOGGER.info("start to stop chronos...");
                    final long start = System.currentTimeMillis();
                    ChronosStartup.this.stop();
                    final long cost = System.currentTimeMillis() - start;
                    LOGGER.info("succ stop chronos, cost:{}ms", cost);
                } catch (Exception e) {
                    LOGGER.error("error while shutdown chronos, err:{}", e.getMessage(), e);
                } finally {
                    /* shutdown log4j2 */
                    //LogManager.shutdown();
                }
            }
        });

        /* 注意: 以下初始化顺序有先后次序 */

        /* init config */
        ConfigManager.initConfig(configFilePath);

        /* init rocksdb */
        RDB.init(ConfigManager.getConfig().getDbConfig().getDbPath());

        /* init zk */
       ZkUtils.init();

        /* init seektimestamp */
        MetaService.load();

        if (ConfigManager.getConfig().isStandAlone()) {
            /* standalone */
            MasterElection.standAlone();
        } else {
            /* 集群模式 master election */
            MasterElection.election(waitForShutdown);
        }

        LOGGER.info("  start up.... ");


    }

    void stop() {


        /* close rocksdb */
        RDB.close();

        if (waitForShutdown != null) {
            waitForShutdown.countDown();
            waitForShutdown = null;
        }
    }
}