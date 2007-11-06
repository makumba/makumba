package org.makumba.db.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.db.sql.SQLPointer;
import org.makumba.providers.CRUDOperationProvider;

/**
 * Hibernate-specific implementation of a {@link CRUDOperationProvider}
 * 
 * @author Manuel Gay
 * @version $Id: HibernateCRUDOperationProvider.java,v 1.1 02.11.2007 14:05:40 Manuel Exp $
 */
public class HibernateCRUDOperationProvider extends CRUDOperationProvider {

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
        
        HibernateTransaction ht = (HibernateTransaction)t;
        
        ht.beginTransaction();
        
        HibernateUtils utils = new HibernateUtils();
        
        DataDefinition dd = ddp.getDataDefinition(type);
        
        String name = utils.arrowToDoubleUnderscore(dd.getName());
            
        Class recordClass = null;
        try {
            recordClass = Class.forName(name);
            System.out.println(recordClass.getName()+": "+ Arrays.toString(recordClass.getMethods()));
            
        } catch (ClassNotFoundException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        
        Object newRecord = null;
        try {
            newRecord = recordClass.newInstance();
        } catch (InstantiationException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (IllegalAccessException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        
        // we need to iterate over the fields we have and set them through the setters
        Enumeration<String> fields = data.keys();
        while(fields.hasMoreElements()) {
            String fieldName = fields.nextElement();
            
            String fieldNameInClass = utils.checkReserved(fieldName);
            
            Object fieldValue = data.get(fieldName);
            FieldDefinition fd = dd.getFieldDefinition(fieldName);
            
            Class fieldType = null;
            
            switch (fd.getIntegerType()) {
                case FieldDefinition._intEnum:
                    //type="enum";
                    //break;
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
                    try {
                        fieldType = Class.forName(utils.arrowToDoubleUnderscore(fd.getPointedType().getName()));
                    } catch (ClassNotFoundException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    
                    // then, we know its pointer so we can read
                    fieldValue = ((HibernateTransaction)t).s.get(fieldType, ((Pointer)fieldValue).getUid());
                    
                    
                    break;
                case FieldDefinition._ptrIndex:
                    fieldType = int.class;
                    break;
                case FieldDefinition._text:
                case FieldDefinition._binary:
                    //FIXME
                    fieldType = Text.class;
                    break;
                default:
                    try {
                        throw new Exception("Unmapped type: " + fd.getName() + "-" + fd.getType());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
            }
           
            Class[] parameterTypes = {fieldType};
            
            // maybe we need an uppercase here, not sure
            Method m = null;
            try {
                System.out.println("Getting setter set"+fieldNameInClass+" of class "+recordClass.getName()+", trying to pass new value of type "+parameterTypes[0]);
                m = recordClass.getMethod("set"+fieldNameInClass, parameterTypes);
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                m.invoke(newRecord, fieldValue);
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
            
        }
        
        ht.s.persist(newRecord);
        
        ht.t.commit();
        
        Object pointerId = null;
        
        try {
            Class[] noParam = {};
            Method getId = recordClass.getMethod("getprimaryKey", noParam);
            
            Object[] args = {};
            pointerId = getId.invoke(newRecord, args);
            
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
        
        if(pointerId != null)
            return new SQLPointer(type, new Long((Integer)pointerId));
        else
            throw new MakumbaError("Unexpected return type while trying to get ID of inserted record");
    }

}
