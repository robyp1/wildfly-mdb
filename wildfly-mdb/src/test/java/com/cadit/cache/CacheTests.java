package com.cadit.cache;

import com.cadit.mdb.EjbLocator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CacheTests {

    final static CacheManagerTest cacheManagerTest = new CacheManagerTest();

    @BeforeClass
    public static void setUp() {
    }


    @Test
    public void testHitInMemCache() throws InterruptedException {
        cacheManagerTest.setCache(new SoftCache<>());
        cacheManagerTest.put("k1", "v1");
        cacheManagerTest.put("k2", "v2");
        assertThat(cacheManagerTest.get("k1")).isEqualTo("v1");
        assertThat(cacheManagerTest.get("k2")).isEqualTo("v2");

    }

    @Test
    public void testMissInMemCache() throws InterruptedException {
        SoftCache<String, String> cacheEmpty = new SoftCache<>();

        cacheEmpty.put("k3", "v3");
        cacheEmpty.put("k4", "v4");
        cacheEmpty.get("k3");
        cacheEmpty.get("k4");

        ExpireTimeAccessCheckerTest taskSpy = spy(ExpireTimeAccessCheckerTest.class);
        when(taskSpy.getExpiredTime()).thenReturn(TimeUnit.SECONDS.toMillis(1L));//quando fa getExpiredTime all'interno del run() gli torna 0L
        taskSpy.setCache(cacheEmpty);
        CacheManagerBean mockCacheMaangerBean = mock(CacheManagerBean.class);
        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
        doNothing().when(mockCacheMaangerBean).removeFromDb(valueCapture.capture(), valueCapture.capture());
        //devo mocckare anche EjbLocator! sennò nel run ho un nullpointer exception
        MockedStatic<EjbLocator> ejbLocatorMockedStatic = mockStatic(EjbLocator.class);
        ejbLocatorMockedStatic.when(EjbLocator::locateCacheManagerBean).thenReturn(mockCacheMaangerBean);
        taskSpy.run();
        assertThat(cacheEmpty.get("k3")).isNotEqualTo("v3");
        assertThat(cacheEmpty.get("k4")).isNotEqualTo("v4");
    }


    @Test
    public void testConcurrentInMemCache() throws InterruptedException {
        Executor executor = Executors.newFixedThreadPool(3);
        SoftCache<String, String> cache = new SoftCache<>();
        CompletableFuture<Void> c1 = new CompletableFuture<>();
        //partono 3 processi paralleli asincroni, quando completano tutti si interpretano i risultati in console
        c1.runAsync(
                () -> {
                    cache.put("k1", "v1");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k1:" + cache.get("k1"));
                    cache.put("k2", "v1");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k2:" + cache.get("k2"));
                    cache.put("k3", "v1");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k3:" + cache.get("k3"));
                    c1.complete(null);
                }, executor
        );
        CompletableFuture<Void> c2 = new CompletableFuture<>();
        c2.runAsync(
                () -> {
                    cache.put("k3", "v2");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k3:" + cache.get("k3"));
                    cache.put("k2", "v2");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k2:" + cache.get("k2"));
                    c2.complete(null);
                }, executor
        );
        CompletableFuture<Void> c3 = new CompletableFuture<>();
        c3.runAsync(
                () -> {
                    cache.put("k2", "v3");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k2:" + cache.get("k2"));
                    cache.put("k3", "v3");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k3:" + cache.get("k3"));
                    cache.put("k4", "v4");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k4:" + cache.get("k4"));
                    cache.put("k5", "v5");
                    System.out.println(Thread.currentThread() + " -> " + System.nanoTime() + " k5:" + cache.get("k5"));
                    c3.complete(null);
                }, executor
        );
        CompletableFuture<Void> allCompleted = CompletableFuture.allOf(c1, c2, c3);
        while (!allCompleted.isDone()) ;
        System.out.println(System.nanoTime());
        assertThat(cache.get("k1")).isNotNull();
        assertThat(cache.get("k2")).isNotNull();
        assertThat(cache.get("k3")).isNotNull();
        assertThat(cache.get("k4")).isNotNull();
        assertThat(cache.get("k5")).isNotNull();
        System.out.println(cache.get("k1"));
        System.out.println(cache.get("k2"));
        System.out.println(cache.get("k3"));
        System.out.println(cache.get("k4"));
        System.out.println(cache.get("k5"));

    }

    @AfterClass
    public static void leaveResource() throws InterruptedException {
    }


}
