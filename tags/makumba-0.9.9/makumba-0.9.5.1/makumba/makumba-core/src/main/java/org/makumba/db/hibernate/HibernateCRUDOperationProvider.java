package org.makumba.db.hibernate;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.commons.NameResolver;
import org.makumba.commons.SQLPointer;
import org.makumba.db.DataHolder;
import org.makumba.providers.CRUDOperationProvider;

/**
 * Hibernate-specific implementation of a {@link CRUDOperationProvider} FIXME there are probably more bugs with the
 * collections of non-generated mappings
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: HibernateCRUDOperationProvider.java,v 1.1 02.11.2007 14:05:40 Manuel Exp $
 */
public class HibernateCRUDOperationProvider extends CRUDOperationProvider {

    @Override
    public void checkInsert(Transaction t, String type, Dictionary<String, Object> fieldsToCheck,
            Dictionary<String, DataHolder> fieldsToIgnore, Dictionary<String, Object> allFields) {

        DataDefinition dd = ddp.getDataDefinition(type);

        checkFieldNames(fieldsToCheck, dd);
        for (String string : dd.getFieldNames()) {
            String name = string;
            if (fieldsToIgnore.get(name) == null) {
                Object o = fieldsToCheck.get(name);
                if (o != null) {

                    // TODO this does not check if we have the rights to copy

                    dd.getFieldDefinition(name).checkInsert(fieldsToCheck);

                    fieldsToCheck.put(name, dd.getFieldDefinition(name).checkValue(o));
                }
            }
        }

        // TODO we still need to check for multi-field key uniqueness that span over more than one table

    }

    @Override
    public void checkUpdate(Transaction t, String type, Pointer pointer, Dictionary<String, Object> fieldsToCheck,
            Dictionary<String, DataHolder> fieldsToIgnore, Dictionary<String, Object> allFields) {

        // DataDefinition dd =
        checkUpdate(type, fieldsToCheck, fieldsToIgnore);

        // TODO we still need to check for multi-field key uniqueness that span over more than one table

    }

