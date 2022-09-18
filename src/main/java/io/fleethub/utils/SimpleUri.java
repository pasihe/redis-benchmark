package io.fleethub.utils;

public class SimpleUri {

    private String host;
    private int port;

    public static SimpleUri create(String host, int port) {
        SimpleUri uri = new SimpleUri();
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
