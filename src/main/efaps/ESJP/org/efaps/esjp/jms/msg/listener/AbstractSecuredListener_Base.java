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

import javax.jms.JMSException;
import javax.jms.Message;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("b343b42a-5c6c-493c-83ae-b18d36376e91")
@EFapsRevision("$Rev$")
public abstract class AbstractSecuredListener_Base
    extends AbstractListener
{

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSecuredListener_Base.class);

    @Override
    public void onMessage(final Message _msg)
    {
        try {
            AbstractSecuredListener_Base.LOG.debug("Recieved Message: {}", _msg.getJMSMessageID());
            try {
                Context.begin(getUserName(), false);
                onContextMessage(_msg);
                Context.commit();
            } catch (final EFapsException e) {
                AbstractSecuredListener_Base.LOG.error("EFapsException", e);
            } finally {
                try {
                    if (Context.isThreadActive() && !Context.isTMNoTransaction()) {
                        Context.rollback();
                    }
                } catch (final EFapsException e) {
                    AbstractSecuredListener_Base.LOG.error("EFapsException", e);
                }
                try {
                    AbstractSecuredListener_Base.LOG.debug("finished with Message: {}", _msg.getJMSMessageID());
                } catch (final JMSException e) {
                    AbstractSecuredListener_Base.LOG.error("JMSException", e);
                }
            }
        } catch (final Throwable e) {
            AbstractSecuredListener_Base.LOG.error("Unexpected exception!!!!!!!!", e);

        }
    }

    public abstract String getUserName();

    public abstract Object onContextMessage(Message _msg);
}
