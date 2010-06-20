/**
 * testing new files
 */
package org.makumba;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import antlr.collections.AST;

/**
 * This class represents a makumba query fragment function
 * 
 * @author Rudolf Mayer
 * @author Cristian Bogdan
 * @version $Id: QueryFragmentFunction.java,v 1.1 Jun 20, 2010 3:17:42 PM manu Exp $
 */
public class QueryFragmentFunction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private String sessionVariableName;

    private String queryFragment;

    private AST parsedQueryFragment;

    private DataDefinition parameters;

    private String errorMessage;

    private DataDefinition holder;

    public AST getParsedQueryFragment() {
        return parsedQueryFragment;
    }

    public QueryFragmentFunction(DataDefinition holder, String name, String sessionVariableName, String queryFragment,
            DataDefinition parameters, String errorMessage, AST parsedQueryFragment) {
        super();
        this.name = name;
        this.sessionVariableName = sessionVariableName;
        this.queryFragment = queryFragment;
        this.parameters = parameters;
        if (errorMessage != null) {
            this.errorMessage = errorMessage;
        } else {
            this.errorMessage = "";
        }
        this.parsedQueryFragment = parsedQueryFragment;
        this.holder = holder;
    }

    public String getName() {
        return name;
    }

    public DataDefinition getHoldingDataDefinition() {
        return holder;
    }

    public void setHoldingDataDefinition(DataDefinition holder) {
        this.holder = holder;
    }

    public String getSessionVariableName() {
        return sessionVariableName;
    }

    public DataDefinition getParameters() {
        return parameters;
    }

    public String getQueryFragment() {
        return queryFragment;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isActorFunction() {
        return getName().startsWith("actor");
    }

    public boolean isSubquery() {
        return getQueryFragment().toUpperCase().startsWith("SELECT ");
    }

    public boolean isSessionFunction() {
        return !isActorFunction() && getParameters().getFieldNames().size() == 0;
    }

    @Override
    public String toString() {
        String s = "";
        Vector<String> fieldNames = getParameters().getFieldNames();
        for (Iterator<String> iter = fieldNames.iterator(); iter.hasNext();) {
            String name = iter.next();
            s += getParameters().getFieldDefinition(name).getType() + " " + name;
            if (iter.hasNext()) {
                s += ", ";
            }
        }
        s += "";
        return (org.apache.commons.lang.StringUtils.isNotBlank(sessionVariableName) ? sessionVariableName + "%" : "")
                + getName() + "(" + s + ") { " + queryFragment.trim() + " } "
                + (org.apache.commons.lang.StringUtils.isNotBlank(errorMessage) ? ":\"" + errorMessage + "\"" : "");
    }

}