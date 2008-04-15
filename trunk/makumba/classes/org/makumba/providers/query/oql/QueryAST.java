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

package org.makumba.providers.query.oql;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.commons.NameResolver;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

import antlr.SemanticException;
import antlr.collections.AST;

/** an OQL query, writes out the translated SQL query */
public class QueryAST extends OQLAST implements org.makumba.OQLAnalyzer, QueryAnalysis {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    public QueryAST() {
    }

    public QueryAST(antlr.Token t) {
        super(t);
    }

    String originalQuery;

    public void setOQL(String s) {
        originalQuery = s;
    }

    public String getOQL() {
        return originalQuery;
    }

    /** markers in the chain of tokens for the different parts of the query */
    AST fromAST, whereAST, groupAST, orderAST, afterWhereAST, firstProjection;

    /** mark the FROM token during parsing */
    public void setFromAST(AST token) {
        fromAST = token;
    }

    /** mark the WHERE token during parsing */
    public void setWhereAST(AST token) {
        whereAST = token;
    }

    /** mark the GROUP BY token during parsing */
    public void setGroupAST(AST token) {
        groupAST = token;
        afterWhereAST = token;
    }

    /** mark the ORDER BY token during parsing */
    public void setOrderAST(AST token) {
        orderAST = token;
        if (groupAST == null)
            afterWhereAST = token;
    }

    /** the enclosing query, never tested */
    QueryAST superQuery;

    public void setSuperQuery(QueryAST sp) {
        superQuery = sp;
    }

    public QueryAST getSuperQuery() {
        return superQuery;
    }

    /** label of the projection, in case of an all-table projection, doesn't work presently */
    String oneProjectionLabel;

    /** projections */
    Vector<Projection> projections = new Vector<Projection>();

    /** labels of the projections, given or attributed automatically */
    Vector projectionLabels;

    /** searcher for projection labels */
    Hashtable<String, Projection> projectionLabelSearch = new Hashtable<String, Projection>();

    /** parameters for this query */
    Vector<ParamAST> parameters = new Vector<ParamAST>();

    /** the type of the returned result */
    DataDefinition resultInfo;

    /** the parameter types */
    DataDefinition paramInfo;

    /** add a projection during parsing */
    public void addProjection(Projection p) {
        projections.addElement(p);
        if (p.as.length() > 0)
            projectionLabelSearch.put(p.as, p);
    }

    /** add a parameter during parsing */
    public void addParameter(ParamAST p) {
        parameters.addElement(p);
    }

    /** the number of parameters */
    public int parameterNumber() {
        return parameters.size();
    }

    /** the parameter at the given index */
    public int parameterAt(int i) {
        return ((ParamAST) parameters.elementAt(i)).number;
    }

    /** get the type of the returned result */
    public org.makumba.DataDefinition getProjectionType() {
        if (oneProjectionLabel != null)
            return (DataDefinition) labels.get(oneProjectionLabel);

        return resultInfo;
    }

    public DataDefinition getParameterTypes() {
        return paramInfo;
    }

    /** get the type of the returned result */
    public org.makumba.DataDefinition getLabelType(String s) {
        String s1 = (String) aliases.get(s);
        if (s1 != null)
            s = s1;
        return (DataDefinition) labels.get(s);
    }

    /** set the unique projection during parsing, doesn't work */
    public void setOneProjection(String label) {
        oneProjectionLabel = label;
    }

    /** treat the existing projections, check the type of each and make up the returned type */
    void computeProjectionTypes() throws antlr.RecognitionException {
        if (oneProjectionLabel != null) {
            if (labels.get(oneProjectionLabel) == null)
                throw new antlr.SemanticException("undefined projection label: \"" + oneProjectionLabel + "\"");
        } else {
            resultInfo = ddp.getVirtualDataDefinition("Result for " + originalQuery);

            for (int i = 0; i < projections.size(); i++) {
                Projection proj = (Projection) projections.elementAt(i);

                if (proj.as == null || proj.as.length() == 0)
                    proj.as = "col" + (i + 1);
                
                Object type = proj.expr.getMakumbaType();
                if (type == null) {
                    System.out.println(((char) 7) + "\n\nno type computed for " + proj.expr.getText() + "\n\n");
                    type = "int";
                }

                if (type.toString().startsWith("set"))
                    throw new antlr.SemanticException("You cannot select a set; projection \"" + proj.as
                            + "\" with expression \"" + proj.expr + "\" has type " + type);

                FieldDefinition fd;

                if (type instanceof String)
                    fd = ddp.makeFieldOfType(proj.as, (String) type, proj.expr.getText());
                else
                    fd = ddp.makeFieldWithName(proj.as, (FieldDefinition) type, proj.expr.getText());

                resultInfo.addField(fd);
            }
        }
    }

