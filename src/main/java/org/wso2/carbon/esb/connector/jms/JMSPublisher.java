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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.jms.iowrappers.BytesMessageOutputStream;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.Value;

import javax.activation.DataHandler;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Queue;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.Message;
import javax.jms.MapMessage;
import javax.jms.TextMessage;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Properties;
import javax.jms.DeliveryMode;
import java.util.Map;
import java.util.Set;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class maintains all the JMS sessions and connections required to publish a message to a single topic/queue.
 */
public class JMSPublisher {

    private static final Log log = LogFactory.getLog(JMSPublisher.class);
    /**
     * Properties read from the above file.
     */
    private static Properties jndiProperties;
    /**
     * URL of the JNDI provider.
     */
    private final String javaNamingProviderUrl;

    /**
     * Connection Factory type specific to message broker
     */
    private String javaNamingFactoryInitial;
    /**
     * Name of destination.
     */
    private String destinationName;
    /**
     * Name of connection factory.
     */
    private String connectionFactoryName;
    /**
     * "queue" or "topic"
     */
    private String destinationType;
    /**
     * A Object encapsulates a set of connection configuration parameters that has been defined by an administrator.
     */
    private ConnectionFactory connectionFactory;
    /**
     * A Connection object is a client's active connection to its JMS provider.
     */
    private Connection connection;

    /**
     * A Session object is a single-threaded context for producing and consuming messages.
     */
    private Session session;

    /**
     * A client uses a MessageProducer object to send messages to a destination.
     */
    private MessageProducer messageProducer;
    /**
     * The caller's user name
     */
    private String username;
    /**
     * The caller's password
     */
    private String password;
    /**
     * The message priority for this message producer; must be a value between 0 and 9
     */
    private String priority;
    /**
     * The message delivery mode for this message producer
     */
    private String deliveryMood;
    /**
     * The message time to live in milliseconds
     */
    private String timeToLive;

    /**
     * Initialize the JMSPublisher for a specific destination planning to use a pre-defined JMS connection factory.
     *
     * @param destinationName       Name of topic
     * @param username              The caller's user name
     * @param password              The caller's password
     * @param connectionFactoryName Name of JMS connection factory as defined in jndi.properties file.
     * @param javaNamingProviderUrl URL of the JNDI provider.
     * @param priority              The message priority for this message producer; must be a value between 0 and 9
     * @param deliveryMood          The message delivery mode for this message producer
     * @param timeToLive            The message time to live in milliseconds
     * @throws NamingException if the jndi processing results in an invalid naming convention or non-existent properties.
     * @throws JMSException    Connectivity issues, invalid destination type
     */
    public JMSPublisher(String destinationName, String connectionFactoryName, String destinationType,
                        String javaNamingProviderUrl, String javaNamingFactoryInitial, String username, String password,
                        String priority, String deliveryMood, String timeToLive)
            throws JMSException, NamingException {
        this.destinationName = destinationName;
        this.connectionFactoryName = connectionFactoryName;
        this.destinationType = destinationType;
        this.javaNamingProviderUrl = javaNamingProviderUrl;
        this.javaNamingFactoryInitial = javaNamingFactoryInitial;
        this.username = username;
        this.password = password;
        this.deliveryMood = deliveryMood;
        this.priority = priority;
        this.timeToLive = timeToLive;
        if (null == jndiProperties) {
            initializeJNDIProperties();
        }
        switch (destinationType) {
            case JMSConnectorConstants.QUEUE_NAME_PREFIX:
                initializeQueueProducer();
                break;
            case JMSConnectorConstants.TOPIC_NAME_PREFIX:
                initializeTopicProducer();
                break;
            default:
                throw new JMSException("Invalid destination type. It must be a queue or a topic. " +
                        "Current value : " + destinationType);
        }
    }

