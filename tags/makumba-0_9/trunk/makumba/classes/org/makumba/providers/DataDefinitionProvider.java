package org.makumba.providers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.FieldGroup;
import org.makumba.FieldMetadata;
import org.makumba.MakumbaError;
import org.makumba.QueryFragmentFunctions;
import org.makumba.TypeMetadata;
import org.makumba.ValidationDefinition;
import org.makumba.ValidationRule;
import org.makumba.commons.ClassResource;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.bytecode.AbstractClassWriter;
import org.makumba.providers.bytecode.EntityClassGenerator;
import org.makumba.providers.bytecode.JavassistClassWriter;
import org.makumba.providers.datadefinition.FieldGroupImpl;
import org.makumba.providers.datadefinition.FieldMetadataImpl;
import org.makumba.providers.datadefinition.TypeMetadataImpl;
import org.makumba.providers.datadefinition.mdd.DataDefinitionImpl;
import org.makumba.providers.datadefinition.mdd.FieldDefinitionImpl;
import org.makumba.providers.datadefinition.mdd.MDDFactory;

/**
 * This class is a facade for creating different kinds of DataDefinitionProviders. Its constructor knows from a
 * Configuration (or in the future maybe through other means) which implementation to use, and provides this
 * implementation methods to its client, without revealing the implementation used.<br>
 * FIXME caching using named resources for getTypeMetadata and getFieldGroup<br>
 * TODO improved caching for FieldMetadata (later)<br>
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public class DataDefinitionProvider {

    private DataDefinitionProvider() {
    }

    private static class SingletonHolder implements org.makumba.commons.SingletonHolder {
        private static DataDefinitionProvider singleton = new DataDefinitionProvider();

        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    /**
     * Gives an instance of a {@link DataDefinitionProvider}.
     */
    public static DataDefinitionProvider getInstance() {
        return SingletonHolder.singleton;
    }

    /**
     * Sets the root of the webapp in which to look for data definitions
     */
    public static void setWebappRoot(String w) {
        webappRoot = w;
    }

    /**
     * Gets the data definition with the given absolute name
     * 
     * @throws org.makumba.DataDefinitionNotFoundError
     *             if the name is not a valid record info name
     * @throws org.makumba.DataDefinitionParseError
     *             if the syntax is wrong or a referred resource can't be found
     */
    public DataDefinition getDataDefinition(String typeName) {
        if (!classesGenerated && !isGenerating && Configuration.getGenerateEntityClasses()) {
            generateEntityClasses();
            classesGenerated = true;
        }
        return getMDD(typeName.replaceAll("__", "->"));
    }

    /**
     * Gets the validation definition of the given type
     */
    public ValidationDefinition getValidationDefinition(String typeName) {
        // for the moment, the implementation of the validation definition and the data definition is the same
        return (ValidationDefinition) getDataDefinition(typeName);
    }

    // TODO this is a temporary method, as long as virtual DataDefinition-s are being used that should be replaced with
    // FieldGroups. so this method should instead use FieldMetadata or even the type & field name to identify the field
    public Collection<ValidationRule> getValidationRules(FieldDefinition fd) {
        return ((FieldDefinitionImpl) fd).getValidationRules();
    }

    /**
     * Gets all the query functions defined for this type
     */
    public QueryFragmentFunctions getQueryFragmentFunctions(String typeName) {
        // for the moment, the implementation of the validation definition and the query fragment definition is the same
        return ((DataDefinitionImpl) getDataDefinition(typeName)).getFunctions();
    }

    /**
     * makes a virtual data definition which is not parsed from a MDD
     * 
     * @param name
     *            the name of the data definition to create
     * @return a new virtual {@link DataDefinition} instance
     */
    public DataDefinition getVirtualDataDefinition(String name) {
        return new DataDefinitionImpl(name.replaceAll("__", "->"));
    }

    /**
     * makes a field definition from the indicated string
     * 
     * @param name
     *            the name of the field
     * @param definition
     *            the definition string, e.g. "ptr general.Person ;pointer to a person"
     * @return a field definition built on a definition string
     */
    public FieldDefinition makeFieldDefinition(String name, String definition) {

        String def = name.replaceAll("__", "->") + "=" + definition.replaceAll("__", "->");
        // FIXME should not use a new MDD for this / should not put the name as the one of the field for the type name!
        return MDDFactory.getInstance().getVirtualDataDefinition(name.replaceAll("__", "->"), def).getFieldDefinition(
            name.replaceAll("__", "->"));
    }

    /**
     * makes a field definition with the elementary type
     * 
     * @param name
     *            the name of the field
     * @param type
     *            the type of the field
     * @return a field definition generated by the name and the type of the field
     */
    public FieldDefinition makeFieldOfType(String name, String type) {

        if (type.startsWith("ptr ")) {
            return makeFieldDefinition(name, type);
        }

        return new FieldDefinitionImpl(name.replaceAll("__", "->"), type.replaceAll("__", "->"));
    }

    /**
     * makes a field definition identical with the given one, except for the name
     * 
     * @param name
     *            the name of the field
     * @param type
     *            the FieldDefinition used as model
     * @return a copy of the initial field definition with a different name
     */
    public FieldDefinition makeFieldOfType(String name, String type, String description) {
        return new FieldDefinitionImpl(name.replaceAll("__", "->"), type.replaceAll("__", "->"), description);
    }

    /**
     * makes a field definition with the elementary type
     * 
     * @param name
     *            the name of the field
     * @param type
     *            the elementary type of the field
     * @param description
     *            the description of the field
     * @return a field definition generated by the name, type and description of the field
     */
    public FieldDefinition makeFieldWithName(String name, FieldDefinition type) {
        return new FieldDefinitionImpl(name.replaceAll("__", "->"), type);
    }

    /**
     * makes a field definition identical with the given one, except for the name and the description
     * 
     * @param name
     *            the name of the field
     * @param type
     *            the FieldDefinition used as model
     * @param description
     *            the description of the field
     * @return a copy of the initial field definition with a different name and description
     */
    public FieldDefinition makeFieldWithName(String name, FieldDefinition type, String description) {
        return new FieldDefinitionImpl(name.replaceAll("__", "->"), type, description);
    }

    /**
     * gives a list of data definitions in the default locations of the data definition provider
     * 
     * @return a vector with references to the data definitions in the default locations of the data definition provider
     */
    public Vector<String> getDataDefinitionsInDefaultLocations() {
        return getDataDefinitionsInDefaultLocations((String[]) null);
    }

    /**
     * gives a list of data definitions in a given location
     * 
     * @param location
     *            the location where the data definitions should be
     * @return a vector with references to the data definitions in the location
     */
    public Vector<String> getDataDefinitionsInLocation(String location) {
        return mddsInDirectory(location);
    }

    /**
     * gives a list of data definitions in the default locations of the data definition provider, ignoring those MDDs
     * that start with any of the strings in the ignoreList
     * 
     * @param ignoreList
     *            a list of prefixes for MDDs to be ignored
     * @return a vector with references to the data definitions in the default locations of the data definition provider
     */
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
            for (String element : list) {
                String s = element;
                if (s.endsWith(".mdd")) {
                    s = dir.toString() + java.io.File.separatorChar + s;
                    s = s.substring(baselength, s.length() - 4); // cut off the ".mdd"
                    s = s.replace(java.io.File.separatorChar, '.');
                    mdds.add(s);
                } else {
                    java.io.File f = new java.io.File(dir, s);
                    if (f.isDirectory()) {
                        fillMdds(baselength, f, mdds);
                    }
                }
            }
        }
    }

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
        if (subfieldCheck == null) {
            throw new DataDefinitionParseError("subfield not found: " + name + " in " + dd.getName());
        }

        dd = subfieldCheck.getSubtable();
        return dd;
    }

    private static DataDefinition getSimpleMDD(String path) {
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
            ((DataDefinitionImpl) dd).setName(path);
        } else {
            java.util.logging.Logger.getLogger("org.makumba.debug.abstr").severe("shit happens: " + path);
        }
        return dd;
    }

    /**
     * Finds a data definition, based on its name and extensions
     */
    public static java.net.URL findDataDefinition(String s, String ext) {
        // must specify a filename, not a directory (or package), see bug 173
        java.net.URL u = findDataDefinitionOrDirectory(s, ext);
        if (u != null && (s.endsWith("/") || org.makumba.commons.ClassResource.get((s + '/')) != null)) {
            return null;
        }
        return u;
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
    public static URL findDataDefinitionOrDirectory(String s, String ext) {
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
            if (!f.exists() || f.exists() && !f.isDirectory()) {
                throw new MakumbaError("webappRoot " + webappRoot + " does not appear to be a valid directory");
            }
            String mddPath = webappRoot + "/WEB-INF/classes/dataDefinitions/" + s.replace('.', '/') + "." + ext;
            File mdd = new File(mddPath.replaceAll("/", Matcher.quoteReplacement(File.separator)));
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
            u = org.makumba.commons.ClassResource.get(("dataDefinitions/" + s.replace('.', '/') + "." + ext));
            if (u == null) {
                u = org.makumba.commons.ClassResource.get((s.replace('.', '/') + "." + ext));
            }
        }
        return u;
    }

    /**
     * Finds the folder in which the classes should be generated based on a seed file
     */
    public static String findClassesRootFolder(String locatorSeed) {
        String rootFolder = "";
        try {
            rootFolder = new File(ClassResource.get(locatorSeed).getFile()).getParentFile().getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootFolder;
    }

    private static String webappRoot;

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

        @Override
        protected void configureResource(Object name, Object hashName, Object resource) throws Throwable {
            ((DataDefinitionImpl) resource).build();
        }

    });

    /***
     * Methods related to the refactoring from DataDefinition/FieldDefinition to Class/FieldMetadata.<br>
     * Will probably move somewhere else or stay here but under a different name.
     ***/

    /** have the entity classes already been generated? **/
    private static boolean classesGenerated = false;

    /** are we generating the classes at the moment ? **/
    private static boolean isGenerating = false;

    private HashMap<String, TypeMetadata> simpleTypeMetadataCache = new HashMap<String, TypeMetadata>();

    private HashMap<String, FieldGroup> simpleFieldGroupCache = new HashMap<String, FieldGroup>();

    public TypeMetadata getTypeMetadata(String typeName) {
        // FIXME named resources caching
        if (!simpleTypeMetadataCache.containsKey(typeName)) {
            simpleTypeMetadataCache.put(typeName, new TypeMetadataImpl(typeName));
        }
        return simpleTypeMetadataCache.get(typeName);
    }

    public FieldGroup getFieldGroup(String typeName) {
        // FIXME named resources caching
        if (!simpleFieldGroupCache.containsKey(typeName)) {
            DataDefinition dd = getDataDefinition(typeName);
            LinkedHashMap<String, FieldMetadata> fields = new LinkedHashMap<String, FieldMetadata>();
            for (String fieldName : dd.getFieldNames()) {
                // TODO FieldMetaData construction optimization and caching
                fields.put(fieldName, new FieldMetadataImpl(typeName, fieldName));
            }
            simpleFieldGroupCache.put(typeName, new FieldGroupImpl(fields));
        }
        return simpleFieldGroupCache.get(typeName);
    }

    /**
     * Generates the classes for all Java entities based on the MDDs of the web-application
     */
    public void generateEntityClasses() {
        // FIXME organize the test MDDs better and remove this after development is over
        generateEntityClasses(getDataDefinitionsInDefaultLocations("test.brokenMdds", "test.ParserTest",
            "test.Functions"));
    }

    public void generateEntityClasses(Vector<String> dds) {
        // FIXME concurrency on this method

        isGenerating = true;

        Map<String, Vector<FieldDataDTO>> entities = new LinkedHashMap<String, Vector<FieldDataDTO>>();
        NameResolver nr = new NameResolver(); // FIXME find a way to pass the properties

        for (String type : dds) {
            Vector<FieldDataDTO> fields = getFieldDataDTOs(type);
            entities.put(getDataDefinition(type).getName(), fields);
            nr.initializeType(getDataDefinition(type));
        }

        try {
            AbstractClassWriter ac = new JavassistClassWriter();
            new EntityClassGenerator(entities, findClassesRootFolder("Makumba.conf"), ac, nr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        isGenerating = false;
    }

    // TODO this method is here because we do not want the EntityResolver to know about DataDefinition
    // it should move away once the NameResolver works with FieldGroups
    public void initializeNameResolver(NameResolver nr, String typeName) {
        nr.initializeType(getDataDefinition(typeName));
    }

    /**
     * Given a type, returns a vector of {@link FieldDataDTO} representing the fields of this type
     */
    public Vector<FieldDataDTO> getFieldDataDTOs(String type) {
        Vector<FieldDataDTO> fields = new Vector<FieldDataDTO>();

        for (String field : getDataDefinition(type).getFieldNames()) {
            FieldDefinitionImpl fd = (FieldDefinitionImpl) getDataDefinition(type).getFieldDefinition(field);
            FieldDataDTO f = new FieldDataDTO(fd);
            fields.add(f);
        }
        return fields;
    }
}