package com.cadit.domain;

import com.cadit.domain.ExpireTimeAccess;

public interface CachePoller {

    /**
     * esegue il pooling della cache ogni tot tempo per invalidare le entry
     * utilizzate almeno una volta e ora non più utilizzante per un tempo
     * indicato dall ExpireTimeAccessChecker
     *
     * @param expiredTimeChecker
     */
    void pollExpiredDataCache(ExpireTimeAccess expiredTimeChecker);
}
