package com.theparida.core;

import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public final class Config {
    private static final Properties VALUES = new Properties();
    static {
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in == null) throw new IllegalStateException("Missing config.properties");
            VALUES.load(in);
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    private Config() { }
    public static String get(String key) { return System.getProperty(key, VALUES.getProperty(key, "")); }
    public static boolean flag(String key) { return Boolean.parseBoolean(get(key)); }
    public static String baseUrl() {
        String url = get("baseUrl");
        URI uri = URI.create(url);
        if (!java.util.Set.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null
                || uri.getQuery() != null || uri.getFragment() != null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("baseUrl must be an HTTP(S) website root, without query or fragment");
        }
        return url.endsWith("/") ? url : url + "/";
    }
    public static int timeout() { return Integer.parseInt(get("timeoutSeconds")); }
}
