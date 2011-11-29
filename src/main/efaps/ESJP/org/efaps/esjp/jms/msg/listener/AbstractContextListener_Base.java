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

import java.io.File;
import java.io.StringReader;

import javax.jms.BytesMessage;
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

import org.efaps.db.Context;
import org.efaps.esjp.jms.actions.DBPropertiesAction;
import org.efaps.esjp.jms.actions.INoUserContextRequired;
import org.efaps.esjp.jms.actions.Login;
import org.efaps.jms.JmsHandler;
import org.efaps.jms.JmsHandler.JmsDefinition;
import org.efaps.jms.JmsSession;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener which opens a Context on a receiving a Message and closes it after the execution of the subclasses.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractContextListener_Base
    implements MessageListener
{

    public final static String SESSIONKEY_PROPNAME = "SessionKey";

    public final static String FILENAME_PROPNAME = "FileName";

    public final static String ERRORCODE_PROPNAME = "ErrorCode";


    public enum ERROCODE
    {
        SESSIONTIMEOUT;
    }

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
                // open a context, because the classes are loaded from the eFaspClassLoader and this loader
                // needs a database connection
                if (!Context.isTMActive()) {
                    Context.begin(null, false);
                }
                final JAXBContext jc = JAXBContext.newInstance(getNoLoginClasses());
                final Unmarshaller unmarschaller = jc.createUnmarshaller();
                final Source source = new StreamSource(new StringReader(xml));
                final Object object = unmarschaller.unmarshal(source);
                if (object != null && object instanceof Login) {
                    final Login loginObject = (Login) object;
                    sessionKey = JmsSession.login(loginObject.getUserName(), loginObject.getPassword(),
                                    loginObject.getApplicationKey());
                    respond(_msg, sessionKey, null);
                } else if (object instanceof INoUserContextRequired) {
                    AbstractContextListener_Base.LOG.debug("Executing INoUserContextRequired: '{}'" , object);
                    final Object object2 = onSessionMessage(_msg);
                    respond(_msg, null, object2);
                }
                if (Context.isThreadActive()) {
                    AbstractContextListener_Base.LOG.debug("perfoming rollback");
                    Context.rollback();
                }
            } else {
                AbstractContextListener_Base.LOG.debug("Recieved SessionKey: '{}'" , sessionKey);
                final JmsSession session = JmsSession.getSession(sessionKey);
                if (session == null) {
                    respondError(_msg, ERROCODE.SESSIONTIMEOUT.name());
                } else {
                    session.openContext();
                    final Object object = onSessionMessage(_msg);
                    respond(_msg, null, object);
                    session.closeContext();
                }
            }
        } catch (final JMSException e) {
            AbstractContextListener_Base.LOG.error("JMSException", e);
        } catch (final EFapsException e) {
            AbstractContextListener_Base.LOG.error("EFapsException", e);
        } catch (final JAXBException e) {
            AbstractContextListener_Base.LOG.error("JAXBException", e);
        } finally {
            if (Context.isThreadActive()) {
                try {
                    Context.rollback();
                } catch (final EFapsException e) {
                    AbstractContextListener_Base.LOG.error("JAXBException", e);
                }
            }
        }
    }


    protected void respondError(final Message _msg,
                                final String _errorCode)
        throws JMSException
    {
        final Destination replyto = _msg.getJMSReplyTo();
        if (replyto instanceof  Queue) {
            final String name = ((Queue) replyto).getQueueName();
            final JmsDefinition def = JmsHandler.getJmsDefinition(name);
            final TextMessage msg = def.getSession().createTextMessage();
            msg.setJMSCorrelationID(_msg.getJMSCorrelationID());
            msg.setStringProperty(AbstractContextListener_Base.ERRORCODE_PROPNAME, _errorCode);
            def.getMessageProducer().send(msg);
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
                Message msg;
                if (_object != null && _object instanceof File) {
                    msg = def.getSession().createBytesMessage();
                    msg.setStringProperty(AbstractContextListener_Base.FILENAME_PROPNAME, ((File) _object).getName());
                    respondSessionBytestMessage((BytesMessage) msg, _object);
                } else {
                    msg = def.getSession().createTextMessage();
                    if (_sessionKey == null && _object != null) {
                        respondSessionTextMessage((TextMessage) msg, _object);
                    } else {
                        msg.setStringProperty(AbstractContextListener_Base.SESSIONKEY_PROPNAME, _sessionKey);
                    }
                }
                msg.setJMSCorrelationID(_msg.getJMSCorrelationID());
                def.getMessageProducer().send(msg);
            }
        }
    }

    protected Class<?>[] getNoLoginClasses()
    {
        return new Class[] { Login.class, DBPropertiesAction.class };
    }

    protected abstract Object onSessionMessage(final Message _msg);

    protected abstract void respondSessionTextMessage(final TextMessage _msg,
                                                      final Object _object)
        throws JMSException;

    protected abstract void respondSessionBytestMessage(final BytesMessage _msg,
                                                        final Object _object)
        throws JMSException;

}
