///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.makumba.Attributes;
import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.FieldValueDiff;
import org.makumba.InvalidValueException;
import org.makumba.LogicException;
import org.makumba.LogicInvocationError;
import org.makumba.LogicNotFoundException;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.QueryFragmentFunction;
import org.makumba.Transaction;
import org.makumba.UnauthenticatedException;
import org.makumba.UnauthorizedException;
import org.makumba.commons.DbConnectionProvider;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

/** business logic administration */
public class Logic {
    static HashMap<String, Object> nameToObject = new HashMap<String, Object>();

    private static DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    static public String getSearchMessage(String cls) {
        @SuppressWarnings("unchecked")
        Hashtable<String, String> hashtable = (Hashtable<String, String>) NamedResources.getStaticCache(logix).getSupplementary();
        return hashtable.get(cls);
    }

    public static Object getController(String className) {
        if (nameToObject.get(className) == null) {
            try {
                Object controller = Class.forName(className).newInstance();
                nameToObject.put(className, controller);
                return controller;
            } catch (ClassNotFoundException e) {
                java.util.logging.Logger.getLogger("org.makumba.controller").log(Level.SEVERE,
                    "Error while trying to load controller class " + className, e);
            } catch (InstantiationException e) {
                java.util.logging.Logger.getLogger("org.makumba.controller").log(Level.SEVERE,
                    "Error while trying to load controller class " + className, e);
            } catch (IllegalAccessException e) {
                java.util.logging.Logger.getLogger("org.makumba.controller").log(Level.SEVERE,
                    "Error while trying to load controller class " + className, e);
            }
        }
        return nameToObject.get(className);
    }

    /**
     * Finds the name of the package to be used for the given directory.<br>
     * 
     * @param directory
     *            the directory to find the package for.
     * @return the package name found, or an empty String in case no package was found.
     */
    public static String findPackageName(String directory) {
        String packageName = "";
        String defaultPackage = "";
        String longestKey = "";

        if (Configuration.getLogicPackages() != null) {
            for (String k : Configuration.getLogicPackages().keySet()) {
                if (k.equals("default") && longestKey.length() == 0) {
                    defaultPackage = Configuration.getLogicPackages().get(k);
                } else if (directory.startsWith(k) && k.length() > longestKey.length()) {
                    longestKey = k;
                    packageName = Configuration.getLogicPackages().get(k);
                }
            }
            if (longestKey.length() == 0 && defaultPackage.length() > 0) {
                packageName = defaultPackage;
            }
        }
        return packageName;
    }

