# redis-cluster-benchmark
Benchmark test for Java Redis Clients: Jedis, Lettuce, Redisson.
uses JMH (Java Microbenchmark Harness) 1.3.5.
Tested with Ubuntu 22, Windows 10, JDK 17.x 
https://github.com/openjdk/jmh

#### Benchmarks included
- Jedis (v4.3.0-m1)
  - JedisBench.jedisGet             
  - JedisBench.jedisSet
  - _more to come when batch and pipeline for cluster is supported by Jedis_
- Lettuce (v6.2.0)
  - LettuceBench.lettuceAsyncGet        
  - LettuceBench.lettuceAsyncSet        
  - LettuceBench.lettuceBatchGet        
  - LettuceBench.lettuceBatchSet        
  - LettuceBench.lettucePipelineGet    
  - LettuceBench.lettucePipelineSet     
  - LettuceBench.lettuceReactiveGet
  - LettuceBench.lettuceReactiveSet
- Redisson (3.17.6)
  - RedissonBench.redissonGet
  - RedissonBench.redissonSet
  - RedissonBench.redissonAsyncGet 
  - RedissonBench.redissonAsyncSet 
  - RedissonBench.redissonBatchGet  
  - RedissonBench.redissonBatchSet
  - RedissonBench.redissonReactiveGet  
  - RedissonBench.redissonReactiveSet 
    
### Install
```
$ git clone https://github.com/pasihe/redis-benchmark.git

$ mvn clean install
```

### Run benchmarks

_Before run set configurations to config.conf file_

Parameter options:

`-wi 5` *Warm-up*
* 5 warm-up cycles (Without measurement, providing the opportunity to the JVM to optimize the code before the measurement starts).

`-i 20` *Measurements iterations*
* 20 real measurement iterations for every test.

`-t 10` *Threads*

* Amount of threads to run benchmark. Set max to use all available threads.

`-f 10` *Forks*
* Separate execution environments.

---

#### Run examples
Jedis
```
$ java -jar target/benchmarks.jar RedissonBenchmark -f 1 -wi 1 -i 10 -t 8
     *    (we requested eight forks; there are also other options, see -h)
```

Lettuce
```
$ java -jar target/benchmarks.jar RedissonBenchmark -f 1 -wi 1 -i 10 -t 8
     *    (we requested eight forks; there are also other options, see -h)
```

Redisson
```
$ java -jar target/benchmarks.jar RedissonBenchmark -f 1 -wi 1 -i 10 -t 8
     *    (we requested eight forks; there are also other options, see -h)
```

All using max threads and outputting results to csv
```
$ java -jar target/benchmarks.jar JedisBench RedissonBench LettuceBench -f 1 -wi 1 -i 1 -t max -w 1 -si true -rf csv -rff all-benchmark.csv
```

### benchmark results

upcoming

---

## `jmh` command line options

