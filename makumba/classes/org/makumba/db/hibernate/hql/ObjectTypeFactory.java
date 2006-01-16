package org.makumba.db.hibernate.hql;

import java.util.Map;

import antlr.SemanticException;
import antlr.collections.AST;

public interface ObjectTypeFactory {
    public ObjectType make(AST lhs, AST rhs, Map aliasTypes)throws SemanticException;
}
