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

package org.efaps.esjp.jms.attributes;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * TODO comment!
 * @param <T> Object type
 * @author The eFaps Team
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "attribute.abstract")
public abstract class AbstractAttribute<T>
    implements IAttribute<T>
{

    /**
     * The set of restrictions for this Attribute.
     */
    @XmlElement
    private final Set<AttrSetting> attrSettings = new HashSet<AttrSetting>();

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractAttribute<T> addSetting(final EnumSet<AttrSetting> _attrSettings)
    {
        this.attrSettings.addAll(_attrSettings);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSetting(final AttrSetting _attrSetting)
    {
        return this.attrSettings.contains(_attrSetting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSettings(final EnumSet<AttrSetting> _attrSettings)
    {
        return this.attrSettings.contains(_attrSettings);
    }
}

