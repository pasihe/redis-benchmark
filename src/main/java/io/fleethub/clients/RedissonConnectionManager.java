package io.fleethub.clients;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import io.fleethub.utils.BenchmarkConfiguration;
import io.fleethub.utils.SimpleUri;

import java.util.List;
import java.util.stream.Collectors;

public final class RedissonConnectionManager {

    private static RedissonConnectionManager connectionManager;
    private static RedissonClient client;

    private RedissonClient createConnection() {
        List<SimpleUri> uris = BenchmarkConfiguration.get().getRedisUris();
        List<String> nodeAddresses = uris.stream()
                .map(simpleUri -> "redis://" + simpleUri.getHost() + ":" + simpleUri.getPort())
                .collect(Collectors.toList());
        Config config = new Config();
        config.setNettyThreads(0);
        config.useClusterServers().addNodeAddress(nodeAddresses.toArray(new String[nodeAddresses.size()]));
        this.client = Redisson.create(config);
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
        if(connectionManager==null || connectionManager.client().isShutdown()) {
            connectionManager = new RedissonConnectionManager();
            connectionManager.createConnection();
        }
        return connectionManager;
    }

    public void reconnect() {
        connectionManager.createConnection();
    }
}
