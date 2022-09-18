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