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

package org.efaps.esjp.jms.commons;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.efaps.esjp.jms.AbstractObject;
import org.efaps.esjp.jms.annotation.Type;
import org.efaps.esjp.jms.contacts.Contact;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlType(name="commons.abstractdocument")
@Type(uuid="ad1df372-bf47-49d2-af3e-a1c4065f062d")
public abstract class AbstractDocument
    extends AbstractObject
{

    @XmlElement(name = "date")
    private Date date;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "duedate")
    private Date dueDate;

    @XmlElement(name = "contact")
    private Contact contact;

    @XmlElement(name = "note")
    private String note;
}
