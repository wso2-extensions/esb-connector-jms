package org.wso2.carbon.esb.connector;
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
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

public class Init extends AbstractConnector {
    private static final Log log = LogFactory.getLog(Init.class);

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String destinationName = (String) messageContext.getProperty(JMSConnectorConstants.Destination_Name);
        String destinationType = (String) messageContext.getProperty(JMSConnectorConstants.Destination_Type);
        if (StringUtils.isBlank(destinationName)) {
            handleException("Could not find a valid topic name to publish the message.", messageContext);
        }
        if ((!JMSConnectorConstants.QUEUE_NAME_PREFIX.equals(destinationType)) &&
                (!JMSConnectorConstants.TOPIC_NAME_PREFIX.equals(destinationType))) {
            handleException("Invalid destination type. It must be a queue or a topic. Current value : " +
                    destinationType, messageContext);
        }
        //TODO key should be the combination of destinationType, destinationName,ConnectionFactoryName,tenantID
        String publisherCacheKey = destinationType + ":/" + destinationName;
        if (null == PublisherCache.getJMSPublisherPoolCache().get(publisherCacheKey)) {
            synchronized (publisherCacheKey.intern()) {
                if (null == PublisherCache.getJMSPublisherPoolCache().get(publisherCacheKey)) {
                    String namingFactory = (String) messageContext.getProperty(JMSConnectorConstants.NamingFactory);
                    String connectionFactoryValue = (String) messageContext
                            .getProperty(JMSConnectorConstants.ConnectionFactoryValue);
                    int cacheExpirationInterval = Integer.parseInt((String) messageContext
                            .getProperty(JMSConnectorConstants.Cache_Expiration_Interval));
                    String connectionFactoryName = (String) messageContext
                            .getProperty(JMSConnectorConstants.Connection_Factory_Name);
                    int connectionPoolSize = Integer.parseInt((String) messageContext
                            .getProperty(JMSConnectorConstants.Connection_Pool_Size));
                    PublisherCache.setCacheExpirationInterval(cacheExpirationInterval);
                    log.info("JMS Publisher pool cache miss for destination : " + destinationName);
                    PublisherCache.getJMSPublisherPoolCache().put(publisherCacheKey,
                            new PublisherPool(destinationName, destinationType, connectionFactoryName,
                                    connectionPoolSize, connectionFactoryValue, namingFactory));
                }
            }
        }
    }
}