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


import scala.util.parsing.combinator.testing.Str;

import javax.xml.namespace.QName;

public class JMSConnectorConstants {

    public static final String DESTINATION_NAME = "destinationName";
    public static final String CONNECTION_POOL_SIZE = "connectionPoolSize";
    public static final String DESTINATION_TYPE = "destinationType";
    public static final String CONNECTION_FACTORY_NAME = "connectionFactoryName";
    public static final String JAVA_NAMING_FACTORY_INITIAL = "javaNamingFactoryInitial";
    public static final String JAVA_NAMING_PROVIDER_URL = "javaNamingProviderUrl";
    public static final String MESSAGE_TYPE = "JMS_MESSAGE_TYPE";
    public static final String BYTE_MESSAGE = "JMS_BYTE_MESSAGE";
    public static final String HEADER_ACCEPT_MULTIPART_RELATED = "multipart/related";
    public static final String TEXT_MESSAGE = "JMS_TEXT_MESSAGE";
    public static final String MAP_MESSAGE = "JMS_MAP_MESSAGE";
    public static final String COORELATION_ID = "JMS_COORELATION_ID";
    public static final String DELIVERY_MODE = "JMS_DELIVERY_MODE";
    public static final String PRIORITY = "JMS_PRIORITY";
    public static final String TIME_TO_LIVE = "JMS_TIME_TO_LIVE";
    public static final String MESSAGE_ID = "JMS_MESSAGE_ID";
    public static final String TRANSPORT_HEADERS = "TRANSPORT_HEADERS";
    public static final String TOPIC_NAME_PREFIX = "topic";
    public static final String QUEUE_NAME_PREFIX = "queue";
    public static final String EXPIRATION = "JMS_EXPIRATION";
    public static final String TIMESTAMP = "JMS_TIMESTAMP";
    public static final String USER_TRANSACTION = "UserTransaction";
    //A message level property indicating a commit is required after the next immediate send over a transport
    public static final String JTA_COMMIT_AFTER_SEND = "JTA_COMMIT_AFTER_SEND";
    public static final String TENANT_ID = "tenant.info.id";
    public static final String SOAPACTION = "SOAPAction";
    public static final String USERNAME = "jms_username";
    public static final String PASSWORD = "jms_password";

    public static final QName DEFAULT_BINARY_WRAPPER = new QName("http://ws.apache.org/commons/ns/payload",
            "binary");
    public static final QName DEFAULT_TEXT_WRAPPER = new QName("http://ws.apache.org/commons/ns/payload",
            "text");
    public static final QName MAP_QNAME =
            new QName("http://axis.apache.org/axis2/java/transports/jms/map-payload", "JMSMap",
                    "");
}