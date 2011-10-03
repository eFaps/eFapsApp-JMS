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


package org.efaps.esjp.jms.products;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.efaps.esjp.jms.AbstractObject;
import org.efaps.esjp.jms.annotation.Type;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Type(uuid="29c61210-5e44-453b-a171-95993c95da36")
@XmlType(name="product.abstractproduct")
public abstract class AbstractProduct
    extends AbstractObject
{
    @XmlElement(name="name")
    public String name;

    @XmlElement(name="description")
    private String description;

    @XmlElement(name="active")
    private boolean active;

    @XmlElement(name="dimension")
    private String dimension;
}
