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
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import org.makumba.Attributes;
import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.LogicInvocationError;
import org.makumba.LogicNotFoundException;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.commons.DbConnectionProvider;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProviderInterface;

/** busines logic administration */
public class Logic {
    static Properties controllerConfig;

    static java.net.URL controllerURL;

    static HashMap<String, Object> nameToObject = new HashMap<String, Object>();

    /** for the default transaction provider * */
    static Configuration configuration = new Configuration();

    private static final String HANDLER_METHOD_HEAD = "public void ";

    private static final String HANDLER_METHOD_END = " throws LogicException {}";

    private static DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    static {
        controllerConfig = new Properties();
        try {
            controllerURL = org.makumba.commons.ClassResource.get("MakumbaController.properties");
            controllerConfig.load(controllerURL.openStream());
        } catch (Exception e) {
            controllerConfig = null;
        }
    }

    static public String getSearchMessage(String cls) {
        return (String) ((Hashtable) NamedResources.getStaticCache(logix).getSupplementary()).get(cls);
    }

    public static Object getController(String className) {
        if (nameToObject.get(className) == null) {
            try {
                Object controller = Class.forName(className).newInstance();
                nameToObject.put(className, controller);
                return controller;
            } catch (ClassNotFoundException e) {
                java.util.logging.Logger.getLogger("org.makumba." + "controller").log(Level.SEVERE,
                    "Error while trying to load controller class " + className, e);
            } catch (InstantiationException e) {
                java.util.logging.Logger.getLogger("org.makumba." + "controller").log(Level.SEVERE,
                    "Error while trying to load controller class " + className, e);
            } catch (IllegalAccessException e) {
                java.util.logging.Logger.getLogger("org.makumba." + "controller").log(Level.SEVERE,
                    "Error while trying to load controller class " + className, e);
            }
        }
        return nameToObject.get(className);
    }

    /**
     * Finds the name of the package to be used for the given directory. The method uses the {@link #controllerConfig}
     * to find a matching package name.
     * 
     * @param directory
     *            the directory to find the package for.
     * @return the package name found, or an empty String in case no package was found.
     */
    public static String findPackageName(String directory) {
        String packageName = "";
        String defaultPackage = "";
        String longestKey = "";

        if (controllerConfig != null) {
            for (Enumeration e = controllerConfig.keys(); e.hasMoreElements();) {
                String k = (String) e.nextElement();

                if (k.equals("default") && longestKey.length() == 0) {
                    defaultPackage = controllerConfig.getProperty(k);
                } else if (directory.startsWith(k) && k.length() > longestKey.length()) {
                    longestKey = k;
                    packageName = controllerConfig.getProperty(k);
                }
            }
            if (longestKey.length() == 0 && defaultPackage.length() > 0) {
                packageName = defaultPackage;
            }
        }
        return packageName;
    }

