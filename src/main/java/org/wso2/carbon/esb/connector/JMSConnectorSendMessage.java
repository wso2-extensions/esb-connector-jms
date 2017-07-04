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
package org.wso2.carbon.esb.connector;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.IOException;

/**
 * JMS connector implementation.
 */
public class JMSConnectorSendMessage extends AbstractConnector {

    /**
     * JNDI Prefix for topics.
     */
    public static final String TOPIC_NAME_PREFIX = "topic";
    /**
     * JNDI Prefix for queues.
     */
    public static final String QUEUE_NAME_PREFIX = "queue";
    private static final Log log = LogFactory.getLog(JMSConnectorSendMessage.class);

    /**
     * @param messageContext
     * @throws ConnectException
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String destinationName = messageContext.getProperty(JMSConnectorConstants.Destination_Name).toString();
        //Type of destination. "queue" or "topic".
        String destinationType = messageContext.getProperty(JMSConnectorConstants.Destination_Type).toString();
        //Connection factory name as configured in jndi.properties file. Could use different connection factories for
        // topics and queues.
        String connectionFactoryName = messageContext
                .getProperty(JMSConnectorConstants.Connection_Factory_Name).toString();
        //Maximum number of connections allowed for a single destination+destinationType combination.
        if (StringUtils.isBlank(destinationName)) {
            handleException("Could not find a valid topic name to publish the message.", messageContext);
        }
        if ((!JMSConnectorSendMessage.QUEUE_NAME_PREFIX.equals(destinationType)) &&
                (!JMSConnectorSendMessage.TOPIC_NAME_PREFIX.equals(destinationType))) {
            handleException("Invalid destination type. It must be a queue or a topic. Current value : " +
                    destinationType, messageContext);
        }
        if (log.isDebugEnabled()) {
            log.debug("Processing message for destination : " + destinationType + " : " + destinationName + " with "
                    + "connection factory : " + connectionFactoryName);
        }
        //Interval at which the cache should expire (in seconds).
        if (StringUtils.isBlank(destinationName)) {
            handleException("Could not find a valid topic name to publish the message.", messageContext);
        }
        PublisherPool publisherPool;
        PublisherContext publisherContext = null;
        //TODO key should be the combination of destinationType, destinationName,ConnectionFactoryName,tenantID
        String publisherContextKey = destinationType + ":/" + destinationName; //queue:/queueA
        publisherPool = PublisherCache.getJMSPublisherPoolCache().get(publisherContextKey);
        if (null == publisherPool) {
            handleException("Pool cannot be empty please create a connection pool", messageContext);
        }
        try {
            try {
                publisherContext = publisherPool.getPublisher();
            } catch (PublisherNotAvailableException e) {
                e.printStackTrace();
            }
            assert publisherContext != null;
            publisherContext.publishMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
        } catch (JMSException e) {
            try {
                publisherPool.close();
            } catch (JMSException e1) {
                handleException("JMSException while trying clear publisher connections due to failover : ", e,
                        messageContext);
            }
        } catch (AxisFault e) {
            handleException("AxisFault : ", e, messageContext);
        } catch (IOException e) {
            handleException("IOException : " + e, messageContext);
        } catch (NamingException e) {
            handleException("NamingException : ", e, messageContext);
        } finally {
            if (null != publisherContext) {
                try {
                    publisherPool.releasePublisher(publisherContext);
                } catch (JMSException e) {
                    handleException("Error while releasing publisher after sending message : ", e, messageContext);
                }
            }
        }

    }
}
