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
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.LogicInvocationError;
import org.makumba.LogicNotFoundException;
import org.makumba.MakumbaSystem;
import org.makumba.CompositeValidationException;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.util.DbConnectionProvider;
import org.makumba.util.NamedResourceFactory;
import org.makumba.util.NamedResources;

/** busines logic administration */
public class Logic {
    static Properties controllerConfig;

    static java.net.URL controllerURL;

    static HashMap nameToObject = new HashMap();

    private static final String HANDLER_METHOD_HEAD = "public void ";

    private static final String HANDLER_METHOD_END = " throws LogicException {}";

    static {
        controllerConfig = new Properties();
        try {
            controllerURL = org.makumba.util.ClassResource.get("MakumbaController.properties");
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
                MakumbaSystem.getMakumbaLogger("controller").log(Level.SEVERE,
                    "Error while trying to load controller class " + className, e);
            } catch (InstantiationException e) {
                MakumbaSystem.getMakumbaLogger("controller").log(Level.SEVERE,
                    "Error while trying to load controller class " + className, e);
            } catch (IllegalAccessException e) {
                MakumbaSystem.getMakumbaLogger("controller").log(Level.SEVERE,
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

        protected Object makeResource(Object p) {
            String path = (String) p;
            String msg = "Searching for business logic for " + path + ":";
            String className = "";
            int n = path.lastIndexOf(".");
            if (n != -1)
                path = path.substring(0, n);

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
                        if (className.length() > 0 && className.lastIndexOf(".") != className.length() - 1)
                            className += ".";
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
                for (int i = 1; i <= dir.length(); i++)
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
                while (st.hasMoreTokens()) {
                    dir = st.nextToken();
                    if (dir.length() == 0)
                        continue;
                    else {
                        dir = firstUpper(dir);
                        continue loop;
                    }
                }

                break;
            }

            if (lastFound == null) {
                msg += "\nNo matching class found for " + p + "!";
                lastFound = new LogicNotFoundException(msg);
            } else
                msg += "\nFound class " + lastFound.getClass().getName();

            MakumbaSystem.getMakumbaLogger("controller").info(msg);
            ((Hashtable) supplementary).put(p, msg);
            Object foundClass = nameToObject.get(lastFound.getClass().getName());
            if (foundClass != null) {
                return foundClass;
            } else {
                return lastFound;
            }

        }
    }, true);

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
            if (imin == -1)
                return ret;
            a = a.substring(minimum + separators[imin].length());
        }
    }

    static String firstUpper(String a) {
        char f = Character.toUpperCase(a.charAt(0));
        if (a.length() > 1)
            return f + a.substring(1);
        return "" + f;
    }

    /** gets the logic associated with the given package and path according to directory rules */
    public static Object getLogic(String path) {
        return NamedResources.getStaticCache(logix).getResource(path);
    }

    static Class[] argDb = { Attributes.class, Database.class };

    public static Object getAttribute(Object controller, String attname, Attributes a, String db,
            DbConnectionProvider dbcp) throws NoSuchMethodException, LogicException {
        if (controller instanceof LogicNotFoundException)
            throw new NoSuchMethodException("no controller=> no attribute method");
        Transaction d = dbcp.getConnectionTo(db);
        Object[] args = { a, d };
        try {
            return (controller.getClass().getMethod("find" + firstUpper(attname), argDb)).invoke(controller, args);
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(e.getMessage());
        } catch (InvocationTargetException f) {
            d.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException)
                throw (LogicException) g;
            throw new LogicInvocationError(g);
        }
    }

    static Class[] editArgs = { Pointer.class, Dictionary.class, Attributes.class, Database.class };

    static Class[] opArgs = { Dictionary.class, Attributes.class, Database.class };

    static Class[] noClassArgs = {};

    static Object[] noObjectArgs = {};

    public static String getControllerFile(Object controller) {
        String ctrlClass = controller.getClass().getName();
        java.net.URL u = org.makumba.util.ClassResource.get(ctrlClass.replace('.', '/') + ".java");
        if (u != null)
            return getFilePath(u);
        return org.makumba.util.ClassResource.get(ctrlClass.replace('.', '/') + ".class").toString();
    }

    public static String getFilePath(java.net.URL u) {
        try {
            return new java.io.File((u.getFile())).getCanonicalPath();
        } catch (java.io.IOException ioe) {
            throw new org.makumba.util.RuntimeWrappedException(ioe);
        }
    }

    public static Method getMethod(String name, Class[] args, Object controller) {
        try {
            Method m = controller.getClass().getMethod(name, args);
            if (!Modifier.isPublic(m.getModifiers()))
                return null;
            return m;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static void doInit(Object controller, Attributes a, String dbName, DbConnectionProvider dbcp)
            throws LogicException {
        if (controller instanceof LogicNotFoundException)
            return;
        Transaction db = dbcp.getConnectionTo(dbName);
        Method init = getMethod("checkAttributes", argDb, controller);
        Method oldInit = getMethod("requiredAttributes", noClassArgs, controller);
        if (init == null && oldInit == null)
            return;
        if (init != null) {
            Object[] args = { a, db };
            try {
                init.invoke(controller, args);
            } catch (IllegalAccessException g) {
                throw new LogicInvocationError(g);
            } catch (InvocationTargetException f) {
                db.rollback();
                Throwable g = f.getTargetException();
                if (g instanceof LogicException)
                    throw (LogicException) g;
                throw new LogicInvocationError(g);
            }
        } else {
            MakumbaSystem.getMakumbaLogger("controller").warning(
                "requiredAttributes() is deprecated. Use checkAttributes(Attributes a, Database db) instead");
            Object attrs = null;
            try {
                attrs = oldInit.invoke(controller, noObjectArgs);
            } catch (IllegalAccessException g) {
                throw new LogicInvocationError(g);
            } catch (InvocationTargetException f) {
                db.rollback();
                Throwable g = f.getTargetException();
                if (g instanceof LogicException)
                    throw (LogicException) g;
                throw new LogicInvocationError(g);
            }
            if (attrs == null)
                return;
            if (attrs instanceof String) {
                a.getAttribute((String) attrs);
                return;
            }
            if (attrs instanceof String[]) {
                for (int i = 0; i < ((String[]) attrs).length; i++)
                    a.getAttribute(((String[]) attrs)[i]);
                return;
            }
            return;
        }
    }

    public static Object doOp(Object controller, String opName, Dictionary data, Attributes a, String dbName,
            DbConnectionProvider dbcp) throws LogicException {
        if (opName == null)
            return null;
        if ((controller instanceof LogicNotFoundException))
            throw new ProgrammerError("there is no controller object to look for the Form handler method " + opName);

        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] editArg = { data, a, db };
        Method op = null;
        op = getMethod(opName, opArgs, controller);
        if (op == null)
            throw new ProgrammerError("Class " + controller.getClass().getName() + " (" + getControllerFile(controller)
                    + ")\n" + "does not define the method\n" + HANDLER_METHOD_HEAD + opName
                    + "(Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
                    + "The method is declared as a makumba form handler, so it has to be defined");

        try {
            return op.invoke(controller, editArg);
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException)
                throw (LogicException) g;
            if (g instanceof CompositeValidationException)
                throw (CompositeValidationException) g;
            throw new LogicInvocationError(g);
        }
    }

    public static Pointer doEdit(Object controller, String handlerName, String typename, Pointer p, Dictionary data,
            Attributes a, String dbName, DbConnectionProvider dbcp) throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] editArg = { p, data, a, db };
        Method edit = null;
        if (!(controller instanceof LogicNotFoundException)) {
            String upper = upperCase(typename);
            String handlerName2 = "on_edit" + upper;
            edit = getMethod(handlerName, editArgs, controller);
            if (edit == null)
                throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
                        + getControllerFile(controller) + ")\n" + "does not define the method\n" + HANDLER_METHOD_HEAD
                        + handlerName + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END
                        + "\n" + "so it does not allow EDIT operations on the type " + typename
                        + "\nDefine that method (even with an empty body) to allow such operations.");
        }

        try {
            if (edit != null)
                edit.invoke(controller, editArg);
            db.update(p, data);
            return p;
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException)
                throw (LogicException) g;
            throw new LogicInvocationError(g);
        }
    }

    static Class[] deleteArgs = { Pointer.class, Attributes.class, Database.class };

    public static Pointer doDelete(Object controller, String typename, Pointer p, Attributes a, String dbName,
            DbConnectionProvider dbcp) throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] deleteArg = { p, a, db };
        Method delete = null;
        String upper = upperCase(typename);

        if (!(controller instanceof LogicNotFoundException)) {
            delete = getMethod("on_delete" + upper, deleteArgs, controller);
            if (delete == null)
                throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
                        + getControllerFile(controller) + ")\n" + "does not define the method\n" + HANDLER_METHOD_HEAD
                        + "on_delete" + upper + "(Pointer p, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
                        + "so it does not allow DELETE operations on the type " + typename
                        + "\nDefine that method (even with an empty body) to allow such operations.");
        }

        try {
            if (delete != null)
                delete.invoke(controller, deleteArg);
            db.delete(p);
            return null;
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException)
                throw (LogicException) g;
            throw new LogicInvocationError(g);
        }
    }

    public static Pointer doAdd(Object controller, String handlerName, String typename, Pointer p, Dictionary data,
            Attributes a, String dbName, DbConnectionProvider dbcp) throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] addArg = { p, data, a, db };
        Method on = null;
        Method after = null;
        String upper = upperCase(typename);
        int n = typename.lastIndexOf("->");
        String field = typename.substring(n + 2);
        typename = typename.substring(0, n);

        if (!(controller instanceof LogicNotFoundException)) {
            on = getMethod(handlerName, editArgs, controller);
            after = getMethod("after_add" + upper, editArgs, controller);

            if (on == null && after == null)
                throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
                        + getControllerFile(controller) + ")\n" + "does not define neither of the methods\n"
                        + HANDLER_METHOD_HEAD + handlerName + "(Pointer p, Dictionary d, Attributes a, Database db)"
                        + HANDLER_METHOD_END + "\n" + HANDLER_METHOD_HEAD + "after_add" + upper
                        + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
                        + "so it does not allow ADD operations on the type " + typename + ", field " + field
                        + "\nDefine any of the methods (even with an empty body) to allow such operations.");
        }

        try {
            if (on != null)
                on.invoke(controller, addArg);
            addArg[0] = db.insert(p, field, data);
            if (after != null)
                after.invoke(controller, addArg);
            return (Pointer) addArg[0];
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException)
                throw (LogicException) g;
            throw new LogicInvocationError(g);
        }
    }

    static Class[] newArgs = { Dictionary.class, Attributes.class, Database.class };

    public static Pointer doNew(Object controller, String handlerName, String typename, Dictionary data, Attributes a,
            String dbName, DbConnectionProvider dbcp) throws LogicException {
        Transaction db = dbcp.getConnectionTo(dbName);
        Object[] onArgs = { data, a, db };
        Object[] afterArgs = { null, data, a, db };
        Method on = null;
        Method after = null;
        String upper = upperCase(typename);

        if (!(controller instanceof LogicNotFoundException)) {
            on = getMethod(handlerName, newArgs, controller);
            after = getMethod("after_new" + upper, editArgs, controller);
            if (on == null && after == null)
                throw new ProgrammerError("Class " + controller.getClass().getName() + " ("
                        + getControllerFile(controller) + ")\n" + "does not define neither of the methods\n"
                        + HANDLER_METHOD_HEAD + handlerName + "(Dictionary d, Attributes a, Database db)"
                        + HANDLER_METHOD_END + "\n" + HANDLER_METHOD_HEAD + "after_new" + upper
                        + "(Pointer p, Dictionary d, Attributes a, Database db)" + HANDLER_METHOD_END + "\n"
                        + "so it does not allow NEW operations on the type " + typename
                        + ".\nDefine any of the methods (even with an empty body) to allow such operations.");
        }
        try {
            if (on != null)
                on.invoke(controller, onArgs);
            afterArgs[0] = db.insert(typename, data);
            if (after != null)
                after.invoke(controller, afterArgs);
            return (Pointer) afterArgs[0];
        } catch (IllegalAccessException g) {
            throw new LogicInvocationError(g);
        } catch (InvocationTargetException f) {
            db.rollback();
            Throwable g = f.getTargetException();
            if (g instanceof LogicException)
                throw (LogicException) g;
            throw new LogicInvocationError(g);
        }
    }
}
