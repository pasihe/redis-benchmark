package io.fleethub.benchmark;

import io.fleethub.clients.JedisConnectionManager;
import io.fleethub.utils.BenchmarkConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import io.fleethub.utils.KeyGenerator;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Warmup(iterations = 1)
@Threads(1)
@State(Scope.Benchmark)
@Measurement(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class JedisBench {

    private JedisConnectionManager jedis;
    private static Integer jedisGetCount = 0;
    private static Integer jedisSetCount = 0;

    @Setup
    public void Setup() {
        KeyGenerator.createBenchmarkKeys();
        jedis = JedisConnectionManager.instance();
    }

    @Benchmark
    public String jedisGet() {
        if (jedisGetCount >= BenchmarkConfiguration.get().getAmountOfKeys()) {
            jedisGetCount = 0;
        }
        jedisGetCount++;
        String result = null;
        try {
            result = jedis.client().get(String.format(KeyGenerator.KeyPrefix, jedisGetCount));
        } catch (Exception e) {
            jedis = JedisConnectionManager.instance();
        }
        return result;
    }
    @Benchmark
    public String jedisSet() {
        jedisSetCount++;
        String result = null;
        try {
            result = jedis.client().set(String.format("JedisSetTest%s", jedisSetCount), jedisSetCount.toString());
        }  catch (Exception e) {
            jedis = JedisConnectionManager.instance();
        }
        return result;
    }

    //@Benchmark
    public String jedisPipelineGet() {
        /*
            TODO:
            Waiting for Jedis supporting pipeline and batch in cluster mode.
            https://github.com/redis/jedis/pull/1455

            Jedis also supports batching for transactions but not currently in cluster mode.
         */
        return null;
    }
    @TearDown
    public void CloseConnection() {
        try {
            jedis.client().close();
        }
        catch (Exception e) {
            e.printStackTrace();

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
        System.out.println("Starting Jedis benchmark...");

        Options options = new OptionsBuilder()
                .include(JedisBench.class.getSimpleName())
                .forks(0)
                .build();
        new Runner(options).run();
    }
}
