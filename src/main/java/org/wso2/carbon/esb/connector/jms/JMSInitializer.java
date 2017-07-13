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

    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        String destinationName = (String) messageContext.getProperty(JMSConnectorConstants.DESTINATION_NAME);
        String destinationType = (String) messageContext.getProperty(JMSConnectorConstants.DESTINATION_TYPE);
        String connectionFactoryName = (String) messageContext
                .getProperty(JMSConnectorConstants.CONNECTION_FACTORY_NAME);
        String javaNamingFactoryInitial = (String) messageContext
                .getProperty(JMSConnectorConstants.JAVA_NAMING_FACTORY_INITIAL);
        String javaNamingProviderUrl = (String) messageContext
                .getProperty(JMSConnectorConstants.JAVA_NAMING_PROVIDER_URL);
        int connectionPoolSize = Integer.parseInt((String) messageContext
                .getProperty(JMSConnectorConstants.CONNECTION_POOL_SIZE));
        String username = (String) messageContext.getProperty(JMSConnectorConstants.USERNAME);
        String password = (String) messageContext.getProperty(JMSConnectorConstants.PASSWORD);
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
        String tenantID = String.valueOf(((Axis2MessageContext) messageContext).getProperties()
                .get(JMSConnectorConstants.TENANT_ID));
        String publisherCacheKey = tenantID + ":" + connectionFactoryName + ":" + destinationType + ":" + destinationName;
        if (null == JMSPublisherPoolManager.getInstance().getPoolFromMap(publisherCacheKey)) {
            synchronized (publisherCacheKey.intern()) {
                if (null == JMSPublisherPoolManager.getInstance().getPoolFromMap(publisherCacheKey)) {
                    JMSPublisherPoolManager.getInstance().addPoolToMap(publisherCacheKey, new PublisherPool(destinationName,
                            destinationType, connectionFactoryName, connectionPoolSize, javaNamingProviderUrl,
                            javaNamingFactoryInitial,username,password));
                    if (log.isDebugEnabled()) {
                        log.debug("JMS Publisher pool created for destination : " + destinationName);
                    }
                }
            }
        }
    }
}