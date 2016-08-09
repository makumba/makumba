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
//  $Id: HsqldbDatabase.java 5926 2012-06-29 20:03:20Z cristian_bogdan $
//  $Name$
/////////////////////////////////////

package org.makumba.db.makumba.sql;

import java.util.Properties;

import org.makumba.FieldDefinition;

//this file was based in the Claudspace... so it needs to be

/** the database adapter for Derby (JavaDB) */
public class DerbyDatabase extends org.makumba.db.makumba.sql.Database {

    /** simply calls super */
    public DerbyDatabase(Properties p) {
        super(p);
    }

    @Override
    protected String getColumnAlterKeyword() {
        return "ALTER COLUMN";
    }

    @Override
    public String foreignKeyCreateSyntax(FieldDefinition fd, FieldDefinition foreign) {
        getTable(foreign.getDataDefinition());

        return "ALTER TABLE " + getDBName(fd.getDataDefinition()) + " ADD CONSTRAINT " + getIndexPrefix(fd)
                + shortIndexName(getDBName(foreign.getDataDefinition()), fd.getName()) + " FOREIGN KEY " + " ("
                + getFieldDBName(fd) + ") REFERENCES " + tableFieldIndex(foreign);
    }

    @Override
    protected String in_timestamp_Create(FieldDefinition fd) {
        return base_inCreate(fd) + " DEFAULT CURRENT_TIMESTAMP";
    }

    @Override
    public boolean writesDateModifyInInsert() {
        return false;
    }

    @Override
    public boolean writesDateCreateInInsert() {
        return false;
    }

    /*
     * it is be possible to do this
     * http://casaburo.blogspot.se/2011/10/on-update-lastupdatecurrenttimestamp.html
     * @see org.makumba.db.makumba.sql.Database#automaticUpdateTimestamp()
     */
    @Override
    public boolean automaticUpdateTimestamp() {
        return false;
    }

    @Override
    public boolean commitBeforeAddingForeignKey() {
        return true;
    }

}
