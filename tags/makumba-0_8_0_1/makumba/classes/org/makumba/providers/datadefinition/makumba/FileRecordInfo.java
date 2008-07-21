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
//  $Id: DataDefinition.java 2745 2008-07-03 10:11:52Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.providers.datadefinition.makumba;

/**
 * A DataDefinition for storing files. The record parse will replcae 'fieldName = file' with this data definition to
 * support rapid application development with file storage.
 * 
 * @author Rudolf Mayer
 * @version $Id: FileDataDefinition.java,v 1.1 Jul 18, 2008 1:12:42 AM rudi Exp $
 */
public class FileRecordInfo extends RecordInfo {
    public FileRecordInfo(RecordInfo ri, String subfield) {
        super(ri, subfield);
        addField1(new FieldInfo("content", "binary"));
        addField1(new FieldInfo("contentLength", "int"));
        addField1(new FieldInfo("contentType", "char[255]"));
        addField1(new FieldInfo("originalName", "char[255]"));
        addField1(new FieldInfo("name", "char[255]"));
    }

    private static final long serialVersionUID = 1L;

}