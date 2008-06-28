package org.makumba.providers.query.hql;

/** This class would determine types based on Hibernate metadata info, hence it would be Makumba-independent.
 * It would require a SessionFactory. */
public class HibernateObjectType implements ObjectType {
   
    public Object determineType(String type, String field) {
        /* analysis can look like
        Type t= SessionFactory.getClassMetadata(type).getPropertyType(rhs.getText());
        then check if t is a ptr or a set, and to what type it points, and do setText() with that type
        */
        
        // TODO Auto-generated method stub
        return null;
    }

    public int getTypeOf(Object descriptor) {
        // TODO Auto-generated method stub
        return -1;
    }

}
