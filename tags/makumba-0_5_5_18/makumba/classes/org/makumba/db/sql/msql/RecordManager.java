package org.makumba.db.sql.msql;

/** the mSQL RecordHandler, identical with the sql RecordHandler. Due to RecordHandler field generation conventions, it will have different FieldHandler types.
 * More specifically, text fields are created differently on msql, with the special textManager provided in this package. Date constants are written differently, so a new dateSQLManager is needeed. mSQL doesn't have timestamps, so they are simulated with dates. The other handlers are the general SQL handlers. */
public class RecordManager extends org.makumba.db.sql.RecordManager
{}

