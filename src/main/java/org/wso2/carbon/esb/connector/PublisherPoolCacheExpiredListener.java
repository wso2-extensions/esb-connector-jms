/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.esb.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.jms.JMSException;

/**
 * Event handler to properly clean up the JMS Publisher context during a cache entry removal / expiration.
 *
 * @param <K> Key of cache
 * @param <V> Value of cache
 */
public class PublisherPoolCacheExpiredListener<K, V> implements CacheEntryExpiredListener<K, V> {

    private static final Log log = LogFactory.getLog(PublisherPoolCacheExpiredListener.class);

    /**
     * @param expiredPublisherPool The expired entry of publisher pool.
     * @throws CacheEntryListenerException exception while expire the event
     */
    @Override
    public void entryExpired(CacheEntryEvent<? extends K, ? extends V> expiredPublisherPool)
            throws CacheEntryListenerException {
        if (expiredPublisherPool.getValue() instanceof PublisherPool) {
            try {
                log.info("Clearing PublisherPool for key : " + expiredPublisherPool.getKey());
                ((PublisherPool) expiredPublisherPool.getValue()).close();
            } catch (JMSException e) {
                log.error("Error while clearing PublisherPool for key" + expiredPublisherPool.getKey(), e);
            }
        } else {
            log.warn("Expired entry is not a PublisherPool for key : " + expiredPublisherPool.getKey());
        }
    }
}