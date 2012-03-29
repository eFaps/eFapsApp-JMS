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

import javax.jms.MessageListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.jms.JmsResourceConfig;

/**
 * Basic Class for all Listeners.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("8422a9cf-67b8-4165-b4b2-33183f8e4e0d")
@EFapsRevision("$Rev$")
public abstract class AbstractListener_Base
    implements MessageListener
{

    /**
     * Get the JAXBContext used in this Listener.
     *
     * @return an JAXBContext
     * @throws JAXBException on error
     */
    protected JAXBContext getJAXBContext()
        throws JAXBException
    {
        final JAXBContext context = JAXBContext.newInstance(getClasses());
        return context;
    }

    protected Class<?>[] getClasses()
    {
        return JmsResourceConfig.getResourceConfig().getClasses()
                        .toArray(new Class<?>[JmsResourceConfig.getResourceConfig().getClasses().size()]);
    }
}
