package org.sample;

import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class JedisConnectionManager {
    private static JedisConnectionManager connectionManager;
    private static Boolean connectionCreated = false;
    private Jedis jedisStandalone;
    private JedisSentinelPool jedisSentinelPool;
    private JedisCluster jedisCluster;

    private void createConnection() {
        List<SimpleUri> uris = BenchmarkConfiguration.get().getRedisUris();
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        for (SimpleUri redisUri : uris) {
            jedisClusterNodes.add(new HostAndPort(redisUri.getHost(), redisUri.getPort()));
        }
        jedisCluster = new JedisCluster(jedisClusterNodes);
    }

    public JedisCluster client() {
        return jedisCluster;
    }

    /**
     * Get singleton Instance
     * @return JedisConnectionManager
     */
    public static JedisConnectionManager instance() {
        if(connectionManager==null) {
            connectionManager = new JedisConnectionManager();
            connectionManager.createConnection();
        }
        return connectionManager;
    }
}
