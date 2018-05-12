/*
 * Created on Jun 28, 2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.makumba.sql;

import java.util.Properties;

import org.makumba.FieldDefinition;

public class InformixDatabase extends Database {
    public InformixDatabase(Properties p) {
        super(p);
    }

    // moved from informix.dateTimeManager; .textManager and .timeStampManager
    @Override
    protected String getFieldDBType(FieldDefinition fd) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._date:
                return "DATETIME YEAR TO FRACTION";
            case FieldDefinition._text:
                return "BYTE";
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return "DATETIME YEAR TO FRACTION";
            default:
                return super.getFieldDBType(fd);
        }

    }
}
