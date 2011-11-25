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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PrintListener
    extends  AbstractContextListener_Base
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object onSessionMessage(final Message _msg)
    {
        AbstractContextListener_Base.LOG.info(_msg.toString());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void respondSessionTextMessage(final TextMessage _msg,
                                             final Object _object)
        throws JMSException
    {
        _msg.setText("respond");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void respondSessionBytestMessage(final BytesMessage _msg,
                                               final Object _object)
        throws JMSException
    {

    }
}
