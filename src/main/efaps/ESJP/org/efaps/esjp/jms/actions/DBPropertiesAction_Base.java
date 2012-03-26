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

package org.efaps.esjp.jms.actions;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.esjp.jms.kernel.DBProperty;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "action.dbproperties_base")
public abstract class DBPropertiesAction_Base
    implements IAction, INoUserContextRequired
{

    @XmlElement(name = "dbproperty")
    @XmlElementWrapper(name = "dbproperties")
    private final ArrayList<DBProperty> properties = new ArrayList<DBProperty>();

    @Override
    public Object execute()
        throws EFapsException
    {
        for (final DBProperty property : this.properties) {
            property.setValue(DBProperties.getProperty(property.getKey(), property.getLanguage()));
        }
        return this;
    }
}