    static int logix = NamedResources.makeStaticCache("Business logic classes", new NamedResourceFactory() {
        private static final long serialVersionUID = 1L;
        {
            supplementary = new Hashtable<Object, Object>();
        }

        @Override
        protected Object makeResource(Object p) {
            String path = (String) p;
            String msg = "Searching for business logic for " + path + ":";
            String className = "";
            int n = path.lastIndexOf(".");
            if (n != -1) {
                path = path.substring(0, n);
            }

            String defa = "";
            String maxKey = "";

            // FIXME: maybe unify code with public static String findPackageName(String)
            if (Configuration.getLogicPackages() != null) {
                msg += "\nfollowing rules from " + Configuration.MAKUMBA_CONF + " found at:\n\t"
                        + Configuration.getApplicationConfigurationSource();
                if (Configuration.getLogicPackages() != null) {
                    for (String k : Configuration.getLogicPackages().keySet()) {
                        if (k.equals("default") && maxKey.length() == 0) {
                            defa = Configuration.getLogicPackages().get(k);
                            continue;
                        }
                        if (path.startsWith(k) && k.length() > maxKey.length()) {
                            maxKey = k;
                            className = Configuration.getLogicPackages().get(k);
                            if (className.length() > 0 && className.lastIndexOf(".") != className.length() - 1) {
                                className += ".";
                            }
                        }
                    }
                }
                if (maxKey.length() == 0 && defa.length() > 0) {
                    msg += "\nfollowing default rule from " + Configuration.MAKUMBA_CONF;
                    className = defa + ".";
                } else if (maxKey.length() > 0) {
                    msg += "\nfollowing rule based on longest matching key from " + Configuration.MAKUMBA_CONF
                            + "\n\tkey is: \"" + maxKey + "\"";
                }
                path = path.substring(maxKey.length());
            } else {
                msg += "\ncould not find " + Configuration.MAKUMBA_CONF + " in CLASSPATH";
            }

            msg += "\ndetermined base: \"" + className + "\"";
            StringTokenizer st = new StringTokenizer(path, "/");

            Object lastFound = null;
            String dir = " ";

            loop: while (true) {
                String base = className;
                for (int i = 1; i <= dir.length(); i++) {
                    if (i == dir.length() || Character.isUpperCase(dir.charAt(i))) {
                        className = base + dir.substring(0, i).trim();
                        try {
                            msg += "\ntrying \"" + className + "Logic\"";
                            lastFound = Class.forName(className + "Logic").newInstance();
                            msg += "... found.";
                        } catch (ClassNotFoundException e) {
                            msg += "... not found";
                        } catch (IllegalAccessException f) {
                            msg += "... no public constructor";
                        } catch (InstantiationException f) {
                            msg += "... abstract class";
                        }
                    }
                }
                while (st.hasMoreTokens()) {
                    dir = st.nextToken();
                    if (dir.length() == 0) {
                        continue;
                    } else {
                        dir = firstUpper(dir);
                        continue loop;
                    }
                }

                break;
            }

            if (lastFound == null) {
                msg += "\nNo matching class found for " + p + "!";
                lastFound = new LogicNotFoundException(msg);
            } else {
                msg += "\nFound class " + lastFound.getClass().getName();
            }

            java.util.logging.Logger.getLogger("org.makumba.controller").info(msg);
            @SuppressWarnings("unchecked")
            Hashtable<Object, Object> supl = (Hashtable<Object, Object>) supplementary;
            supl.put(p, msg);
            Object foundClass = nameToObject.get(lastFound.getClass().getName());
            if (foundClass != null) {
                return foundClass;
            } else {
                return lastFound;
            }

        }
    }, false);

    static class AuthorizationConstraint {
        String key;

        String value;

        String rule;

        String fromWhere;

        String message;

        void check() {
            if (message == null || message.length() == 0) {
                message = "Authorization constraint failed: " + key + "= " + value;
            }
        }
    }

    /** gets the authorization constraint associated with the given URI path */
    public static Object getAuthorizationConstraint(String path) {
        return NamedResources.getStaticCache(authConstraints).getResource(path);
    }

