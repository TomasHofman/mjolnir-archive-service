# mjolnir-archive-service

Service for removing user memberships in GitHub organizations and archiving repositories of those users.

The application connects to a messaging queue, where it waits for notifications about users who should be removed from managed GitHub organizations.
Once the notification is received, repository forks belonging to given user are archived and his membership to the organization is cancelled.

## Building

Build a WAR with maven:

```
mvn clean package
```

WAR will be created in `webapp/target/` directory.

## Database preparation

Initialize your database with SQL scripts in `dbscripts/` directory:

* `dbscripts/create.sql`
* `dbscripts/load.sql`

## Deployment

You can deploy the app in any Java EE 8 / Jakarta EE 8 compliant application server. Configuration samples provided here are for JBoss EAP / Wildfly.

Start with default `standalone-full.xml`.

Configure a datasource with JNDI name "java:jboss/datasources/mjolnir/MjolnirDS". Example for a local H2 database:

```
                <datasource jndi-name="java:jboss/datasources/mjolnir/MjolnirDS" pool-name="MjolnirDS">
                    <connection-url>jdbc:h2:tcp://localhost/~/mjolnir-db</connection-url>
                    <driver>h2</driver>
                    <security>
                        <user-name>sa</user-name>
                        <password></password>
                    </security>
                </datasource>
```

Configure a resource adapter for connecting to the UMB messaging service:

```xml
    ...
    </extensions>
    <system-properties>
        <property name="UMBServerUrl" value="failover:(ssl://server01:61616,ssl://server02:61616)?jms.rmIdFromConnectionId=true&amp;maxReconnectAttempts=0&amp;reconnectSupported=false&amp;updateURIsSupported=false&amp;priorityBackup=false"/>
        <property name="UMBQueueName" value="QueueName"/>
        <property name="UMBKeyStore" value="/etc/eap-secret-volume/umb.keystore"/>
        <property name="UMBKeyStorePassword" value=""/>
        <property name="UMBTrustStore" value="/etc/eap-secret-volume/umb.truststore"/>
        <property name="UMBTrustStorePassword" value=""/>
    </system-properties>
    <management>
    ...
```

```xml
        <subsystem xmlns="urn:jboss:domain:resource-adapters:5.0">
            <resource-adapters>
                <resource-adapter id="activemq-rar.rar">
                    <archive>
                        activemq-rar.rar
                    </archive>
                    <transaction-support>XATransaction</transaction-support>
                    <config-property name="ServerUrl">
                        ${UMBServerUrl}
                    </config-property>
                    <config-property name="KeyStore">
                        ${UMBKeyStore}
                    </config-property>
                    <config-property name="KeyStorePassword">
                        ${UMBKeyStorePassword}
                    </config-property>
                    <config-property name="TrustStore">
                        ${UMBTrustStore}
                    </config-property>
                    <config-property name="TrustStorePassword">
                        ${UMBTrustStorePassword}
                    </config-property>
                    <config-property name="UseInboundSession">
                        true
                    </config-property>
                    <connection-definitions>
                        <connection-definition class-name="org.apache.activemq.ra.ActiveMQManagedConnectionFactory" jndi-name="java:/AMQConnectionFactory" enabled="true" tracking="false" pool-name="AMQConnectionFactory">
                            <config-property name="KeyStore">
                                ${UMBKeyStore}
                            </config-property>
                            <config-property name="KeyStorePassword">
                                ${UMBKeyStorePassword}
                            </config-property>
                            <config-property name="TrustStore">
                                ${UMBTrustStore}
                            </config-property>
                            <config-property name="TrustStorePassword">
                                ${UMBTrustStorePassword}
                            </config-property>
                            <config-property name="UseInboundSession">
                                true
                            </config-property>
                            <xa-pool>
                                <min-pool-size>1</min-pool-size>
                                <max-pool-size>20</max-pool-size>
                                <prefill>false</prefill>
                                <is-same-rm-override>false</is-same-rm-override>
                            </xa-pool>
                            <recovery no-recovery="true"/>
                        </connection-definition>
                    </connection-definitions>
                    <admin-objects>
                        <admin-object class-name="org.apache.activemq.command.ActiveMQQueue" jndi-name="java:/queue/EmployeeEventsQueue" use-java-context="true" pool-name="EmployeeEventsQueue">
                            <config-property name="PhysicalName">
                                ${UMBQueueName}
                            </config-property>
                        </admin-object>
                    </admin-objects>
                </resource-adapter>
            </resource-adapters>
        </subsystem>
```

## Triggering Tasks

The application exposes following endpoints which can trigger various tasks on request:

* `/archive-users` - starts the job that archives repositories and removes membership of previously discovered offboarded users.
* `/ldap-scan` - performs discovery of offboarded users by querying the company LDAP database.
* `/generate-email-report` - generates an email report listing user removals performed during the last week, overview of current users, etc.
* `/generate-test-email` - generates a test email to verify smtp setting.



