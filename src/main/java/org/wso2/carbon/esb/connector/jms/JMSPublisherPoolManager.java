/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.connector.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Connection pool for JMS producer connection.
 */
public class JMSPublisherPoolManager {
    private static Log log = LogFactory.getLog(JMSPublisherPoolManager.class);

    private Map<String, PublisherPool> publisherPoolManager;

    private static JMSPublisherPoolManager jmsPublisherPoolManager = null;

    JMSPublisherPoolManager() {
        publisherPoolManager = new HashMap<>();
    }

    /**
     * Get single instance of ConnectionPool.
     *
     * @return the connection pool manger
     */
    public static JMSPublisherPoolManager getInstance() {
        if (jmsPublisherPoolManager == null) {
            synchronized (JMSPublisherPoolManager.class) {
                if (jmsPublisherPoolManager == null) {
                    jmsPublisherPoolManager = new JMSPublisherPoolManager();
                }
            }
        }
        return jmsPublisherPoolManager;
    }

    /**
     * @param publisherKey
     * @return
     */
    public PublisherPool getPoolFromMap(String publisherKey) {
        return publisherPoolManager.get(publisherKey);
    }

    /**
     * @param publisherKey
     * @param publisherPool
     */
    public void addPoolToMap(String publisherKey, PublisherPool publisherPool) {
        publisherPoolManager.put(publisherKey, publisherPool);
    }
}