```bash
$ java -jar target/benchmarks.jar -h

Usage: java -jar ... [regexp*] [options]
 [opt] means optional argument.
 <opt> means required argument.
 "+" means comma-separated list of values.
 "time" arguments accept time suffixes, like "100ms".

  [arguments]                 Benchmarks to run (regexp+).

  -bm <mode>                  Benchmark mode. Available modes are: [Throughput/thrpt,
                              AverageTime/avgt, SampleTime/sample, SingleShotTime/ss,
                              All/all]

  -bs <int>                   Batch size: number of benchmark method calls per
                              operation. Some benchmark modes may ignore this
                              setting, please check this separately.

  -e <regexp+>                Benchmarks to exclude from the run.

  -f <int>                    How many times to fork a single benchmark. Use 0 to
                              disable forking altogether. Warning: disabling
                              forking may have detrimental impact on benchmark
                              and infrastructure reliability, you might want
                              to use different warmup mode instead.

  -foe <bool>                 Should JMH fail immediately if any benchmark had
                              experienced an unrecoverable error? This helps
                              to make quick sanity tests for benchmark suites,
                              as well as make the automated runs with checking error
                              codes.

  -gc <bool>                  Should JMH force GC between iterations? Forcing
                              the GC may help to lower the noise in GC-heavy benchmarks,
                              at the expense of jeopardizing GC ergonomics decisions.
                              Use with care.

  -h                          Display help.

  -i <int>                    Number of measurement iterations to do. Measurement
                              iterations are counted towards the benchmark score.

  -jvm <string>               Use given JVM for runs. This option only affects forked
                              runs.

  -jvmArgs <string>           Use given JVM arguments. Most options are inherited
                              from the host VM options, but in some cases you want
                              to pass the options only to a forked VM. Either single
                              space-separated option line, or multiple options
                              are accepted. This option only affects forked runs.

  -jvmArgsAppend <string>     Same as jvmArgs, but append these options before
                              the already given JVM args.

  -jvmArgsPrepend <string>    Same as jvmArgs, but prepend these options before
                              the already given JVM arg.

  -l                          List the benchmarks that match a filter, and exit.

  -lp                         List the benchmarks that match a filter, along with
                              parameters, and exit.

  -lprof                      List profilers.

  -lrf                        List machine-readable result formats.

  -o <filename>               Redirect human-readable output to a given file.

  -opi <int>                  Override operations per invocation, see @OperationsPerInvocation
                              Javadoc for details.

  -p <param={v,}*>            Benchmark parameters. This option is expected to
                              be used once per parameter. Parameter name and parameter
                              values should be separated with equals sign. Parameter
                              values should be separated with commas.

  -prof <profiler>            Use profilers to collect additional benchmark data.
                              Some profilers are not available on all JVMs and/or
                              all OSes. Please see the list of available profilers
                              with -lprof.

  -r <time>                   Minimum time to spend at each measurement iteration.
                              Benchmarks may generally run longer than iteration
                              duration.

  -rf <type>                  Format type for machine-readable results. These
                              results are written to a separate file (see -rff).
                              See the list of available result formats with -lrf.

  -rff <filename>             Write machine-readable results to a given file.
                              The file format is controlled by -rf option. Please
                              see the list of result formats for available formats.

  -si <bool>                  Should JMH synchronize iterations? This would significantly
                              lower the noise in multithreaded tests, by making
                              sure the measured part happens only when all workers
                              are running.

  -t <int>                    Number of worker threads to run with. 'max' means
                              the maximum number of hardware threads available
                              on the machine, figured out by JMH itself.

  -tg <int+>                  Override thread group distribution for asymmetric
                              benchmarks. This option expects a comma-separated
                              list of thread counts within the group. See @Group/@GroupThreads
                              Javadoc for more information.

  -to <time>                  Timeout for benchmark iteration. After reaching
                              this timeout, JMH will try to interrupt the running
                              tasks. Non-cooperating benchmarks may ignore this
                              timeout.

  -tu <TU>                    Override time unit in benchmark results. Available
                              time units are: [m, s, ms, us, ns].

  -v <mode>                   Verbosity mode. Available modes are: [SILENT, NORMAL,
                              EXTRA]

  -w <time>                   Minimum time to spend at each warmup iteration. Benchmarks
                              may generally run longer than iteration duration.

  -wbs <int>                  Warmup batch size: number of benchmark method calls
                              per operation. Some benchmark modes may ignore this
                              setting.

  -wf <int>                   How many warmup forks to make for a single benchmark.
                              All iterations within the warmup fork are not counted
                              towards the benchmark score. Use 0 to disable warmup
                              forks.

  -wi <int>                   Number of warmup iterations to do. Warmup iterations
                              are not counted towards the benchmark score.

  -wm <mode>                  Warmup mode for warming up selected benchmarks.
                              Warmup modes are: INDI = Warmup each benchmark individually,
                              then measure it. BULK = Warmup all benchmarks first,
                              then do all the measurements. BULK_INDI = Warmup
                              all benchmarks first, then re-warmup each benchmark
                              individually, then measure it.

  -wmb <regexp+>              Warmup benchmarks to include in the run in addition
                              to already selected by the primary filters. Harness
                              will not measure these benchmarks, but only use them
                              for the warmup.
```

# references
1. [jmh official site](http://openjdk.java.net/projects/code-tools/jmh/)
2. [jmh sample benchmarks](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/)
3. [Introduction to JMH by Mikhail Vorontsov (java-performance.info)](http://java-performance.info/jmh/)
4. [Avoiding Benchmarking Pitfalls on the JVM](https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html)
5. [Optimize Redis Client Performance for Amazon ElastiCache](https://aws.amazon.com/blogs/database/optimize-redis-client-performance-for-amazon-elasticache/)
6. [Choosing the correct instance type and size for ElastiCache](https://aws.amazon.com/premiumsupport/knowledge-center/elasticache-redis-cluster-instance-type/)
7. [JMH benchmark, how can I use it to test mongodb's data load performance](https://www.mo4tech.com/jmh-benchmark-how-can-i-use-it-to-test-mongodbs-data-load-performance.html)