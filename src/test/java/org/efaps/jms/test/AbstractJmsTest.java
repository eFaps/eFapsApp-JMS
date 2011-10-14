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
import java.util.HashSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.esjp.jms.msg.listener.AbstractContextListener;
import org.efaps.esjp.jms.msg.listener.ActionListener;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.jms.JmsHandler;
import org.efaps.jms.JmsHandler.JmsDefinition;
import org.efaps.test.AbstractTest;
import org.efaps.util.EFapsException;
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
import org.testng.annotations.Test;

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
    private String requestQueueBinding;
    private String respondQueueBinding;

    private EmbeddedJMS jmsServer;
    private Connection connection;

    private MessageProducer requestProducer;
    private MessageProducer respondProducer;
    private String sessionKey;


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
        this.requestQueueBinding = "/queue/resquestQueue";
        this.respondQueueBinding = "/queue/respondQueue";

        final JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl("queue1", null, false,
                        this.requestQueueBinding);
        jmsConfig.getQueueConfigurations().add(queueConfig);
        final JMSQueueConfiguration queueConfig2 = new JMSQueueConfigurationImpl("queue2", null, false,
                        this.respondQueueBinding);
        jmsConfig.getQueueConfigurations().add(queueConfig2);

        // Step 5. Start the JMS Server using the HornetQ core server and the JMS configuration
        this.jmsServer = new EmbeddedJMS();
        this.jmsServer.setConfiguration(configuration);
        this.jmsServer.setJmsConfiguration(jmsConfig);
        this.jmsServer.start();
        System.out.println("Started Embedded JMS Server");

        final ConnectionFactory cf = (ConnectionFactory) this.jmsServer.lookup("/cf");
        this.connection = cf.createConnection();
        this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        this.connection.start();

        // register efaps to recieve request
        final MessageConsumer messageConsumer = getSession().createConsumer(getRequestQueue());
        messageConsumer.setMessageListener(new ActionListener());
        // register a resond in efaps
        JmsHandler.addJmsDefintion(new JmsDefinition(getRespondQueue().getQueueName(), getRespondProducer(),
                        this.session));
    }

    @BeforeTest(description = "connects to the eFaps database", groups = "connect")
    public void initRunlevel()
        throws EFapsException
    {
        Context.begin();
        RunLevel.init("shell");
        RunLevel.execute();
        Context.rollback();
        AppAccessHandler.init("jmsTest", new HashSet<String>());
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

    @Test(dataProvider = "login", dataProviderClass = JmsDataProvider.class)
    public void login(final String _xml)
        throws JMSException
    {
               // server sends request
        final MessageProducer producer = getRequestProducer();
        final TextMessage msg = getSession().createTextMessage();
        msg.setJMSReplyTo(getRespondQueue());
        msg.setText(_xml);
        producer.send(msg);

        final MessageConsumer messageConsumer = getSession().createConsumer(getRespondQueue());
        final Message respondMsg = messageConsumer.receive();

        AbstractJmsTest.this.sessionKey = respondMsg.getStringProperty(AbstractContextListener.SESSIONKEY_PROPNAME);
        messageConsumer.close();
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
     * Getter method for the instance variable {@link #requestQueueBinding}.
     *
     * @return value of instance variable {@link #requestQueueBinding}
     */
    public String getRequestQueueBinding()
    {
        return this.requestQueueBinding;
    }

    public MessageProducer getRequestProducer()
        throws JMSException
    {
        if (this.requestProducer == null) {
            this.requestProducer = getSession().createProducer(getRequestQueue());
        }
        return this.requestProducer;
    }

    public Queue getRequestQueue()
    {
        return (Queue) getJmsServer().lookup(getRequestQueueBinding());
    }

    public MessageProducer getRespondProducer()
        throws JMSException
    {
        if (this.respondProducer == null) {
            this.respondProducer = getSession().createProducer(getRespondQueue());
        }
        return this.respondProducer;
    }

    public Queue getRespondQueue()
    {
        return (Queue) getJmsServer().lookup(getRespondQueueBinding());
    }

    /**
     * Getter method for the instance variable {@link #respondQueueBinding}.
     *
     * @return value of instance variable {@link #respondQueueBinding}
     */
    public String getRespondQueueBinding()
    {
        return this.respondQueueBinding;
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


    /**
     * Getter method for the instance variable {@link #sessionKey}.
     *
     * @return value of instance variable {@link #sessionKey}
     */
    protected String getSessionKey()
    {
        return this.sessionKey;
    }


    /**
     * Setter method for instance variable {@link #sessionKey}.
     *
     * @param _sessionKey value for instance variable {@link #sessionKey}
     */

    protected void setSessionKey(final String _sessionKey)
    {
        this.sessionKey = _sessionKey;
    }

}
