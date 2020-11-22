package com.cadit.mdb;

import com.cadit.cache.CacheManagerBean;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class EjbLocator<T> {


    private final Class<T> classTname;

    public EjbLocator(Class<T> classTname) {
        this.classTname = classTname;
    }

    /**
     * questo modo è legato alla stringa di lookup che varia da app serer ad app server, in tal caso per wildfly 10 ok
     * vedere quindi il {@link com.cadit.mdb.EjbLocator#getBeanManager}
     * ma usare direttamente {@link com.cadit.mdb.EjbLocator#getEjbReference}
     * @return
     */
    @Deprecated
    public static CacheManagerBean locateCacheManagerBean() {
        CacheManagerBean cacheManagerBean  =null;
        try {
            cacheManagerBean = (CacheManagerBean) new InitialContext().lookup("java:module/CacheManagerBean!com.cadit.cache.CacheManagerBean");
        } catch (NamingException e) {
            throw new IllegalStateException("cannot perform JNDI lookup of CacheManagerBean",e);
        }
        System.out.println(cacheManagerBean);
        return cacheManagerBean;
    }

    public T getEjbReference(){
        BeanManager beanManager = getBeanManager();
        Bean<T> beanT = (Bean<T>) beanManager.getBeans(classTname).iterator().next();
        CreationalContext<T> cCtx = beanManager.createCreationalContext(beanT);
        return (T) beanManager.getReference(beanT,classTname,cCtx);
    }


    private static BeanManager getBeanManager(){
        try {
            return (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            throw new IllegalStateException("cannot perform JNDI lookup of BeanManager",e);
        }
    }

}
