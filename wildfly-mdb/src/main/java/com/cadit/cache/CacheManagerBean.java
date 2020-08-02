package com.cadit.cache;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Singleton
@Startup
public class CacheManagerBean {


    /**
     * NB: su was 8.5 con JEE6 va sostituito con un @Timer schedulato, vedere branch  origin/jee6_compatibility
     */
    @Resource// usa il default di WildFly: lookup="java:jboss/ee/concurrency/scheduler/default"
    private ManagedScheduledExecutorService executorService;

    private ScheduledFuture<?> taskFuture;

    @PersistenceContext(name = "cachePU")
    private EntityManager em;//JTA


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

     public <K,V> void removeFromDb(K key, V val) {
        int executed = em.createQuery("delete CacheEntity c where c.key = :key and c.value = :value")
                .setParameter("key", key)
                .setParameter("value", val)
                .executeUpdate();
        em.flush();
        em.clear();

    }


    @PreDestroy
    public void dispose() {
        if (taskFuture != null) {
            taskFuture.cancel(true);
        }
    }

}
