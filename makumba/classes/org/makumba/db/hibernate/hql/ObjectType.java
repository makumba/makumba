package org.makumba.db.hibernate.hql;

import antlr.RecognitionException;

public interface ObjectType {

    /** for simple types return an Integer as defined in DataTypeAST, for composite types return a String with the type name
     * if field is null, the method should return the type if it is a valid type, null otherwise. */
    public Object determineType(String type, String field) throws RecognitionException;

}
