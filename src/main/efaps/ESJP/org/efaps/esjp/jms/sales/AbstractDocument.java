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


package org.efaps.esjp.jms.sales;

import javax.xml.bind.annotation.XmlType;

import org.efaps.esjp.jms.annotation.Type;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlType(name = "sales.abstractdocument")
@Type(uuid="c2615e57-c990-4572-8dfd-eda7f8f76e4d")
public abstract class AbstractDocument
    extends org.efaps.esjp.jms.commons.AbstractDocument
{

}
