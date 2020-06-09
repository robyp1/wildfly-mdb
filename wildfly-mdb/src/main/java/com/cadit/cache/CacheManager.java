package com.cadit.cache;

/**
 * TODO: implementare anche la rimozione dei valori nella get a tempo
 */
public class CacheManager {

    private static final CacheManager instance = new CacheManager();
    private final SoftCache<String,String> cache;

    private CacheManager() {
        cache = new SoftCache<>();
    }

    public final static CacheManager getInstance(){
        return instance;
    }

    public String get(String key){
        return cache.get(key);
    }

    public void set(String key, String value){
        cache.put(key,value);
    }
}