    public void computeParameterTypes() {
        if (parameters.size() == 0)
            return;
        paramInfo = MakumbaSystem.getTemporaryDataDefinition("Parameters for " + originalQuery);

        for (int i = 0; i < parameters.size(); i++) {
            ParamAST param = (ParamAST) parameters.elementAt(i);

            FieldDefinition fd;
            String nm = "param" + i;
            if (param.makumbaType instanceof String)
                fd = MakumbaSystem.makeFieldOfType(nm, (String) param.makumbaType);
            else
                fd = MakumbaSystem.makeFieldWithName(nm, (FieldDefinition) param.makumbaType);
            paramInfo.addField(fd);
        }
    }

    /** associate each label to its makumba type */
    Hashtable<String, DataDefinition> labels = new Hashtable<String, DataDefinition>();

    /** labels explicitly defined in OQL FROM*/
    Hashtable<String, DataDefinition> fromLabels = new Hashtable<String, DataDefinition>();

    /** support aliases in query */
    Hashtable<String, String> aliases = new Hashtable<String, String>();

    /** the joins needed out of the label.field from this query */
    Vector<Join> joins = new Vector<Join>();

    /** finder for joins in the form label.field, used in order not to repeat the same join */
    Hashtable<String, String> joinNames = new Hashtable<String, String>();

    /** the four elements of a join: label1.field1 = label2.field2 */
    class Join {
        String label1;

        String field1;

        String label2;

        String field2;

        boolean leftJoin;

        public Join(String l1, String f1, String l2, String f2, boolean leftJoin) {
            label1 = l1;
            label2 = l2;
            field1 = f1;
            field2 = f2;
            this.leftJoin= leftJoin;
        }

        public String toString() {
            return label1 + "." + field1 + " JOIN " + label2 + "." + field2;
        }
    }

    /** make a new join with the name and associate teh label with the type 
     * @param leftJoin 
     * @throws SemanticException */
    String addJoin(String l1, String f1, String name, String f2, DataDefinition type, boolean leftJoin) throws SemanticException {
        joins.addElement(new Join(l1, f1, name, f2, leftJoin));
        joinNames.put(l1 + "." + f1, name);
        setLabelType(name, type);
        return name;
    }

    /**
     * produce a new label out of label.field, with the indicated labelf name for the result check if the indicated
     * field exists in the type of the label determine the type of the result label if more joins are necesary inbetween
     * (e.g. for sets), add these joins as well
     * @param leftJoin 
     */
    String join(String label, String field, String labelf, boolean leftJoin) throws antlr.RecognitionException {
        String s = (String) joinNames.get(label + "." + field);
        if (s != null)
            return s;

        // need protection to avoid repeating a join
        DataDefinition foreign = null, sub = null;
        DataDefinition type = (DataDefinition) labels.get(label);
        String index = type.getIndexPointerFieldName();

        FieldDefinition fi = type.getFieldDefinition(field);
        if (fi == null)
            throw new antlr.SemanticException("no such field \"" + field + "\" in makumba type \"" + type.getName()
                    + "\"");

        try {
            foreign = fi.getForeignTable();
        } catch (Exception e) {
        }
        try {
            sub = fi.getSubtable();
        } catch (Exception e) {
        }

        String label2 = label;
        if (labelf != null)
            label2 = labelf;

        while (labels.get(label2) != null)
            label2 += "x";

        if (fi.getType().equals("ptr"))
            return addJoin(label, field, label2, foreign.getIndexPointerFieldName(), foreign, leftJoin);
        else if (fi.getType().equals("ptrOne"))
            return addJoin(label, field, label2, sub.getIndexPointerFieldName(), sub, leftJoin);

        else if (fi.getType().equals("setComplex") || fi.getType().equals("setintEnum")
                || fi.getType().equals("setcharEnum"))
            return addJoin(label, index, label2, index, sub, leftJoin);
        else if (fi.getType().equals("set")) {
            label2 = label + "x";
            while (labels.get(label2) != null)
                label2 += "x";

            // FIXME: pointers from set tables are never null, so probably leftJoin should always be false for sets
            addJoin(label, index, label2, index, sub, false);
            labels.put(label2, sub);

            String label3 = label;
            if (labelf != null)
                label3 = labelf;
            while (labels.get(label3) != null)
                label3 += "x";

            return addJoin(label2, sub.getSetMemberFieldName(), label3, foreign.getIndexPointerFieldName(), foreign , leftJoin);
        } else
            throw new antlr.RecognitionException("\"" + field + "\" is not a set or pointer in makumba type \""
                    + type.getName() + "\"");
    }

