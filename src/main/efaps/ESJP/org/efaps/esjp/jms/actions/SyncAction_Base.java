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
import org.efaps.esjp.jms.attributes.LinkAttribute;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SyncAction_Base.class);

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
                final Map<Attribute, IAttribute<?>> attributes = getAttributes(object, null);

                final Long[] exChgIds = getExchangeIds(object.getSyncid());
                final Instance updateinst = getInstance4ExchIds(exChgIds);

                final Update update;
                // update
                if (updateinst.isValid()) {
                    object.setOid(updateinst.getOid());
                    // check if the given Type from the ScynAction is the same
                    // type returned from the Query
                    if (!updateinst.getType().getUUID().equals(UUID.fromString(typeAnno.uuid()))) {
                        throw new EFapsException(this.getClass(), "differentTypes", object.getSyncid());
                    }

                    if (checkSettings4update(attributes) && validate4update(updateinst, attributes)) {
                        update = new Update(updateinst);
                        add4update(update, attributes);
                        update.execute();
                    }
                    // insert
                } else {
                    update = new Insert(UUID.fromString(typeAnno.uuid())).setExchangeIds(exChgIds[0], exChgIds[1]);
                    add4insert(update, attributes);
                    final Map<String, Object> map = validate4insert(updateinst, attributes);
                    if (map.isEmpty()) {
                        update.execute();
                        object.setOid(update.getInstance().getOid());
                    } else {
                        final StringBuilder str = new StringBuilder();
                        for (final Entry<String, Object> entry : map.entrySet()) {
                            str.append(entry.getKey())
                                .append("=")
                                .append(entry.getValue())
                                .append(" - ");
                        }
                        LOG.error("The type '{}' with attributes: '{}', cannot be created",
                                        update.getInstance().getType().getName(), str.toString());
                    }
                }
                syncClassifcation(object);
            }
        }
        return this;
    }

    /**
     * Evaluate the exchangeid.<br/>
     * <b>Must return an array with to longs.!</b>
     *
     * @param _syncid object the exchangeid must be read from
     * @return long array, [exchangeSystemId, exchangeId]
     * @throws EFapsException on error
     */
    protected Long[] getExchangeIds(final String _syncid)
        throws EFapsException
    {
        final Long[] ret = new Long[2];
        final String masterIdStr = _syncid.split(":")[0];
        final String[] mIdStr = masterIdStr.split("\\.");
        final Long mGenId = Long.valueOf(mIdStr[1]);
        final Long mSystemId = Long.valueOf(mIdStr[0]);
        ret[0] = mSystemId;
        ret[1] = mGenId;
        return ret;
    }

    /**
     * Get the Attributes for the Object.
     *
     * @param _object object the attributes are wanted for
     * @param _declaringClass if this parameter is given, it is also evaluated
     *            if the method actual belongs to the given class
     * @return Map of Annotation 2 Attribute
     * @throws EFapsException on error
     */
    protected Map<Attribute, IAttribute<?>> getAttributes(final AbstractObject _object,
                                                          final Class<?> _declaringClass)
        throws EFapsException
    {

        final Map<Attribute, IAttribute<?>> ret = new HashMap<Attribute, IAttribute<?>>();

        final Method[] methods = _object.getClass().getMethods();
        for (final Method method : methods) {
            if (method.isAnnotationPresent(Attribute.class)) {
                final Attribute attributeAnno = method.getAnnotation(Attribute.class);
                if (attributeAnno != null && attributeAnno.method().equals(MethodType.GETTER)
                                && (_declaringClass == null || _declaringClass.equals(method.getDeclaringClass()))) {
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
     * In case of a classification the syncid does not matter. It must only be
     * checked if the classification already exists or not. If it does not exist
     * a new one must be created, else the existing one must be updated. A
     * Classification is not treated as an Object that must be synchronized but
     * as a part of an object. Therefore only the syncid of the object the
     * Classification belongs to is used.
     *
     * @param _object Object the classification must be synced for
     * @throws EFapsException on error
     */
    // TODO what must be done if a classification exists in the target but not
    // in the syncobject
    @SuppressWarnings("unchecked")
    protected void syncClassifcation(final AbstractObject _object)
        throws EFapsException
    {
        if (!_object.getClassifications().isEmpty()) {
            for (final AbstractClassificationObject classification : _object.getClassifications()) {
                final PrintQuery print = new PrintQuery(_object.getOid());
                // TODO check if this classification is allowed at all
                final Type typeAnno = classification.getClass().getAnnotation(Type.class);
                final SelectBuilder selBldr = new SelectBuilder()
                                .clazz(org.efaps.admin.datamodel.Type.get(UUID.fromString(typeAnno.uuid())).getName())
                                .oid();
                print.addSelect(selBldr);
                print.execute();
                final Object clazz = print.getSelect(selBldr);
                if (clazz instanceof String) {
                    final Instance classInst = Instance.get((String) clazz);
                    // update
                    if (classInst.isValid()) {
                        classification.setOid(classInst.getOid());
                        updateClassification(_object, classification);
                        // insert
                    } else {
                        insertClassification(_object, classification);
                    }
                } else if (clazz instanceof List) {
                    for (final String cl : (List<String>) clazz) {
                        final Instance classInst = Instance.get(cl);
                        // update
                        if (classInst.isValid()) {
                            classification.setOid(classInst.getOid());
                            updateClassification(_object, classification);
                            // insert
                        } else {
                            insertClassification(_object, classification);
                        }
                    }
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

        final Classification clazz = (Classification) org.efaps.admin.datamodel.Type.get(UUID.fromString(typeAnno
                        .uuid()));

        final Instance objectInst = Instance.get(_object.getOid());

        // if the classification does not exist yet the relation must be
        // created, and the new instance of the classification inserted
        final Map<Classification, Class<?>> clazz2class = new HashMap<Classification, Class<?>>();
        clazz2class.put(clazz, _classification.getClass());
        final List<Classification> clazzes = new ArrayList<Classification>();
        clazzes.add(clazz);
        Classification clazzTmp = clazz;
        while (clazzTmp != null) {
            final Class<?> clss = clazz2class.get(clazzTmp);
            clazzTmp = (Classification) clazzTmp.getParentClassification();
            if (clazzTmp != null) {
                final PrintQuery print = new PrintQuery(objectInst);
                final SelectBuilder selClass = new SelectBuilder().clazz(clazzTmp.getName()).oid();
                print.addSelect(selClass);
                print.execute();
                final Object clazzCheck = print.getSelect(selClass);
                boolean exist = false;
                if (clazzCheck instanceof String) {
                    final Instance classInst = Instance.get((String) clazzCheck);
                    // update
                    if (classInst.isValid()) {
                        exist = true;
                    }
                } else if (clazzCheck instanceof List) {
                    for (final String cl : (List<String>) clazzCheck) {
                        final Instance classInst = Instance.get(cl);
                        if (classInst.isValid()) {
                            exist = true;
                        }
                    }
                }
                if (!exist) {
                    clazzes.add(clazzTmp);
                    clazz2class.put(clazzTmp, clss.getSuperclass());
                }
            }
        }
        Collections.reverse(clazzes);
        for (final Classification clazzRel : clazzes) {
            final Insert relInsert = new Insert(clazzRel.getClassifyRelationType());
            relInsert.add(clazzRel.getRelLinkAttributeName(), objectInst.getId());
            relInsert.add(clazzRel.getRelTypeAttributeName(), clazzRel.getId());
            relInsert.execute();

            final Map<Attribute, IAttribute<?>> attributes = getAttributes(_classification, clazz2class.get(clazzRel));
            checkSettings4insert(attributes);
            final Insert insert = new Insert(clazzRel);
            insert.add(clazzRel.getLinkAttributeName(), ((Long) objectInst.getId()).toString());
            add4insert(insert, attributes);
            insert.execute();
            _classification.setOid(insert.getInstance().getOid());
        }
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
        final Map<Attribute, IAttribute<?>> attributes = getAttributes(_classification, null);
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
        print.executeWithoutAccessCheck();
        for (final Entry<Attribute, IAttribute<?>> entry : _attributes.entrySet()) {
            final Object obj = print.getAttribute(entry.getKey().name());
            if (obj != null && !obj.equals(entry.getValue().getValue())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    /**
     * Validate the given attributes for insert.
     *
     * @param _updateinst instance to be updated
     * @param _attributes attributes to be checked
     * @return true if update must be done, else false
     * @throws EFapsException on error
     */
    protected Map<String, Object> validate4insert(final Instance _updateinst,
                                      final Map<Attribute, IAttribute<?>> _attributes)
        throws EFapsException
    {
        Map<String, Object> map = new HashMap<String, Object>();
        boolean valid = true;
        for (final Entry<Attribute, IAttribute<?>> entry : _attributes.entrySet()) {
            if (entry.getValue() != null) {
                Object value;
                if (entry.getValue() instanceof LinkAttribute) {
                    value = getLinkValue(entry.getValue());
                } else {
                    value = entry.getValue().getValue();
                }
                if (value != null && value instanceof Long && ((Long) value) == 0) {
                    valid = false;
                }
                map.put(entry.getKey().name(), value);
            }
        }
        if (valid) {
            map = new HashMap<String, Object>();
        }
        return map;
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
            if (entry.getValue() != null) {
                Object value;
                if (entry.getValue() instanceof LinkAttribute) {
                    value = getLinkValue(entry.getValue());
                } else {
                    value = entry.getValue().getValue();
                }
                _update.add(entry.getKey().name(), value);
            }
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
            if (entry.getValue() != null) {
                Object value;
                if (entry.getValue() instanceof LinkAttribute) {
                    value = getLinkValue(entry.getValue());
                } else {
                    value = entry.getValue().getValue();
                }
                _update.add(entry.getKey().name(), value);
            }
        }
    }

    /**
     * @param _attribute IAttribute the link value is wanted for
     * @return Object represting the link
     * @throws EFapsException on error
     */
    protected Object getLinkValue(final IAttribute<?> _attribute)
        throws EFapsException
    {
        Object ret = null;
        final LinkAttribute attribute = (LinkAttribute) _attribute;
        if (attribute.getValue().getOid() != null) {
            ret = Instance.get(attribute.getValue().getOid()).getId();
        } else {
            final Long[] exChgIds = getExchangeIds(attribute.getValue().getSyncid());
            ret = getInstance4ExchIds(exChgIds).getId();
        }
        return ret;
    }

    /**
     * Get an instance for an ExchangeId array.
     *
     * @param _exChgIds array of a exchangeId
     * @return Instance belonging to the given exchangeId Array
     * @throws EFapsException on error
     */
    protected Instance getInstance4ExchIds(final Long[] _exChgIds)
        throws EFapsException
    {
        Instance ret = Instance.get("oid");
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.GeneralInstance.uuid);
        queryBldr.addWhereAttrEqValue(CIAdminCommon.GeneralInstance.ExchangeID, _exChgIds[1]);
        queryBldr.addWhereAttrEqValue(CIAdminCommon.GeneralInstance.ExchangeSystemID, _exChgIds[0]);
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminCommon.GeneralInstance.InstanceID,
                        CIAdminCommon.GeneralInstance.InstanceTypeID);
        // update
        if (multi.execute()) {
            multi.next();
            final Long instanceId = multi.<Long>getAttribute(CIAdminCommon.GeneralInstance.InstanceID);
            final Long instanceTypeId = multi.<Long>getAttribute(CIAdminCommon.GeneralInstance.InstanceTypeID);
            ret = Instance.get(org.efaps.admin.datamodel.Type.get(instanceTypeId),
                            instanceId);

            // if there is another result something is wrong
            if (multi.next()) {
                throw new EFapsException(this.getClass(), "multipleResults4SyncId", _exChgIds[0], _exChgIds[1]);
            }
        }
        return ret;
    }
}
