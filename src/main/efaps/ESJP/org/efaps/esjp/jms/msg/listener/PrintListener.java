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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.efaps.admin.common.Jms;
import org.efaps.admin.common.Jms.JmsDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PrintListener
    implements MessageListener
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PrintListener.class);

    /*
     * (non-Javadoc)
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(final Message _msg)
    {
        PrintListener.LOG.info(_msg.toString());

        try {
            final String correlationId = _msg.getJMSCorrelationID();
            PrintListener.LOG.info(correlationId);
            final Destination replyto = _msg.getJMSReplyTo();
            if (replyto instanceof  Queue) {
                final String name = ((Queue) replyto).getQueueName();
                final JmsDefinition def = Jms.getJmsDefinition(name);
                if (def != null) {
                    final TextMessage msg = def.getSession().createTextMessage();
                    msg.setJMSCorrelationID(correlationId);
                    msg.setText("responce");
                    def.getMessageProducer().send(msg);
                }
            }
        } catch (final JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
