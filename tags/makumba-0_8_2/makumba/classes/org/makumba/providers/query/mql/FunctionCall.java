package org.makumba.providers.query.mql;

import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.DataDefinition.QueryFragmentFunction;

public class FunctionCall {
    
    private DataDefinition.QueryFragmentFunction function;
    
    private boolean isMQLFunction;
    
    private boolean isActorFunction;

    private boolean isFunctionArgument;
    
    private Vector<Node> orderedArgumentOrigins;
    
    private Vector<MqlNode> orderedArguments;

    private DataDefinition parentType;
    
    private String path;
    
    public FunctionCall(QueryFragmentFunction function, Vector<MqlNode> orderedArguments, Vector<Node> orderedArgumentsOrigins, DataDefinition parentType, String path, boolean isFunctionArgument, boolean isMQLFunction, boolean isActorFunction) {
        super();
        this.function = function;
        this.orderedArguments = orderedArguments;
        this.orderedArgumentOrigins = orderedArgumentsOrigins;
        this.parentType = parentType;
        this.path = path;
        this.isFunctionArgument = isFunctionArgument;
        this.isMQLFunction = isMQLFunction;
        this.isActorFunction = isActorFunction;
    }

    public DataDefinition.QueryFragmentFunction getFunction() {
        return function;
    }

    public String getKey() {
        return function == null ? null : function.getName() + "_" + orderedArguments + "_" + parentType + "_" + path + "_" + isFunctionArgument() + "_" + isMQLFunction + "_" + isActorFunction;
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
    
    @Override
    public String toString() {
        return "FunctionCall [function=" + function + ", orderedArguments=" + orderedArguments + ", parentType="
                + parentType + ", path=" + path + ", isFunctionArgument="+isFunctionArgument+ ", isMQLFunction="+isMQLFunction+ ", isActorFunction="+isActorFunction+"]";
    }
    

}
