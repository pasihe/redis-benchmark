package io.fleethub.benchmark;

import io.fleethub.clients.RedissonConnectionManager;
import io.fleethub.utils.BenchmarkConfiguration;
import io.fleethub.utils.KeyGenerator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 1)
@Threads(1)
@State(Scope.Thread)
@Measurement(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Timeout(time = 30, timeUnit = TimeUnit.SECONDS)
public class RedissonBench {

    private RedissonConnectionManager redisson;
    private static Integer redissonGetCount = 0;
    private static Integer redissonSetCount = 0;
    private static Integer redissonAsyncGetCount = 0;
    private static Integer redissonAsyncSetCount = 0;
    private static Integer redissonReactiveGetCount = 0;
    private static Integer redissonReactiveSetCount = 0;
    private static Integer redissonBatchGetCount = 0;
    private static Integer redissonBatchSetCount = 0;
    private static Integer redissonObjectMapLocalCacheGetCount=0;
    private static Integer redissonGetMultiValueKeyCount = 0;
    private static Integer redissonGetSetMultiValueKeyCount = 0;

    @Setup()
    public void MainSetup() {
        //KeyGenerator.createBenchmarkKeys();
        KeyGenerator.createExtraBenchmarkKeys();
        //KeyGenerator.createBenchmarkMaps();
    }

    @Setup(Level.Trial)
    public void InstantiateClientForBenchmark() {
        redisson = new RedissonConnectionManager();
        redisson.connect();
    }

    @Benchmark
    public String redissonGet() {
        if (redissonGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            redissonGetCount = 0;
        }
        redissonGetCount++;
        String result = null;
        try {
            RBucket<String> bucket = redisson.client().getBucket(
                    String.format(KeyGenerator.KeyPrefix, redissonGetCount),StringCodec.INSTANCE);
            result = bucket.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String redissonSet() {
        redissonSetCount++;
        String result = null;
        try {
            RBucket<String> bucket = redisson.client().getBucket(
                    String.format("RedissonSyncSetTest%s", redissonSetCount), StringCodec.INSTANCE);
            bucket.set(redissonSetCount.toString());
            result = "ok";
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String redissonAsyncGet() {
        if (redissonAsyncGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            redissonAsyncGetCount = 0;
        }
        redissonAsyncGetCount++;
        String result = null;
        try {
            RBucket<String> bucket = redisson.client().getBucket(
                    String.format(KeyGenerator.KeyPrefix, redissonAsyncGetCount),StringCodec.INSTANCE);
            RFuture<String> future = bucket.getAsync();
            result = future.get();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Benchmark
    public String redissonAsyncSet() {
        redissonAsyncSetCount++;
        String result = null;
        try {
            RBucket<String> bucket = redisson.client().getBucket(
                    String.format("RedissonAsyncSetTest%s", redissonAsyncSetCount), StringCodec.INSTANCE);
            RFuture<Void> future = bucket.setAsync(redissonAsyncSetCount.toString());
            future.get();
            result = "ok";
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String redissonReactiveGet() {
        if (redissonReactiveGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            redissonReactiveGetCount = 0;
        }
        redissonReactiveGetCount++;
        String result = null;
        try {
            RedissonReactiveClient reactiveClient = redisson.client().reactive();
            RBucketReactive<String> bucket = reactiveClient.getBucket(
                    String.format(KeyGenerator.KeyPrefix, redissonReactiveGetCount),StringCodec.INSTANCE);
            Mono<String> value = bucket.get();
            result = value.block();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Benchmark
    public String redissonReactiveSet() {
        redissonReactiveSetCount++;
        String result = null;
        try {
            RedissonReactiveClient reactiveClient = redisson.client().reactive();
            RBucketReactive<String> bucket = reactiveClient.getBucket(
                    String.format("RedissonReactiveSetTest%s", redissonReactiveSetCount), StringCodec.INSTANCE);
            Mono<Void> value = bucket.set(redissonReactiveSetCount.toString());
            value.block();
            result = "ok";
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    @OperationsPerInvocation(20)
    public String redissonBatchGet() {
        if (redissonBatchGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            redissonBatchGetCount = 0;
        }
        int pipelineSize = 20;
        String result = null;
        try {
            RBatch batch = redisson.client().createBatch(BatchOptions.defaults());
            List<RFuture<String>> futures = new ArrayList<>();
            for (int i=0;i<pipelineSize;i++) {
                redissonBatchGetCount++;
                RBucketAsync<String> bucket = batch.getBucket(String.format(KeyGenerator.KeyPrefix, redissonBatchGetCount), StringCodec.INSTANCE);
                futures.add(bucket.getAsync());
            }
            RFuture<BatchResult<?>> resFuture = batch.executeAsync();
            List<?> responses = resFuture.get().getResponses();
            result = String.valueOf(responses.size());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    @OperationsPerInvocation(20)
    public int redissonBatchSet() {
        redissonBatchSetCount++;
        int pipelineSize = 20;
        int result = 0;
        try {
            RBatch batch = redisson.client().createBatch(BatchOptions.defaults());
            List<RFuture<Void>> futures = new ArrayList<>();
            for (int i=0;i<pipelineSize;i++) {
                redissonBatchSetCount++;
                RBucketAsync<String> bucket = batch.getBucket(
                        String.format("RedissonBatchSetTest%s", redissonBatchSetCount), StringCodec.INSTANCE);
                futures.add(bucket.setAsync(redissonBatchSetCount.toString()));
            }
            RFuture<BatchResult<?>> resFuture = batch.executeAsync();
            List<?> responses = resFuture.get().getResponses();
            result = responses.size();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public List<String> redissonObjectMapLocalCacheGet() {
        if (redissonObjectMapLocalCacheGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            redissonObjectMapLocalCacheGetCount = 0;
        }
        List<String> result = null;
        redissonObjectMapLocalCacheGetCount++;
        try {
            result = redisson.getLocalCachedMap().get(String.format(KeyGenerator.MapKeyPrefix, redissonObjectMapLocalCacheGetCount));
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public List<String> redissonGetMultiValueKey() {
        if (redissonGetMultiValueKeyCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            redissonGetMultiValueKeyCount = 0;
        }
        redissonGetMultiValueKeyCount++;
        List<String> result = null;
        try {
            RBucket<List<String>> bucket = redisson.client().getBucket(
                    String.format(KeyGenerator.ExtraKeyPrefix, redissonGetMultiValueKeyCount));
            result = bucket.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Benchmark
    public List<String> redissonGetSetMultiValueKey() {
        if (redissonGetSetMultiValueKeyCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            redissonGetSetMultiValueKeyCount = 0;
        }
        redissonGetSetMultiValueKeyCount++;
        List<String> result = null;
        try {
            RSet<String> set = redisson.client().getSet(
                    String.format(KeyGenerator.ExtraKeySetPrefix, redissonGetSetMultiValueKeyCount));
            result = set.readAll().stream().collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @TearDown(Level.Trial)
    public void CloseConnection() {
        if (redisson != null && redisson.client()!=null) {
            redisson.client().shutdown();
        }
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println("Starting Redisson benchmark...");

        Options options = new OptionsBuilder()
                .include(RedissonBench.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(options).run();
    }

}