    static int logix = NamedResources.makeStaticCache("Business logic classes", new NamedResourceFactory() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        {
            supplementary = new Hashtable();
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

            if (controllerConfig != null) {
                msg += "\nfollowing rules from MakumbaController.properties found at:\n\t" + getFilePath(controllerURL);
                for (Enumeration e = controllerConfig.keys(); e.hasMoreElements();) {
                    String k = (String) e.nextElement();

                    if (k.equals("default") && maxKey.length() == 0) {
                        defa = controllerConfig.getProperty(k);
                        continue;
                    }
                    if (path.startsWith(k) && k.length() > maxKey.length()) {
                        maxKey = k;
                        className = controllerConfig.getProperty(k);
                        if (className.length() > 0 && className.lastIndexOf(".") != className.length() - 1) {
                            className += ".";
                        }
                    }
                }

                if (maxKey.length() == 0 && defa.length() > 0) {
                    msg += "\nfollowing default rule from MakumbaController.properties";
                    className = defa + ".";
                } else if (maxKey.length() > 0) {
                    msg += "\nfollowing rule based on longest matching key from MakumbaController.properties\n\tkey is: \""
                            + maxKey + "\"";
                }
                path = path.substring(maxKey.length());
            } else {
                msg += "\ncould not find MakumbaController.properties in CLASSPATH";
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

            java.util.logging.Logger.getLogger("org.makumba." + "controller").info(msg);
            ((Hashtable) supplementary).put(p, msg);
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
        
        void check(){
            if(message==null || message.length()==0){
                message="Authorization constraint failed: "+key+"= "+value;
            }
        }
    }

    /** gets the authorization constraint associated with the given URI path */
    public static Object getAuthorizationConstraint(String path) {
        return NamedResources.getStaticCache(authConstraints).getResource(path);
    }

    static int authConstraints = NamedResources.makeStaticCache("Authorization constraints",
        new NamedResourceFactory() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected Object makeResource(Object p) {
                if (controllerConfig == null)
                    return "none";
                String path = (String) p;
                int n = path.lastIndexOf(".");
                if (n != -1) {
                    path = path.substring(0, n);
                }

                String maxKey = "";
                String rule = "none";
                String params = "";

                for (Enumeration e = controllerConfig.keys(); e.hasMoreElements();) {
                    String k = (String) e.nextElement();
                    if (!k.startsWith("authorize%"))
                        continue;
                    String k1 = k.substring("authorize%".length());
                    if (path.startsWith(k1) && k1.length() > maxKey.length()) {
                        maxKey = k1;
                        rule = controllerConfig.getProperty(k);
                    }
                }
                String originalRule = rule;

                if (rule.equals("none"))
                    return rule;

                AuthorizationConstraint ac = new AuthorizationConstraint();
                ac.key = maxKey;
                ac.value = rule;

                rule = rule.trim();
                params = "";
                int lpar = rule.indexOf("(");
                if (lpar == 0) {
                    int rpar = rule.indexOf(")");
                    if (rpar == -1)
                        throw new ProgrammerError("Parameter list should end with ) in authorization constraint "
                                + maxKey);
                    params = rule.substring(1, rpar);
                    rule = rule.substring(rpar + 1).trim();
                    if (params.trim().length() == 0)
                        throw new ProgrammerError("Parameter list should not be empty in authorization constraint "
                                + maxKey);
                }
                int ms = rule.indexOf("}");
                if (!rule.startsWith("{") || ms < 2)
                    throw new ProgrammerError("body not found for authorization constraint " + maxKey);
                ac.message = rule.substring(ms + 1);
                rule = rule.substring(1, ms);
                ac.rule=rule.trim();
                if (rule.trim().length() == 0)
                    throw new ProgrammerError("empty body for authorization constraint " + maxKey);
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

    static Class<?>[] argDb = { Attributes.class, Database.class };

    public static Object getAttribute(Object controller, String attname, Attributes a, String db,
            DbConnectionProvider dbcp) throws NoSuchMethodException, LogicException {
        if (attname.startsWith("actor_"))
            return computeActor(attname, a, db, dbcp);
        if (controller instanceof LogicNotFoundException) {
            throw new NoSuchMethodException("no controller=> no attribute method");
        }
        // this will throw nosuchmethodexception if the findXXX method is missing so this method will kinda finish here
        Method m = (controller.getClass().getMethod("find" + firstUpper(attname), argDb));

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
        QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(dbcp.getTransactionProvider().getQueryLanguage());
        nextFunction: for (DataDefinition.QueryFragmentFunction f : dd.getFunctions()) {
            if (f.getName().startsWith("actor")) {
                StringBuffer funcCall = new StringBuffer();
                funcCall.append("SELECT ").append(qap.getPrimaryKeyNotation("x")).append(" AS col1 FROM ").append(type).append(
                    " x WHERE x.").append(f.getName()).append("(");
                String separator = "";
                DataDefinition params = f.getParameters();
                HashMap<String, Object> values = new HashMap<String, Object>();
                for (String para : params.getFieldNames()) {
                    try {
                        funcCall.append(separator);
                        separator = ", ";
                        funcCall.append(qap.getParameterSyntax()).append(para);
                        values.put(para, a.getAttribute(para));
                        // TODO: check if the value is assignable to the function parameter type
                    } catch (LogicException ae) {
                        continue nextFunction;
                    }
                }
                funcCall.append(")");
                Vector<Dictionary<String, Object>> v = dbcp.getConnectionTo(db).executeQuery(funcCall.toString(),
                    values);
                if (v.size() == 0) {
                    throw new LogicException(f.getErrorMessage());
                } else if (v.size() > 1) {
                    throw new LogicException("Multiple " + type + " objects fit the function " + f);
                } else {
                    // TODO: compute all statics!
                    // and return a hashmap, then request attributes will know to put them all in the session
                    return v.elementAt(0).get("col1");
                }
            }

        }
        throw new ProgrammerError("No fitting actor() function was found in " + type);
    }

    static Class<?>[] editArgs = { Pointer.class, Dictionary.class, Attributes.class, Database.class };

    static Class<?>[] opArgs = { Dictionary.class, Attributes.class, Database.class };

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
            return new java.io.File((u.getFile())).getCanonicalPath();
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

    public static void doInit(String path, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        Object o = getAuthorizationConstraint(path);
        if (!(o instanceof AuthorizationConstraint))
            return;
        AuthorizationConstraint constraint = (AuthorizationConstraint) o;

        Object result = null;
        if (constraint.fromWhere == null) {
            QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(dbcp.getTransactionProvider().getQueryLanguage());
            // we have no FROM and WHERE section, this leads in a hard-to-analyze query
            // so we try a paramter
            String q1 = qap.inlineFunctions(constraint.rule).trim();
            if (q1.startsWith(qap.getParameterSyntax()) && q1.substring(1).matches("[a-zA-Z]\\w*"))
                result = a.getAttribute(q1.substring(1));
        }

        if (result == null) {
            String query = "SELECT " + constraint.rule + " AS col1 ";
            if (constraint.fromWhere != null)
                query += constraint.fromWhere;

            Vector<Dictionary<String, Object>> v = dbcp.getConnectionTo(dbName).executeQuery(query, null);
            if (v.size() > 1)
                throw new ProgrammerError("Authorization constraint returned multiple values: " + constraint.key + "="
                        + constraint.value);
            if (v.size() == 0)
                throw new LogicException(constraint.message);
            result = v.elementAt(0).get("col1");
        }
        if (result == null || result.equals(Pointer.Null) || result.equals(0) || result.equals(false))
            throw new LogicException(constraint.message);
    }

    public static void doInit(Object controller, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        if (controller instanceof LogicNotFoundException) {
            return;
        }

        Transaction db = dbcp.getConnectionTo(dbName);
        Method init = getMethod("checkAttributes", argDb, controller);
        Method oldInit = getMethod("requiredAttributes", noClassArgs, controller);
        if (init == null && oldInit == null) {
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
        } else {
            java.util.logging.Logger.getLogger("org.makumba." + "controller").warning(
                "requiredAttributes() is deprecated. Use checkAttributes(Attributes a, Database db) instead");
            Object attrs = null;
            try {
                attrs = oldInit.invoke(controller, noObjectArgs);
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
            if (attrs == null) {
                return;
            }
            if (attrs instanceof String) {
                a.getAttribute((String) attrs);
                return;
            }
            if (attrs instanceof String[]) {
                for (int i = 0; i < ((String[]) attrs).length; i++) {
                    a.getAttribute(((String[]) attrs)[i]);
                }
                return;
            }
            return;
        }
    }

    public static TransactionProviderInterface getTransactionProvider(Object controller) throws LogicInvocationError {
        Method connectionProvider = null;
        if (controller != null)
            connectionProvider = Logic.getMethod("getTransactionProvider", null, controller);
        String transactionProviderClass = null;
        try {
            if (connectionProvider != null)
                transactionProviderClass = (String) connectionProvider.invoke(controller);

            if (transactionProviderClass == null) {
                transactionProviderClass = configuration.getDefaultTransactionProviderClass();
            }

            return (TransactionProviderInterface) Class.forName(transactionProviderClass).newInstance();
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
        if ((controller instanceof LogicNotFoundException)) {
            throw new ProgrammerError("there is no controller object to look for the Form handler method " + opName);
        }

        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] editArg = { data, a, db };
        Method op = null;
        op = getMethod(opName, opArgs, controller);
        if (op == null) {
            throw new ProgrammerError("Class " + controller.getClass().getName() + " (" + getControllerFile(controller)
                    + ")\n" + "does not define the method\n" + HANDLER_METHOD_HEAD + opName
                    + "(Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
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

    public static Pointer doEdit(Object controller, String handlerName, String afterHandlerName, String typename,
            Pointer p, Dictionary<String, Object> data, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] editArg = { p, data, a, db };
        Method edit = null;
        Method afterEdit = null;
        if (!(controller instanceof LogicNotFoundException)) {
            edit = getMethod(handlerName, editArgs, controller);
            afterEdit = getMethod(afterHandlerName, editArgs, controller);
            if (edit == null) {
                throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
                        + getControllerFile(controller) + ")\n" + "does not define the method\n" + HANDLER_METHOD_HEAD
                        + handlerName + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END
                        + "\n" + "so it does not allow EDIT operations on the type " + typename
                        + "\nDefine that method (even with an empty body) to allow such operations.");
            }
        }

        try {
            if (edit != null) {
                edit.invoke(controller, editArg);
            }
            db.update(p, data);
            if (afterEdit != null) {
                afterEdit.invoke(controller, editArg);
            }
            return p;
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

    static Class<?>[] deleteArgs = { Pointer.class, Attributes.class, Database.class };

    public static Pointer doDelete(Object controller, String typename, Pointer p, Attributes a, String dbName,
            DbConnectionProvider dbcp) throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] deleteArg = { p, a, db };
        Method delete = null;
        Method afterDelete = null;
        String upper = upperCase(typename);

        if (!(controller instanceof LogicNotFoundException)) {
            delete = getMethod("on_delete" + upper, deleteArgs, controller);
            afterDelete = getMethod("after_delete" + upper, deleteArgs, controller);
            if (delete == null) {
                throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
                        + getControllerFile(controller) + ")\n" + "does not define any of the methods\n"
                        + HANDLER_METHOD_HEAD + "on_delete" + upper + "(Pointer p, Attributes a, Database db)"
                        + HANDLER_METHOD_END + "\n" + HANDLER_METHOD_HEAD + "after_delete" + upper
                        + "(Pointer p, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
                        + "so it does not allow DELETE operations on the type " + typename
                        + "\nDefine that method (even with an empty body) to allow such operations.");
            }
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
            on = getMethod(handlerName, editArgs, controller);
            after = getMethod(afterHandlerName, editArgs, controller);

            if (on == null && after == null) {
                throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
                        + getControllerFile(controller) + ")\n" + "does not define neither of the methods\n"
                        + HANDLER_METHOD_HEAD + handlerName + "(Pointer p, Dictionary d, Attributes a, Database db)"
                        + HANDLER_METHOD_END + "\n" + HANDLER_METHOD_HEAD + afterHandlerName
                        + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
                        + "so it does not allow ADD operations on the type " + typename + ", field " + field
                        + "\nDefine any of the methods (even with an empty body) to allow such operations.");
            }
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

    static Class<?>[] newArgs = { Dictionary.class, Attributes.class, Database.class };

    public static Pointer doNew(Object controller, String handlerName, String afterHandlerName, String typename,
            Dictionary<String, Object> data, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] onArgs = { data, a, db };
        Object[] afterArgs = { null, data, a, db };
        Method on = null;
        Method after = null;

        if (!(controller instanceof LogicNotFoundException)) {
            on = getMethod(handlerName, newArgs, controller);
            after = getMethod(afterHandlerName, editArgs, controller);
            if (on == null && after == null) {
                throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
                        + getControllerFile(controller) + ")\n" + "does not define neither of the methods\n"
                        + HANDLER_METHOD_HEAD + handlerName + "(Dictionary d, Attributes a, Database db)"
                        + HANDLER_METHOD_END + "\n" + HANDLER_METHOD_HEAD + afterHandlerName
                        + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
                        + "so it does not allow NEW operations on the type " + typename
                        + ".\nDefine any of the methods (even with an empty body) to allow such operations.");
            }
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
            Throwable g = f.getTargetException();
            if (g instanceof LogicException) {
                throw (LogicException) g;
            }
            throw new LogicInvocationError(g);
        }
    }
}
