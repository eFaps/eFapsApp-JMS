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
@XmlType(name = "attribute.linkobject")
public class LinkObject
{
    /**
     * Syncid of this Object. Only used for syncing.
     */
    @XmlAttribute(name = "syncid")
    private String syncid;

    /**
     * OID of this Object.
     */
    @XmlAttribute(name = "oid")
    private String oid;

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
    public LinkObject setSyncid(final String _syncid)
    {
        this.syncid = _syncid;
        return this;
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
    public LinkObject setOid(final String _oid)
    {
        this.oid = _oid;
        return this;
    }
}
