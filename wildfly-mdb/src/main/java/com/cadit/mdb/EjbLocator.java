package com.cadit.mdb;

import com.cadit.cache.CacheManagerBean;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class EjbLocator {

    public static CacheManagerBean locateCacheManagerBean() {
        CacheManagerBean cacheManagerBean  =null;
        try {
            cacheManagerBean = (CacheManagerBean) new InitialContext().lookup("java:module/CacheManagerBean!com.cadit.cache.CacheManagerBean");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        System.out.println(cacheManagerBean);
        return cacheManagerBean;
    }

}
