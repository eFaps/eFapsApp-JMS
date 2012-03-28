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

import java.util.EnumSet;


/**
 * TODO comment!
 * @param <T> Type of value for the attribute
 * @author The eFaps Team
 * @version $Id$
 */
public interface IAttribute<T>
{
    /**
     * @return the value for this attribute
     */
    T getValue();

    /**
     * @param _value set the value for this attribute
     */
    void setValue(Object _value);

    /**
     * Add a setting to this attribute.
     *
     * @param _attrSettings settings for this Attribute
     * @return the Attribute itself
     */
    IAttribute<T> addSetting(final EnumSet<AttrSetting> _attrSettings);

    /**
     * Check if this attribute contains a given Setting.
     * @param _attrSetting the setting to check for
     * @return has this attribute the given setting
     */
    boolean hasSetting(final AttrSetting _attrSetting);

    /**
     * Check if this attribute contains the given Settings.
     * @param _attrSetting the settings to check for
     * @return has this attribute the given setting
     */
    boolean hasSettings(final EnumSet<AttrSetting> _attrSetting);

}
