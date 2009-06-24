package org.makumba.providers.datadefinition.mdd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.DataDefinitionProviderInterface;

public class MDDProvider implements DataDefinitionProviderInterface {
    
    private static String webappRoot;


    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#getDataDefinition(java.lang.String)
     */
    public DataDefinition getDataDefinition(String typeName) {
        return getMDD(typeName.replaceAll("__", "->"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#getVirtualDataDefinition(java.lang.String)
     */
    public DataDefinition getVirtualDataDefinition(String name) {
        return new DataDefinitionImpl(name.replaceAll("__", "->"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldDefinition(java.lang.String,
     *      java.lang.String)
     */
    public FieldDefinition makeFieldDefinition(String name, String definition) {
        
        String def = name.replaceAll("__", "->") + "=" + definition.replaceAll("__", "->");
        
        return MDDFactory.getInstance().getVirtualDataDefinition(name.replaceAll("__", "->"), def).getFieldDefinition(name.replaceAll("__", "->"));
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldOfType(java.lang.String, java.lang.String)
     */
    public FieldDefinition makeFieldOfType(String name, String type) {
        return new FieldDefinitionImpl(name.replaceAll("__", "->"), type.replaceAll("__", "->"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldOfType(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public FieldDefinition makeFieldOfType(String name, String type, String description) {
        return new FieldDefinitionImpl(name.replaceAll("__", "->"), type.replaceAll("__", "->"), description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldWithName(java.lang.String,
     *      org.makumba.FieldDefinition)
     */
    public FieldDefinition makeFieldWithName(String name, FieldDefinition type) {
        return new FieldDefinitionImpl(name.replaceAll("__", "->"), type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldWithName(java.lang.String,
     *      org.makumba.FieldDefinition, java.lang.String)
     */
    public FieldDefinition makeFieldWithName(String name, FieldDefinition type, String description) {
        return new FieldDefinitionImpl(name.replaceAll("__", "->"), type, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#getDataDefinitionsInLocation(java.lang.String)
     */
    public Vector<String> getDataDefinitionsInLocation(String location) {
        return mddsInDirectory(location);
    }
    
    public Vector<String> getDataDefinitionsInDefaultLocations() {
        return getDataDefinitionsInDefaultLocations((String[]) null);
    }
    
    public Vector<String> getDataDefinitionsInDefaultLocations(String... ignoreList) {
        Vector<String> mdds = mddsInDirectory("dataDefinitions");
        Vector<String> mddsInClasses = mddsInDirectory(""); // should direct to classes dir
        // take all MDDs that are new in classes, i.e. not already found in dataDefinitions
        for (String string : mddsInClasses) {
            if (!string.startsWith("dataDefinitions.")) {
                mdds.add(string);
            }
        }
        // check for MDDs in packages that should be removed
        if (ignoreList != null) {
            Vector<String> mddCopy = new Vector<String>(mdds);
            for (String s : ignoreList) {
                for (String mdd : mddCopy) {
                    if (mdd.startsWith(s)) {
                        mdds.remove(mdd);
                    }
                }
            }
        }
        return mdds;
    }

    /**
     * Discover mdds in a directory in classpath.
     * 
     * @return filenames as Vector of Strings.
     */
    private Vector<String> mddsInDirectory(String dirInClasspath) {
        Vector<String> mdds = new java.util.Vector<String>();
        try {
            java.net.URL u = org.makumba.commons.ClassResource.get(dirInClasspath);
            // we need to create the file path with this method. rather than u.getFile(), as that method would keep
            // e.g. %20 for spaces in the path, which fails on windows.
            if (u != null) {
                java.io.File dir = new File(u.toURI());
                fillMdds(dir.toString().length() + 1, dir, mdds);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return mdds;
    }

    private void fillMdds(int baselength, java.io.File dir, java.util.Vector<String> mdds) {
        if (dir.isDirectory()) {
            String[] list = dir.list();
            for (int i = 0; i < list.length; i++) {
                String s = list[i];
                if (s.endsWith(".mdd")) {
                    s = dir.toString() + java.io.File.separatorChar + s;
                    s = s.substring(baselength, s.length() - 4); // cut off the ".mdd"
                    s = s.replace(java.io.File.separatorChar, '.');
                    mdds.add(s);
                } else {
                    java.io.File f = new java.io.File(dir, s);
                    if (f.isDirectory())
                        fillMdds(baselength, f, mdds);
                }
            }
        }
    }
    
    
    /**
     * returns the record info with the given absolute name
     * 
     * @throws org.makumba.DataDefinitionNotFoundError
     *             if the name is not a valid record info name
     * @throws org.makumba.DataDefinitionParseError
     *             if the syntax is wrong or a referred resource can't be found
     */
    public static DataDefinition getMDD(String name) {
        int n = name.indexOf("->");
        if (n == -1) {
            try {
                return getSimpleMDD(name);
            } catch (DataDefinitionNotFoundError e) {
                n = name.lastIndexOf(".");
                if (n == -1) {
                    throw e;
                }
                try {
                    return getMDD(name.substring(0, n) + "->" + name.substring(n + 1));
                } catch (DataDefinitionParseError f) {
                    throw e;
                }
            }
        }

        DataDefinition dd = getMDD(name.substring(0, n));
        while (true) {
            name = name.substring(n + 2);
            n = name.indexOf("->");
            if (n == -1) {
                break;
            }
            dd = dd.getFieldDefinition(name.substring(0, n)).getSubtable();
        }
        FieldDefinition subfieldCheck = dd.getFieldDefinition(name);
        if(subfieldCheck==null)
            throw new DataDefinitionParseError("subfield not found: "+name+" in "+dd.getName());
        
        dd = subfieldCheck.getSubtable();
        return dd;
    }

    public static synchronized DataDefinition getSimpleMDD(String path) {
        // this is to avoid a stupid error if path is "..."
        boolean dot = false;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '.') {
                if (dot) {
                    throw new DataDefinitionParseError("two consecutive dots not allowed in type name");
                }
                dot = true;
            } else {
                dot = false;
            }

            // check if type name looks valid (no weird characters or
            // spaces)
            if (path.charAt(i) != '/' && path.charAt(i) != '.') {
                if (i == 0 && !Character.isJavaIdentifierStart(path.charAt(i)) || i > 0
                        && !Character.isJavaIdentifierPart(path.charAt(i))) {
                    throw new DataDefinitionParseError("Invalid character \"" + path.charAt(i) + "\" in type name \""
                            + path + "\"");
                }
            }
        }

        if (path.indexOf('/') != -1) {
            path = path.replace('/', '.');
            if (path.charAt(0) == '.') {
                path = path.substring(1);
            }
        }

        DataDefinition dd = null;
        try {
            dd = (DataDefinition) NamedResources.getStaticCache(infos).getResource(path);
        } catch (RuntimeWrappedException e) {
            if (e.getCause() instanceof DataDefinitionParseError) {
                throw (DataDefinitionParseError) e.getCause();
            }
            if (e.getCause() instanceof DataDefinitionNotFoundError) {
                throw (DataDefinitionNotFoundError) e.getCause();
            }
            if (e.getCause() instanceof MakumbaError) {
                throw (MakumbaError) e.getCause();
            }
            throw e;
        }
        if (path.indexOf("./") == -1) {
            ((DataDefinitionImpl) dd).name = path;
        } else {
            java.util.logging.Logger.getLogger("org.makumba.debug.abstr").severe("shit happens: " + path);
        }
        return dd;
    }
    
    
    /**
     * Finds a data definition, based on its name and extensions
     */
    static public java.net.URL findDataDefinition(String s, String ext) {
        // must specify a filename, not a directory (or package), see bug 173
        java.net.URL u = findDataDefinitionOrDirectory(s, ext);
        if (u != null && (s.endsWith("/") || getResource(s + '/') != null)) {
            return null;
        }
        return u;
    }
    
    
    static java.net.URL getResource(String s) {
        return org.makumba.commons.ClassResource.get(s);
    }
    
    /**
     * Looks up a data definition. First tries to see if an arbitrary webapp root path was passed, if not uses the
     * classpath
     * 
     * @param s
     *            the name of the type
     * @param ext
     *            the extension (e.g. mdd)
     * @return a URL to the MDD file, null if none was found
     */
    private static URL findDataDefinitionOrDirectory(String s, String ext) {
        java.net.URL u = null;
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        if (s.endsWith(".") || s.endsWith("//")) {
            return null;
        }

        // if a webappRoot was passed, we fetch the MDDs from there, not using the CP
        if (webappRoot != null) {
            File f = new File(webappRoot);
            if (!f.exists() || (f.exists() && !f.isDirectory())) {
                throw new MakumbaError("webappRoot " + webappRoot + " does not appear to be a valid directory");
            }
            String mddPath = webappRoot + "/WEB-INF/classes/dataDefinitions/" + s.replace('.', '/') + "." + ext;
            File mdd = new File(mddPath.replaceAll("/", File.separator));
            if (mdd.exists()) {
                try {
                    u = new java.net.URL("file://" + mdd.getAbsolutePath());
                } catch (MalformedURLException e) {
                    throw new MakumbaError("internal error while trying to retrieve URL for MDD "
                            + mdd.getAbsolutePath());
                }
            }
        }

        if (u == null) {
            u = getResource("dataDefinitions/" + s.replace('.', '/') + "." + ext);
            if (u == null) {
                u = getResource(s.replace('.', '/') + "." + ext);
            }
        }
        return u;
    }
    
    public static int infos = NamedResources.makeStaticCache("MDDs parsed", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        @Override
        protected Object getHashObject(Object name) {
            java.net.URL u = findDataDefinition((String) name, "mdd");
            if (u == null) {
                throw new DataDefinitionNotFoundError((String) name);
            }
            return u;
        }

        @Override
        protected Object makeResource(Object name, Object hashName) {
            String nm = (String) name;
            if (nm.indexOf('/') != -1) {
                nm = nm.replace('/', '.').substring(1);
            }
            return MDDFactory.getInstance().getDataDefinition(nm);
        }
        

        
    });
    public MDDProvider() {

    }
    
}



