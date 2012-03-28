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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.jms.AbstractObject;
import org.efaps.esjp.jms.annotation.Attribute;
import org.efaps.esjp.jms.annotation.MethodType;
import org.efaps.esjp.jms.annotation.Type;
import org.efaps.esjp.jms.attributes.AttrSetting;
import org.efaps.esjp.jms.attributes.IAttribute;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "action.sync_base")
@EFapsUUID("5d32e03e-7649-45b6-9d0e-696199b3fd52")
@EFapsRevision("$Rev$")
public abstract class SyncAction_Base
    extends AbstractAction
    implements INoUserContextRequired
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
                final Map<Attribute, IAttribute<?>> attributes = new HashMap<Attribute, IAttribute<?>>();

                final Method[] methods = object.getClass().getDeclaredMethods();
                for (final Method method : methods) {
                    if (method.isAnnotationPresent(Attribute.class)) {
                        final Attribute attributeAnno = method.getAnnotation(Attribute.class);
                        if (attributeAnno != null && attributeAnno.method().equals(MethodType.GETTER)) {
                            try {
                                final IAttribute<?> attribute = (IAttribute<?>) method.invoke(object);
                                attributes.put(attributeAnno, attribute);
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

                final String syncids = object.getSyncid();
                final String masterIdStr = syncids.split(":")[0];
                final String mGenIdStr = masterIdStr.split("\\.")[1];
                final Long mGenId = Long.valueOf(mGenIdStr);

                final Update update;
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.GeneralInstance.uuid);
                queryBldr.addWhereAttrEqValue(CIAdminCommon.GeneralInstance.ExchangeID, mGenId);
                final MultiPrintQuery multi = queryBldr.getPrint();
                multi.addAttribute(CIAdminCommon.GeneralInstance.InstanceID,
                                CIAdminCommon.GeneralInstance.InstanceTypeID);
                // update
                if (multi.execute()) {
                    multi.next();
                    final Long instanceId = multi.<Long>getAttribute(CIAdminCommon.GeneralInstance.InstanceID);
                    final Long instanceTypeId = multi.<Long>getAttribute(CIAdminCommon.GeneralInstance.InstanceTypeID);
                    final Instance updateinst = Instance.get(org.efaps.admin.datamodel.Type.get(instanceTypeId),
                                    instanceId);

                    // if there is another result something is wrong
                    if (multi.next()) {
                        throw new EFapsException(this.getClass(), "multipleResults4SyncId", syncids);
                    }
                    // check if the given Type from the ScynAction is the same type returned from the Query
                    if (!updateinst.getType().getUUID().equals(UUID.fromString(typeAnno.uuid()))) {
                        throw new EFapsException(this.getClass(), "differentTypes", syncids);
                    }

                    if (checkSettings4update(attributes) && validate4update(updateinst, attributes)) {
                        update = new Update(updateinst);
                        add4update(update, attributes);
                        update.execute();
                        object.setOid(update.getInstance().getOid());
                    }
                    // insert
                } else {
                    update = new Insert(UUID.fromString(typeAnno.uuid())).setExchangeIds(new Long(0), mGenId);
                    add4insert(update, attributes);
                    update.execute();
                    object.setOid(update.getInstance().getOid());
                }
            }
        }
        return this;
    }

    /**
     * Validate the given attributes for update.
     *
     * @param _updateinst instance to be updated
     * @param _attributes attributes to be checked
     * @return true if update must be done, else false
     * @throws EFapsException on error
     */
    protected boolean validate4update(final Instance _updateinst,
                                      final Map<Attribute, IAttribute<?>> _attributes)
        throws EFapsException
    {
        boolean ret = false;
        final PrintQuery print = new PrintQuery(_updateinst);
        for (final Entry<Attribute, IAttribute<?>> entry : _attributes.entrySet()) {
            print.addAttribute(entry.getKey().name());
        }
        print.execute();
        for (final Entry<Attribute, IAttribute<?>> entry : _attributes.entrySet()) {
            final Object obj = print.getAttribute(entry.getKey().name());
            if (obj.equals(entry.getValue().getValue())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    /**
     * Check the setting of the Attribute if an update must be made.
     *
     * @param _attributes attributes to be checked
     * @return true if update must be done else false
     */
    protected boolean checkSettings4update(final Map<Attribute, IAttribute<?>> _attributes)
    {
        boolean ret = false;
        final Iterator<Entry<Attribute, IAttribute<?>>> iter = _attributes.entrySet().iterator();
        while (iter.hasNext()) {
            final Entry<Attribute, IAttribute<?>> entry = iter.next();
            if (entry.getValue().hasSetting(AttrSetting.SYNC_INCLUDE)) {
                ret = true;
            } else {
                iter.remove();
            }
        }
        return ret;
    }

    /**
     * Add the attributes and their values to the update.
     *
     * @param _update Update to be added to
     * @param _attributes atributes to be added
     * @throws EFapsException on error
     */
    protected void add4update(final Update _update,
                              final Map<Attribute, IAttribute<?>> _attributes)
        throws EFapsException
    {
        for (final Entry<Attribute, IAttribute<?>> entry : _attributes.entrySet()) {
            _update.add(entry.getKey().name(), entry.getValue().getValue());
        }
    }

    /**
     * Add the attributes and their values to the update.
     *
     * @param _update Update to be added to
     * @param _attributes atributes to be added
     * @throws EFapsException on error
     */
    protected void add4insert(final Update _update,
                              final Map<Attribute, IAttribute<?>> _attributes)
        throws EFapsException
    {
        for (final Entry<Attribute, IAttribute<?>> entry : _attributes.entrySet()) {
            _update.add(entry.getKey().name(), entry.getValue().getValue());
        }
    }
}