    static int authConstraints = NamedResources.makeStaticCache("Authorization constraints",
        new NamedResourceFactory() {
            private static final long serialVersionUID = 1L;

            @Override
            protected Object makeResource(Object p) {
                final Map<String, String> authorizationDefinitions = Configuration.getAuthorizationDefinitions();
                if (authorizationDefinitions == null) {
                    return "none";
                }
                String path = (String) p;
                int n = path.lastIndexOf(".");
                if (n != -1) {
                    path = path.substring(0, n);
                }

                String maxKey = "";
                String rule = "none";
                String params = "";

                for (String k : authorizationDefinitions.keySet()) {
                    if (path.startsWith(k) && k.length() > maxKey.length()) {
                        maxKey = k;
                        rule = authorizationDefinitions.get(k);
                    }
                }

                if (rule.equals("none")) {
                    return rule;
                }

                AuthorizationConstraint ac = new AuthorizationConstraint();
                ac.key = maxKey;
                ac.value = rule;

                rule = rule.trim();
                params = "";
                int lpar = rule.indexOf("(");
                if (lpar == 0) {
                    int rpar = rule.indexOf(")");
                    if (rpar == -1) {
                        throw new ProgrammerError("Parameter list should end with ) in authorization constraint "
                                + maxKey);
                    }
                    params = rule.substring(1, rpar);
                    rule = rule.substring(rpar + 1).trim();
                    if (params.trim().length() == 0) {
                        throw new ProgrammerError("Parameter list should not be empty in authorization constraint "
                                + maxKey);
                    }
                }
                int ms = rule.indexOf("}");
                if (!rule.startsWith("{") || ms < 2) {
                    throw new ProgrammerError("body not found for authorization constraint " + maxKey);
                }
                ac.message = rule.substring(ms + 1);
                rule = rule.substring(1, ms);
                ac.rule = rule.trim();
                if (rule.trim().length() == 0) {
                    throw new ProgrammerError("empty body for authorization constraint " + maxKey);
                }
                if (params.length() > 0) {
                    QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(getTransactionProvider(getLogic(path)).getQueryLanguage());
                    Map<String, DataDefinition> m = qap.getQueryAnalysis("SELECT 1 FROM " + params).getLabelTypes();
                    StringBuffer where = new StringBuffer();
                    String separator = "";
                    for (String label : m.keySet()) {
                        where.append(separator).append(label).append("=").append(qap.getParameterSyntax()).append(label);
                        separator = " AND ";
                    }
                    ac.fromWhere = " FROM " + params + " WHERE " + where;
                }
                ac.check();
                return ac;
            }
        }, false);

    static String[] separators = { ".", "->" };

    public static String upperCase(String a) {
        String ret = "";
        while (true) {
            int minimum = a.length();
            int imin = -1;
            for (int i = 0; i < separators.length; i++) {
                int j = a.indexOf(separators[i]);
                if (j != -1 && j < minimum) {
                    minimum = j;
                    imin = i;
                }
            }

            ret += firstUpper(a.substring(0, minimum));
            if (imin == -1) {
                return ret;
            }
            a = a.substring(minimum + separators[imin].length());
        }
    }

    static String firstUpper(String a) {
        char f = Character.toUpperCase(a.charAt(0));
        if (a.length() > 1) {
            return f + a.substring(1);
        }
        return "" + f;
    }

    /** gets the logic associated with the given package and path according to directory rules */
    public static Object getLogic(String path) {
        return NamedResources.getStaticCache(logix).getResource(path);
    }

    @SuppressWarnings("deprecation")
    static Class<?>[] argDbOld = { Attributes.class, org.makumba.Database.class };

    static Class<?>[] argDb = { Attributes.class, Transaction.class };

