package com.cadit.cache;

import com.cadit.mdb.EjbLocator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

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

    @AfterClass
    public static void leaveResource() throws InterruptedException {
    }


}
