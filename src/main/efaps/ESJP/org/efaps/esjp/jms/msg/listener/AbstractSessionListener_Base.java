/*
 * Copyright 2003 - 2015 The eFaps Team
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
 */

package org.efaps.esjp.jms.msg.listener;

import java.io.File;
import java.io.StringReader;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.efaps.admin.program.esjp.EFapsApplication;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.jms.actions.INoUserContextRequired;
import org.efaps.esjp.jms.actions.Login;
import org.efaps.jms.JmsHandler;
import org.efaps.jms.JmsHandler.JmsDefinition;
import org.efaps.jms.JmsSession;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener which work with a user Session and opens a Context
 * on a receiving a Message and closes it after the execution of
 * the subclasses.
 *
 * @author The eFaps Team
 */
@EFapsUUID("7e3f555b-a293-47d7-be15-2f4fc80eecbb")
@EFapsApplication("eFapsApp-JMS")
public abstract class AbstractSessionListener_Base
    extends AbstractListener
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
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSessionListener_Base.class);

    @Override
    public void onMessage(final Message _msg)
    {
        Object retObject = null;
        String sessionKey = null;
        String errorMsg = null;
        try {
            AbstractSessionListener_Base.LOG.debug("Recieved Message: {}", _msg.getJMSMessageID());
            sessionKey = _msg.getStringProperty(AbstractSessionListener_Base.SESSIONKEY_PROPNAME);
            try {
                if (sessionKey == null) {
                    final TextMessage msg = (TextMessage) _msg;
                    final String xml = msg.getText();
                    // open a context, because the classes are loaded from the eFaspClassLoader and this loader
                    // needs a database connection
                    if (!Context.isTMActive()) {
                        Context.begin(null, Context.Inheritance.Local);
                    }
                    // to ensure that the inner class is loaded
                    ERROCODE.SESSIONTIMEOUT.name();
                    final JAXBContext jc = getJAXBContext();
                    final Unmarshaller unmarschaller = jc.createUnmarshaller();
                    final Source source = new StreamSource(new StringReader(xml));
                    final Object reqObject = unmarschaller.unmarshal(source);
                    if (reqObject != null && reqObject instanceof Login) {
                        final Login loginObject = (Login) reqObject;
                        sessionKey = JmsSession.login(loginObject.getUserName(), loginObject.getPassword(),
                                        loginObject.getApplicationKey());
                    } else if (reqObject instanceof INoUserContextRequired) {
                        AbstractSessionListener_Base.LOG.debug("Executing INoUserContextRequired: '{}'", reqObject);
                        retObject = onSessionMessage(_msg);
                    }
                    if (Context.isThreadActive() && Context.isTMActive()) {
                        AbstractSessionListener_Base.LOG.debug("perfoming rollback");
                        Context.rollback();
                    }
                } else {
                    AbstractSessionListener_Base.LOG.debug("Recieved SessionKey: '{}'", sessionKey);
                    final JmsSession session = JmsSession.getSession(sessionKey);
                    if (session == null) {
                        errorMsg = ERROCODE.SESSIONTIMEOUT.name();
                    } else {
                        session.openContext();
                        retObject = onSessionMessage(_msg);
                        session.closeContext();
                    }
                    // the session key will not be returned
                    sessionKey = null;
                }

            } catch (final JMSException e) {
                AbstractSessionListener_Base.LOG.error("JMSException", e);
                errorMsg = "error";
            } catch (final EFapsException e) {
                AbstractSessionListener_Base.LOG.error("EFapsException", e);
                errorMsg = "error";
            } catch (final JAXBException e) {
                AbstractSessionListener_Base.LOG.error("JAXBException", e);
                errorMsg = "error";
            } finally {
                try {
                    if (Context.isThreadActive() && !Context.isTMNoTransaction()) {
                        Context.rollback();
                    }
                } catch (final EFapsException e) {
                    AbstractSessionListener_Base.LOG.error("EFapsException", e);
                }
                try {
                    AbstractSessionListener_Base.LOG.debug("finished with Message: {}", _msg.getJMSMessageID());
                } catch (final JMSException e) {
                    AbstractSessionListener_Base.LOG.error("JMSException", e);
                }
            }
        } catch (final Throwable e) {
            AbstractSessionListener_Base.LOG.error("Unexpected exception!!!!!!!!", e);
            errorMsg = "unexpected exception";
        }

        if (errorMsg == null) {
            respond(_msg, sessionKey, retObject);
        } else {
            respondError(_msg, errorMsg);
        }
        AbstractSessionListener_Base.LOG.debug(
                        "responded for Messgage with Sessionkey '{}' and errorMsg '{}' on Msg {}", new Object[] {
                                        sessionKey, errorMsg, _msg });
    }

    protected void respondError(final Message _msg,
                                final String _errorCode)
    {
        try {
            final Destination replyto = _msg.getJMSReplyTo();
            if (replyto instanceof Queue) {
                final String name = ((Queue) replyto).getQueueName();
                final JmsDefinition def = JmsHandler.getJmsDefinition(name);
                final TextMessage msg = def.getSession().createTextMessage();
                msg.setJMSCorrelationID(_msg.getJMSCorrelationID());
                msg.setStringProperty(AbstractSessionListener_Base.ERRORCODE_PROPNAME, _errorCode);
                def.getMessageProducer().send(msg);
            }
        } catch (final JMSException e) {
            AbstractSessionListener_Base.LOG.error("JMSException", e);
        }
    }

    protected void respond(final Message _msg,
                           final String _sessionKey,
                           final Object _object)

    {
        try {
            final Destination replyto = _msg.getJMSReplyTo();
            if (replyto instanceof Queue) {
                final String name = ((Queue) replyto).getQueueName();
                final JmsDefinition def = JmsHandler.getJmsDefinition(name);
                if (def != null) {
                    Message msg;
                    if (_object != null && _object instanceof File) {
                        msg = def.getSession().createBytesMessage();
                        msg.setStringProperty(AbstractSessionListener_Base.FILENAME_PROPNAME,
                                        ((File) _object).getName());
                        respondSessionBytestMessage((BytesMessage) msg, _object);
                    } else {
                        msg = def.getSession().createTextMessage();
                        if (_sessionKey == null && _object != null) {
                            respondSessionTextMessage((TextMessage) msg, _object);
                        } else {
                            msg.setStringProperty(AbstractSessionListener_Base.SESSIONKEY_PROPNAME, _sessionKey);
                        }
                    }
                    msg.setJMSCorrelationID(_msg.getJMSCorrelationID());
                    AbstractSessionListener_Base.LOG.debug("Using CorrelationID '{}' for sending Message: {}",
                                    msg.getJMSCorrelationID(), msg);
                    def.getMessageProducer().send(msg);
                }
            }
        } catch (final Throwable e) {
            AbstractSessionListener_Base.LOG.error("Throwable", e);
            respondError(_msg, "Throwable");
        }
    }



    protected abstract Object onSessionMessage(final Message _msg);

    protected abstract void respondSessionTextMessage(final TextMessage _msg,
                                                      final Object _object)
        throws JMSException;

    protected abstract void respondSessionBytestMessage(final BytesMessage _msg,
                                                        final Object _object)
        throws JMSException;

}