    /**
     * add a FROM projection, if it's just a label declaration, associate the label with the type, otherwise
     * (label.field) generate the needed joins
     */
    public void addFrom(String frm, String label, boolean leftJoin) throws antlr.RecognitionException {
        String iterator = frm;
        DataDefinition type = null;
        try {
            // if it's a type, we just add it as such
            type = ddp.getDataDefinition(iterator);
        } catch (org.makumba.DataDefinitionNotFoundError e) {
        } catch (org.makumba.DataDefinitionParseError p) {
            throw new antlr.RecognitionException(p.getMessage());
        }
        if (type != null) {
            setLabelType(label, type);
            fromLabels.put(label, type);
            return;
        }

        // if it's a label.something, we add joins...
        int i = iterator.indexOf('.');

        if (i > 0) {
            String lbl = iterator.substring(0, i);
            while (true) {
                if (labels.get(lbl) == null) {
                    String lbl1 = (String) aliases.get(lbl);
                    if (lbl1 == null)
                        throw new antlr.SemanticException("could not find type \"" + frm + "\" or label \"" + lbl
                                + "\"");
                    lbl = lbl1;
                }
                iterator = iterator.substring(i + 1);
                String field = iterator;
                i = iterator.indexOf('.');
                if (i == -1) {
                    join(lbl, field, label, leftJoin);
                    break;
                }
                field = iterator.substring(0, i);
                lbl = join(lbl, field, null, leftJoin);
            }
        } else {
            if (labels.get(frm) == null)
                throw new antlr.SemanticException("could not find type \"" + frm + "\"");
            aliases.put(label, frm);
        }

    }

    private void setLabelType(String label, DataDefinition type) throws SemanticException {
        if(labels.get(label)!=null)
            throw new antlr.SemanticException("label defined twice: "+label);
        labels.put(label, type);
    }

    /** expressions, for type analysis */
    Vector<AST> expressions = new Vector<AST>();

    public void addExpression(AST token) {
        expressions.addElement(token);
    }

    /** treat the makumba identifiers, generate the needed joins */
    void computeExpressionTypes() throws antlr.RecognitionException {
        for (Enumeration e = expressions.elements(); e.hasMoreElements();) {
            OQLAST expr = ((OQLAST) e.nextElement());
            // System.out.println(expr.getClass()+" "+expr);
            expr.getMakumbaType();
        }
    }

    /** makumba identifiers label.field or label detected inside expressions */
    Vector<AST> expressionIdentifiers = new Vector<AST>();

    /** add a makumba identifier */
    public void addExpressionIdentifier(AST token) {
        expressionIdentifiers.addElement(token);
    }

    /** treat the makumba identifiers, generate the needed joins */
    void treatExpressionIdentifiers() throws antlr.RecognitionException {
        for (Enumeration e = expressionIdentifiers.elements(); e.hasMoreElements();) {
            AST token = (AST) e.nextElement();
            IdAST id = (IdAST) token;
            id.query = this;

            String s = token.getText();
            String field = null;
            String initial = s;
            int i = s.indexOf(".");

            String label = s;
            if (i != -1)
                label = s.substring(0, i);
            String l1 = (String) aliases.get(label);
            if (l1 != null)
                label = l1;
            DataDefinition ri = (DataDefinition) labels.get(label);
            if (ri == null) {
                if (i == -1 && projectionLabelSearch.get(label) != null) {
                    boolean outOfWhere = true;

                    // we seem to have a projection label which needs
                    // no type checking (as it is checked in the projection part)
                    // we just need to check if we are out of the WHERE clause
                    for (AST a = token; a != null; a = a.getNextSibling())
                        if (a == afterWhereAST) {
                            outOfWhere = false;
                            break;
                        }
                    if (outOfWhere) {
                        id.projectionLabel = label;
                        continue;
                    }
                }
                throw new antlr.SemanticException("undefined label: \"" + label + "\"");
            }

            if (i != -1)
                while (true) {
                    s = s.substring(i + 1);
                    field = s;
                    i = s.indexOf('.');
                    if (i != -1)
                        field = s.substring(0, i);
                    FieldDefinition fi = ri.getFieldDefinition(field);
                    if (fi == null)
                        throw new antlr.SemanticException("no such field \"" + field + "\" in makumba type \""
                                + ri.getName() + "\"");

                    if ((fi.getType().equals("set") || fi.getType().equals("setComplex")) && i != -1)
                        throw new antlr.SemanticException("set joins not allowed outside where clause; \"" + initial
                                + "\" contains reference to \"" + ri.getName() + "->" + field + "\"");
                    if (i == -1)
                        break;
                    
                    // FIXME: in fact it'd be better to have left join on "true" here!
                    // but we preserve backwards compatibility
                    label = join(label, field, null, false);
                    ri = fi.getPointedType();
                }
            id.label = label;
            id.field = field;
            if (id.field == null && (ri.getParentField() != null)) {
                String stp = ri.getParentField().getType();
                if (stp.equals("setintEnum") || stp.equals("setcharEnum"))
                    id.field = "enum";
            }
            if (id.field == null)
                id.field = ri.getIndexPointerFieldName();

            id.makumbaType = ri.getFieldDefinition(id.field);
        }
    }

