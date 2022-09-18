package io.fleethub.clients;

import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.fleethub.utils.BenchmarkConfiguration;
import io.fleethub.utils.SimpleUri;

import java.util.List;
import java.util.stream.Collectors;

public final class LettuceConnectionManager {

    private static LettuceConnectionManager connectionManager;
    private StatefulRedisClusterConnection<String, String> connection;

    private void createConnection() {
        List<SimpleUri> uris = BenchmarkConfiguration.get().getRedisUris();
        List<RedisURI> addresses =  uris.stream()
                .map(simpleUri -> RedisURI.create(simpleUri.getHost(),simpleUri.getPort()))
                .collect(Collectors.toList());
        RedisClusterClient clusterClient = RedisClusterClient.create(addresses);
        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh()
                .build();

        clusterClient.setOptions(ClusterClientOptions.builder()
                .topologyRefreshOptions(topologyRefreshOptions)
                .build());
        connection = clusterClient.connect();
    }

    public RedisStringAsyncCommands<String, String> async() {
        return connection.async();
    }

    public StatefulRedisClusterConnection client() {
        return connection;
    }

    /**
     * Get singleton Instance
     * @return LettuceConnectionManager
     */
    public static LettuceConnectionManager instance() {
        if(connectionManager==null || connectionManager.client().isOpen()==false) {
            connectionManager = new LettuceConnectionManager();
            connectionManager.createConnection();
        }
        return connectionManager;
    }
}
