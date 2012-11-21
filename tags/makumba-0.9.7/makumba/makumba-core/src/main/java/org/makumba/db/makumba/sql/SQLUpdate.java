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
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.db.makumba.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.DBError;
import org.makumba.MakumbaError;
import org.makumba.NotUniqueException;
import org.makumba.commons.ParamInfo;
import org.makumba.commons.RegExpUtils;
import org.makumba.db.NativeQuery;
import org.makumba.db.makumba.Update;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

public class SQLUpdate implements Update {

    String debugString;

    String updateCommand;

    NativeQuery nat;

    boolean hasMultiple;

    static QueryAnalysisProvider qP = QueryProvider.getQueryAnalzyer("oql");

    /**
     * compute a query SELECT a=b, c=d FROM type WHERE condition, analyze it and then transform it into UPDATE type SET
     * a=b, c=d WHERE condition or DELETE FROM type WHERE condition. This computes and caches and update command in
     * String format without expanded parameters.
     * 
     * @param db
     *            the database whose name resovler hook to use
     * @param type
     *            the type where the update is made
     * @param set
     *            the list of SET statements, null for DELETE
     * @param where
     *            the WHERE condition
     */
    SQLUpdate(Database db, String type, String set, String where) {
        debugString = (set == null ? "delete" : "update") + " on type: <" + type + ">"
                + (set == null ? " " : " setting: <" + set + ">") + " where: <" + where + ">";

        // a primitive check, better one needs to be done after OQLAnalyzer's job
        if (type != null && type.indexOf(',') >= 0) {
            throw new org.makumba.OQLParseError("Only 1 table can be involved in " + debugString);
        }

        // make sure whitespace only consists of spaces
        type = type.replace('\t', ' ');

        // we determine the dummy label used in the arguments
        String label;
        try {
            label = type.substring(type.trim().indexOf(' ') + 1).trim();
        } catch (StringIndexOutOfBoundsException e) {
            throw new org.makumba.OQLParseError("Invalid delete/update 'type' section: " + type);
        }

        // to get the right SQL, we compile an imaginary MQL command made as follows:
        String OQLQuery = "SELECT " + (set == null ? label : set) + " FROM " + type;
        if (where != null) {
            OQLQuery += " WHERE " + where;
        }

        nat = NativeQuery.getNativeQuery(OQLQuery, "mql", null, db.getNameResolverHook());

        String fakeCommand;
        String fakeCommandUpper;
        try {
            fakeCommand = nat.getCommandForWriter(new ParamInfo.Writer() {
                @Override
                public void write(ParamInfo p, StringBuffer ret) {
                    ret.append('?').append(p.getName()).append('#').append(p.getPosition());
                    hasMultiple = true;
                }
            });
        } catch (RuntimeException e) {
            throw new MakumbaError(e, debugString + "\n" + OQLQuery);
        }
        fakeCommandUpper = fakeCommand.toUpperCase();
        StringBuffer replaceLabel = new StringBuffer();

        // we remove all "label." sequences from the SELECT part of the command
        int n = 0;
        int lastN;
        int maxN = fakeCommandUpper.indexOf(" FROM ");
        while (true) {
            lastN = n;
            n = fakeCommand.indexOf(label + ".", lastN);
            if (n == -1 || n > maxN) {
                replaceLabel.append(fakeCommand.substring(lastN, maxN));
                break;
            }
            replaceLabel.append(fakeCommand.substring(lastN, n));
            n += label.length() + 1;
        }

        // we remove the last instance of " label" from the FROM part of command
        lastN = fakeCommandUpper.indexOf(" WHERE ");
        if (lastN < 0) {
            lastN = fakeCommand.length();
        }
        n = fakeCommand.lastIndexOf(" " + label, lastN);
        replaceLabel.append(fakeCommand.substring(maxN, n));

        // we remove all "label." sequences from the WHERE part of the command
        n = lastN; // start where we left off above
        while (true) {
            lastN = n;
            n = fakeCommand.indexOf(label + ".", lastN);
            if (n == -1) {
                replaceLabel.append(fakeCommand.substring(lastN));
                break;
            }
            replaceLabel.append(fakeCommand.substring(lastN, n));
            n += label.length() + 1;
        }

        fakeCommand = replaceLabel.toString();
        fakeCommandUpper = fakeCommand.toUpperCase();

        // now we break the query SQL in pieces to form the update SQL
        StringBuffer command = new StringBuffer();
        command.append(set == null ? "DELETE FROM" : "UPDATE");
        command.append(fakeCommand.substring(fakeCommandUpper.indexOf(" FROM ") + 5,
            fakeCommandUpper.indexOf(" WHERE ")));
        if (set != null) {
            String setString = fakeCommand.substring(fakeCommandUpper.indexOf("SELECT ") + 7,
                fakeCommandUpper.indexOf(" FROM "));
            n = 0;
            while (true) {
                n = setString.toLowerCase().indexOf("is null", n);
                if (n == -1) {
                    n = setString.toLowerCase().indexOf("is  null", n);
                    if (n == -1) {
                        break;
                    }
                    setString = setString.substring(0, n) + " = null" + setString.substring(n + 8);
                    continue;
                }
                setString = setString.substring(0, n) + " = null" + setString.substring(n + 7);
            }
            command.append(" SET ").append(setString);
        }
        if (where != null) {
            command.append(fakeCommand.substring(fakeCommandUpper.indexOf(" WHERE ")));
        }

        debugString += "\n generated SQL: " + command;
        updateCommand = command.toString();
    }

