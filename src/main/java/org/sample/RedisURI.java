package org.sample;

public class RedisURI {

    private String host;
    private int port;

    public static RedisURI create(String host, int port) {
        RedisURI uri = new RedisURI();
        uri.setHost(host);
        uri.setPort(port);
        return uri;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