    /** writes SELECT [DISTINCT] */
    protected void writeDistinct(NameResolver nr, StringBuffer ret) {
        if (getFirstChild().getText().toLowerCase().equals("distinct")) {
            ret.append(" DISTINCT ");
            firstProjection = getFirstChild().getNextSibling();
        } else
            firstProjection = getFirstChild();
    }

    /** writes the part between SELECT and from FROM (i.e. the projections) */
    protected void writeProjection(NameResolver nr, StringBuffer ret) {
        if (oneProjectionLabel != null)
            ret.append(" ").append(oneProjectionLabel).append(".*");
        else
            for (AST a = firstProjection; a != fromAST; a = a.getNextSibling())
                ret.append(" ").append(((OQLAST) a).writeInSQLQuery(nr));

    }

    /** writes the iterator definitions (FROM part) */
    protected void writeFrom(NameResolver nr, StringBuffer ret) {
        boolean comma = false;

        for (Enumeration e = fromLabels.keys(); e.hasMoreElements();) {
            String label = (String) e.nextElement();
                            
            if (comma)
                ret.append(" JOIN ");
            comma = true;

            ret.append(getTableName(label, nr))
            //.append(" AS ")
            .append(" ").append(label);
        }
    }

    /** return the database-level name of the type of the given label */
    protected String getTableName(String label, NameResolver nr) {
        DataDefinition ri = (DataDefinition) labels.get(label);
        try {
//            return ((org.makumba.db.sql.TableManager) d.getTable(ri)).getDBName();
            return nr.resolveTypeName(ri);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return ri.getName();
        }
    }

    /** return the database-level name of the given field of the given label*/
    protected String getFieldName(String label, String field, NameResolver nr) {
        DataDefinition ri = (DataDefinition) labels.get(label);
        try {
            //return ((org.makumba.db.sql.TableManager) d.getTable(ri)).getFieldDBName(field);
            return nr.resolveFieldName(ri, field);
        } catch (NullPointerException e) {
            return field;
        }
    }

    /** write the translator-generated joins */
    protected void writeJoins(NameResolver nr, StringBuffer ret) {
        //boolean and = false;
        for (Enumeration e = joins.elements(); e.hasMoreElements();) {
            Join j = (Join) e.nextElement();
//            if (and)
//                ret.append(" AND ");
//            and = true;
            
            if(j.leftJoin)
                ret.append(" LEFT");
            ret.append(" JOIN ");
            ret.append(getTableName(j.label2, nr))
            //.append(" AS ")
            .append(" ").append(j.label2);
            ret.append(" ON ");
            ret.append(j.label1).append(".").append(getFieldName(j.label1, j.field1, nr)).append("= ").append(j.label2).append(
               ".").append(getFieldName(j.label2, j.field2, nr));
        }
    }

    /** writes the where conditions */
    protected void writeConditions(NameResolver nr, StringBuffer ret) {
        ret.append("(");
        for (AST a = whereAST.getNextSibling(); a != afterWhereAST; a = a.getNextSibling())
            ret.append(" ").append(((OQLAST) a).writeInSQLQuery(nr));
        ret.append(")");
    }

    /** writes the rest of the query, after the WHERE part */
    protected void writeAfterWhere(NameResolver nr, StringBuffer ret) {
        for (AST a = afterWhereAST; a != null; a = a.getNextSibling())
            ret.append(" ").append(((OQLAST) a).writeInSQLQuery(nr));
    }

    /** prepare the query for writing, by looking at the expression identifiers and projections */
    public void prepare() throws antlr.RecognitionException {
        treatExpressionIdentifiers();
        computeExpressionTypes();
        computeProjectionTypes();
        computeParameterTypes();
    }

    /** write in SQL query, calling the methods for the sections*/
    @Override
    public String writeInSQLQuery(NameResolver nr) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        writeDistinct(nr, sb);
        writeProjection(nr, sb);
        if (labels.size() > 0) {
            sb.append(" FROM ");
            writeFrom(nr, sb);
        }
        writeJoins(nr, sb);
        
        if (whereAST != null ) {
            sb.append(" WHERE ");
                writeConditions(nr, sb);
        }
        writeAfterWhere(nr, sb);
        return sb.toString();
    }

    public String getQuery() {
        return getOQL();
    }

    public String getPreProcessedQuery(String query) {
        return getQuery();
    }

    public Map<String, DataDefinition> getLabelTypes() {
        
        return labels;
    }

}
