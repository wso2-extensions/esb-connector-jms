## JMS ESB Integration Connector

Pre-requisites:

 - Maven 3.x
 - Java 1.7
 
 Tested Platform:
  - UBUNTU 14.04
  - WSO2 ESB 4.9.0
  - ActiveMQ 5.14.5
  
  
1. Copy the following client libraries from the <AMQ_HOME>/lib directory to the <ESB_HOME>/repository/components/lib directory.   

   activemq-broker-5.8.0.jar
   activemq-client-5.8.0.jar
   activemq-kahadb-store-5.8.0.jar 
   geronimo-jms_1.1_spec-1.1.1.jar
   geronimo-j2ee-management_1.1_spec-1.0.1.jar
   geronimo-jta_1.0.1B_spec-1.0.1.jar
   hawtbuf-1.9.jar
   Slf4j-api-1.6.6.jar
   activeio-core-3.1.4.jar (available in <AMQ_HOME>/lib/optional folder)  
 
2. Copy init_jms.xml file from esb-connector-jms/src/test/resources/ and paste it into 
     <ESB_HOME>/repository/deployment/server/synapse-configs/default/local-entries/jms_init.xml

3. Start the ActiveMQ

4. Make sure the ESB 5.0.0 zip file available at "esb-connector-jms/repository/"

5. Go to "esb-connector-jms/" and type "mvn clean install" to test and build
