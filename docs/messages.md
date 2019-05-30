# Working with Messages in JMS


### Publishing messages
The publishMessage operation allows you to publish messages to a JMS message broker.

**publishMessage**
```xml
<jms.publishMessage>
    <messageType>{$ctx:messageType}</messageType>
    <messageDeliveryMode>{$ctx:messageDeliveryMode}</messageDeliveryMode>
    <messageCorrelationID>{$ctx:messageCorrelationID}</messageCorrelationID>
    <messageId>{$ctx:messageId}</messageId>
    <messageTimestamp>{$ctx:messageTimestamp}</messageTimestamp>
    <messageExpiration>{$ctx:messageExpiration}</messageExpiration>
    <messagePriority>{$ctx:messagePriority}</messagePriority>
</jms.publishMessage>
```

**Properties**

* messageCorrelationID : The message ID of a message being referred to.
* messageId : The ID of the message.
* messageType : The message type.
* messageTimestamp : The timestamp for the message.
* messageExpiration : The expiration time of the message.
* messagePriority :The priority of the message.
* messageDeliveryMode : The delivery mode for the message.

>If required, you can add SOAPAction as an optional parameter to the publishMessage operation:
>
>```xml
><connectionFactoryName.SOAPAction>Value</connectionFactoryName.SOAPAction>
>```
>
>You can add the parameter as follows in the publishMessage operation:
>
>```xml
><jms.publishMessage>
>    <QueueConnectionFactory.SOAPAction>Value</QueueConnectionFactory.SOAPAction>
></jms.publishMessage>
>```

**Sample request**
Following is a sample request that can be handled by the publishMessage operation.

```json
{
  "success":"ok"
}
```

**Sample response**
Given below is a sample response for the createDocumentWithIndex operation.

```json
{ "MessageID":'urn:uuid:b587afab-68d9-41f6-9007-d6cd9cb5d74d'}
```

**Sample configuration**
Following is a sample proxy service that illustrates how to connect to a JMS broker with the init operation and use the publishMessage operation to publish messages to a queue.

**Sample Proxy**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<proxy xmlns="http://ws.apache.org/ns/synapse"
       name="JMSAPublisher"
       startOnLoad="true"
       statistics="disable"
       trace="disable"
       transports="http,https">
   <target>
      <inSequence>
         <property name="OUT_ONLY" value="true"/>
         <jms.publishMessage configKey="jms_init"/>
         <respond/>
      </inSequence>
   </target>
   <description/>
</proxy>
```