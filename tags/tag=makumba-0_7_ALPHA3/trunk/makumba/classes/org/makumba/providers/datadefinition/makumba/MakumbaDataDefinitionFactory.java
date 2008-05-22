package org.makumba.providers.datadefinition.makumba;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.providers.DataDefinitionProviderInterface;

/**
 * This class is the Makumba implementation of a data definition provider, based on MDD files.<br>
 * TODO refactor together with RecordInfo to build objects (and not use static methods)
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public class MakumbaDataDefinitionFactory implements DataDefinitionProviderInterface {

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#getDataDefinition(java.lang.String)
     */
    public DataDefinition getDataDefinition(String typeName) {
        return RecordInfo.getRecordInfo(typeName.replaceAll("__", "->"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#getVirtualDataDefinition(java.lang.String)
     */
    public DataDefinition getVirtualDataDefinition(String name) {
        return new RecordInfo(name.replaceAll("__", "->"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldDefinition(java.lang.String,
     *      java.lang.String)
     */
    public FieldDefinition makeFieldDefinition(String name, String definition) {
        return FieldInfo.getFieldInfo(name.replaceAll("__", "->"), definition.replaceAll("__", "->"), true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldOfType(java.lang.String, java.lang.String)
     */
    public FieldDefinition makeFieldOfType(String name, String type) {
        return FieldInfo.getFieldInfo(name.replaceAll("__", "->"), type.replaceAll("__", "->"), false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldOfType(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public FieldDefinition makeFieldOfType(String name, String type, String description) {
        return FieldInfo.getFieldInfo(name.replaceAll("__", "->"), type.replaceAll("__", "->"), false, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldWithName(java.lang.String,
     *      org.makumba.FieldDefinition)
     */
    public FieldDefinition makeFieldWithName(String name, FieldDefinition type) {
        return FieldInfo.getFieldInfo(name, type, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#makeFieldWithName(java.lang.String,
     *      org.makumba.FieldDefinition, java.lang.String)
     */
    public FieldDefinition makeFieldWithName(String name, FieldDefinition type, String description) {
        return FieldInfo.getFieldInfo(name, type, false, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.providers.DataDefinitionProviderInterface#getDataDefinitionsInLocation(java.lang.String)
     */
    public Vector getDataDefinitionsInLocation(String location) {
        return mddsInDirectory(location);
    }

    /**
     * Discover mdds in a directory in classpath.
     * 
     * @return filenames as Vector of Strings.
     */
    private java.util.Vector mddsInDirectory(String dirInClasspath) {
        java.util.Vector mdds = new java.util.Vector();
        try {
            java.net.URL u = org.makumba.commons.ClassResource.get(dirInClasspath);
            // we need to create the file path with this methode. rather than u.getFile(), as that method would keep
            // e.g. %20 for spaces in the path, which fails on windows.
            java.io.File dir = new File(u.toURI());
            fillMdds(dir.toString().length() + 1, dir, mdds);
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

    public MakumbaDataDefinitionFactory() {

    }

}
