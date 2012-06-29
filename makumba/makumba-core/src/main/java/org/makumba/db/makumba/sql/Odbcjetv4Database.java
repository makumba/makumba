/*
 * Created on Jun 28, 2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.makumba.sql;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import org.makumba.FieldDefinition;

public class Odbcjetv4Database extends Database {
    // moved from odbcjet.v4.dateTimeManager, .timestampManager
    static DateFormat odbcDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.UK);

    public Odbcjetv4Database(Properties p) {
        super(p);

    }

    // Moved from odbcjet.v4.dateTimeManager
    @Override
    public String writeConstant(FieldDefinition fd, Object o) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._date:
                return "\'" + odbcDate.format(new Timestamp(((java.util.Date) o).getTime())) + "\'";
            default:
                return super.writeConstant(fd, o);
        }
    }

}