    /**
     * Utility method to direct any exceptions to the ESB mediation engine.
     *
     * @param msg description of error
     * @param e   Exception
     * @throws SynapseException The SynapseException exception
     */
    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    /**
     * Create the JNDI properties for the JMS communication within the connector.
     */
    private void initializeJNDIProperties() {
        jndiProperties = new Properties();
        jndiProperties.put("connectionfactory." + connectionFactoryName, javaNamingProviderUrl);
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, javaNamingFactoryInitial);
    }

    /**
     * Will create message producer for queue
     *
     * @throws NamingException The NamingException
     * @throws JMSException    The JMSException
     */
    private void initializeQueueProducer() throws NamingException, JMSException {
        if (!jndiProperties.containsKey(JMSConnectorConstants.QUEUE_NAME_PREFIX + "." + destinationName)) {
            jndiProperties.put(JMSConnectorConstants.QUEUE_NAME_PREFIX + "." + destinationName, destinationName);
        }
        if (log.isDebugEnabled()) {
            log.debug("New connection required to perform the operation");
        }
        InitialContext initialJMSContext = new InitialContext(jndiProperties);
        connectionFactory = (QueueConnectionFactory) initialJMSContext.lookup(connectionFactoryName);
        if (username != null && password != null) {
            connection = ((QueueConnectionFactory) connectionFactory).createQueueConnection(username, password);
        } else {
            connection = ((QueueConnectionFactory) connectionFactory).createQueueConnection();
        }
        String contextKey = connectionFactoryName + ":/" + destinationType + ":/" + destinationName;
        connection.setExceptionListener(new JMSExceptionListener(contextKey));
        session = ((QueueConnection) connection).createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        Queue queue = (Queue) initialJMSContext.lookup(destinationName);
        messageProducer = ((QueueSession) session).createSender(queue);
        setOptionalParameters(messageProducer);
    }

    /**
     * Will set optional parameters to messageProducer
     *
     * @param messageProducer A client uses a MessageProducer object to send messages to a destination.
     */
    private void setOptionalParameters(MessageProducer messageProducer) {
        try {
            if (deliveryMood != null) {
                messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
            }
            if (priority != null) {
                messageProducer.setPriority(Integer.parseInt(priority));
            }
            if (timeToLive != null) {
                messageProducer.setTimeToLive(Long.parseLong(timeToLive));
            }
        } catch (JMSException e) {
            log.error("Error while setting JMS optional parameters", e);
        }
    }

    /**
     * Will create message producer for topic
     *
     * @throws NamingException The NamingException
     * @throws JMSException    The JMSException
     */
    private void initializeTopicProducer() throws NamingException, JMSException {
        if (!jndiProperties.containsKey(JMSConnectorConstants.TOPIC_NAME_PREFIX + "." + destinationName)) {
            jndiProperties.put(JMSConnectorConstants.TOPIC_NAME_PREFIX + "." + destinationName, destinationName);
        }
        InitialContext initialJMSContext = new InitialContext(jndiProperties);
        connectionFactory = (TopicConnectionFactory) initialJMSContext.lookup(connectionFactoryName);
        if (username != null && password != null) {
            connection = ((TopicConnectionFactory) connectionFactory).createTopicConnection(username, password);
        } else {
            connection = ((TopicConnectionFactory) connectionFactory).createTopicConnection();
        }
        String contextKey = connectionFactoryName + ":/" + destinationType + ":/" + destinationName;
        connection.setExceptionListener(new JMSExceptionListener(contextKey));
        session = ((TopicConnection) connection).createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        Topic topic = (Topic) initialJMSContext.lookup(destinationName);
        messageProducer = ((TopicSession) session).createPublisher(topic);
        setOptionalParameters(messageProducer);
    }

    /**
     * Method exposed to publish a message using this JMS context (session, connection).
     *
     * @param synapseMessageContext The Synapse message context
     * @throws JMSException The JMSException
     */
    public void publishMessage(org.apache.synapse.MessageContext synapseMessageContext) throws JMSException {
        MessageContext messageContext = ((Axis2MessageContext) synapseMessageContext).getAxis2MessageContext();
        if (null != session && null != messageProducer) {
            Message messageToPublish = createJMSMessage(messageContext);
            setDynamicMessageHeaders(synapseMessageContext, messageToPublish);
            publish(messageToPublish, messageContext);
        }
    }

    /**
     * Create a JMS Message from the given MessageContext and using the given session
     *
     * @param msgContext The MessageContext
     * @return a JMS message from the context and session
     * @throws JMSException on exception
     */
    private Message createJMSMessage(MessageContext msgContext) throws JMSException {
        Message message = null;
        String msgType = (String) msgContext.getProperty(JMSConnectorConstants.JMS_MESSAGE_TYPE);
        // check the first element of the SOAP body, do we have content wrapped using the
        // default wrapper elements for binary (BaseConstants.DEFAULT_BINARY_WRAPPER) or
        // text (BaseConstants.DEFAULT_TEXT_WRAPPER) ? If so, do not create SOAP messages
        // for JMS but just get the payload in its native format
        String jmsPayloadType = guessMessageType(msgContext);
        if (jmsPayloadType == null) {
            OMOutputFormat format = BaseUtils.getOMOutputFormat(msgContext);
            MessageFormatter messageFormatter;
            try {
                messageFormatter = MessageProcessorSelector.getMessageFormatter(msgContext);
            } catch (AxisFault axisFault) {
                throw new JMSException("Unable to get the message formatter to use");
            }
            String contentType = messageFormatter.getContentType(msgContext, format, msgContext.getSoapAction());
            boolean useBytesMessage = msgType != null && JMSConnectorConstants.BYTE_MESSAGE.equals(msgType) ||
                    contentType.contains(JMSConnectorConstants.HEADER_ACCEPT_MULTIPART_RELATED);
            OutputStream out;
            StringWriter sw;
            if (useBytesMessage) {
                BytesMessage bytesMsg = session.createBytesMessage();
                sw = null;
                out = new BytesMessageOutputStream(bytesMsg);
                message = bytesMsg;
            } else {
                sw = new StringWriter();
                try {
                    out = new WriterOutputStream(sw, format.getCharSetEncoding());
                } catch (UnsupportedCharsetException ex) {
                    handleException("Unsupported encoding " + format.getCharSetEncoding(), ex);
                    return null;
                }
            }
            try {
                messageFormatter.writeTo(msgContext, format, out, true);
                out.close();
            } catch (IOException e) {
                handleException("IO Error while creating BytesMessage", e);
            }
            if (!useBytesMessage) {
                TextMessage txtMsg = session.createTextMessage();
                txtMsg.setText(sw.toString());
                message = txtMsg;
            }
        } else if (JMSConnectorConstants.BYTE_MESSAGE.equals(jmsPayloadType)) {
            message = session.createBytesMessage();
            BytesMessage bytesMsg = (BytesMessage) message;
            OMElement wrapper = msgContext.getEnvelope().getBody().
                    getFirstChildWithName(JMSConnectorConstants.DEFAULT_BINARY_WRAPPER);
            OMNode omNode = wrapper.getFirstOMChild();
            if (omNode != null && omNode instanceof OMText) {
                Object dh = ((OMText) omNode).getDataHandler();
                if (dh != null && dh instanceof DataHandler) {
                    try {
                        ((DataHandler) dh).writeTo(new BytesMessageOutputStream(bytesMsg));
                    } catch (IOException e) {
                        handleException("Error serializing binary content of element : " +
                                JMSConnectorConstants.DEFAULT_BINARY_WRAPPER, e);
                    }
                }
            }
        } else if (JMSConnectorConstants.TEXT_MESSAGE.equals(jmsPayloadType)) {
            message = session.createTextMessage();
            TextMessage txtMsg = (TextMessage) message;
            txtMsg.setText(msgContext.getEnvelope().getBody().
                    getFirstChildWithName(JMSConnectorConstants.DEFAULT_TEXT_WRAPPER).getText());
        } else if (JMSConnectorConstants.MAP_MESSAGE.equalsIgnoreCase(jmsPayloadType)) {
            message = session.createMapMessage();
            convertXMLtoJMSMap(msgContext.getEnvelope().getBody().getFirstChildWithName(
                    JMSConnectorConstants.MAP_QNAME), (MapMessage) message);
        }
        return message;
    }

    /**
     * Will set optional dynamic parameters to message if any
     *
     * @param msgContext The synapse message context
     * @param message    The Message interface is the root interface of all JMS messages.
     */
    private void setDynamicMessageHeaders(org.apache.synapse.MessageContext msgContext, Message message) {
        if (message == null) {
            return;
        }
        String messageID = (String) msgContext.getProperty(JMSConnectorConstants.JMS_MESSAGE_ID);
        String jmsType = (String) msgContext.getProperty(JMSConnectorConstants.JMS_MESSAGE_TYPE);
        String timestamp = (String) msgContext.getProperty(JMSConnectorConstants.JMS_TIMESTAMP);
        String correlationID = (String) msgContext.getProperty(JMSConnectorConstants.JMS_CORRELATION_ID);
        String expiration = (String) msgContext.getProperty(JMSConnectorConstants.JMS_EXPIRATION);
        String priority = (String) msgContext.getProperty(JMSConnectorConstants.JMS_PRIORITY);
        String deliveryMood = (String) msgContext.getProperty(JMSConnectorConstants.JMS_DELIVERY_MODE);
        try {
            if (StringUtils.isNotEmpty(messageID)) {
                message.setJMSMessageID(messageID);
            }
            if (StringUtils.isNotEmpty(jmsType)) {
                message.setJMSType(jmsType);
            }
            if (StringUtils.isNotEmpty(timestamp)) {
                message.setJMSTimestamp(Long.parseLong(timestamp));
            }
            if (StringUtils.isNotEmpty(correlationID)) {
                message.setJMSCorrelationID(correlationID);
            }
            if (StringUtils.isNotEmpty(expiration)) {
                message.setJMSExpiration(Long.parseLong(expiration));
            }
            if (StringUtils.isNotEmpty(priority)) {
                message.setJMSPriority(Integer.parseInt(priority));
            }
            if (StringUtils.isNotEmpty(deliveryMood)) {
                message.setJMSDeliveryMode(Integer.parseInt(deliveryMood));
            }
            Hashtable<String, Object> dynamicHeaders = getDynamicParameters(msgContext, connectionFactoryName);
            if (dynamicHeaders.size() > 0) {
                Set<String> headers = dynamicHeaders.keySet();
                for (String header : headers) {
                    Object value = dynamicHeaders.get(header);
                    if (value instanceof String) {
                        message.setStringProperty(header, (String) value);
                    } else if (value instanceof Boolean) {
                        message.setBooleanProperty(header, (Boolean) value);
                    } else if (value instanceof Integer) {
                        message.setIntProperty(header, (Integer) value);
                    } else if (value instanceof Long) {
                        message.setLongProperty(header, (Long) value);
                    } else if (value instanceof Double) {
                        message.setDoubleProperty(header, (Double) value);
                    } else if (value instanceof Float) {
                        message.setFloatProperty(header, (Float) value);
                    }
                }
            }
        } catch (JMSException e) {
            log.error("Error while set the optional parameters to message" + e.getMessage());
        }
    }

    /**
     * Will convert XML element to JMS map.
     *
     * @param element The OMElement
     * @param message The JMS MapMessage
     * @throws JMSException The JMSException
     */
    private void convertXMLtoJMSMap(OMElement element, MapMessage message) throws JMSException {
        Iterator itr = element.getChildElements();
        while (itr.hasNext()) {
            OMElement elem = (OMElement) itr.next();
            message.setString(elem.getLocalName(), elem.getText());
        }
    }

    /**
     * Guess the message type to use for JMS looking at the message contexts' envelope
     *
     * @param msgContext the message context
     * @return JMSConnectorConstants.JMS_BYTE_MESSAGE or JMSConnectorConstants.JMS_TEXT_MESSAGE or null
     */
    private String guessMessageType(MessageContext msgContext) {
        OMElement firstChild = msgContext.getEnvelope().getBody().getFirstElement();
        if (firstChild != null) {
            if (JMSConnectorConstants.DEFAULT_BINARY_WRAPPER.equals(firstChild.getQName())) {
                return JMSConnectorConstants.BYTE_MESSAGE;
            } else if (JMSConnectorConstants.DEFAULT_TEXT_WRAPPER.equals(firstChild.getQName())) {
                return JMSConnectorConstants.TEXT_MESSAGE;
            } else if (JMSConnectorConstants.MAP_QNAME.equals(firstChild.getQName())) {
                return JMSConnectorConstants.MAP_MESSAGE;
            }
        }
        return null;
    }

    /**
     * Will generate the dynamic parameters from message context parameter
     *
     * @param connectionFactoryName The connectionFactoryName to generate the dynamic parameters(connection factory name)
     * @param messageContext        The message contest
     * @return extract the value's from properties and make its as hashable
     */
    private Hashtable<String, Object> getDynamicParameters(org.apache.synapse.MessageContext messageContext,
                                                           String connectionFactoryName) {
        Hashtable<String, Object> dynamicValues = new Hashtable<>();
        String key = JMSConnectorConstants.METHOD_NAME + connectionFactoryName;
        Map<String, Object> propertiesMap = (((Axis2MessageContext) messageContext).getProperties());
        for (String keyValue : propertiesMap.keySet()) {
            if (keyValue.startsWith(key)) {
                Value propertyValue = (Value) propertiesMap.get(keyValue);
                dynamicValues.put(keyValue.substring(key.length() + 1, keyValue.length()), propertyValue.getKeyValue());
            }
        }
        return dynamicValues;
    }

    /**
     * Perform actual publish of JMS message to the Destination selected
     *
     * @param message the JMS message
     * @param msgCtx  the Axis2 MessageContext
     */
    private void publish(Message message, MessageContext msgCtx) {
        boolean sendingSuccessful = false;
        // perform actual message sending
        try {
            if (JMSConnectorConstants.QUEUE_NAME_PREFIX.equals(destinationType)) {
                try {
                    messageProducer.send(message);
                } catch (JMSException e) {
                    //create a queue reference in MB before publishing.
                    session.createQueue(destinationName);
                    messageProducer.send(message);
                }
            } else {
                ((TopicPublisher) messageProducer).publish(message);
            }
            if (log.isDebugEnabled()) {
                log.debug("Published message to " + destinationType + " : " + destinationName);
            }
            // set the actual MessageID to the message context for use by any others down the line
            String msgId = null;
            try {
                msgId = message.getJMSMessageID();
                if (msgId != null) {
                    msgCtx.setProperty(JMSConnectorConstants.MESSAGE_ID, msgId);
                }
            } catch (JMSException ignore) {
            }
            sendingSuccessful = true;
            if (log.isDebugEnabled()) {
                log.debug("Sent Message Context ID : " + msgCtx.getMessageID() + " with JMS Message ID : " + msgId
                        + " to destination : " + messageProducer.getDestination());
            }
        } catch (JMSException e) {
            handleException("Error sending message with MessageContext ID : " + msgCtx.getMessageID()
                    + " to destination " + destinationType + " : " + destinationName, e);
        } finally {
            try {
                if (session.getTransacted()) {
                    if (sendingSuccessful) {
                        session.commit();
                    } else {
                        session.rollback();
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug((sendingSuccessful ? "Committed" : "Rolled back") +
                            " local (JMS Session) Transaction");
                }
            } catch (JMSException e) {
                handleException("Error committing/rolling back local (i.e. session) " +
                        "transaction after sending of message with MessageContext ID : " +
                        msgCtx.getMessageID() + " to destination : " + destinationName, e);
            }
        }
    }

    /**
     * Method to properly shutdown the JMS sessions and connections in the proper order. This is normally called when
     * a cached publisherContext expires.
     *
     * @throws JMSException The JMSException
     */
    public void close() throws JMSException {
        if (null != messageProducer) {
            messageProducer.close();
        }
        if (null != session) {
            session.close();
        }
        if (null != connection) {
            connection.close();
        }
        if (null != connectionFactory) {
            connectionFactory = null;
        }
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