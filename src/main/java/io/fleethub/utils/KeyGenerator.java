package io.fleethub.utils;

import io.fleethub.clients.RedissonConnectionManager;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.concurrent.TimeUnit;

public class KeyGenerator {
    public static final String KeyPrefix = "Benchmark%s";
    private static final String BenchmarkKeysCreated = "BenchmarkKeysCreated";

    public static void createBenchmarkKeys() {

        try {
            if(RedissonConnectionManager.instance().client() instanceof RedissonClient==false
                    || RedissonConnectionManager.instance().client().isShutdown()) {
                RedissonConnectionManager.instance().reconnect();
            }
            RBucket<String> bucket = RedissonConnectionManager.instance().client().getBucket(
                    BenchmarkKeysCreated, StringCodec.INSTANCE);
            String keysCreated = bucket.get();
            if (keysCreated != null && keysCreated.equals("y")) {
                return;
            }

            String data = BenchmarkConfiguration.get().getKeyContentData();
            Integer amountOfKeys = BenchmarkConfiguration.get().getAmountOfKeys();
            for (int i = 0; i <= amountOfKeys; i++) {
                progressPercentage(i, amountOfKeys);
                String keyName = String.format(KeyGenerator.KeyPrefix, i);
                RBucket<String> keyBucket = RedissonConnectionManager.instance().client().getBucket(keyName, StringCodec.INSTANCE);
                keyBucket.set(data);
            }
            RBucket<String> expirationBucket = RedissonConnectionManager.instance().client().getBucket(BenchmarkKeysCreated, StringCodec.INSTANCE);
            expirationBucket.set("y", 28800, TimeUnit.SECONDS);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {

                RedissonConnectionManager.instance().client().shutdown();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

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