    @Override
    public Pointer insert(Transaction t, String type, Dictionary<String, Object> data) {

        try {

            HibernateTransaction ht = (HibernateTransaction) t;

            DataDefinition dd = ddp.getDataDefinition(type);

            String name = NameResolver.arrowToDoubleUnderscore(dd.getName());

            if (dd.getName().indexOf("->") > -1 && HibernateSFManager.getFullyQualifiedName(name) == null) {

                // we have a non-generated mapping that has a set
                // this is evil
                FieldDefinition fi = dd.getParentField();

                Pointer base = (Pointer) data.get(dd.getParentField().getDataDefinition().getName());

                Class<?> c = getPointerClass(base.getType());
                Object baseObject = getPointedObject(t, c, base);

                String fieldNameInClass = getFieldNameInClass(c, fi.getName());

                Method m = c.getMethod("get" + fieldNameInClass, new Class[] {});

                @SuppressWarnings("unchecked")
                Collection<Object> invoke = (Collection<Object>) m.invoke(baseObject, new Object[] {});
                Collection<Object> col = invoke;
                if (col == null) {
                    col = new HashSet<Object>();
                    m = c.getMethod("set" + fieldNameInClass, new Class[] { Collection.class });
                    m.invoke(baseObject, new Object[] { col });
                }

                // now we add our new data
                Enumeration<Object> e = data.elements();
                while (e.hasMoreElements()) {
                    Object o = e.nextElement();
                    if (!(o instanceof Pointer && ((Pointer) o).equals(base))) {

                        if (o instanceof Text) {
                            o = ((Text) o).getString();
                        }

                        col.add(o);
                    }
                }

                ht.s.saveOrUpdate(baseObject);
                ht.s.flush();

            } else {

                Class<?> recordClass = null;
                recordClass = Class.forName(HibernateSFManager.getFullyQualifiedName(name));
                // System.out.println(recordClass.getName() + ": " + Arrays.toString(recordClass.getMethods()));

                Object newRecord = null;
                newRecord = recordClass.newInstance();

                // we need to iterate over the fields we have and set them through the setters
                fillObject(t, data, dd, recordClass, newRecord);

                if (isGenerated(recordClass) && data.get("TS_create") == null) {
                    Class<?>[] classes = new Class<?>[] { java.util.Date.class };
                    Object[] now = new Object[] { new Date() };

                    Method m = recordClass.getMethod("setTS_create", classes);
                    m.invoke(newRecord, now);

                    m = recordClass.getMethod("setTS_modify", classes);
                    m.invoke(newRecord, now);

                }

                ht.s.persist(newRecord);
                ht.s.flush();

                Object pointerId = null;

                Class<?>[] noParam = {};

                String idMethodName = "getprimaryKey";
                if (!isGenerated(recordClass)) {
                    idMethodName = "getId";
                }
                Method getId = recordClass.getMethod(idMethodName, noParam);

                Object[] args = {};
                pointerId = getId.invoke(newRecord, args);

                String returnType = getId.getReturnType().getName();

                if (pointerId != null) {
                    return new SQLPointer(type, isInteger(returnType) ? new Long((Integer) pointerId)
                            : (Long) pointerId);
                } else {
                    throw new MakumbaError("Unexpected return type while trying to get ID of inserted record");
                }
            }
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }

    private void fillObject(Transaction t, Dictionary<String, Object> data, DataDefinition dd, Class<?> recordClass,
            Object newRecord) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Enumeration<String> fields = data.keys();
        while (fields.hasMoreElements()) {
            String fieldName = fields.nextElement();

            String fieldNameInClass = NameResolver.checkReserved(fieldName);

            Object fieldValue = data.get(fieldName);
            FieldDefinition fd = dd.getFieldDefinition(fieldName);

            Class<?> fieldType = null;

            switch (fd.getIntegerType()) {
                case FieldDefinition._intEnum:
                    // type="enum";
                    // break;
                case FieldDefinition._int:
                    fieldType = Integer.class;
                    if (fieldValue == Pointer.NullInteger) {
                        fieldValue = null;
                    }
                    break;
                case FieldDefinition._real:
                    fieldType = Double.class;
                    if (fieldValue == Pointer.NullReal) {
                        fieldValue = null;
                    }
                    break;
                case FieldDefinition._charEnum:
                case FieldDefinition._char:
                    fieldType = String.class;
                    if (fieldValue == Pointer.NullString) {
                        fieldValue = null;
                    }
                    break;
                case FieldDefinition._dateModify:
                case FieldDefinition._dateCreate:
                case FieldDefinition._date:
                    fieldType = Date.class;
                    if (fieldValue == Pointer.NullDate) {
                        fieldValue = null;
                    }
                    break;
                case FieldDefinition._ptr:
                case FieldDefinition._ptrOne:
                case FieldDefinition._ptrRel:
                    // jackpot! we need to get an instance of the object, not only its pointer

                    // first we read its type
                    fieldType = getPointerClass(fd.getPointedType().getName());
                    if (fieldValue == Pointer.Null) {
                        fieldValue = null;
                        break;
                    }
                    // then, we know its pointer so we can read
                    // System.out.println("Going to load the object of type "+fieldType+" and with primary key
                    // "+((Pointer) fieldValue).getUid());
                    Pointer pointer = (Pointer) fieldValue;
                    fieldValue = getPointedObject(t, fieldType, pointer);
                    break;
                case FieldDefinition._ptrIndex:
                    fieldType = int.class;
                    if (fieldValue == Pointer.Null) {
                        fieldValue = null;
                    }
                    break;
                case FieldDefinition._text:
                    if (!isGenerated(recordClass)) {
                        fieldType = String.class;
                        // FIXME: this is a memory killer, should use streams
                        fieldValue = ((Text) fieldValue).getString();
                        if (fieldValue == Pointer.NullText) {
                            fieldValue = null;
                        }
                        break;
                    }

                case FieldDefinition._binary:
                    // FIXME this might not work since we need a byte[] as type
                    fieldType = Text.class;
                    if (fieldValue == Pointer.NullText) {
                        fieldValue = null;
                    }
                    break;
                case FieldDefinition._boolean:
                    fieldType = Boolean.class;
                    if (fieldValue == Pointer.NullBoolean) {
                        fieldValue = null;
                    }
                    break;
                default:
                    throw new RuntimeException("Unmapped type: " + fd.getName() + "-" + fd.getType());

            }

            Class<?>[] parameterTypes = { fieldType };

            // maybe we need an uppercase here, not sure
            Method m = null;
            // System.out.println("Getting setter set" + fieldNameInClass + " of class " + recordClass.getName()
            // + ", trying to pass new value of type " + parameterTypes[0])fi.getName();

            if (!isGenerated(recordClass)) {
                for (Method met : recordClass.getMethods()) {
                    if (met.getName().toLowerCase().equals("set" + fieldNameInClass.toLowerCase())) {
                        fieldNameInClass = met.getName().substring(3);
                        parameterTypes = new Class[] { met.getParameterTypes()[0] };
                        break;
                    }
                }
            }

            m = recordClass.getMethod("set" + fieldNameInClass, parameterTypes);
            m.invoke(newRecord, fieldValue);

        }
    }

    private Object getPointedObject(Transaction t, Class<?> pointerClass, Pointer pointer) {
        return ((HibernateTransaction) t).s.get(pointerClass, getTypedId(pointerClass, pointer));
    }

    private Class<?> getPointerClass(String type) throws ClassNotFoundException {
        return Class.forName(NameResolver.arrowToDoubleUnderscore(HibernateSFManager.getFullyQualifiedName(type)));
    }

