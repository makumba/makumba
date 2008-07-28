package org.makumba;

public class ForeignKeyError extends DBError {
    private static final long serialVersionUID = 1L;

    public ForeignKeyError(java.sql.SQLException se) {
        super("Foreign Key exception. " + se.getMessage());
    }

}