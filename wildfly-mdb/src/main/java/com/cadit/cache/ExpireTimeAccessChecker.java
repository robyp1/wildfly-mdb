package com.cadit.cache;

import com.cadit.domain.ExpireTimeAccess;
import com.cadit.mdb.EjbLocator;

import java.util.concurrent.TimeUnit;

public class ExpireTimeAccessChecker implements ExpireTimeAccess,Runnable {

    private final Long DEFAULT_EXPIRE_TIME = 3L;
    private SoftCache cache;
    private Long expiredTime = DEFAULT_EXPIRE_TIME;
    private CacheManagerBean cacheManagerBean;


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

    public CacheManagerBean getCacheManagerBean() {
        return cacheManagerBean;
    }

    public void setCacheManagerBean(CacheManagerBean cacheManagerBean) {
        this.cacheManagerBean = cacheManagerBean;
    }

    @Override
    public void run() {
        if (getCacheManagerBean() == null){
            cacheManagerBean = new EjbLocator<>(CacheManagerBean.class).getEjbReference();
        }
        cache.pollExpiredDataCache(this, cacheManagerBean);
    }


}