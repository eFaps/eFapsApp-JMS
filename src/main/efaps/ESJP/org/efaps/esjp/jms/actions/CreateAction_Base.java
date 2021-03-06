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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.efaps.db.Insert;
import org.efaps.esjp.jms.AbstractObject;
import org.efaps.esjp.jms.annotation.Attribute;
import org.efaps.esjp.jms.annotation.MethodType;
import org.efaps.esjp.jms.annotation.Type;
import org.efaps.esjp.jms.attributes.IAttribute;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "action.create_base")
public abstract class CreateAction_Base
    extends AbstractAction
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute()
        throws EFapsException
    {
        for (final AbstractObject object : getObjects()) {
            final Type typeAnno = object.getClass().getAnnotation(Type.class);
            if (typeAnno != null) {
                final Insert insert = new Insert(UUID.fromString(typeAnno.uuid()));
                final Method[] methods = object.getClass().getDeclaredMethods();
                for (final Method method : methods) {
                    if (method.isAnnotationPresent(Attribute.class)) {
                        final Attribute attributeAnno = method.getAnnotation(Attribute.class);
                        if (attributeAnno != null && attributeAnno.method().equals(MethodType.GETTER)) {
                            try {
                                final IAttribute<?> value = (IAttribute<?>) method.invoke(object);
                                insert.add(attributeAnno.name(), value.getValue());
                            } catch (final IllegalArgumentException e) {
                                throw new EFapsException("IllegalArgumentException", e);
                            } catch (final IllegalAccessException e) {
                                throw new EFapsException("IllegalAccessException", e);
                            } catch (final InvocationTargetException e) {
                                throw new EFapsException("InvocationTargetException", e);
                            }
                        }
                    }
                }
                insert.execute();
                object.setOid(insert.getInstance().getOid());
            }
        }
        return this;
    }
}
