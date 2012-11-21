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
//  $Id: charViewer.java 2212 2008-01-04 11:16:10Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.list.html;

import java.util.Dictionary;

import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;

/**
 * Boolean viewer, that simply displays "Yes" or "No" instead of "true" and "false"<br>
 * TODO: add possibility to provide other display values than "Yes" or "No"
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: booleanViewer.java,v 1.1 May 11, 2008 9:45:07 PM manu Exp $
 */
public class booleanViewer extends FieldViewer {
    static String[] params = { "default" };

    static String[][] paramValues = { null };

    @Override
    public String[] getAcceptedParams() {
        return params;
    }

    @Override
    public String[][] getAcceptedValue() {
        return paramValues;
    }

    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder {
        static FieldFormatter singleton = new booleanViewer();

        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    private booleanViewer() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    @Override
    public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary<String, Object> formatParams) {
        return (Boolean) o ? "Yes" : "No";
    }

}
