/*
 * Created on Apr 1, 2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.list.engine;

import java.util.ArrayList;
import java.util.List;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.ProgrammerError;
import org.makumba.QueryFragmentFunction;
import org.makumba.providers.QueryAnalysisProvider;

public class ComposedQueryAuthorization {

    static class AuthorizationInfo {
        String expr;

        int index;

        int ptrIndex;

        String message;

        String query;

        public FieldDefinition fd;

        public boolean alreadyInWhere;

    }

    Projections proj;

    ArrayList<AuthorizationInfo> authorizationInfos = new ArrayList<AuthorizationInfo>();

    private String where;

    public static ComposedQueryAuthorization getAuthorization(ComposedQuery composedQuery, String where, String q) {
        // TODO add cache
        return new ComposedQueryAuthorization(composedQuery.derivedSections[ComposedQuery.FROM], where, q,
                composedQuery.basicProjections, composedQuery.authorization, composedQuery.qep);
    }

    // TODO: later: see if the expression is a field, not a pointer...
    // TODO: queries that just do a count() or sum()...
    ComposedQueryAuthorization(String from, String wh, String q, Projections pr, List<String> authorization,
            QueryAnalysisProvider qep) {
        if (authorization.isEmpty()) {
            return;
        }
        this.where = wh;
        boolean auto = authorization.size() == 1
                && (authorization.get(0).equals("auto") || authorization.get(0).equals("binding"));
        boolean filter = authorization.size() == 1 && authorization.get(0).equals("filter");

        if (auto || filter) {
            authorization = qep.getQueryAnalysis(q).getPaths();
        }

        StringBuffer query = new StringBuffer("SELECT ");
        String sep = "";
        for (String expr : authorization) {
            query.append(sep).append(expr);
            sep = ",";
        }
        query.append(" FROM ").append(from);

        DataDefinition dd = qep.getQueryAnalysis(query.toString()).getProjectionType();
        for (int i = 0; i < authorization.size(); i++) {
            String expr = authorization.get(i);

            FieldDefinition fd = dd.getFieldDefinition(i);

            if (!fd.getType().startsWith("ptr")) {
                throw new ProgrammerError("Authorization is only possible on pointer expressions for now: " + expr
                        + " is of type " + fd.getType());
            }

            QueryFragmentFunction func = fd.getPointedType().getFunctionOrPointedFunction("canRead");

            // TODO: the canRead() function may be missing but may be present in a supertype!!!
            if (func == null) {
                if (!auto && !filter) {
                    throw new ProgrammerError("Type indicated for authorization " + fd.getPointedType()
                            + " has no canRead() defined");
                } else {
                    continue;
                }
            }

            AuthorizationInfo ai = new AuthorizationInfo();

            ai.fd = fd;
            ai.expr = expr;
            ai.query = q;
            ai.message = func.getErrorMessage();
            ai.alreadyInWhere = isFiltered(where, expr);
            authorizationInfos.add(ai);

        }
        if (!authorizationInfos.isEmpty() && !filter) {
            proj = new Projections(pr);
        } else {
            // we found no canRead to add
            proj = pr;
        }

        removeSupertypeCanRead();

        for (java.util.Iterator<AuthorizationInfo> it = authorizationInfos.iterator(); it.hasNext();) {
            AuthorizationInfo ai = it.next();
            if (ai.alreadyInWhere) {
                it.remove();
            }
        }
        if (filter) {
            String separator = "";
            if (where != null && where.trim().length() > 0) {
                separator = " AND ";
            } else {
                where = "";
            }

            for (AuthorizationInfo ai : authorizationInfos) {
                where += separator;
                where += ai.expr + ".canRead()";
                separator = " AND ";
            }
            authorizationInfos.clear();
            System.out.println(where);

        } else {
            for (AuthorizationInfo ai : authorizationInfos) {
                String canRead = ai.expr + ".canRead()";
                proj.checkProjectionInteger(canRead);
                proj.checkProjectionInteger(ai.expr);
                ai.index = proj.getProjectionIndex(canRead);
                ai.ptrIndex = proj.getProjectionIndex(ai.expr);
            }
        }
    }

    private static boolean isFiltered(String where, String expr) {
        // FIXME: this is a string hack, needs be done via tree analysis
        String canRead = expr + ".canRead()";
        return where != null
                && (where.trim().equals(canRead) || where.trim().startsWith(canRead + " AND") || where.contains("AND "
                        + canRead));

    }

    /*
     *   if object.canRead() is present then object.superType.canRead() should not be present.
     */
    public void removeSupertypeCanRead() {

        ArrayList<AuthorizationInfo> toRemove = new ArrayList<AuthorizationInfo>();
        for (int i = 0; i < authorizationInfos.size(); i++) {
            AuthorizationInfo ai = authorizationInfos.get(i);
            for (int j = 0; j < authorizationInfos.size(); j++) {
                if (i == j) {
                    continue;
                }
                AuthorizationInfo aj = authorizationInfos.get(j);
                if (!aj.expr.startsWith(ai.expr)) {
                    continue;
                }
                // could be that some expressions repeat...
                if (aj.expr.equals(ai.expr)) {
                    toRemove.add(aj);
                    continue;
                }
                String fieldDotField = aj.expr.substring(ai.expr.length());
                int n = 1;
                if (fieldDotField.charAt(0) != '.') {
                    continue;
                }
                DataDefinition def = ai.fd.getPointedType();
                boolean remove = true;
                while (n != -1) {
                    int index = fieldDotField.indexOf(n, '.');
                    int m = index;
                    if (m == -1) {
                        m = fieldDotField.length();
                    }
                    String field = fieldDotField.substring(n, m);
                    FieldDefinition fd = def.getFieldDefinition(field);
                    if (!fd.isNotNull() || !fd.getType().equals("ptr") || !fd.isFixed()) {
                        remove = false;
                        break;
                    }
                    n = index;
                    def = fd.getPointedType();
                }
                if (remove) {
                    toRemove.add(aj);
                }
            }
        }

        for (AuthorizationInfo aj : toRemove) {
            authorizationInfos.remove(aj);
        }
    }

    public Projections getProjections() {
        return proj;
    }

    public List<AuthorizationInfo> getAuthorizationInfos() {
        return authorizationInfos;
    }

    public String getWhere() {
        return where;
    }

}
