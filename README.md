### JMS ESB Connector

The JMS [Connector](https://docs.wso2.com/display/EI650/Working+with+Connectors) allows you to access the [Java Message Service (JMS)](https://docs.oracle.com/cd/E19798-01/821-1841/bncdr/index.html) API through WSO2 ESB, and acts as a message producer that facilitates publishing messages. The JMS API is a Java API that allows applications to create, send, receive, and read messages. Designed by Sun and several partner companies, the JMS API defines a common set of interfaces and associated semantics that allow programs written in the Java programming language to communicate with other messaging implementations. For more information on the JMS API, see [Oracle JMS documentation](http://docs.oracle.com/javaee/6/tutorial/doc/bncdr.html).

## Compatibility

| Connector version | Supported JMS version | Supported WSO2 ESB/EI version |
| ------------- | ------------- | ------------- |
| [1.0.1](https://github.com/wso2-extensions/esb-connector-jms/tree/org.wso2.carbon.connector.jms-1.0.1) | 1.1 | EI 6.5.0 |
| [1.0.0](https://github.com/wso2-extensions/esb-connector-jms/tree/org.wso2.carbon.connector.jms-1.0.0) | 1.1 | ESB 5.0.0, 4.9.0 |


## Getting started

#### Download and install the connector

1. Download the connector from the [WSO2 Store](https://store.wso2.com/store/assets/esbconnector/details/e57e41ab-7795-47f1-af4d-071fed5755da) by clicking the Download Connector button.
2. Then you can follow this [Documentation](https://docs.wso2.com/display/EI650/Working+with+Connectors+via+the+Management+Console) to add and enable the connector via the Management Console in your EI instance.
3. For more information on using connectors and their operations in your EI configurations, see [Using a Connector](https://docs.wso2.com/display/EI650/Using+a+Connector).
4. If you want to work with connectors via EI tooling, see [Working with Connectors via Tooling](https://docs.wso2.com/display/EI650/Working+with+Connectors+via+Tooling).

#### Configuring the connector operations

To get started with JMS connector and their operations, see [Configuring JMS Operations](docs/config.md).


## Building From the Source

Follow the steps given below to build the JMS connector from the source code:

1. Get a clone or download the source from [Github](https://github.com/wso2-extensions/esb-connector-jms).
2. Run the following Maven command from the `esb-connector-jms` directory: `mvn clean install`.
3. The JMS connector zip file is created in the `esb-connector-jms/target` directory

## How You Can Contribute

As an open source project, WSO2 extensions welcome contributions from the community.
Check the [issue tracker](https://github.com/wso2-extensions/esb-connector-jms/issues) for open issues that interest you. We look forward to receiving your contributions.