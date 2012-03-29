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

package org.efaps.esjp.jms;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlRootElement(name = "abstractobject")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractObject
{
    /**
     * OID of this Object.
     */
    @XmlAttribute(name = "oid")
    private String oid;

    /**
     * Syncid of this Object. Only used for syncing.
     */
    @XmlAttribute(name = "syncid")
    private String syncid;

    /**
     * The objects classifying this object.
     */
    @XmlElementWrapper(name = "classifications")
    @XmlElementRef
    private final List<AbstractClassificationObject> classifications = new ArrayList<AbstractClassificationObject>();

    /**
     * Getter method for the instance variable {@link #classifications}.
     *
     * @return value of instance variable {@link #classifications}
     */
    public List<AbstractClassificationObject> getClassifications()
    {
        return this.classifications;
    }

    /**
     * Getter method for the instance variable {@link #syncid}.
     *
     * @return value of instance variable {@link #syncid}
     */
    public String getSyncid()
    {
        return this.syncid;
    }

    /**
     * Setter method for instance variable {@link #syncid}.
     *
     * @param _syncid value for instance variable {@link #syncid}
     */
    public void setSyncid(final String _syncid)
    {
        this.syncid = _syncid;
    }

    /**
     * Getter method for the instance variable {@link #oid}.
     *
     * @return value of instance variable {@link #oid}
     */
    public String getOid()
    {
        return this.oid;
    }

    /**
     * Setter method for instance variable {@link #oid}.
     *
     * @param _oid value for instance variable {@link #oid}
     */
    public void setOid(final String _oid)
    {
        this.oid = _oid;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                        .append("oid", this.oid)
                        .append("syncid", this.syncid)
                        .toString();
    }
}
