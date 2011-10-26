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

package org.efaps.esjp.jms.msg.listener;

import java.io.StringReader;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.efaps.esjp.jms.actions.Login;
import org.efaps.jms.JmsHandler;
import org.efaps.jms.JmsHandler.JmsDefinition;
import org.efaps.jms.JmsSession;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener which opens a Context on a receiving a Message
 * and closes it after the execution of the subclasses.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractContextListener_Base
    implements MessageListener
{

    public final static String SESSIONKEY_PROPNAME = "SessionKey";

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractContextListener_Base.class);

    @Override
    public void onMessage(final Message _msg)
    {
        try {
            String sessionKey = _msg.getStringProperty(AbstractContextListener_Base.SESSIONKEY_PROPNAME);
            if (sessionKey == null) {
                final TextMessage msg = (TextMessage) _msg;
                final String xml = msg.getText();
                final JAXBContext jc = JAXBContext.newInstance(Login.class);
                final Unmarshaller unmarschaller = jc.createUnmarshaller();
                final Source source = new StreamSource(new StringReader(xml));
                final Login loginObject = (Login) unmarschaller.unmarshal(source);
                if (loginObject != null) {
                    sessionKey = JmsSession.login(loginObject.getUserName(), loginObject.getPassword(),
                                    loginObject.getApplicationKey());
                    respond(_msg, sessionKey, null);
                }
            } else {
                final JmsSession session = JmsSession.getSession(sessionKey);
                session.openContext();
                final Object object = onSessionMessage(_msg);
                respond(_msg, null, object);
                session.closeContext();
            }
        } catch (final JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void respond(final Message _msg,
                           final String _sessionKey,
                           final Object _object)
        throws JMSException
    {
        final Destination replyto = _msg.getJMSReplyTo();
        if (replyto instanceof  Queue) {
            final String name = ((Queue) replyto).getQueueName();
            final JmsDefinition def = JmsHandler.getJmsDefinition(name);
            if (def != null) {
                final TextMessage msg = def.getSession().createTextMessage();
                msg.setJMSCorrelationID(_msg.getJMSCorrelationID());
                if (_sessionKey == null && _object != null) {
                    respondSessionMessage(msg, _object);
                } else {
                    msg.setStringProperty(AbstractContextListener_Base.SESSIONKEY_PROPNAME, _sessionKey);
                }
                def.getMessageProducer().send(msg);
            }
        }
    }


    protected abstract Object onSessionMessage(final Message _msg);

    protected abstract void respondSessionMessage(final TextMessage _msg, final Object _object) throws JMSException;
}
