## redis-benchmark
Tested with JDK 17.x

#### Install and run:
```
$ git clone https://github.com/pasihe/redis-benchmark.git

$ mvn clean install

$ java -jar target/benchmarks.jar RedisBenchmark -f 1 -wi 1 -i 10 -t 8
     *    (we requested eight forks; there are also other options, see -h)
```

Before run set configurations to config.conf file
