package com.cadit.cache;

import com.cadit.domain.ExpireTimeAccess;

/**
 * creata perchè mock non istanzia il costruttore con parametri, nemmeno classi sinleton
 */
public class ExpireTimeAccessCheckerTest implements Runnable, ExpireTimeAccess {

    private SoftCache<String, String> cache ;
    private Long expiredTime;

    public ExpireTimeAccessCheckerTest(){
    }

    public SoftCache<String, String> getCache() {
        return cache;
    }

    public void setCache(SoftCache<String, String> cache) {
        this.cache = cache;
    }

    public Long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Long expiredTime) {
        this.expiredTime = expiredTime;
    }

    @Override
    public void run() {
        cache.pollExpiredDataCache(this);
    }
}
