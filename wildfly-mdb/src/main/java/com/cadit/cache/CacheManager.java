package com.cadit.cache;

public class CacheManager {

    private static final CacheManager instance = new CacheManager();
    private final SoftCache<String, String> cache;

    private CacheManager() {
        cache = new SoftCache<>();
    }


    static CacheManager getInstance() {
        return instance;
    }


    public ExpireTimeAccessChecker expireTaskChecker() {
        return new ExpireTimeAccessChecker(cache);
    }


    public String get(String key) {
        return cache.get(key); //threadsafe, no lock necessary
    }

    public void put(String key, String value) {
        cache.put(key, value);//threadsafe, no lock necessary
    }
}
