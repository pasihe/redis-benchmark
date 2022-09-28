package io.fleethub.benchmark;

import io.fleethub.clients.RedissonConnectionManager;
import io.fleethub.utils.KeyGenerator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RSet;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 1)
@Threads(1)
@State(Scope.Thread)
@Measurement(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Timeout(time = 30, timeUnit = TimeUnit.SECONDS)
public class RedissonLocalCacheBench {

    private RedissonConnectionManager redisson;
    private RLocalCachedMap<String, RSet<List<String>>> localOnlyCache;

    @Setup()
    public void mainSetup() {
        // cache loading inside each iteration
    }

    @Setup(Level.Trial)
    public void instantiateClientForBenchmark() {
        redisson = new RedissonConnectionManager();
        redisson.connect();
        createLocalOnlyCache();
    }

    private void createLocalOnlyCache() {
        LocalCachedMapOptions options = LocalCachedMapOptions.defaults()
                .timeToLive(10, TimeUnit.MINUTES) // Only entry stored in local cache get this ttl and not entry stored in Redis.
                .storeMode(LocalCachedMapOptions.StoreMode.LOCALCACHE)
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.NONE) // LFU counts frequence and least used removed
                .cacheSize(1000000)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.NONE)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.NONE);
        localOnlyCache = redisson.client().getLocalCachedMap("devices", options);
    }

    @Benchmark
    public void populateCache() {
        localOnlyCache.readAllKeySet();
    }

    @TearDown(Level.Trial)
    public void closeConnection() {
        if (redisson != null && redisson.client()!=null) {
            redisson.client().shutdown();
        }
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println("Starting "+RedissonBench.class.getSimpleName()+" benchmark...");

        Options options = new OptionsBuilder()
                .include(RedissonBench.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
