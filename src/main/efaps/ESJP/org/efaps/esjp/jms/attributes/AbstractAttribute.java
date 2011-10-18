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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "attribute.abstract")
public abstract class AbstractAttribute<T>
    implements IAttribute<T>
{
    @XmlAttribute
    private boolean print;

    /**
     * Getter method for the instance variable {@link #print}.
     *
     * @return value of instance variable {@link #print}
     */
    public boolean isPrint()
    {
        return this.print;
    }

    /**
     * Setter method for instance variable {@link #print}.
     *
     * @param _print value for instance variable {@link #print}
     */
    public void setPrint(final boolean _print)
    {
        this.print = _print;
    }
}
