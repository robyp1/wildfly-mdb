package com.cadit.cache;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Singleton
@Startup
public class CacheManagerBean {


    @Resource// usa il default di WildFly: lookup="java:jboss/ee/concurrency/scheduler/default"
    private ManagedScheduledExecutorService executorService;
    private ScheduledFuture<?> taskFuture;


    @PostConstruct
    public void runExpiredCheckerThread() {
        CacheManager cacheManager = CacheManager.getInstance();
        SoftCache.ExpireTimeAccessChecker task = cacheManager.expireTaskChecker();
        //schedule a task periodically each 8 secs
        taskFuture = executorService.scheduleAtFixedRate(task, 0, 8, TimeUnit.SECONDS);
    }

    public String get(String key) {
        return CacheManager.getInstance().get(key);
    }

    public void set(String key, String value) {
        CacheManager.getInstance().put(key, value);
    }


    @PreDestroy
    public void dispose() {
        if (taskFuture != null) {
            taskFuture.cancel(true);
        }
    }

}
