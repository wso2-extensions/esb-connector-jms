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

import javax.xml.namespace.QName;

public class JMSConnectorConstants {

    public static final String METHOD_NAME = "publishMessage:";
    public static final String DESTINATION_NAME = "jmsDestinationName";
    public static final String CONNECTION_POOL_SIZE = "jmsConnectionPoolSize";
    public static final String DESTINATION_TYPE = "jmsDestinationType";
    public static final String CONNECTION_FACTORY_NAME = "jmsConnectionFactoryName";
    public static final String JAVA_NAMING_FACTORY_INITIAL = "jmsJavaNamingFactoryInitial";
    public static final String JAVA_NAMING_PROVIDER_URL = "jmsJavaNamingProviderUrl";
    public static final String PRIORITY = "jmsPriority";
    public static final String DELIVERY_MODE = "jmsDeliveryMode";
    public static final String TIME_TO_LIVE = "jmsTimeToLive";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static final String BYTE_MESSAGE = "ByteMessage";
    public static final String TEXT_MESSAGE = "TextMessage";
    public static final String MAP_MESSAGE = "MapMessage";
    public static final String HEADER_ACCEPT_MULTIPART_RELATED = "multipart/related";

    public static final String JMS_MESSAGE_TYPE = "messageType";
    public static final String JMS_PRIORITY = "messagePriority";
    public static final String JMS_CORRELATION_ID = "messageCorrelationID";
    public static final String JMS_DELIVERY_MODE = "messageDeliveryMode";
    public static final String JMS_MESSAGE_ID = "messageId";
    public static final String JMS_EXPIRATION = "messageExpiration";
    public static final String JMS_TIMESTAMP = "messageTimestamp";

    public static final String TOPIC_NAME_PREFIX = "topic";
    public static final String QUEUE_NAME_PREFIX = "queue";
    public static final String MESSAGE_ID = "MessageID";

    public static final QName DEFAULT_BINARY_WRAPPER = new QName("http://ws.apache.org/commons/ns/payload",
            "binary");
    public static final QName DEFAULT_TEXT_WRAPPER = new QName("http://ws.apache.org/commons/ns/payload",
            "text");
    public static final QName MAP_QNAME =
            new QName("http://axis.apache.org/axis2/java/transports/jms/map-payload", "JMSMap",
                    "");
}