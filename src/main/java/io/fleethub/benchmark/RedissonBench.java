package io.fleethub.benchmark;

import io.fleethub.clients.RedissonConnectionManager;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import io.fleethub.utils.BenchmarkConfiguration;
import io.fleethub.utils.KeyGenerator;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 1)
@Threads(1)
@State(Scope.Benchmark)
@Measurement(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
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

    @Setup
    public void Setup() {
        KeyGenerator.createBenchmarkKeys();
        redisson = RedissonConnectionManager.instance();
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

    @TearDown
    public void CloseConnection() {
        try {
            redisson.client().shutdown();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println("Starting Redisson benchmark...");

        Options options = new OptionsBuilder()
                .include(RedissonBench.class.getSimpleName())
                .forks(0)
                .build();
        new Runner(options).run();
    }

}

