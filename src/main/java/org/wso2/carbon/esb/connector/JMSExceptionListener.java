/*
 *
 *  * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.esb.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

/**
 * Custom exception handler to react to JMS exceptions.
 */
public class JMSExceptionListener implements ExceptionListener {

    private static final Log log = LogFactory.getLog(JMSExceptionListener.class);

    private String publisherCacheKey;

    /**
     * @param publisherCacheKey
     */
    public JMSExceptionListener(String publisherCacheKey) {
        this.publisherCacheKey = publisherCacheKey;
    }

    /**
     * @param e
     */
    @Override
    public void onException(JMSException e) {
        synchronized (publisherCacheKey.intern()) {
            log.error("Cache will be cleared due to JMSException for destination : " + publisherCacheKey, e);
            PublisherPool publisherPool = PublisherCache.getJMSPublisherPoolCache().getAndRemove(publisherCacheKey);
            try {
                publisherPool.close();
            } catch (JMSException e1) {
                log.error("Error while trying to remove obsolete publisher pool for : " + publisherCacheKey, e1);
            }
            log.error("Cache has been cleared to a JMSException for destination : " + publisherCacheKey, e);
        }
    }
}