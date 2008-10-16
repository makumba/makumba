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

import com.freeware.inifiles.INIFile;

/**
 * This INI file reader builds on {@link INIFile}, and adds other methods useful for Makumba.
 * 
 * @author Rudolf Mayer
 * @version $Id: AdvancedINIFileReader.java,v 1.1 Oct 6, 2008 2:52:01 AM rudi Exp $
 */
public class MakumbaINIFileReader extends INIFile {

    public MakumbaINIFileReader(String name) {
        super(name);
    }

    public String getStringProperty(String section, String property, MakumbaINIFileReader otherConfig) {
        return getStringProperty(section, property) != null ? getStringProperty(section, property)
                : otherConfig.getStringProperty(section, property);
    }

    public String getProperty(String section, String property) {
        return getStringProperty(section, property) != null ? getStringProperty(section, property) : "__NOT__SET__";
    }

    public boolean getBooleanProperty(String section, String property, MakumbaINIFileReader otherConfig) {
        return Boolean.parseBoolean(getStringProperty(section, property) != null ? getStringProperty(section, property)
                : otherConfig.getStringProperty(section, property));
    }

}
