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

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.efaps.admin.datamodel.Status;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "attribute.status")
public class StatusAttribute
    extends AbstractAttribute<Long>
{
    /**
     * The actual value.
     */
    @XmlAttribute
    private Long value;

    /**
     * The key to the Status.
     */
    @XmlElement
    private String statusKey;


    /**
     * The UUID of the Status Type.
     */
    @XmlElement
    private UUID statusUUID;

    /**
     * Standard Constructor.
     */
    public StatusAttribute()
    {
        super();
    }

    /**
     * Constructor setting the value.
     * @param _value value for this attribute
     */
    public StatusAttribute(final Long _value)
    {
        super();
        this.value = _value;
    }

    /**
     * Constructor setting the value.
     * @param _statusUUID UUID of the Status Type
     * @param _statusKey  key of the Status
     */
    public StatusAttribute(final UUID _statusUUID,
                           final String _statusKey)
    {
        super();
        this.value = Long.valueOf(0);
        this.statusUUID = _statusUUID;
        this.statusKey = _statusKey;
    }

    /**
     * Getter method for the instance variable {@link #statusKey}.
     *
     * @return value of instance variable {@link #statusKey}
     */
    public String getStatusKey()
    {
        return this.statusKey;
    }

    /**
     * Setter method for instance variable {@link #statusKey}.
     *
     * @param _statusKey value for instance variable {@link #statusKey}
     */
    public void setStatusKey(final String _statusKey)
    {
        this.statusKey = _statusKey;
    }

    /**
     * Getter method for the instance variable {@link #statusUUID}.
     *
     * @return value of instance variable {@link #statusUUID}
     */
    public UUID getStatusUUID()
    {
        return this.statusUUID;
    }

    /**
     * Setter method for instance variable {@link #statusUUID}.
     *
     * @param _statusUUID value for instance variable {@link #statusUUID}
     */

    public void setStatusUUID(final UUID _statusUUID)
    {
        this.statusUUID = _statusUUID;
    }

    /**
     * Getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public Long getValue()
    {
        Long ret = this.value;
        if (ret == 0) {
            Status status = null;
            try {
                status = Status.find(this.statusUUID, this.statusKey);
            } catch (final CacheReloadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (status != null) {
                ret = status.getId();
            }
        }
        return ret;
    }

    /**
     * Setter method for instance variable {@link #value}.
     *
     * @param _value value for instance variable {@link #value}
     */
    public void setValue(final Object _value)
    {
        this.value = (Long) _value;
    }
}
