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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.context.CarbonContext;

import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * JMS connector publish operation implementation.
 *
 * @since 1.0.0
 */
public class JMSConnector extends AbstractConnector {

    private static final Log log = LogFactory.getLog(JMSConnector.class);

    /**
     * @param messageContext The message context
     * @throws ConnectException The connection exception from esb mediator
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String destinationName = (String) messageContext.getProperty(JMSConnectorConstants.DESTINATION_NAME);
        String destinationType = (String) messageContext.getProperty(JMSConnectorConstants.DESTINATION_TYPE);
        String connectionFactoryName = (String) messageContext
                .getProperty(JMSConnectorConstants.CONNECTION_FACTORY_NAME);
        if (log.isDebugEnabled()) {
            log.debug("Processing message for destination : " + destinationType + " : " + destinationName
                    + " with connection factory : " + connectionFactoryName);
        }
        JMSPublisherPool jmsPublisherPool;
        JMSPublisher JMSPublisher = null;
        String tenantID = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        String publisherCacheKey = tenantID + ":" + connectionFactoryName + ":" + destinationType + ":" + destinationName;
        jmsPublisherPool = JMSPublisherPoolManager.getJMSPublisherPool(publisherCacheKey);
        if (jmsPublisherPool == null) {
            handleException("Publisher pool cannot be empty please create the pool", messageContext);
            return;
        }
        try {
            JMSPublisher = jmsPublisherPool.getPublisher();
            JMSPublisher.publishMessage(messageContext);
        } catch (NamingException e) {
            handleException("NamingException : ", e, messageContext);
        } catch (JMSException e) {
            try {
                if (JMSPublisher != null) {
                    JMSPublisher.close();
                }
            } catch (JMSException e1) {
                handleException("JMSException while trying clear publisher connections due to failover : ", e1,
                        messageContext);
            }
        } finally {
            if (null != JMSPublisher) {
                jmsPublisherPool.releasePublisher(JMSPublisher);
            }
        }
    }
}