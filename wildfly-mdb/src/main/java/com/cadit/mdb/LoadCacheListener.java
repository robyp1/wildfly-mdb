package com.cadit.mdb;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class LoadCacheListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //invoked by servlet container only the first time wildfly start..
        //TODO: caricare da database la cache di questo server leggendo da database l'ultimo snapshots
        //caricare direttamente con CacheManager senza mandare messaggi jms
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    //invoked by servlet container only the first time wildfly end..
    }
}
