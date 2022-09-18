package io.fleethub.clients;

import io.fleethub.utils.BenchmarkConfiguration;
import io.fleethub.utils.SimpleUri;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
