package io.fleethub.benchmark;

import io.fleethub.clients.LettuceConnectionManager;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import io.lettuce.core.api.reactive.RedisStringReactiveCommands;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
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
public class LettuceBench {

    private LettuceConnectionManager lettuce;
    private static Integer lettuceAsyncGetCount = 0;
    private static Integer lettuceAsyncSetCount = 0;
    private static Integer lettuceReactiveGetCount = 0;
    private static Integer lettuceReactiveSetCount = 0;
    private static Integer lettucePipelineGetCount = 0;
    private static Integer lettucePipelineSetCount = 0;
    private static Integer lettuceBatchGetCount = 0;
    private static Integer lettuceBatchSetCount = 0;

    @Setup
    public void Setup() {
        KeyGenerator.createBenchmarkKeys();
        lettuce = LettuceConnectionManager.instance();
    }

    @Benchmark
    public String lettuceAsyncGet() {
        if (lettuceAsyncGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            lettuceAsyncGetCount = 0;
        }
        lettuceAsyncGetCount++;

        RedisStringAsyncCommands<String, String> async = lettuce.client().async();
        RedisFuture<String> future = async.get(String.format(KeyGenerator.KeyPrefix, lettuceAsyncGetCount));
        String result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceAsyncSet() {
        lettuceAsyncSetCount++;
        RedisStringAsyncCommands<String, String> async = lettuce.client().async();
        RedisFuture<String> future = async.set(String.format("LettuceSetAsync%s", lettuceAsyncSetCount), lettuceAsyncSetCount.toString());
        String result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceReactiveGet() {
        if (lettuceReactiveGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            lettuceReactiveGetCount = 0;
        }
        lettuceReactiveGetCount++;
        RedisStringReactiveCommands<String, String> reactive = lettuce.client().reactive();
        Mono<String> future = reactive.get(String.format(KeyGenerator.KeyPrefix, lettuceReactiveGetCount));
        String result = null;
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Benchmark
    public String lettuceReactiveSet() {
        lettuceReactiveSetCount++;
        RedisStringReactiveCommands<String, String> reactive = lettuce.client().reactive();
        Mono<String> future = reactive.set(String.format("lettuceSetReactive%s", lettuceReactiveSetCount), lettuceReactiveSetCount.toString());
        String result = null;
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    @OperationsPerInvocation(20)
    public Boolean lettucePipelineGet() {
        if (lettucePipelineGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            lettucePipelineGetCount = 0;
        }
        int pipelineSize = 20;
        boolean result=false;

        RedisAdvancedClusterAsyncCommands<String, String> commands = lettuce.client().async();
        List<RedisFuture<?>> futures = new ArrayList<>();
        for (int i=0;i<pipelineSize;i++) {
            lettucePipelineGetCount++;
            futures.add(commands.get(String.format(KeyGenerator.KeyPrefix, lettucePipelineGetCount)));
        }

        try {
            // synchronization: Wait until all futures complete
            result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS,
                    futures.toArray(new RedisFuture[futures.size()]));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Benchmark
    @OperationsPerInvocation(20)
    public Boolean lettucePipelineSet() {
        int pipelineSize = 20;
        boolean result=false;
        RedisAdvancedClusterAsyncCommands<String, String> commands = lettuce.client().async();
        List<RedisFuture<?>> futures = new ArrayList<>();
        for (int i=0;i<pipelineSize;i++) {
            lettucePipelineSetCount++;
            futures.add(commands.set(
                    String.format("lettuceSetPipeline%s", lettucePipelineSetCount),
                    lettucePipelineSetCount.toString()));
        }
        try {
            // synchronization: Wait until all futures complete
            result = LettuceFutures.awaitAll(50, TimeUnit.SECONDS,
                    futures.toArray(new RedisFuture[futures.size()]));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Benchmark
    @OperationsPerInvocation(20)
    public Boolean lettuceBatchGet() {
        if (lettuceBatchGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            lettuceBatchGetCount = 0;
        }
        int batchSize = 20;
        boolean result=false;
        StatefulRedisClusterConnection connection = lettuce.client();
        RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
        List<RedisFuture<?>> futures = new ArrayList<>();

        connection.setAutoFlushCommands(false);
        for (int i=0;i<batchSize;i++) {
            lettuceBatchGetCount++;
            futures.add(commands.get(String.format(KeyGenerator.KeyPrefix, lettuceBatchGetCount)));
        }
        try {
            // synchronization: Wait until all futures complete
            connection.flushCommands();
            result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS,
                    futures.toArray(new RedisFuture[futures.size()]));

        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.setAutoFlushCommands(true);
        return result;
    }
    @Benchmark
    @OperationsPerInvocation(20)
    public Boolean lettuceBatchSet() {
        int batchSize = 20;
        boolean result=false;
        StatefulRedisClusterConnection connection = lettuce.client();
        RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
        List<RedisFuture<?>> futures = new ArrayList<>();

        connection.setAutoFlushCommands(false);
        for (int i=0;i<batchSize;i++) {
            lettuceBatchSetCount++;
            futures.add(commands.set(
                    String.format("lettuceSetBatch%s", lettuceBatchSetCount),
                    lettuceBatchSetCount.toString()));
        }
        try {
            // synchronization: Wait until all futures complete
            connection.flushCommands();
            result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS,
                    futures.toArray(new RedisFuture[futures.size()]));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @TearDown
    public void CloseConnection() {
        try {
            lettuce.client().close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws RunnerException {
        System.out.println("Starting Lettuce benchmark...");

        Options options = new OptionsBuilder()
                .include(LettuceBench.class.getSimpleName())
                .forks(0)
                .build();
        new Runner(options).run();
    }
}
