package com.cadit.mdb;

import com.cadit.cache.CacheManagerBean;
import com.cadit.data.CacheEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.List;

@WebListener
public class LoadCacheListener implements ServletContextListener {

    @PersistenceContext(name = "cachePU")
    private EntityManager entityManager;//JTA

    private final Logger log = LoggerFactory.getLogger(ReadMessageMDB.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //invoked by servlet container only the first time wildfly start..
        // carica da database la cache di questo server leggendo da database l'ultimo snapshots
        //direttamente con CacheManagerBean senza mandare messaggi jms
        CacheManagerBean cacheManager = EjbLocator.locateCacheManagerBean();
        List<CacheEntity> cacheEntries = entityManager.createQuery("select c from CacheEntity c", CacheEntity.class)
                .getResultList();
        for (CacheEntity cacheEntry : cacheEntries) {//load cache with data persisted
            cacheManager.set(cacheEntry.getKey(), cacheEntry.getValue());
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //invoked by servlet container only the first time wildfly end..


    }
}
