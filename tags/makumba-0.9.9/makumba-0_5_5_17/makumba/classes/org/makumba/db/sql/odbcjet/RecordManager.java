package org.makumba.db.sql.odbcjet;

/** the ODBC RecordHandler, identical with the sql RecordHandler. Due to RecordHandler field generation conventions, it will have different FieldHandler types, according to the org.makumba.db.sql/odbc/redirectManager.properties file: 
<pre>
dateCreate=date
dateModify=date
</pre> 
 * More specifically, dates are written out differently,  with the special dateManager provided in this package. The other handlers are the general SQL handlers. */
public class RecordManager extends org.makumba.db.sql.RecordManager
{
}


