package com.cadit.cache;

import javax.ejb.Lock;
import javax.ejb.LockType;

public class CacheManager {

    private static final CacheManager instance = new CacheManager();
    private final SoftCache<String, String> cache;

    private CacheManager() {
        cache = new SoftCache<>();
    }


    static CacheManager getInstance() {
        return instance;
    }


    public SoftCache.ExpireTimeAccessChecker expireTaskChecker() {
        return cache.new ExpireTimeAccessChecker(5L);
    }


    @Lock(LockType.READ)
    public String get(String key) {
        return cache.get(key); //threadsafe, no writelock necessary
    }

    @Lock(LockType.READ)
    public void put(String key, String value) {
        cache.put(key, value);//threadsafe, no writelock necessary
    }
}
