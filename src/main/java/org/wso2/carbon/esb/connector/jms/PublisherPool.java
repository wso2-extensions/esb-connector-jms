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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Manage a pool of connections for a single destinationType+destination combination to avoid sharing a single
 * connection during parallel invocations of the mediator through the proxy.
 */
public class PublisherPool {

    private static final Log log = LogFactory.getLog(PublisherPool.class);

    /**
     * Maximum number of connections allowed in a single pool meant for a single destination.
     */
    private final int maxSize;

    private String destination;
    private String destinationType;
    private String connectionFactoryName;
    private String connectionFactoryValue;
    private String namingFactory;
    /**
     * Will maintain already created and available connections upto the max limit
     */
    private List<PublisherContext> freePublishers = new ArrayList<PublisherContext>();

    /**
     * Will maintain connections currently in use and upto the max limit.
     */
    private List<PublisherContext> busyPublishers = new ArrayList<PublisherContext>();

    /**
     * Lock to ensure that freePublishers and busyPublishers collections are updated consistently.
     */
    private Lock lock = new ReentrantLock();

    /**
     * @param destination            The name of the queue/topic.
     * @param destinationType        The message type queue/topic.
     * @param connectionFactoryName  The name of the connection factory.
     * @param maxPoolSize            The maximum connection size of pool.
     * @param connectionFactoryValue URL of the JNDI provider.
     * @param namingFactory          JNDI initial context factory class.
     */
    public PublisherPool(String destination, String destinationType, String connectionFactoryName, int maxPoolSize,
                         String connectionFactoryValue, String namingFactory) {
        this.destination = destination;
        this.destinationType = destinationType;
        this.connectionFactoryName = connectionFactoryName;
        this.maxSize = maxPoolSize;
        this.connectionFactoryValue = connectionFactoryValue;
        this.namingFactory = namingFactory;
    }

    /**
     * @return The publisher to publish the message
     * @throws JMSException                   The JMXException
     * @throws NamingException                The NamingException
     * @throws PublisherNotAvailableException The PublisherNotAvailableException
     */
    public PublisherContext getPublisher()
            throws JMSException, NamingException, PublisherNotAvailableException {
        lock.lock();
        try {
            printDebugLog("Requesting publisher.");
            if (freePublishers.size() > 0) {
                PublisherContext publisher = freePublishers.remove(0);
                busyPublishers.add(publisher);
                printDebugLog("Returning an existing free publisher with hash : " + publisher);
                return publisher;
            } else if (canHaveMorePublishers()) {
                PublisherContext publisher = new PublisherContext(destination, connectionFactoryName, destinationType,
                        connectionFactoryValue, namingFactory);
                busyPublishers.add(publisher);
                printDebugLog("Created and returning a whole new publisher for destination with hash : " + publisher);
                return publisher;
            } else {
                log.warn("The Publisher pool is fully utilized." + " destination : " + destinationType + ":"
                        + destination + ", free publishers : " + freePublishers.size() + ", busy publishers : "
                        + busyPublishers.size());
            }
        } finally {
            lock.unlock();
        }
        throw new PublisherNotAvailableException(destinationType, destination, freePublishers.size(),
                busyPublishers.size());
    }

    /**
     * Will release the publisher after the message published.
     *
     * @param publisher The publisher to be expired
     * @throws JMSException The JMXException
     */
    public void releasePublisher(PublisherContext publisher) throws JMSException {
        lock.lock();
        try {
            printDebugLog("Releasing Publisher : " + publisher);
            busyPublishers.remove(publisher);
            printDebugLog("Removed publisher from busy pool.");
            if (canHaveMorePublishers()) {
                freePublishers.add(publisher);
                printDebugLog("Added publisher back to free pool.");
            } else {
                printDebugLog("Destroying publisher because we have reached maximum size of publisher pool.");
                publisher.close();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method will check whether we can have more publisher or not.
     *
     * @return The boolean values whether can add publisher or not
     */
    public boolean canHaveMorePublishers() {
        return busyPublishers.size() + freePublishers.size() < maxSize;
    }

    /**
     * @param message The message to print
     */
    private void printDebugLog(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message + " destination : " + destinationType + ":" + destination + ", free publishers : " +
                    freePublishers.size() + ", busy publishers : " + busyPublishers.size());
        }
    }

    /**
     * Will clear all publishers from publisherPool.
     *
     * @throws JMSException The JMXException
     */
    public void close() throws JMSException {
        printDebugLog("Destroying publisher pool");
        lock.lock();
        try {
            for (PublisherContext freePublisher : freePublishers) {
                freePublisher.close();
            }
            for (PublisherContext busyPublisher : busyPublishers) {
                busyPublisher.close();
            }
            freePublishers.clear();
            busyPublishers.clear();
        } finally {
            lock.unlock();
        }
    }
}