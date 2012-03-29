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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.db.Update;
import org.efaps.esjp.jms.AbstractClassificationObject;
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
                final Map<Attribute, IAttribute<?>> attributes = getAttributes(object);

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
                syncClassifcation(object);
            }
        }
        return this;
    }

    /**
     * Get the Attributes for the Object.
     *
     * @param _object object the attributes are wanted for
     * @return Map of Annotation 2 Attribute
     * @throws EFapsException on error
     */
    protected Map<Attribute, IAttribute<?>> getAttributes(final AbstractObject _object)
        throws EFapsException
    {

        final Map<Attribute, IAttribute<?>> ret = new HashMap<Attribute, IAttribute<?>>();

        final Method[] methods = _object.getClass().getDeclaredMethods();
        for (final Method method : methods) {
            if (method.isAnnotationPresent(Attribute.class)) {
                final Attribute attributeAnno = method.getAnnotation(Attribute.class);
                if (attributeAnno != null && attributeAnno.method().equals(MethodType.GETTER)) {
                    try {
                        final IAttribute<?> attribute = (IAttribute<?>) method.invoke(_object);
                        ret.put(attributeAnno, attribute);
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
        return ret;
    }

    /**
     * Sync the classifications for an object.<br/>
     * In case of a classification the syncid does not matter. It must only be checked if the classification already
     * exists or not. If it does not exist a new one must be created, else the existing one must be updated. A
     * Classification is not treated as an Object that must be synchronized but as a part of an object. Therefore only
     * the syncid of the object the Classification belongs to is used.
     *
     * @param _object Object the classification must be synced for
     * @throws EFapsException on error
     */
    // TODO what must be done if a classification exists in the target but not in the syncobject
    protected void syncClassifcation(final AbstractObject _object)
        throws EFapsException
    {
        if (!_object.getClassifications().isEmpty()) {
            final PrintQuery print = new PrintQuery(_object.getOid());

            for (final AbstractClassificationObject classification : _object.getClassifications()) {
                // TODO check if this classification is allowed at all
                final Type typeAnno = classification.getClass().getAnnotation(Type.class);
                final SelectBuilder selBldr = new SelectBuilder()
                                .clazz(org.efaps.admin.datamodel.Type.get(UUID.fromString(typeAnno.uuid())).getName())
                                .oid();
                print.addSelect(selBldr);
                print.execute();
                final Instance classInst = Instance.get(print.<String>getSelect(selBldr));
                // update
                if (classInst.isValid()) {
                    classification.setOid(classInst.getOid());
                    updateClassification(_object, classification);
                    // insert
                } else {
                    insertClassification(_object, classification);
                }
            }
        }
    }

    /**
     * Insert the Classification in eFaps.<br/>
     *
     * @param _object Object the given classification belongs to
     *
     * @param _classification Classification to be updated
     * @throws EFapsException on error
     */
    protected void insertClassification(final AbstractObject _object,
                                        final AbstractClassificationObject _classification)
        throws EFapsException
    {
        final Type typeAnno = _classification.getClass().getAnnotation(Type.class);
        final Map<Attribute, IAttribute<?>> attributes = getAttributes(_classification);
        checkSettings4insert(attributes);

        final Classification clazz = (Classification) org.efaps.admin.datamodel.Type.get(UUID.fromString(typeAnno.uuid()));

        final Instance objectInst = Instance.get(_object.getOid());

        // if the classification does not exist yet the relation must be
        // created, and the new instance of the classification inserted
        final List<Classification> clazzes = new ArrayList<Classification>();
        clazzes.add(clazz);
        Classification clazzTmp = clazz;
        while (clazzTmp != null) {
            clazzTmp = (Classification) clazzTmp.getParentClassification();
            if (clazzTmp != null) {
                clazzes.add(clazzTmp);
            }
        }
        Collections.reverse(clazzes);
        for (final Classification clazzRel : clazzes) {
            final Insert relInsert = new Insert(clazzRel.getClassifyRelationType());
            relInsert.add(clazzRel.getRelLinkAttributeName(), objectInst.getId());
            relInsert.add(clazzRel.getRelTypeAttributeName(), clazzRel.getId());
            relInsert.execute();

        }

        final Insert insert = new Insert(UUID.fromString(typeAnno.uuid()));
        insert.add(clazz.getLinkAttributeName(), ((Long) objectInst.getId()).toString());
        add4insert(insert, attributes);
        insert.execute();

        _classification.setOid(insert.getInstance().getOid());

    }

    /**
     * Update the Classification in eFaps.<br/>
     * <b>For the given Sync-Classification the oid must already be set!</b>
     *
     * @param _object Object the given classification belongs to
     *
     * @param _classification Classification to be updated
     * @throws EFapsException on error
     */
    protected void updateClassification(final AbstractObject _object,
                                        final AbstractClassificationObject _classification)
        throws EFapsException
    {
        final Instance instance = Instance.get(_classification.getOid());
        final Map<Attribute, IAttribute<?>> attributes = getAttributes(_classification);
        if (checkSettings4update(attributes) && validate4update(instance, attributes)) {
            final Update update = new Update(instance);
            add4update(update, attributes);
            update.execute();
        }
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
     * Check the setting of the Attribute if an insert must be made.
     *
     * @param _attributes attributes to be checked
     * @return true if update must be done else false
     */
    protected boolean checkSettings4insert(final Map<Attribute, IAttribute<?>> _attributes)
    {
        return checkSettings4update(_attributes);
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
            if (entry.getValue() != null && entry.getValue().hasSetting(AttrSetting.SYNC_INCLUDE)) {
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
