/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sample;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisStringAsyncCommands;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 1)
@Threads(1)
@State(Scope.Benchmark)
@Measurement(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class RedisBenchmark {

    private Config config;
    private RedissonConnectionManager redisson;
    private LettuceConnectionManager lettuce;
    //private StatefulRedisClusterConnection lettuceClient;
    private static Integer redisGetCount = 0;
    private static Integer redisSetCount = 0;
    private static Integer lettuceAsyncGetCount = 0;
    private static Integer lettuceAsyncSetCount = 0;
    private static Integer lettuceReactiveGetCount = 0;
    private static Integer lettuceReactiveSetCount = 0;

    @Setup
    public void Setup() {

        Util.createBenchmarkKeys();
        redisson = RedissonConnectionManager.instance();
        lettuce = LettuceConnectionManager.instance();
    }


    @Benchmark
    public String SimpleGet() {
        if (redisGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            redisGetCount = 0;
        }
        redisGetCount++;
        String result = null;
        try {
            RBucket<String> bucket = redisson.client().getBucket(String.format("Benchmark%s", redisGetCount),StringCodec.INSTANCE);
            result = bucket.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String SimpleSet() {
        redisSetCount++;
        String result = null;
        try {
            RBucket<String> bucket = redisson.client().getBucket(String.format("RedisSetTest%s", redisSetCount), StringCodec.INSTANCE);
            bucket.set(redisSetCount.toString());
            result = "ok";
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Benchmark
    public String lettuceSimpleAsyncGet() {
        if (lettuceAsyncGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            lettuceAsyncGetCount = 0;
        }
        lettuceAsyncGetCount++;
        RedisStringAsyncCommands<String, String> async = lettuce.async();
        RedisFuture<String> future = async.get(String.format(Util.KeyPrefix, lettuceAsyncGetCount));
        String result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Benchmark
    public String lettuceSimpleAsyncSet() {
        lettuceAsyncSetCount++;
        RedisStringAsyncCommands<String, String> async = lettuce.async();
        RedisFuture<String> future = async.set(String.format("LettuceSetAsync%s", lettuceAsyncSetCount), lettuceAsyncSetCount.toString());
        String result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /*
    @Benchmark
    public String lettuceSimpleReactiveGet() {
        if (lettuceReactiveGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            lettuceReactiveGetCount = 0;
        }
        lettuceReactiveGetCount++;
        RedisStringReactiveCommands<String, String> reactive = lettuceClient.reactive();
        Mono<String> future = reactive.get(String.format(Util.KeyPrefix, lettuceReactiveGetCount));
        String result = null;
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    @Benchmark
    public String lettuceSimpleReactiveSet() {
        lettuceReactiveSetCount++;
        RedisStringReactiveCommands<String, String> reactive = lettuceClient.reactive();
        Mono<String> future = reactive.set(String.format("lettuceSetReactive%s", lettuceReactiveSetCount), lettuceReactiveSetCount.toString());
        String result = null;
        try {
            result = future.block();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
*/
    @TearDown
    public void CloseConnection() {
        try {
            redisson.client().shutdown();
            lettuce.client().close();
        }
        catch (Exception e) {

        }
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *
     * You might notice the larger the repetitions count, the lower the "perceived"
     * cost of the operation being measured. Up to the point we do each addition with 1/20 ns,
     * well beyond what hardware can actually do.
     *
     * This happens because the loop is heavily unrolled/pipelined, and the operation
     * to be measured is hoisted from the loop. Morale: don't overuse loops, rely on JMH
     * to get the measurement right.
     *
     * You can run this test:
     *
     * a) Via the command line:
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar RedisBenchmark -f 1 -wi 1 -i 10 -t 8
     *    (we requested eight fork; there are also other options, see -h)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */
    public static void main(String[] args) throws RunnerException {
        System.out.println("Starting benchmark...");

        Options options = new OptionsBuilder()
                .include(RedisBenchmark.class.getSimpleName())
                //.output("redis-throughput.log")
                .forks(0)
                .build();
        new Runner(options).run();
    }
}