    public static Object getAttribute(Object controller, String attname, Attributes a, String db,
            DbConnectionProvider dbcp) throws NoSuchMethodException, LogicException {
        if (attname.startsWith("actor_")) {
            return computeActor(attname, a, db, dbcp);
        }
        if (controller instanceof LogicNotFoundException) {
            throw new NoSuchMethodException("no controller=> no attribute method");
        }
        // this will throw nosuchmethodexception if the findXXX method is missing so this method will kinda finish here
        Method m = null;
        // JASPER: I don't understand why this way of looking up a method is different frmo the others
        try {
            m = controller.getClass().getMethod("find" + firstUpper(attname), argDb);
        } catch (Exception e) {
            if (m == null) {
                m = controller.getClass().getMethod("find" + firstUpper(attname), argDbOld);
                java.util.logging.Logger.getLogger("org.makumba.controller").fine(
                    "The use of Database is deprecated. Use Transaction instead.");
            }
        }
        // This doesn't seem to work.
        // m = getMethod("find" + firstUpper(attname), argDb, argDbOld, controller);
        Transaction d = dbcp.getConnectionTo(db);
        Object[] args = { a, d };
        try {
            return m.invoke(controller, args);
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(e.getMessage());
        } catch (InvocationTargetException f) {
            d.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException) {
                throw (LogicException) g;
            }
            throw new LogicInvocationError(g);
        }
    }

    public static Object computeActor(String attname, Attributes a, String db, DbConnectionProvider dbcp)
            throws LogicException {
        String type = attname.substring(6).replace('_', '.');
        DataDefinition dd = ddp.getDataDefinition(type);
        String field = null;
        if (dd == null) {
            int lastDot = type.lastIndexOf('.');
            if (lastDot == -1) {
                throw new ProgrammerError("Unknown actor: " + type);
            }
            type = type.substring(0, lastDot);
            field = type.substring(lastDot + 1);
            dd = ddp.getDataDefinition(type);
            if (dd == null) {
                throw new ProgrammerError("Unknown actor: " + type + "." + field);
            }
        }

        QueryFragmentFunction match = null;
        MakumbaActorHashMap matchValues = null;
        QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(dbcp.getTransactionProvider().getQueryLanguage());
        nextFunction: for (QueryFragmentFunction f : ddp.getQueryFragmentFunctions(dd.getName()).getActorFunctions()) {
            MakumbaActorHashMap values = new MakumbaActorHashMap();
            DataDefinition params = f.getParameters();
            if (match != null
                    && match.getParameters().getFieldDefinitions().size() > params.getFieldDefinitions().size()) {
                continue; // don't look at this function if we already have a match with more parameters
            }
            for (FieldDefinition fd : params.getFieldDefinitions()) {
                try {
                    // check if all the params defined in the function exist as parameter
                    values.put(fd.getName(), a.getAttribute(fd.getName()));
                    // TODO: check if the value is assignable to the function parameter type
                } catch (LogicException ae) {
                    continue nextFunction;
                }
            }
            match = f;
            matchValues = values;
        }
        if (match == null) {
            if (ddp.getQueryFragmentFunctions(dd.getName()).getActorFunctions().size() == 0) { // if we have no actor
                // function at all
                // report this as programmer error
                throw new ProgrammerError("No fitting actor() function was found in " + type);
            } else {
                // otherwise, if there is no fitting function, throw UnauthenticatedException to trigger login
                //
                // FIXME: this is not totally correct, cause in the case of having an actor function defined
                // and coming from the login page, but having a mismatch of the parameter names in the function with the
                // inputs in the login form, we will still report UnauthenticatedException, even though we should throw
                // a ProgrammerError
                throw new UnauthenticatedException("Please provide username and password");
            }
        }
        java.util.logging.Logger.getLogger("org.makumba.db.query.inline").fine(match + " \n" + a);

        StringBuffer funcCall = new StringBuffer();
        funcCall.append("SELECT ").append(qap.getPrimaryKeyNotation("x")).append(" AS col1 FROM ").append(type).append(
            " x WHERE x.").append(match.getName());
        funcCall.append("(");
        String separator = "";

        DataDefinition params = match.getParameters();
        for (FieldDefinition fd : params.getFieldDefinitions()) {
            funcCall.append(separator);
            separator = ", ";
            funcCall.append(qap.getParameterSyntax()).append(fd.getName());
        }
        funcCall.append(")");
        Transaction connection = dbcp.getConnectionTo(db);
        Vector<Dictionary<String, Object>> v;
        try {
            v = connection.executeQuery(funcCall.toString(), matchValues);
        } catch (MakumbaError e) {
            throw new ProgrammerError("Error while computing actor " + attname + " during execution of query "
                    + funcCall.toString() + " " + e.getMessage());
        }
        if (v.size() == 0) {
            if (match.getErrorMessage().trim().length() > 0) {
                throw new UnauthenticatedException(match.getErrorMessage().trim());
            } else {
                throw new UnauthenticatedException("Could not instantiate actor of type " + type);
            }
        } else if (v.size() > 1) {
            throw new LogicException("Multiple " + type + " objects fit the actor function " + match);
        }

        Pointer p = (Pointer) v.elementAt(0).get("col1");
        Dictionary<String, Object> obj = connection.read(p, null);

        MakumbaActorHashMap ret = new MakumbaActorHashMap();
        String att = actorPrefix(dd);
        ret.put(att, p);

        for (Enumeration<String> e = obj.keys(); e.hasMoreElements();) {
            String k = e.nextElement();
            ret.put(att + "_" + k, obj.get(k));
        }

        // computeThickActorSession(attname, type, dd, qap, connection, p, ret, att);

        return ret;
    }

    /** Computes a thick session of the current actor, by evaluating all functions with no parameters. */
    protected static void computeThickActorSession(String attname, String type, DataDefinition dd,
            QueryAnalysisProvider qap, Transaction connection, Pointer p, MakumbaActorHashMap ret, String att)
            throws ProgrammerError {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("x", p);
        for (QueryFragmentFunction g : ddp.getQueryFragmentFunctions(dd.getName()).getSessionFunctions()) {
            StringBuffer fc = new StringBuffer();
            fc.append("SELECT x.").append(g.getName()).append("() AS col1 FROM ").append(type).append(" x WHERE x=").append(
                qap.getParameterSyntax()).append("x");
            Object result;
            try {
                result = connection.executeQuery(fc.toString(), param).elementAt(0).get("col1");
            } catch (MakumbaError e) {
                throw new ProgrammerError("Error while computing function " + g.getName() + " of actor " + attname
                        + " during execution of query " + fc.toString() + " " + e.getMessage());
            }
            ret.put(att + "_" + g.getName(), result);
            if (g.getSessionVariableName() != null) {
                ret.put(g.getSessionVariableName(), result);
            }
        }
    }

    private static String actorPrefix(DataDefinition dd) {
        return "actor_" + dd.getName().replace(".", "_");
    }

    public static Set<String> logoutActor(DataDefinition dd) {
        Set<String> ret = new HashSet<String>();
        String att = actorPrefix(dd);
        ret.add(att);
        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            ret.add(att + "_" + fd.getName());
        }
        for (QueryFragmentFunction g : ddp.getQueryFragmentFunctions(dd.getName()).getSessionFunctions()) {
            ret.add(att + "_" + g.getName());
        }
        return ret;
    }

    @SuppressWarnings("deprecation")
    static Class<?>[] editArgsOld = { Pointer.class, Dictionary.class, Attributes.class, org.makumba.Database.class };

    static Class<?>[] editArgs = { Pointer.class, Dictionary.class, Attributes.class, Transaction.class };

    @SuppressWarnings("deprecation")
    static Class<?>[] opArgsOld = { Dictionary.class, Attributes.class, org.makumba.Database.class };

    static Class<?>[] opArgs = { Dictionary.class, Attributes.class, Transaction.class };

    static Class<?>[] noClassArgs = {};

    static Object[] noObjectArgs = {};

    public static String getControllerFile(Object controller) {
        String ctrlClass = controller.getClass().getName();
        java.net.URL u = org.makumba.commons.ClassResource.get(ctrlClass.replace('.', '/') + ".java");
        if (u != null) {
            return getFilePath(u);
        }
        return org.makumba.commons.ClassResource.get(ctrlClass.replace('.', '/') + ".class").toString();
    }

    public static String getFilePath(java.net.URL u) {
        try {
            return new java.io.File(u.getFile()).getCanonicalPath();
        } catch (java.io.IOException ioe) {
            throw new org.makumba.commons.RuntimeWrappedException(ioe);
        }
    }

    public static Method getMethod(String name, Class<?>[] args, Object controller) {
        try {
            Method m = null;
            if (args != null) {
                m = controller.getClass().getMethod(name, args);
            } else {
                m = controller.getClass().getMethod(name);
            }
            if (!Modifier.isPublic(m.getModifiers())) {
                return null;
            }
            return m;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Method getMethod(String name, Class<?>[] args, Class<?>[] argsOld, Object controller) {
        /*
         * This version of getMethod was introduced to look up methods for which an argument type changed/deprecated
         * (More specifically: org.makumba.Database got replaced by org.makumba.Transaction), but to allow backward
         * compatibility at the same time.
         */
        Method m = getMethod(name, args, controller);
        if (m == null) {
            m = getMethod(name, argsOld, controller);
            java.util.logging.Logger.getLogger("org.makumba.controller").fine(
                "In " + controller.getClass().getName() + "." + name
                        + ": The use of Database is deprecated. Use Transaction instead.");
        }
        return m;
    }

    public static void doInit(String path, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        Object o = getAuthorizationConstraint(path);
        if (!(o instanceof AuthorizationConstraint)) {
            return;
        }
        AuthorizationConstraint constraint = (AuthorizationConstraint) o;
        QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(dbcp.getTransactionProvider().getQueryLanguage());

        String query = "SELECT " + constraint.rule + " AS col1 ";
        if (constraint.fromWhere != null) {
            query += constraint.fromWhere;
        }
        Vector<Dictionary<String, Object>> v;
        try {
            v = dbcp.getConnectionTo(dbName).executeQuery(query, null);
        } catch (MakumbaError e) {
            throw new ProgrammerError("Error while checking authorization constraint " + constraint.key
                    + " during execution of query " + query + " " + e.getMessage());
        }
        if (v.size() > 1) {
            throw new ProgrammerError("Authorization constraint returned multiple values: " + constraint.key + "="
                    + constraint.value);
        }
        if (v.size() == 0) {
            throw new UnauthorizedException(constraint.message);
        }
        Object result = v.elementAt(0).get("col1");
        if (result == null || result.equals(Pointer.Null) || result.equals(0) || result.equals(false)) {
            throw new UnauthorizedException(constraint.message);
        }
    }

    public static void doInit(Object controller, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        if (controller instanceof LogicNotFoundException) {
            return;
        }

        Transaction db = dbcp.getConnectionTo(dbName);
        Method init = getMethod("checkAttributes", argDb, argDbOld, controller);
        if (init == null) {
            return;
        }
        if (init != null) {
            Object[] args = { a, db };
            try {
                init.invoke(controller, args);
            } catch (IllegalAccessException g) {
                throw new LogicInvocationError(g);
            } catch (InvocationTargetException f) {
                db.rollback();
                Throwable g = f.getTargetException();
                if (g instanceof LogicException) {
                    throw (LogicException) g;
                }
                throw new LogicInvocationError(g);
            }
        }
    }

    public static TransactionProvider getTransactionProvider(Object controller) throws LogicInvocationError {
        Method connectionProvider = null;
        if (controller != null) {
            connectionProvider = Logic.getMethod("getTransactionProvider", null, controller);
        }
        String transactionProviderClass = null;
        try {
            if (connectionProvider != null) {
                transactionProviderClass = (String) connectionProvider.invoke(controller);
            }

            if (transactionProviderClass == null) {
                return TransactionProvider.getInstance();
            }

            Method getInstance = Class.forName(transactionProviderClass).getDeclaredMethod("getInstance",
                new Class<?>[] {});
            return (TransactionProvider) getInstance.invoke(null, new Object[] {});

        } catch (Throwable e) {
            LogicException le = new LogicException("Could not instantiate transaction provider "
                    + transactionProviderClass != null ? transactionProviderClass : "");
            throw new LogicInvocationError(le);
        }
    }

    public static Object doOp(Object controller, String opName, Dictionary<String, Object> data, Attributes a,
            String dbName, DbConnectionProvider dbcp) throws LogicException {
        if (opName == null) {
            return null;
        }
        if (controller instanceof LogicNotFoundException) {
            throw new ProgrammerError("there is no controller object to look for the Form handler method " + opName);
        }

        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] editArg = { data, a, db };
        Method op = null;
        op = getMethod(opName, opArgs, opArgsOld, controller);
        if (op == null) {
            throw new ProgrammerError("Class " + controller.getClass().getName() + " (" + getControllerFile(controller)
                    + ")\n" + "does not define the method\n" + "public void " + opName
                    + "(Dictionary d, Attributes a, Database db)" + " throws LogicException {}" + "\n"
                    + "The method is declared as a makumba form handler, so it has to be defined");
        }

        try {
            return op.invoke(controller, editArg);
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException) {
                throw (LogicException) g;
            }
            if (g instanceof CompositeValidationException) {
                throw (CompositeValidationException) g;
            }
            throw new LogicInvocationError(g);
        }
    }

    public static List<FieldValueDiff> doEdit(Object controller, String handlerName, String afterHandlerName,
            String typename, Pointer p, Dictionary<String, Object> data, Attributes a, String dbName,
            DbConnectionProvider dbcp, String recordChangesIn) throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] editArg = { p, data, a, db };
        Method edit = null;
        Method afterEdit = null;
        List<FieldValueDiff> diff = null;

        if (!(controller instanceof LogicNotFoundException)) {
            edit = getMethod(handlerName, editArgs, editArgsOld, controller);
            afterEdit = getMethod(afterHandlerName, editArgs, editArgsOld, controller);
            // if (edit == null) {
            // throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
            // + getControllerFile(controller) + ")\n" + "does not define the method\n" + HANDLER_METHOD_HEAD
            // + handlerName + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END
            // + "\n" + "so it does not allow EDIT operations on the type " + typename
            // + "\nDefine that method (even with an empty body) to allow such operations.");
            // }
        }

        try {
            if (edit != null) {
                edit.invoke(controller, editArg);
            }
            if (StringUtils.isNotBlank(recordChangesIn)) {
                diff = db.updateWithValueDiff(p, data);
            } else {
                db.update(p, data);
            }
            if (afterEdit != null) {
                afterEdit.invoke(controller, editArg);
            }
            return diff;
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException) {
                throw (LogicException) g;
            }
            throw new LogicInvocationError(g);
        }
    }

    @SuppressWarnings("deprecation")
    static Class<?>[] deleteArgsOld = { Pointer.class, Attributes.class, org.makumba.Database.class };

    static Class<?>[] deleteArgs = { Pointer.class, Attributes.class, Transaction.class };

    public static Pointer doDelete(Object controller, String typename, Pointer p, Attributes a, String dbName,
            DbConnectionProvider dbcp) throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] deleteArg = { p, a, db };
        Method delete = null;
        Method afterDelete = null;
        String upper = upperCase(typename);

        if (!(controller instanceof LogicNotFoundException)) {
            delete = getMethod("on_delete" + upper, deleteArgs, deleteArgsOld, controller);
            afterDelete = getMethod("after_delete" + upper, deleteArgs, deleteArgsOld, controller);
            // if (delete == null) {
            // throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
            // + getControllerFile(controller) + ")\n" + "does not define any of the methods\n"
            // + HANDLER_METHOD_HEAD + "on_delete" + upper + "(Pointer p, Attributes a, Database db)"
            // + HANDLER_METHOD_END + "\n" + HANDLER_METHOD_HEAD + "after_delete" + upper
            // + "(Pointer p, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
            // + "so it does not allow DELETE operations on the type " + typename
            // + "\nDefine that method (even with an empty body) to allow such operations.");
            // }
        }

        try {
            if (delete != null) {
                delete.invoke(controller, deleteArg);
            }
            db.delete(p);
            if (afterDelete != null) {
                afterDelete.invoke(controller, deleteArg);
            }
            return null;
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException) {
                throw (LogicException) g;
            }
            throw new LogicInvocationError(g);
        }
    }

    public static Pointer doAdd(Object controller, String handlerName, String afterHandlerName, String typename,
            Pointer p, Dictionary<String, Object> data, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] addArg = { p, data, a, db };
        Method on = null;
        Method after = null;
        int n = typename.lastIndexOf("->");
        String field = typename.substring(n + 2);
        typename = typename.substring(0, n);

        if (!(controller instanceof LogicNotFoundException)) {
            on = getMethod(handlerName, editArgs, editArgsOld, controller);
            after = getMethod(afterHandlerName, editArgs, editArgsOld, controller);

            // if (on == null && after == null) {
            // throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
            // + getControllerFile(controller) + ")\n" + "does not define neither of the methods\n"
            // + HANDLER_METHOD_HEAD + handlerName + "(Pointer p, Dictionary d, Attributes a, Database db)"
            // + HANDLER_METHOD_END + "\n" + HANDLER_METHOD_HEAD + afterHandlerName
            // + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
            // + "so it does not allow ADD operations on the type " + typename + ", field " + field
            // + "\nDefine any of the methods (even with an empty body) to allow such operations.");
            // }
        }

        try {
            if (on != null) {
                on.invoke(controller, addArg);
            }
            addArg[0] = db.insert(p, field, data);
            if (after != null) {
                after.invoke(controller, addArg);
            }
            return (Pointer) addArg[0];
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException) {
                throw (LogicException) g;
            }
            throw new LogicInvocationError(g);
        }
    }

    @SuppressWarnings("deprecation")
    static Class<?>[] newArgsOld = { Dictionary.class, Attributes.class, org.makumba.Database.class };

    static Class<?>[] newArgs = { Dictionary.class, Attributes.class, Transaction.class };

    public static Pointer doNew(Object controller, String handlerName, String afterHandlerName, String typename,
            Dictionary<String, Object> data, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] onArgs = { data, a, db };
        Object[] afterArgs = { null, data, a, db };
        Method on = null;
        Method after = null;

        if (!(controller instanceof LogicNotFoundException)) {
            on = getMethod(handlerName, newArgs, newArgsOld, controller);
            after = getMethod(afterHandlerName, editArgs, editArgsOld, controller);
            // if (on == null && after == null) {
            // throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
            // + getControllerFile(controller) + ")\n" + "does not define neither of the methods\n"
            // + HANDLER_METHOD_HEAD + handlerName + "(Dictionary d, Attributes a, Database db)"
            // + HANDLER_METHOD_END + "\n" + HANDLER_METHOD_HEAD + afterHandlerName
            // + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
            // + "so it does not allow NEW operations on the type " + typename
            // + ".\nDefine any of the methods (even with an empty body) to allow such operations.");
            // }
        }

        try {
            if (on != null) {
                on.invoke(controller, onArgs);
            }
            afterArgs[0] = db.insert(typename, data);
            if (after != null) {
                after.invoke(controller, afterArgs);
            }
            return (Pointer) afterArgs[0];
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            return handleException(f.getTargetException());
        }
    }

    /**
     * Handles an exception caught while processing form operations. Does the following operations:
     * <ul>
     * <li>passes on {@link LogicException}</li>
     * <li>passes on {@link InvalidValueException}</li>
     * <li>Wraps a {@link InvalidValueException} into a {@link CompositeValidationException} and passes it on</li>
     * <li>Wraps any other {@link Throwable} into a {@link LogicInvocationError}</li>
     * </ul>
     */
    private static Pointer handleException(Throwable g) throws LogicException, LogicInvocationError {
        if (g instanceof LogicException) {
            throw (LogicException) g;
        } else if (g instanceof InvalidValueException) {
            // pack the exception in a CompositeValidationException, cause that will be treated later on
            throw new CompositeValidationException((InvalidValueException) g);
        } else if (g instanceof CompositeValidationException) {
            // just propagate the exception, will be treated later on
            throw (CompositeValidationException) g;
        }
        throw new LogicInvocationError(g);
    }
}
