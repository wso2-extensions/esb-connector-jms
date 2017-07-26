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

package org.wso2.carbon.connector.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Manage a pool of connections for a single destinationType+destination combination to avoid sharing a single
 * connection during parallel invocations of the mediator through the proxy.
 */
public class JMSPublisherPool {

    private static final Log log = LogFactory.getLog(JMSPublisherPool.class);

    private final int maxSize;
    private String destination;
    private String destinationType;
    private String connectionFactoryName;
    private String javaNamingProviderUrl;
    private String javaNamingFactoryInitial;
    private String username;
    private String password;
    private String priority;
    private String persistent;
    private String timeToLive;
    private Queue<JMSPublisher> publisherQueue;

    /**
     * @param destination              The name of the queue/topic.
     * @param destinationType          The message type queue/topic.
     * @param connectionFactoryName    The name of the connection factory.
     * @param maxPoolSize              The maximum connection size of pool.
     * @param javaNamingProviderUrl    URL of the JNDI provider.
     * @param javaNamingFactoryInitial JNDI initial context factory class.
     * @param priority                 The message priority for this message producer; must be a value between 0 and 9
     * @param deliveryMood             The message delivery mode for this message producer
     * @param timeToLive               The message time to live in milliseconds
     */
    public JMSPublisherPool(String destination, String destinationType, String connectionFactoryName, int maxPoolSize,
                            String javaNamingProviderUrl, String javaNamingFactoryInitial, String username,
                            String password, String priority, String deliveryMood, String timeToLive) {
        this.destination = destination;
        this.destinationType = destinationType;
        this.connectionFactoryName = connectionFactoryName;
        this.maxSize = maxPoolSize;
        this.javaNamingProviderUrl = javaNamingProviderUrl;
        this.javaNamingFactoryInitial = javaNamingFactoryInitial;
        this.publisherQueue = new ConcurrentLinkedQueue<JMSPublisher>();
        this.username = username;
        this.password = password;
        this.persistent = deliveryMood;
        this.priority = priority;
        this.timeToLive = timeToLive;
    }

    /**
     * Will get the single publisher from the pool.
     *
     * @return The publisher to publish the message
     * @throws JMSException    The JMXException
     * @throws NamingException The NamingException
     */
    public JMSPublisher getPublisher() throws JMSException, NamingException {
        JMSPublisher publisher = publisherQueue.poll();
        if (publisher != null) {
            if (log.isDebugEnabled()) {
                log.debug("Returning an existing free publisher");
            }
            return publisher;
        } else {
            publisher = new JMSPublisher(destination, connectionFactoryName, destinationType,
                    javaNamingProviderUrl, javaNamingFactoryInitial, username, password, priority, persistent, timeToLive);
            if (log.isDebugEnabled()) {
                log.debug("Created and returning a new publisher for destination");
            }
            return publisher;
        }
    }

    /**
     * Will release the publisher after the message published.
     *
     * @param publisher The publisher to be expired
     */
    public void releasePublisher(JMSPublisher publisher) {
        if (publisherQueue.size() < maxSize) {
            publisherQueue.add(publisher);
            if (log.isDebugEnabled()) {
                log.debug("Added publisher back to free pool.");
            }
        } else {
            publisherQueue.poll().close();
            publisherQueue.add(publisher);
            if (log.isDebugEnabled()) {
                log.debug("Destroying publisher because we have reached maximum size of publisher pool.");
            }
        }
    }

    /**
     * Will clear all publishers from publisherPool.
     *
     * @throws JMSException The JMXException
     */
    public void close() throws JMSException {
        if (log.isDebugEnabled()) {
            log.debug("Destroying publisher pool");
        }
        for (JMSPublisher freePublisher : publisherQueue) {
            freePublisher.close();
        }
        publisherQueue.clear();
    }

    /**
     * In case cache expiry does not happen, the GC collection should trigger the shutdown of the context.
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}