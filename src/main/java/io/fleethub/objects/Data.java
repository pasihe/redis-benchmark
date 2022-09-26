package io.fleethub.objects;

import java.util.concurrent.TimeUnit;

public class Data {

    private  Long Id;
    private Long ttl;
    private TimeUnit timeUnit;

    public Data(Long id, Long ttl, TimeUnit timeUnit) {
        Id = id;
        this.ttl = ttl;
        this.timeUnit = timeUnit;
    }

    public Long getTTL() {
        return ttl;
    }

    public void setTTL(Long ttl) {
        this.ttl = ttl;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
