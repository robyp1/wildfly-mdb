package com.cadit.cache;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Singleton
@Startup
public class CacheManagerBean {


    @Resource
    private TimerService timerService;

    @PersistenceContext(name = "cachePU")
    private EntityManager em;//JTA


    @PostConstruct
    public void init(){
        //schedule a task periodically each 8 secs
        ScheduleExpression schedulerExpression = new ScheduleExpression()
                .hour("*")
                .minute("*")
                .second("*/8"); // ogni 8 secondi
        timerService.createCalendarTimer(schedulerExpression, new TimerConfig("CacheManagerTaskTimer",false));
    }

    @Timeout
    @Lock(value = LockType.WRITE)
    public void runExpiredCheckerThread() {
        CacheManager cacheManager = CacheManager.getInstance();
        SoftCache.ExpireTimeAccessChecker task = cacheManager.expireTaskChecker();
        task.run();

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


}
