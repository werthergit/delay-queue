package org.werther.dq.ha;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class LeaderElectionDemo {
    private static CuratorFramework client = CuratorFrameworkFactory.newClient("zk01-dev.chj.cloud:10311", new ExponentialBackoffRetry(3000, 3));
    private static String path = "/a1/dq/mutex/path/0001";
    static {
        client.start();
    }

    public static void main(String[] args) throws InterruptedException {
        startThread0();
        Thread.sleep(10);
        startThread1();
        Thread.sleep(50000);
        client.close();
    }

    public static void startThread0() {
        new Thread(() -> {
            LeaderSelector selector = new LeaderSelector(client, path, new LeaderSelectorListener() {
                @Override
                public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                    System.out.println("thread0 is leader");
                }

                @Override
                public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                    System.out.println("thread0 state changed，state is " + connectionState.isConnected());
                }
            });
            selector.autoRequeue();
            selector.start();
        }).start();
    }

    public static void startThread1() {
        new Thread(() -> {
            LeaderSelector selector = new LeaderSelector(client, path, new LeaderSelectorListener() {
                @Override
                public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                    System.out.println("thread1 is leader");
                }

                @Override
                public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                    System.out.println("thread1 state changed，state is " + connectionState.isConnected());
                }
            });
            selector.autoRequeue();
            selector.start();
        }).start();
    }
}
