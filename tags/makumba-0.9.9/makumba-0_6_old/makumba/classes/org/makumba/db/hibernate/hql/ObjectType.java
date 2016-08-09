package org.makumba.db.hibernate.hql;

import antlr.RecognitionException;

public interface ObjectType {

    /** for composite types return a String with the type name, for simple types return a descriptor object
     * if field is null, the method should return the type if it is a valid type, null otherwise. */
    public Object determineType(String type, String field) throws RecognitionException;
    
    /** given a type descriptor, resolve it to an integer for type analysis */
    public int getTypeOf(Object descriptor);

}