    @Override
    public void updateSet1(Transaction t, Pointer base, FieldDefinition fi, Object val) {

        if (fi.getType().equals("set")) {

            try {
                @SuppressWarnings("unchecked")
                Collection<Pointer> values = (Collection<Pointer>) val;
                if (values.isEmpty()) {
                    return;
                }

                HibernateTransaction ht = (HibernateTransaction) t;
                Class<?> c = getPointerClass(base.getType());
                Object baseObject = getPointedObject(t, c, base);

                Method m = c.getMethod("get" + getFieldNameInClass(c, fi.getName()), new Class[] {});

                @SuppressWarnings("unchecked")
                Collection<Object> invoke = (Collection<Object>) m.invoke(baseObject, new Object[] {});
                Collection<Object> col = invoke;
                if (col == null) {
                    col = new HashSet<Object>();
                    m = c.getMethod("set" + fi.getName(), new Class[] { Collection.class });
                    m.invoke(baseObject, new Object[] { col });
                }

                // we convert all the pointers to objects so Hibernate can handle them
                for (Pointer p : values) {

                    Class<?> c1 = getPointerClass(p.getType());
                    col.add(getPointedObject(t, c1, p));
                }

                ht.s.saveOrUpdate(baseObject);
                ht.s.flush();

            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            super.updateSet1(t, base, fi, val);
        }

    }

    @Override
    public void deleteSet(Transaction t, Pointer base, FieldDefinition fi) {

        // Hibernate automatically cascades deletes in the case of sets

        if (fi.getType().equals("set")) {

            try {
                HibernateTransaction ht = (HibernateTransaction) t;
                Class<?> c = getPointerClass(base.getType());
                Object baseObject = getPointedObject(t, c, base);

                String fieldNameInClass = fi.getName();
                Class<?>[] parameterTypes = new Class[] { Collection.class };

                if (!isGenerated(c)) {
                    for (Method met : c.getMethods()) {
                        if (met.getName().toLowerCase().equals("set" + fieldNameInClass.toLowerCase())) {
                            fieldNameInClass = met.getName().substring(3);
                            parameterTypes = new Class[] { met.getParameterTypes()[0] };
                            break;
                        }
                    }
                }

                Method m = c.getMethod("set" + fieldNameInClass, parameterTypes);
                m.invoke(baseObject, new Object[] { new ArrayList<Object>() });

                ht.s.saveOrUpdate(baseObject);
                ht.s.flush();

            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            super.deleteSet(t, base, fi);
        }

    }

    @Override
    public int update1(Transaction t, Pointer p, DataDefinition dd, Dictionary<String, Object> dic) {

        if (dic.isEmpty()) {
            return 0;
        }

        try {

            HibernateTransaction ht = (HibernateTransaction) t;

            String name = NameResolver.arrowToDoubleUnderscore(dd.getName());

            Class<?> recordClass = null;
            recordClass = Class.forName(HibernateSFManager.getFullyQualifiedName(name));
            // System.out.println(recordClass.getName() + ": " + Arrays.toString(recordClass.getMethods()));

            Object record = null;

            record = ht.s.get(recordClass, getTypedId(recordClass, p));

            // we need to iterate over the fields we have and set them through the setters
            fillObject(t, dic, dd, recordClass, record);

            if (isGenerated(recordClass)) {
                Class<?>[] classes = new Class[] { java.util.Date.class };
                Object[] now = new Object[] { new Date() };
                Method m = recordClass.getMethod("setTS_modify", classes);
                m.invoke(record, now);
            }

            ht.s.saveOrUpdate(record);
            ht.s.flush();

            return 1;

        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;

    }

    /**
     * Figures whether a class was generated by Makumba
     * 
     * @param clazz
     *            the class
     * @return <code>true</code> if it was generated, <code>false</code> otherwise
     */
    public static boolean isGenerated(Class<?> clazz) {
        for (String s : HibernateSFManager.getGeneratedClasses()) {
            if (s.equals(clazz.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Figures the name of a field in a class (meaning the right capitalisation)
     * 
     * @param clazz
     *            the class
     * @param fieldName
     *            the name of the field
     * @return the fieldName, with the right capitals
     */
    private String getFieldNameInClass(Class<?> clazz, String fieldName) {
        if (!isGenerated(clazz)) {
            for (Method met : clazz.getMethods()) {
                if (met.getName().toLowerCase().equals("get" + fieldName.toLowerCase())) {
                    return met.getName().substring(3);
                }
            }
        }

        return fieldName;
    }

    /**
     * Figures the pointer value of a hibernate object, with the right type of the primary key (hibernate id) field
     * 
     * @param clazz
     *            the class
     * @param p
     *            the Pointer
     * @return a long or int value, depending on the type of the field in the class
     */
    private Serializable getTypedId(Class<?> clazz, Pointer p) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals("getId") || m.getName().equals("getprimaryKey")) {
                if (isInteger(m.getReturnType().getName())) {
                    return p.getId();
                } else if (isLong(m.getReturnType().getName())) {
                    return p.longValue();
                }
            }
        }
        return p.getId();
    }

    public static boolean isInteger(String name) {
        return name.equals("int") || name.indexOf("Integer") > -1;
    }

    public static boolean isLong(String name) {
        return name.equals("long") || name.indexOf("Long") > -1;
    }

}
