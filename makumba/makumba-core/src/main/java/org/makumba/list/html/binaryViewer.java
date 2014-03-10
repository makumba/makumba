// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: textViewer.java 2468 2008-05-27 16:25:53Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.list.html;

import java.util.Dictionary;

import org.makumba.Pointer;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.providers.Configuration;

/**
 * This viewer creates a link to the download servlet serving the binary content.
 * 
 * @author Rudolf Mayer
 * @version $Id: binaryViewer.java,v 1.1 Jul 23, 2008 10:35:16 PM rudi Exp $
 */
public class binaryViewer extends FieldViewer {
    static String[] params = {};

    static String[][] paramValues = {};

    @Override
    public String[] getAcceptedParams() {
        return params;
    }

    @Override
    public String[][] getAcceptedValue() {
        return paramValues;
    }

    private static final class SingletonHolder {
        static FieldFormatter singleton = new binaryViewer();
    }

    private binaryViewer() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    @Override
    public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary<String, Object> formatParams) {
        Pointer pointer = (Pointer) o;
        return Configuration.getMakumbaToolsLocation() + "/makumbaDownload?type=" + pointer.getType() + "&value="
                + pointer.toExternalForm();
    }
}
