/*
 * Created on 18-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.makumba.sql;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Hashtable;

import org.makumba.FieldDefinition;
import org.makumba.Text;

/**
 * @author Bart TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
 *         Style - Code Templates
 */
public class OdbcjetTableManager extends org.makumba.db.makumba.sql.TableManager {

    @Override
    protected void create(org.makumba.db.makumba.sql.SQLDBConnection dbc, String tblname, boolean really)
            throws java.sql.SQLException {
        super.create(dbc, tblname, really);
        if (really) {
            dbc.commit();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException t) {
            }
        }
    }

    @Override
    protected void indexCreated(org.makumba.db.makumba.sql.SQLDBConnection dbc) {
        dbc.commit();
    }

    // moved from odbcjet.charManager, .intManager
    @Override
    protected boolean unmodified(String fieldName, int type, int size,
            java.util.Vector<Hashtable<String, Object>> columns, int index) throws java.sql.SQLException {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return super.unmodified(fieldName, type, size, columns, index) || type == java.sql.Types.VARCHAR;
            case FieldDefinition._int:
            case FieldDefinition._intEnum:
            case FieldDefinition._ptr:
            case FieldDefinition._ptrOne:
            case FieldDefinition._ptrRel:
            case FieldDefinition._ptrIndex:
                return super.unmodified(fieldName, type, size, columns, index) || type == java.sql.Types.DOUBLE;
            default:
                return super.unmodified(fieldName, type, size, columns, index);
        }
    }

    // moved from odbcjet.dateTimeManager
    /** stupdid odbc needs a {ts 'date'} format when writing date constants */
    @Override
    public String writeConstant(String fieldName, Object o) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._date:
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return "{ts " + super.writeConstant(fieldName, o) + "}";
            default:
                return super.writeConstant(fieldName, o);
        }
    }

    // moved from odbcjet.textManager
    @Override
    public void setNullArgument(String fieldName, PreparedStatement ps, int n) throws SQLException {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._text:
                ps.setNull(n, Types.LONGVARCHAR);
                break;
            default:
                super.setNullArgument(fieldName, ps, n);
        }
    }

    // moved from odbcjet.textManager
    @Override
    public void setArgument(String fieldName, PreparedStatement ps, int n, Object o) throws SQLException {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._text:
                Text t = (Text) o;
                if (t.length() == 0) {
                    ps.setBytes(n, new byte[0]);
                } else {
                    ps.setBinaryStream(n, t.toBinaryStream(), t.length());
                }
                break;
            default:
                super.setArgument(fieldName, ps, n, o);
        }
    }

    // moved from odbcjet.textManager
    @Override
    protected String getFieldDBType(String fieldName) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._text:
                return "LONGBINARY";
            default:
                return super.getFieldDBType(fieldName);
        }
    }

    // moved from odbcjet.textManager
    /**
     * get the java value of the recordSet column corresponding to this field. This method should return null if the SQL
     * field is null
     */
    @Override
    public Object get_text_Value(String fieldName, ResultSet rs, int i) throws SQLException {
        InputStream in = rs.getBinaryStream(i);
        if (rs.wasNull()) {
            return null;
        }
        return new Text(in);
    }
}