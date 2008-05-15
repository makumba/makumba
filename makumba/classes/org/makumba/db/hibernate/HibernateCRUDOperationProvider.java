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
import java.util.Iterator;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.commons.NameResolver;
import org.makumba.commons.SQLPointer;
import org.makumba.providers.CRUDOperationProvider;

/**
 * Hibernate-specific implementation of a {@link CRUDOperationProvider}
 * 
 * @author Manuel Gay
 * @version $Id: HibernateCRUDOperationProvider.java,v 1.1 02.11.2007 14:05:40 Manuel Exp $
 */
public class HibernateCRUDOperationProvider extends CRUDOperationProvider {
    
    private NameResolver nr = new NameResolver();

    @Override
    public void checkInsert(Transaction t, String type, Dictionary fieldsToCheck, Dictionary fieldsToIgnore,
            Dictionary allFields) {

        DataDefinition dd = ddp.getDataDefinition(type);

        dd.checkFieldNames(fieldsToCheck);
        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
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
    public void checkUpdate(Transaction t, String type, Pointer pointer, Dictionary fieldsToCheck,
            Dictionary fieldsToIgnore, Dictionary allFields) {

        DataDefinition dd = checkUpdate(type, fieldsToCheck, fieldsToIgnore);

        // TODO we still need to check for multi-field key uniqueness that span over more than one table

    }

    @Override
    public Pointer insert(Transaction t, String type, Dictionary data) {

        try {

            HibernateTransaction ht = (HibernateTransaction) t;

            DataDefinition dd = ddp.getDataDefinition(type);

            String name = nr.arrowToDoubleUnderscore(dd.getName());

            Class recordClass = null;
            recordClass = Class.forName(HibernateSFManager.getFullyQualifiedName(name));
            // System.out.println(recordClass.getName() + ": " + Arrays.toString(recordClass.getMethods()));

            Object newRecord = null;
            newRecord = recordClass.newInstance();

            // we need to iterate over the fields we have and set them through the setters
            fillObject(t, data, dd, recordClass, newRecord);

            if (data.get("TS_create") == null) {
                Class[] classes = new Class[] { java.util.Date.class };
                Object[] now = new Object[] { new Date() };

                Method m = recordClass.getMethod("setTS_create", classes);
                m.invoke(newRecord, now);

                m = recordClass.getMethod("setTS_modify", classes);
                m.invoke(newRecord, now);

            }

            ht.s.persist(newRecord);
            ht.s.flush();

            Object pointerId = null;

            Class[] noParam = {};
            Method getId = recordClass.getMethod("getprimaryKey", noParam);

            Object[] args = {};
            pointerId = getId.invoke(newRecord, args);

            if (pointerId != null)
                return new SQLPointer(type, new Long((Integer) pointerId));
            else
                throw new MakumbaError("Unexpected return type while trying to get ID of inserted record");

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

    private void fillObject(Transaction t, Dictionary data, DataDefinition dd, Class recordClass,
            Object newRecord) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Enumeration<String> fields = data.keys();
        while (fields.hasMoreElements()) {
            String fieldName = fields.nextElement();

            String fieldNameInClass = nr.checkReserved(fieldName);
            


            Object fieldValue = data.get(fieldName);
            FieldDefinition fd = dd.getFieldDefinition(fieldName);

            Class fieldType = null;

            switch (fd.getIntegerType()) {
                case FieldDefinition._intEnum:
                    // type="enum";
                    // break;
                case FieldDefinition._int:
                    fieldType = Integer.class;
                    break;
                case FieldDefinition._real:
                    fieldType = Double.class;
                    break;
                case FieldDefinition._charEnum:
                case FieldDefinition._char:
                    fieldType = String.class;
                    break;
                case FieldDefinition._dateModify:
                case FieldDefinition._dateCreate:
                case FieldDefinition._date:
                    fieldType = Date.class;
                    break;
                case FieldDefinition._ptr:
                case FieldDefinition._ptrOne:
                case FieldDefinition._ptrRel:
                    // jackpot! we need to get an instance of the object, not only its pointer

                    // first we read its type
                    fieldType = getPointerClass(fd.getPointedType().getName());

                    // then, we know its pointer so we can read
                    // System.out.println("Going to load the object of type "+fieldType+" and with primary key
                    // "+((Pointer) fieldValue).getUid());
                    Pointer pointer = (Pointer) fieldValue;
                    fieldValue = getPointedObject(t, fieldType, pointer);
                    break;
                case FieldDefinition._ptrIndex:
                    fieldType = int.class;
                    break;
                case FieldDefinition._text:
                case FieldDefinition._binary:
                    // FIXME
                    fieldType = Text.class;
                    break;
                case FieldDefinition._boolean:
                    fieldType = Boolean.class;
                default:
                    throw new RuntimeException("Unmapped type: " + fd.getName() + "-" + fd.getType());

            }

            Class[] parameterTypes = { fieldType };

            // maybe we need an uppercase here, not sure
            Method m = null;
            // System.out.println("Getting setter set" + fieldNameInClass + " of class " + recordClass.getName()
            // + ", trying to pass new value of type " + parameterTypes[0]);
            
            if(!isGenerated(recordClass)) {
                for(Method met : recordClass.getMethods()) {
                    if(met.getName().toLowerCase().equals("set"+fieldNameInClass)) {
                        fieldNameInClass = met.getName().substring(3);
                        break;
                    }
                }
            }
            
            m = recordClass.getMethod("set" + fieldNameInClass, parameterTypes);
            m.invoke(newRecord, fieldValue);

        }
    }

    private Object getPointedObject(Transaction t, Class pointerClass, Pointer pointer) {
        return ((HibernateTransaction) t).s.get(pointerClass, getId(pointerClass, pointer));
    }

    private Class<?> getPointerClass(String type) throws ClassNotFoundException {
        return Class.forName(nr.arrowToDoubleUnderscore(HibernateSFManager.getFullyQualifiedName(type)));
    }

    @Override
    public void updateSet1(Transaction t, Pointer base, FieldDefinition fi, Object val) {

        if (fi.getType().equals("set")) {

            try {

                Collection values = (Collection) val;
                if (values.isEmpty())
                    return;

                HibernateTransaction ht = (HibernateTransaction) t;
                Class c = getPointerClass(base.getType());
                Object baseObject = getPointedObject(t, c, base);

                Method m = c.getMethod("get" + fi.getName(), new Class[] {});

                Collection<Object> col = (Collection) m.invoke(baseObject, new Object[] {});
                if (col == null) {
                    col = new HashSet();
                    m = c.getMethod("set" + fi.getName(), new Class[] { Collection.class });
                    m.invoke(baseObject, new Object[] { col });
                }

                // we convert all the pointers to objects so Hibernate can handle them
                for (Iterator i = values.iterator(); i.hasNext();) {
                    Pointer p = (Pointer) i.next();
                    Class c1 = getPointerClass(p.getType());
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
                Class c = getPointerClass(base.getType());
                Object baseObject = getPointedObject(t, c, base);

                /*
                 * Collection col = (Collection) m.invoke(baseObject, new Object[] {}); if(col != null)
                 * col.removeAll(col);
                 */
                Method m = c.getMethod("get" + fi.getName(), new Class[] {});
                m = c.getMethod("set" + fi.getName(), new Class[] { Collection.class });
                m.invoke(baseObject, new Object[] { new ArrayList() });

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
    public void update1(Transaction t, Pointer p, DataDefinition dd, Dictionary dic) {

        if (dic.isEmpty())
            return;

        try {

            HibernateTransaction ht = (HibernateTransaction) t;
            
            String name = nr.arrowToDoubleUnderscore(dd.getName());

            Class recordClass = null;
            recordClass = Class.forName(HibernateSFManager.getFullyQualifiedName(name));
            // System.out.println(recordClass.getName() + ": " + Arrays.toString(recordClass.getMethods()));

            Object record = null;
            
            record = ht.s.get(recordClass, getId(recordClass, p));

            // we need to iterate over the fields we have and set them through the setters
            fillObject(t, dic, dd, recordClass, record);

            if(isGenerated(recordClass)) {
                Class[] classes = new Class[] { java.util.Date.class };
                Object[] now = new Object[] { new Date() };
                Method m = recordClass.getMethod("setTS_modify", classes);
                m.invoke(record, now);
            }

            ht.s.saveOrUpdate(record);
            ht.s.flush();

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

    }
    
    /**
     * Returns the right pointer id depending on whether this class is generated by makumba or not
     * @param clazz the class
     * @param p the pointer
     * @return a makumba UID if this was generated by makumba, otherwise the long value representing the key on db level
     */
    private Serializable getId(Class clazz, Pointer p) {
        
        for (String s : HibernateSFManager.getGeneratedDataDefinitions()) {
            if(s.equals(clazz.getCanonicalName())) {
                return p.getUid();
            }
        }
        return p.longValue();
    }
    
    /**
     * Figures whether a class was generated by Makumba
     * @param clazz the class
     * @return <code>true</code> if it was generated, <code>false</code> otherwise
     */
    private boolean isGenerated(Class clazz) {
        for (String s : HibernateSFManager.getGeneratedDataDefinitions()) {
            if(s.equals(clazz.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }
    
    private String getIdType(Class clazz) {
        for(Method m : clazz.getMethods()) {
            if(m.getName().equals("getId") || m.getName().equals("getprimaryKey")) {
                return m.getReturnType().getName();
            }
        }
        return "int";
    }

}
