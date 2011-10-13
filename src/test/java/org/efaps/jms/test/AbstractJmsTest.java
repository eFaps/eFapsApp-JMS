/*
 * Copyright 2003 - 2011 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.jms.test;

import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.efaps.test.AbstractTest;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.JMSQueueConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AbstractJmsTest
    extends AbstractTest
{

    private Session session;
    private String queueName;

    private EmbeddedJMS jmsServer;
    private Connection connection;

    @BeforeTest(description = "connects to the eFaps database", groups = "connect")
    public void startJms()
        throws Exception

    {
        // Step 1. Create HornetQ core configuration, and set the properties accordingly
        final Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        configuration.getAcceptorConfigurations()
                        .add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));

        final TransportConfiguration connectorConfig = new TransportConfiguration(NettyConnectorFactory.class.getName());

        configuration.getConnectorConfigurations().put("connector", connectorConfig);

        // Step 2. Create the JMS configuration
        final JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        // Step 3. Configure the JMS ConnectionFactory
        final ArrayList<String> connectorNames = new ArrayList<String>();
        connectorNames.add("connector");
        final ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl("cf", false,
                        connectorNames, "/cf");
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

        // Step 4. Configure the JMS Queue
        this.queueName = "/queue/queue1";

        final JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl("queue1", null, false, this.queueName);
        jmsConfig.getQueueConfigurations().add(queueConfig);

        // Step 5. Start the JMS Server using the HornetQ core server and the JMS configuration
        this.jmsServer = new EmbeddedJMS();
        this.jmsServer.setConfiguration(configuration);
        this.jmsServer.setJmsConfiguration(jmsConfig);
        this.jmsServer.start();
        System.out.println("Started Embedded JMS Server");

        final ConnectionFactory cf = (ConnectionFactory) this.jmsServer.lookup("/cf");
        this.jmsServer.lookup(this.queueName);
        this.connection = cf.createConnection();
        this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.connection.start();
    }

    @AfterTest(dependsOnGroups = "cleanup")
    public void stopJms()
        throws Exception
    {
        if (this.connection != null) {
            this.connection.close();
        }
        this.jmsServer.stop();
    }

    /**
     * Getter method for the instance variable {@link #session}.
     *
     * @return value of instance variable {@link #session}
     */
    public Session getSession()
    {
        return this.session;
    }

    /**
     * Getter method for the instance variable {@link #queueName}.
     *
     * @return value of instance variable {@link #queueName}
     */
    public String getQueueName()
    {
        return this.queueName;
    }

    /**
     * Getter method for the instance variable {@link #jmsServer}.
     *
     * @return value of instance variable {@link #jmsServer}
     */
    public EmbeddedJMS getJmsServer()
    {
        return this.jmsServer;
    }

}
