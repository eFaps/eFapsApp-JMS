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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import org.efaps.esjp.jms.AbstractObject;
import org.efaps.esjp.jms.contacts.Contact;
import org.efaps.esjp.jms.products.StandartProduct;
import org.efaps.esjp.jms.sales.Invoice;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlType(name="action.abstract")
public abstract class AbstractAction
{
    @XmlElements({
        @XmlElement(name="invoice", type = Invoice.class),
        @XmlElement(name="contact", type = Contact.class),
        @XmlElement(name="standartproduct", type = StandartProduct.class)
    })
    @XmlElementWrapper
    private final ArrayList<AbstractObject> objects = new ArrayList<AbstractObject>();


    /**
     * Execute the action.
     * @throws EFapsException on error
     */
    public abstract void execute() throws EFapsException;

    /**
     * Getter method for the instance variable {@link #objects}.
     *
     * @return value of instance variable {@link #objects}
     */
    protected ArrayList<AbstractObject> getObjects()
    {
        return this.objects;
    }
}
