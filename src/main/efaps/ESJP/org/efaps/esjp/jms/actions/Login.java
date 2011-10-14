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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "login")
@XmlType(name = "action.login")
public class Login
{

    @XmlElement(name = "username", required = true)
    private String userName;

    @XmlElement(name = "password", required = true)
    private String password;

    @XmlElement(name = "applicationkey", required = true)
    private String applicationKey;

    /**
     * Getter method for the instance variable {@link #userName}.
     *
     * @return value of instance variable {@link #userName}
     */
    public String getUserName()
    {
        return this.userName;
    }

    /**
     * Getter method for the instance variable {@link #password}.
     *
     * @return value of instance variable {@link #password}
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * Getter method for the instance variable {@link #applicationKey}.
     *
     * @return value of instance variable {@link #applicationKey}
     */
    public String getApplicationKey()
    {
        return this.applicationKey;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("userName", this.userName).append("password", this.password)
                        .append("applicationKey", this.applicationKey).toString();
    }
}
