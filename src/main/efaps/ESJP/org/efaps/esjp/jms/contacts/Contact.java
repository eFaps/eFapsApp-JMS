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

package org.efaps.esjp.jms.contacts;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.esjp.jms.AbstractObject;
import org.efaps.esjp.jms.annotation.Type;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "contact")
@XmlType(name = "contacts.contact")
@Type(uuid = "b072573d-8a56-49aa-9bbd-ff14fceea2f6")
public class Contact
    extends AbstractObject
{

    @XmlElement(name = "name", required = true)
    private String name;

    @XmlElements({
                    @XmlElement(name = "client", type = ClassificationClient.class),
                    @XmlElement(name = "organisation", type = ClassificationOrganisation.class)
    })
    @XmlElementWrapper
    private final ArrayList<AbstractClassification> classifications = new ArrayList<AbstractClassification>();

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString())
                        .append("name", this.name).append("classifications", this.classifications)
                        .toString();
    }
}
