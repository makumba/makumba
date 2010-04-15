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

    HashMap<String, QueryFragmentFunction> functionNameCache = new HashMap<String, QueryFragmentFunction>();

    ArrayList<QueryFragmentFunction> functions = new ArrayList<QueryFragmentFunction>();

    /** adds a new function to this data definition. */
    public void addFunction(String name, QueryFragmentFunction function) {
        functions.add(function);
        functionNameCache.put(name, function);
    }

    /** returns all functions in this data definition. */
    public Collection<QueryFragmentFunction> getFunctions() {
        return functions;
    }

    /** returns all actor functions in this data definition. */
    public Collection<QueryFragmentFunction> getActorFunctions() {
        ArrayList<QueryFragmentFunction> actorFunctions = new ArrayList<QueryFragmentFunction>();
        for (QueryFragmentFunction function : functions) {
            if (function.isActorFunction()) {
                actorFunctions.add(function);
            }
        }
        return actorFunctions;
    }

    /** returns all actor functions in this data definition. */
    public Collection<QueryFragmentFunction> getSessionFunctions() {
        ArrayList<QueryFragmentFunction> sessionFunctions = new ArrayList<QueryFragmentFunction>();
        for (QueryFragmentFunction function : functions) {
            if (function.isSessionFunction()) {
                sessionFunctions.add(function);
            }
        }
        return sessionFunctions;
    }

    /** Returns the function with the specific name. */
    public QueryFragmentFunction getFunction(String name) {
        return functionNameCache.get(name);
    }

    /** Returns the function with the specific name and parameters. */
    public QueryFragmentFunction getFunction(String name, DataDefinition params) {
        for (QueryFragmentFunction function : functions) {
            if (function.getName().equals(name) && function.getParameters().equals(params)) {
                return function;
            }
        }
        return null;
    }

    public int size() {
        return functionNameCache.size();
    }

}
