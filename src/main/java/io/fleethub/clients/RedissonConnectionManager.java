package io.fleethub.clients;

import org.redisson.Redisson;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import io.fleethub.utils.BenchmarkConfiguration;
import io.fleethub.utils.SimpleUri;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class RedissonConnectionManager {

    private static volatile RedissonConnectionManager _instance;
    private static Object _mutex = new Object();
    private RedissonClient _client;
    private RLocalCachedMap<String,List<String>> localCachedMap;

    private RedissonClient createConnection() {
        List<SimpleUri> uris = BenchmarkConfiguration.get().getRedisUris();
        List<String> nodeAddresses = uris.stream()
                .map(simpleUri -> "redis://" + simpleUri.getHost() + ":" + simpleUri.getPort())
                .collect(Collectors.toList());
        Config config = new Config();
        //config.setLockWatchdogTimeout(300L); // default 30000L
        //config.setCleanUpKeysAmount(100000);
        //config.setNettyThreads(1);
        //config.setThreads(8);
        config.useClusterServers()
                //.setMasterConnectionPoolSize(5)
                //.setSlaveConnectionPoolSize(5)
                //.setSlaveConnectionMinimumIdleSize(1)
                //.setMasterConnectionMinimumIdleSize(1)
                /*
                .setSubscriptionConnectionPoolSize(100000)
                .setSubscriptionsPerConnection(100000)
                .setRetryAttempts(10)
                .setConnectTimeout(10000)
                .setTimeout(10000)
                .setIdleConnectionTimeout(10000)
                .setMasterConnectionPoolSize(100)
                .setSlaveConnectionPoolSize(100)*/
                .addNodeAddress(nodeAddresses.toArray(new String[nodeAddresses.size()]));
        this._client = Redisson.create(config);
        return _client;
    }

    public RedissonClient client() {
        return _client;
    }

    public RLocalCachedMap<String,List<String>> getLocalCachedMap() {
        return localCachedMap;
    }

    /**
     * Get singleton Instance
     * @return RedissonConnectionManager
     */
    public static RedissonConnectionManager getInstance() {
        RedissonConnectionManager result = _instance;
        if(result == null) {
            synchronized (_mutex) {
                result = _instance;
                if(result == null) {
                    _instance = result = new RedissonConnectionManager();
                    _instance.createConnection();
                    _instance.createLocalCache();
                }
            }
        }
        if(_instance.client()==null || _instance.client().isShutdown()) {
            _instance.createConnection();
            _instance.createLocalCache();
        }
        return result;
    }

    private void createLocalCache() {

        LocalCachedMapOptions options = LocalCachedMapOptions.defaults()
                .timeToLive(10, TimeUnit.MINUTES) // Only entry stored in local cache get this ttl and not entry stored in Redis.
                .storeMode(LocalCachedMapOptions.StoreMode.LOCALCACHE_REDIS)
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.NONE) // LFU counts frequence and least used removed
                .cacheSize(1000000)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.LOAD)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE);
        localCachedMap = this._client.getLocalCachedMap("testbench-cached", options);
    }

    public void reconnect() {
        this.createConnection();
    }

    public void connect() {
        createConnection();
        createLocalCache();
    }
}
