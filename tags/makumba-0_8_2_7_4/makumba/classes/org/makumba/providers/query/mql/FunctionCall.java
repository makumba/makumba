package org.makumba.providers.query.mql;

import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.DataDefinition.QueryFragmentFunction;

public class FunctionCall {
    
    private DataDefinition.QueryFragmentFunction function;
    
    private boolean isMQLFunction;
    
    private boolean isActorFunction;

    private boolean isFunctionArgument;
    
    private boolean isInWhere;
    
    private Vector<Node> orderedArgumentOrigins;
    
    private Vector<MqlNode> orderedArguments;

    private DataDefinition parentType;
    
    private String path;
    
    private int id = 0;
    
    public FunctionCall(QueryFragmentFunction function, Vector<MqlNode> orderedArguments, Vector<Node> orderedArgumentsOrigins, DataDefinition parentType, String path, boolean isFunctionArgument, boolean isMQLFunction, boolean isActorFunction, boolean isInWhere) {
        super();
        this.function = function;
        this.orderedArguments = orderedArguments;
        this.orderedArgumentOrigins = orderedArgumentsOrigins;
        this.parentType = parentType;
        this.path = path;
        this.isFunctionArgument = isFunctionArgument;
        this.isMQLFunction = isMQLFunction;
        this.isActorFunction = isActorFunction;
        this.isInWhere = isInWhere;
    }

    public DataDefinition.QueryFragmentFunction getFunction() {
        return function;
    }

    public String getKey() {
        String actorType = "";
        if(isActorFunction && orderedArguments.size() > 0) {
            actorType = ASTUtil.getPath(orderedArguments.firstElement());
        }
        return (function == null ? null : function.getName()) + "_" + orderedArguments + "_" + parentType + "_" + path + "_" + isFunctionArgument() + "_" + isMQLFunction + "_" + "isActorFunction:" + isActorFunction + "####" + actorType + "_" + isInWhere + "_" + id;
    }
    
    public static String getActorType(String key) {
        if(key.indexOf("isActorFunction:true") < 0) {
            return null;
        } else {
            int i = key.indexOf("####") + 4;
            String t = key.substring(i);
            t = t.substring(0, t.indexOf("_"));
            return t;
        }
    }
    
    public FunctionCall incrementId() {
        this.id = id + 1;
        return this;
    }
    
    public Vector<Node> getOrderedArgumentOrigins() {
        return orderedArgumentOrigins;
    }

    public Vector<MqlNode> getOrderedArguments() {
        return orderedArguments;
    }

    public DataDefinition getParentType() {
        return parentType;
    }

    /**
     * get the path to the function call, i.e. a.b.c.functionCall()
     */
    public String getPath() {
        return path;
    }

    public boolean isFunctionArgument() {
        return isFunctionArgument;
    }
    
    public boolean isMQLFunction() {
        return isMQLFunction;
    }
    
    public boolean isActorFunction() {
        return isActorFunction;
    }
    
    public boolean isInWhere() {
        return isInWhere;
    }
    
    @Override
    public String toString() {
        return "FunctionCall [function=" + function + ", orderedArguments=" + orderedArguments + ", parentType="
                + parentType + ", path=" + path + ", isFunctionArgument="+isFunctionArgument+ ", isMQLFunction="+isMQLFunction
                + ", isActorFunction="+isActorFunction+", isInWhere=" + isInWhere + "]";
    }
    

}
