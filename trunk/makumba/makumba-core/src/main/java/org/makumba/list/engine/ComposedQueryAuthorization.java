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

    }

    Projections proj;

    ArrayList<AuthorizationInfo> authorizationInfos = new ArrayList<AuthorizationInfo>();

    public static ComposedQueryAuthorization getAuthorization(ComposedQuery composedQuery, String q) {
        // TODO add cache
        return new ComposedQueryAuthorization(composedQuery.derivedSections[ComposedQuery.FROM], q,
                composedQuery.basicProjections, composedQuery.authorization, composedQuery.qep);
    }

    // TODO: later: see if the expression is a field, not a pointer...
    // TODO: queries that just do a count() or sum()...
    ComposedQueryAuthorization(String from, String q, Projections pr, List<String> authorization,
            QueryAnalysisProvider qep) {
        if (authorization.isEmpty()) {
            return;
        }
        boolean auto = false;
        if (authorization.size() == 1 && authorization.get(0).equals("auto")) {
            authorization = qep.getQueryAnalysis(q).getPaths();
            auto = true;
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
                if (!auto) {
                    throw new ProgrammerError("Type indicated for authorization " + fd.getPointedType()
                            + " has no canRead() defined");
                } else {
                    continue;
                }
            }

            if (proj == null) {
                proj = new Projections(pr);
            }
            AuthorizationInfo ai = new AuthorizationInfo();

            ai.fd = fd;
            ai.expr = expr;
            ai.query = q;
            ai.message = func.getErrorMessage();
            authorizationInfos.add(ai);
        }
        if (proj == null) {
            // we found no canRead to add
            proj = pr;
            return;
        }
        // removeSupertypeCanRead();

        for (AuthorizationInfo ai : authorizationInfos) {
            String canRead = ai.expr + ".canRead()";
            proj.checkProjectionInteger(canRead);
            proj.checkProjectionInteger(ai.expr);
            ai.index = proj.getProjectionIndex(canRead);
            ai.ptrIndex = proj.getProjectionIndex(ai.expr);
        }
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

                String fieldDotField = aj.expr.substring(ai.expr.length());
                int n = 1;
                DataDefinition def = ai.fd.getPointedType();
                boolean remove = true;
                while (n != -1) {
                    int m = aj.expr.indexOf(n, '.');
                    if (m == -1) {
                        m = aj.expr.length();
                    }
                    String field = aj.expr.substring(n, m);
                    FieldDefinition fd = def.getFieldDefinition(field);
                    if (!fd.isNotNull() || !fd.getType().equals("ptr") || !fd.isFixed()) {
                        remove = false;
                        break;
                    }
                    n = m;
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

}
