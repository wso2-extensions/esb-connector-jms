# Configuring JMS Operations

[[Prerequisites]](#Prerequisites) [[Initializing the Connector]](#initializing-the-connector)

## Prerequisites
The JMS connector implementation requires an active JMS server instance to be able to send messages. We recommend Apache ActiveMQ, but other implementations such as Apache Qpid and Tibco are also supported. For information on how to configure the JMS transport with the most common broker servers that can be integrated with WSO2 ESB, see [Configuring the JMS Transport](https://docs.wso2.com/display/EI650/Configuring+the+JMS+Transport).

To use the JMS connector, add the <jms.init> element in your configuration before carrying out any other JMS producer operation.

## Initializing the Connector

Specify the init method as follows:
**init**
```xml
<jms.init>
    <connectionFactoryName>QueueConnectionFactory</connectionFactoryName>
    <javaNamingProviderUrl>tcp://localhost:61616</javaNamingProviderUrl>
    <javaNamingFactoryInitial>org.apache.activemq.jndi.ActiveMQInitialContextFactory</javaNamingFactoryInitial>
    <destinationType>queue</destinationType>
    <connectionPoolSize>20</connectionPoolSize>
    <destinationName>MyQueue</destinationName>
</jms.init>
```

**Properties**
* connectionFactoryName : Required - The name of the object to look up.
* javaNamingFactoryInitial : Required - The JNDI initial context factory class. The class must implement the java.naming.spi.InitialContextFactory interface.
* destinationType : Required - The type of the destination. Possible values are queue or topic.
* connectionPoolSize : Required - The maximum number of connections to store in the pool.(For example.,  20)
* destinationName : Required - The name of the queue/topic.
* javaNamingProviderUrl : Required - The URL of the JNDI provider.
* priority  : The message priority for the message producer. This should be a value between 0 and 9. The default value is 4.
* deliveryMode : The message delivery mode for the message producer. Possible values are NON_PERSISTENT and PERSISTENT. The default delivery mode is PERSISTENT.
* timeToLive : The message time to live in milliseconds. The default value is zero, which means by default the time to live is unlimited.
* username: The user name to authenticate with the broker.
* password: The password to authenticate with the broker.
* connectionPoolSize : The number of message requests that can share the JMS connection.


>**Performance tuning tip**
>
>For better throughput, configure the connectionPoolSize parameter in the <init> configuration as follows:
>```
><connectionPoolSize>20</connectionPoolSize>
>```
>
>If you do not specify the connectionPoolSize parameter in the <init> configuration, a JMS connection is created for each message request.

Now that you have connected to JMS, use the information in the following topics to perform operations with the connector:

[Working with Messages in JMS](messages.md)