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
//  $Id: timestampFormatter.java 2568 2008-06-14 01:06:21Z rosso_nero $
//  $Name$
/////////////////////////////////////

/**
 * A data transfer object between query analysis and the parameter expansion in queries. 
 * @author cristi
 */
package org.makumba.commons;

import java.util.List;
import java.util.Map;

import org.makumba.providers.QueryAnalysisProvider;

public class ParamInfo {
    public ParamInfo(String name, int position) {
        this.paramName = name;
        this.paramPosition = position;
    }

    String paramName;

    int paramPosition;

    public String getName() {
        return paramName;
    }

    public int getPosition() {
        return paramPosition;
    }

    public static class Writer {
        public void write(ParamInfo p, StringBuffer ret) {
            ret.append("?");
        }
    }

    public static class MultipleWriter extends Writer {

        private Map<String, Object> args;

        public MultipleWriter(Map<String, Object> args) {
            this.args = args;
        }

        @Override
        public void write(ParamInfo po, StringBuffer ret) {
            String paramName = QueryAnalysisProvider.getActualParameterName(po.getName());
            if (args != null) {
                Object val = args.get(paramName);

                if (val != null && val instanceof List<?>) {
                    List<?> v = (List<?>) args.get(paramName);
                    if (v.isEmpty()) {
                        ret.append("\'\'");
                    } else {
                        ret.append("?");
                        for (int i = 1; i < v.size(); i++) {
                            ret.append(',').append('?');
                        }
                    }
                    return;
                }
                ret.append("?");
            }
        }
    }

}