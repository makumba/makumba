package org.makumba.db.hibernate.hql;

import antlr.RecognitionException;
import antlr.SemanticException;

public interface ObjectType {
    
    public Object determineType(String type, String field) throws RecognitionException, SemanticException;

}
