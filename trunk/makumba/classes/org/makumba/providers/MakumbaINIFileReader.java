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
//  $Id: SourceViewControllerHandler.java 3224 2008-10-05 22:32:17Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.providers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.makumba.commons.FileUtils;

import ch.ubique.inieditor.IniEditor;

/**
 * This INI file reader builds on {@link INIFile}, and adds other methods useful for Makumba.
 * 
 * @author Rudolf Mayer
 * @version $Id: AdvancedINIFileReader.java,v 1.1 Oct 6, 2008 2:52:01 AM rudi Exp $
 */
public class MakumbaINIFileReader extends IniEditor {

    private URL url;

    public MakumbaINIFileReader(URL url) throws IOException {
        super(true); // use case-sensitive section names
        this.url = url;
        load(FileUtils.getInputStream(url));
    }

    public String getStringProperty(String section, String property, MakumbaINIFileReader otherConfig) {
        return get(section, property) != null ? get(section, property) : otherConfig.get(section, property);
    }

    public String getProperty(String section, String property) {
        return get(section, property) != null ? get(section, property) : Configuration.PROPERTY_NOT_SET;
    }

    public boolean getBooleanProperty(String section, String property, MakumbaINIFileReader otherConfig) {
        return Boolean.parseBoolean(get(section, property) != null ? get(section, property) : otherConfig.get(section,
            property));
    }

    public Map<String, String> getProperties(String section) {
        List optionNames = optionNames(section);
        HashMap<String, String> ret = new HashMap<String, String>();
        for (Object object : optionNames) {
            ret.put((String) object, getProperty(section, (String) object));
        }
        return ret;
    }

    public String getSource() {
        return url.getPath();
    }

    // public MakumbaINIFileReader(String name) {
    // super(name);
    // }
    //
    // public String getStringProperty(String section, String property, MakumbaINIFileReader otherConfig) {
    // return getStringProperty(section, property) != null ? getStringProperty(section, property)
    // : otherConfig.getStringProperty(section, property);
    // }
    //
    // public String getProperty(String section, String property) {
    // return getStringProperty(section, property) != null ? getStringProperty(section, property) : "PROPERTY_NOT_SET";
    // }
    //
    // public boolean getBooleanProperty(String section, String property, MakumbaINIFileReader otherConfig) {
    // return Boolean.parseBoolean(getStringProperty(section, property) != null ? getStringProperty(section, property)
    // : otherConfig.getStringProperty(section, property));
    // }

}
