package org.makumba.providers;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.lang.StringUtils;
import org.makumba.ConfigurationError;

/**
 * An extended version of the {@link HierarchicalINIConfiguration} suitable for makumba configuration
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaINIConfiguration.java,v 1.1 Nov 25, 2009 9:46:33 PM manu Exp $
 */
public class MakumbaINIConfiguration extends HierarchicalINIConfiguration {
    
    private static final long serialVersionUID = 1L;

    private MakumbaINIConfiguration defaultConfiguration;
    
    private URL u;
    
    public MakumbaINIConfiguration(URL u, MakumbaINIConfiguration defaultConfiguration) throws ConfigurationException {
        this(u);
        this.defaultConfiguration = defaultConfiguration;
    }
    
    public MakumbaINIConfiguration(URL u) throws ConfigurationException {
        super(u);
        this.u = u;

        // disable saving of the configuration on reloading
        setAutoSave(false);
        // disable automatic reloading of the property file
        setReloadingStrategy(new InvariantReloadingStrategy());

        // we need this or Apache CLI will thing that the spaces in section names means we address more than one node
        setListDelimiter('+');
        ((DefaultExpressionEngine) getExpressionEngine()).setPropertyDelimiter("+");

        load();
    }

    public boolean getBooleanProperty(String section, String property) {
        String k = getSection(section).getString(property);
        if(k == null) {
            k = defaultConfiguration.getSection(section).getString(property);
            if(k == null) {
                return false;
            }
        }
        return Boolean.parseBoolean(k);
    }
    
    /**
     * returns the properties of a section, if it exists
     * @throws ConfigurationError if the section does not exist
     */
    public Map<String, String> getPropertiesAsMap(String section) {
        Map<String, String> m = new HashMap<String, String>();
        SubnodeConfiguration s = getSection(section);
        if(s == null) {
            throw new ConfigurationError("Section " + section + " does not exist in Makumba.conf");
        }
        java.util.Iterator<String> i = s.getKeys();
        while(i.hasNext()) {
            String k = i.next();
            String originalK = k;
            // the ini configuration escapes the property delimiters so we have to unescape them here
            if(k.indexOf("..") > 0) {
                k = StringUtils.replace(k, "..", ".");
            }
            m.put(k, getSection(section).getString(originalK));
        }
        return m;
    }
    

    /**
     * Returns the properties of a section, enriched with the default properties
     */
    public Map<String, String> getPropertiesAsMap(String section, MakumbaINIConfiguration defaultConfig) {
        Map<String, String> defaults = defaultConfig.getPropertiesAsMap(section);
        if(getSections().contains(section)) {
            Map<String, String> application = getPropertiesAsMap(section);
            final Set<String> keySet = application.keySet();
            for (String string : keySet) {
                if (application.get(string) != null) {
                    defaults.put(string, application.get(string));
                }
            }
        } else {
            Configuration.logger.info("No application specific config found for '" + section
                    + "', using only internal defaults.");
        }
        return defaults;
    }

    public Map<String, Map<String, String>> getPropertiesStartingWith(String sectionPrefix) {
        LinkedHashMap<String, Map<String, String>> res = new LinkedHashMap<String, Map<String, String>>();
        final Set<String> sectionNames = getSections();
        for (String section : sectionNames) {
            if (section.startsWith(sectionPrefix)) {
                res.put(section.substring(sectionPrefix.length()), getPropertiesAsMap(section));
            }
        }
        return res;
    }
    

    public String getProperty(String section, String property) {
        String s = getSection(section).getString(property);
        if(s == null) {
            s = defaultConfiguration.getSection(section).getString(property);
            if(s == null) {
                return Configuration.PROPERTY_NOT_SET;
            }
        }
        return s;
    }

    public String getSource() {
        return u.getPath();
    }
    
    
}
