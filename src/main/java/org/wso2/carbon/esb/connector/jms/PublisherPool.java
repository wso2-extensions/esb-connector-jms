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
import org.apache.synapse.MessageContext;

import java.util.concurrent.ConcurrentLinkedQueue;
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
    private String javaNamingProviderUrl;
    private String javaNamingFactoryInitial;
    private String username;
    private String password;
    /**
     * Will maintain already created and available connections upto the max limit
     */
    private java.util.Queue<PublisherContext> freePublishers;

    /**
     * @param destination              The name of the queue/topic.
     * @param destinationType          The message type queue/topic.
     * @param connectionFactoryName    The name of the connection factory.
     * @param maxPoolSize              The maximum connection size of pool.
     * @param javaNamingProviderUrl    URL of the JNDI provider.
     * @param javaNamingFactoryInitial JNDI initial context factory class.
     * @param username
     * @param password
     */
    public PublisherPool(String destination, String destinationType, String connectionFactoryName, int maxPoolSize,
                         String javaNamingProviderUrl, String javaNamingFactoryInitial, String username, String password) {
        this.destination = destination;
        this.destinationType = destinationType;
        this.connectionFactoryName = connectionFactoryName;
        this.maxSize = maxPoolSize;
        this.javaNamingProviderUrl = javaNamingProviderUrl;
        this.javaNamingFactoryInitial = javaNamingFactoryInitial;
        this.username = username;
        this.password = password;
        this.freePublishers = new ConcurrentLinkedQueue<PublisherContext>();
    }

    /**
     * @return The publisher to publish the message
     * @throws JMSException    The JMXException
     * @throws NamingException The NamingException
     */
    public PublisherContext getPublisher() throws JMSException, NamingException {
        printDebugLog("Requesting publisher.");
        PublisherContext publisher = freePublishers.poll();
        if (publisher != null) {
            printDebugLog("Returning an existing free publisher with hash : " + publisher);
            return publisher;
        } else {
            publisher = new PublisherContext(destination, connectionFactoryName, destinationType,
                    javaNamingProviderUrl, javaNamingFactoryInitial,username,password);
            printDebugLog("Created and returning a new publisher for destination with hash : " + publisher);
            return publisher;
        }
    }

    /**
     * Will release the publisher after the message published.
     *
     * @param publisher The publisher to be expired
     * @throws JMSException The JMXException
     */
    public void releasePublisher(PublisherContext publisher) throws JMSException {
        printDebugLog("Releasing Publisher : " + publisher);
        if (freePublishers.size() < maxSize) {
            freePublishers.add(publisher);
            printDebugLog("Added publisher back to free pool.");
        } else {
            freePublishers.poll().close();
            freePublishers.add(publisher);
            printDebugLog("Destroying publisher because we have reached maximum size of publisher pool.");
        }
    }

    /**
     * @param message The message to print
     */
    private void printDebugLog(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message + " destination : " + destinationType + ":" + destination + ", free publishers : " +
                    freePublishers.size());
        }
    }

    /**
     * Will clear all publishers from publisherPool.
     *
     * @throws JMSException The JMXException
     */
    public void close() throws JMSException {
        printDebugLog("Destroying publisher pool");
        for (PublisherContext freePublisher : freePublishers) {
            freePublisher.close();
        }
        freePublishers.clear();
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