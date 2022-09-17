package org.sample;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.List;
import java.util.stream.Collectors;

public final class RedissonConnectionManager {

    private static RedissonConnectionManager connectionManager;
    private RedissonClient client;

    private RedissonClient createConnection() {
        List<SimpleUri> uris = BenchmarkConfiguration.get().getRedisUris();
        List<String> nodeAddresses = uris.stream()
                .map(simpleUri -> "redis://" + simpleUri.getHost() + ":" + simpleUri.getPort())
                .collect(Collectors.toList());
        Config config = new Config();
        config.setNettyThreads(0);
        config.useClusterServers().addNodeAddress(nodeAddresses.toArray(new String[nodeAddresses.size()]));
        client = Redisson.create(config);
        return client;
    }

    public RedissonClient client() {
        return client;
    }

    /**
     * Get singleton Instance
     * @return RedissonConnectionManager
     */
    public static RedissonConnectionManager instance() {
        if(connectionManager==null) {
            connectionManager = new RedissonConnectionManager();
            connectionManager.createConnection();
        }
        return connectionManager;
    }
}
