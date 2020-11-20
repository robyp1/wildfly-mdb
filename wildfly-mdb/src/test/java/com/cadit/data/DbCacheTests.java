package com.cadit.data;


import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DbCacheTests {

    private EntityManager entityManager;
    private EntityTransaction entityTransaction;

    @Before
    public void setUp() {
        entityManager = Persistence.createEntityManagerFactory("cacheTestPU").createEntityManager();
        entityTransaction = entityManager.getTransaction();
    }

    @Test
    public void testPersistCacheData() {
        entityTransaction.begin();
        CacheEntity cacheEntity1 = new CacheEntity("k1", "v1");
        CacheEntity cacheEntity2 = new CacheEntity("k2", "v2");
        cacheEntity1 = entityManager.merge(cacheEntity1);
        cacheEntity2 = entityManager.merge(cacheEntity2);
        entityTransaction.commit();

        List<CacheEntity> cacheEntries = entityManager.createQuery("select c from CacheEntity c", CacheEntity.class)
                .getResultList();
        assertThat(cacheEntries.size()).isGreaterThan(0);
        CacheEntity cacheEntry1 = cacheEntries.get(0);
        CacheEntity cacheEntry2 = cacheEntries.get(1);
        assertThat(cacheEntry1).isEqualTo(cacheEntity1);
        assertThat(cacheEntry2).isEqualTo(cacheEntity2);
    }


}
