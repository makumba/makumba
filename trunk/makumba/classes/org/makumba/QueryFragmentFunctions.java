package org.makumba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.makumba.DataDefinition.QueryFragmentFunction;

/**
 * This class works as a store for the query functions of a {@link DataDefinition}. It provides methods to get all or
 * just specific types of functions.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class QueryFragmentFunctions {

    HashMap<String, QueryFragmentFunction> functionNames = new HashMap<String, QueryFragmentFunction>();

    /** adds a new function to this data definition. */
    public void addFunction(String name, QueryFragmentFunction function) {
        functionNames.put(name, function);
    }

    /** returns all functions in this data definition. */
    public Collection<QueryFragmentFunction> getFunctions() {
        return functionNames.values();
    }

    /** returns all actor functions in this data definition. */
    public Collection<QueryFragmentFunction> getActorFunctions() {
        ArrayList<QueryFragmentFunction> actorFunctions = new ArrayList<QueryFragmentFunction>();
        for (QueryFragmentFunction function : functionNames.values()) {
            if (function.isActorFunction()) {
                actorFunctions.add(function);
            }
        }
        return actorFunctions;
    }

    /** returns all actor functions in this data definition. */
    public Collection<QueryFragmentFunction> getSessionFunctions() {
        ArrayList<QueryFragmentFunction> sessionFunctions = new ArrayList<QueryFragmentFunction>();
        for (QueryFragmentFunction function : functionNames.values()) {
            if (function.isSessionFunction()) {
                sessionFunctions.add(function);
            }
        }
        return sessionFunctions;
    }

    /** Returns the function with the specific name. */
    public QueryFragmentFunction getFunction(String name) {
        return functionNames.get(name);
    }

    public int size() {
        return functionNames.size();
    }

}