    static Pattern param = Pattern.compile("\\?(" + RegExpUtils.identifier + ")\\#" + "(" + RegExpUtils.digit + "*)");

    @Override
    /**
     * expand multiple parameters in the cached update command, and execute the statement
     */
    public int execute(org.makumba.db.makumba.DBConnection dbc, Map<String, Object> args) {
        String comm = updateCommand;

        if (hasMultiple) {
            StringBuffer command = new StringBuffer();

            Matcher m = param.matcher(updateCommand);

            ParamInfo.Writer multiWriter = new ParamInfo.MultipleWriter(args);

            while (m.find()) {
                ParamInfo po = new ParamInfo(m.group(1), Integer.parseInt(m.group(2)));
                StringBuffer param = new StringBuffer();
                multiWriter.write(po, param);
                m.appendReplacement(command, param.toString());
            }
            m.appendTail(command);
            comm = command.toString();
        }

        PreparedStatement ps = ((SQLDBConnection) dbc).getPreparedStatement(comm);

        try {

            nat.assignParameters(((Database) dbc.getHostDatabase()).makeParameterHandler(ps, dbc, comm), args);

            // org.makumba.db.sql.Database db=(org.makumba.db.sql.Database)dbc.getHostDatabase();

            java.util.logging.Logger.getLogger("org.makumba.db.update.execution").fine(
                "" + ((Database) dbc.getHostDatabase()).getWrappedStatementToString(ps));
            java.util.Date d = new java.util.Date();
            int rez;
            try {
                rez = ps.executeUpdate();
            } catch (SQLException se) {
                Database db = (Database) dbc.getHostDatabase();
                if (db.isForeignKeyViolationException(se)) {
                    throw new org.makumba.ForeignKeyError(db.parseReadableForeignKeyErrorMessage(se));
                } else if (db.isDuplicateException(se)) {

                    NotUniqueException nue = new NotUniqueException(se.getMessage());
                    nue.setFields(db.getDuplicateFields(se));
                    throw nue;

                }
                org.makumba.db.makumba.sql.Database.logException(se);
                throw new DBError(se, debugString);
            }
            long diff = new java.util.Date().getTime() - d.getTime();
            java.util.logging.Logger.getLogger("org.makumba.db.update.performance").fine(
                "" + diff + " ms " + debugString);
            return rez;
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                throw new org.makumba.DBError(e);
            }
        }
    }
}
