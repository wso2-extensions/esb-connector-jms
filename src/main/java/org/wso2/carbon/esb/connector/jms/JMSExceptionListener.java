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
package org.wso2.carbon.esb.connector.jms;

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
     * @param publisherCacheKey The key of the publisher in cache.
     */
    public JMSExceptionListener(String publisherCacheKey) {
        this.publisherCacheKey = publisherCacheKey;
    }

    /**
     * @param e Exception
     */
    @Override
    public void onException(JMSException e) {
        synchronized (publisherCacheKey.intern()) {
            log.error("Cache will be cleared due to JMSException for destination : " + publisherCacheKey, e);
            JMSPublisherPoolManager jmsPublisherPoolManager = new JMSPublisherPoolManager();
            try {
                JMSPublisherPoolManager.getJMSPublisherPool(publisherCacheKey).close();
            } catch (JMSException e1) {
                log.error("Error while close the connections");
            }
        }
    }
}