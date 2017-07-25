/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;

/**
 * JMS connection pool manager.
 */
public class JMSPublisherPoolManager {

    /**
     * Will keep the publisher pools
     */
    private static final Map<String, JMSPublisherPool> publisherPoolManager = new HashMap<>();

    /**
     * Will get the publisher pool from the pool manager.
     *
     * @param publisherKey The publisher key.
     * @return the publisher pool.
     */
    public static JMSPublisherPool getJMSPublisherPool(String publisherKey) {
        return publisherPoolManager.get(publisherKey);
    }

    /**
     * Will add the new pool to the pool manager.
     *
     * @param publisherKey     The publisher key.
     * @param JMSPublisherPool The publisher pool.
     */
    public static void addJMSPublisherPool(String publisherKey, JMSPublisherPool JMSPublisherPool) {
        synchronized (JMSPublisherPoolManager.class) {
            publisherPoolManager.putIfAbsent(publisherKey, JMSPublisherPool);
        }
    }

    /**
     * In case cache expiry does not happen, the GC collection should trigger the shutdown of the context.
     */
    @Override
    protected void finalize() throws Throwable {
        publisherPoolManager.clear();
        super.finalize();
    }
}
