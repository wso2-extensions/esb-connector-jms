package org.wso2.carbon.esb.connector.jms;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

public class JMSInitializer extends AbstractConnector {
    private static final Log log = LogFactory.getLog(JMSInitializer.class);

    //TODO integration test
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String destinationName = (String) messageContext.getProperty(JMSConnectorConstants.DESTINATION_NAME);
        String destinationType = (String) messageContext.getProperty(JMSConnectorConstants.DESTINATION_TYPE);
        //TODO change the name
        String connectionFactoryName = (String) messageContext.getProperty(JMSConnectorConstants.CONNECTION_FACTORY_NAME);
        if (StringUtils.isBlank(destinationName)) {
            handleException("Could not find a valid topic name to publish the message.", messageContext);
        }
        if (StringUtils.isBlank(connectionFactoryName)) {
            handleException("ConnectionFactoryName can not be empty.", messageContext);
        }
        if ((!JMSConnectorConstants.QUEUE_NAME_PREFIX.equals(destinationType)) &&
                (!JMSConnectorConstants.TOPIC_NAME_PREFIX.equals(destinationType))) {
            handleException("Invalid destination type. It must be a queue or a topic. Current value : " +
                    destinationType, messageContext);
        }
        int cacheExpirationInterval = Integer.parseInt((String) messageContext
                .getProperty(JMSConnectorConstants.CACHE_EXPIRATION_INTERVAL));
        PublisherCache.setCacheExpirationInterval(cacheExpirationInterval);
        String tenantID = String.valueOf(((Axis2MessageContext) messageContext).getProperties()
                .get(JMSConnectorConstants.TENANT_ID));
        //TODO
        String publisherCacheKey = tenantID + ":" + connectionFactoryName + ":" + destinationType + ":" + destinationName;
        if (null == PublisherCache.getJMSPublisherPoolCache().get(publisherCacheKey)) {
            synchronized (publisherCacheKey.intern()) {
                if (null == PublisherCache.getJMSPublisherPoolCache().get(publisherCacheKey)) {
                    String namingFactory = (String) messageContext.getProperty(JMSConnectorConstants.NAMING_FACTORY);
                    String connectionFactoryValue = (String) messageContext
                            .getProperty(JMSConnectorConstants.CONNECTION_FACTORY_VALUE);
                    int connectionPoolSize = Integer.parseInt((String) messageContext
                            .getProperty(JMSConnectorConstants.CONNECTION_POOL_SIZE));
                    log.info("JMS Publisher pool cache miss for destination : " + destinationName);
                    PublisherCache.getJMSPublisherPoolCache().put(publisherCacheKey,
                            new PublisherPool(destinationName, destinationType, connectionFactoryName,
                                    connectionPoolSize, connectionFactoryValue, namingFactory));
                }
            }
        }
    }
}