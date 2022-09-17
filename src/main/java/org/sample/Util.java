package org.sample;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Util {
    public static final String KeyPrefix = "Benchmark%s";
    private static final String BenchmarkKeysCreated = "BenchmarkKeysCreated";

    private static RedissonConnectionManager redisson;

    public static void createBenchmarkKeys() {

        redisson = RedissonConnectionManager.instance();
        String keysCreated = get(BenchmarkKeysCreated);
        if (keysCreated != null && keysCreated.equals("y")) {
            return;
        }

        String data = BenchmarkConfiguration.get().getKeyContentData();
        Integer amountOfKeys = BenchmarkConfiguration.get().getAmountOfKeys();
        for (int i = 0; i <= amountOfKeys; i++) {
            progressPercentage(i, amountOfKeys);
            String keyName = String.format(Util.KeyPrefix, i);
            set(keyName, data);
        }
        setWithExpiration(BenchmarkKeysCreated,"y", 28800);
        //redisson.client().shutdown();
    }

    private static String get(String key) {
        RBucket<String> bucket = redisson.client().getBucket(key, StringCodec.INSTANCE);
        return bucket.get();
    }

    private static void set(String key,String value) {
        RBucket<String> bucket = redisson.client().getBucket(key, StringCodec.INSTANCE);
        bucket.set(value);
    }
    private static void setWithExpiration(String key,String value, long timeout) {
        RBucket<String> bucket = redisson.client().getBucket(key, StringCodec.INSTANCE);
        bucket.set(value, timeout, TimeUnit.SECONDS);
    }

    private static void progressPercentage(int done, int total) {
        int size = 5;

        String iconLeftBoundary = "[";
        String iconDone = "=";
        String iconRemain = ".";
        String iconRightBoundary = "]";

        if (done > total) {
            throw new IllegalArgumentException();
        }

        int donePercents = (100 * done) / total;
        int doneLength = size * donePercents / 100;

        StringBuilder bar = new StringBuilder(iconLeftBoundary);
        for (int i = 0; i < size; i++) {
            if (i < doneLength) {
                bar.append(iconDone);
            } else {
                bar.append(iconRemain);
            }
        }

        bar.append(iconRightBoundary);
        System.out.print("\r" + String.format("Creating %s of %s key(s) to the benchmark: ", done, total) + " " + bar + " " + donePercents + "%");

        if (done == total) {
            System.out.print("\n");
        }
    }

}
