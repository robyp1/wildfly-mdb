package com.cadit.cache;

import com.cadit.domain.ExpireTimeAccess;

public class CacheManagerTest {

    private SoftCache<String, String> cache;
    private ExpireTimeAccess expireTimeAccess;

    public ExpireTimeAccess getExpireTimeAccess() {
        return expireTimeAccess;
    }

    public void setExpireTimeAccess(ExpireTimeAccess expireTimeAccess) {
        this.expireTimeAccess = expireTimeAccess;
    }

    public String get(String key) {
        return cache.get(key); //threadsafe, no lock necessary
    }

    public void put(String key, String value) {
        cache.put(key, value);//threadsafe, no lock necessary
    }

    public SoftCache<String, String> getCache() {
        return cache;
    }

    public void setCache(SoftCache<String, String> cache) {
        this.cache = cache;
    }
}
