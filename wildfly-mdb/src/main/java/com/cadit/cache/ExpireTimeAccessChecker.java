package com.cadit.cache;

import com.cadit.domain.ExpireTimeAccess;

import java.util.concurrent.TimeUnit;

public class ExpireTimeAccessChecker implements ExpireTimeAccess,Runnable {

    private final Long DEFAULT_EXPIRE_TIME = 3L;
    private SoftCache cache;
    private Long expiredTime = DEFAULT_EXPIRE_TIME;


    public ExpireTimeAccessChecker(SoftCache cache) {
        this.cache = cache;
    }

    public SoftCache getCache() {
        return cache;
    }

    public void setCache(SoftCache cache) {
        this.cache = cache;
    }

    public Long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Long expiredTimeSec) {
        if (expiredTimeSec != null && expiredTimeSec > 3L) {
            this.expiredTime = TimeUnit.SECONDS.toMillis(expiredTimeSec);
        }
    }

    @Override
    public void run() {
        cache.pollExpiredDataCache(this);
    }


}