package org.makumba.providers.query.mql;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.hibernate.hql.ast.util.ASTUtil;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NameResolver.TextList;
import org.makumba.providers.DataDefinitionProvider;

import antlr.SemanticException;
import antlr.collections.AST;

/** FROM analysis code. Each query/subquery has its own QueryContext.
 * This is ported from the old OQL parser. Additions:
 * 1) look in the parent query context for labels unknown here
 * 2) more than just LEFT JOIN is supported
 * @author Cristian Bogdan
 * @version $Id: QueryContext.java,v 1.1 Aug 5, 2008 5:50:39 PM cristi Exp $
 */
public class QueryContext {
    DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    private QueryContext parent;

    MqlSqlWalker walker;

    MqlNode inTree = null;

    public QueryContext(MqlSqlWalker mqlSqlWalker) {
        walker = mqlSqlWalker;
    }

    public void setParent(QueryContext currentContext) {
        parent = currentContext;

    }

    public QueryContext getParent() {
        return parent;
    }

    public AST createFromElement(String path, AST alias, int joinType) throws SemanticException {
        addFrom(path, alias.getText(), joinType);

        if (inTree == null)
            return inTree = (MqlNode)ASTUtil.create(walker.fact, MqlSqlWalker.FROM_FRAGMENT, "");
        else
            return null;
    }

    public void close() {
        if (inTree != null)
            inTree.setTextList(getFrom());
    }

    private TextList getFrom() {
        TextList tl= new TextList();
        writeFrom(tl);
        writeJoins(tl);

        return tl;
    }

    // //-----------

    /** associate each label to its makumba type */
    Hashtable<String, DataDefinition> labels = new Hashtable<String, DataDefinition>();

    /** labels explicitly defined in OQL FROM */
    Hashtable<String, DataDefinition> fromLabels = new Hashtable<String, DataDefinition>();

    /** support aliases in query */
    Hashtable<String, String> aliases = new Hashtable<String, String>();

    /** the joins needed out of the label.field from this query */
    Vector<Join> joins = new Vector<Join>();

    /** finder for joins in the form label.field, used in order not to repeat the same join */
    Hashtable<String, String> joinNames = new Hashtable<String, String>();

    /** searcher for projection labels */
    Hashtable<String, MqlNode> projectionLabelSearch = new Hashtable<String, MqlNode>();
    
    /** correlation conditions */
    Vector<TextList> filters= new Vector<TextList>();

    /** the four elements of a join: label1.field1 = label2.field2 */
    class Join {
        String label1;

        String field1;

        String label2;

        String field2;

        int joinType;

        public Join(String l1, String f1, String l2, String f2, int joinType) {
            label1 = l1;
            label2 = l2;
            field1 = f1;
            field2 = f2;
            this.joinType = joinType;
        }

        public String toString() {
            return label1 + "." + field1 + " JOIN " + label2 + "." + field2;
        }
    }

    /**
     * make a new join with the name and associate teh label with the type
     * 
     * @param joinType
     * @throws SemanticException
     */
    String addJoin(String l1, String f1, String name, String f2, DataDefinition type, int joinType)
            throws SemanticException {
        joins.addElement(new Join(l1, f1, name, f2, joinType));
        joinNames.put(l1 + "." + f1, name);
        setLabelType(name, type);
        return name;
    }

    /**
     * produce a new label out of label.field, with the indicated labelf name for the result check if the indicated
     * field exists in the type of the label determine the type of the result label if more joins are necesary inbetween
     * (e.g. for sets), add these joins as well
     * 
     * @param joinType
     */
    String join(String label, String field, String labelf, int joinType) throws SemanticException {
        String s = (String) joinNames.get(label + "." + field);
        if (s != null)
            return s;

        // need protection to avoid repeating a join
        DataDefinition foreign = null, sub = null;
        DataDefinition type = (DataDefinition) findLabelType(label);
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

        while (findLabelType(label2) != null)
            label2 += "x";

        if (fi.getType().equals("ptr"))
            return addJoin(label, field, label2, foreign.getIndexPointerFieldName(), foreign, joinType);
        else if (fi.getType().equals("ptrOne"))
            return addJoin(label, field, label2, sub.getIndexPointerFieldName(), sub, joinType);

        else if (fi.getType().equals("setComplex") || fi.getType().equals("setintEnum")
                || fi.getType().equals("setcharEnum"))
            return addJoin(label, index, label2, index, sub, joinType);
        else if (fi.getType().equals("set")) {
            label2 = label + "x";
            while (findLabelType(label2) != null)
                label2 += "x";

            // FIXME: pointers from set tables are never null, so probably leftJoin should always be false for sets
            addJoin(label, index, label2, index, sub, HqlSqlTokenTypes.INNER);
            labels.put(label2, sub);

            String label3 = label;
            if (labelf != null)
                label3 = labelf;
            while (findLabelType(label3) != null)
                label3 += "x";

            return addJoin(label2, sub.getSetMemberFieldName(), label3, foreign.getIndexPointerFieldName(), foreign,
                joinType);
        } else
            throw new SemanticException("\"" + field + "\" is not a set or pointer in makumba type \"" + type.getName()
                    + "\"");
    }

    public void addFrom(String frm, String label, int joinType) throws SemanticException {
        String iterator = frm;
        DataDefinition type = null;
        try {
            // if it's a type, we just add it as such
            type = ddp.getDataDefinition(iterator);
        } catch (org.makumba.DataDefinitionNotFoundError e) {
        } catch (org.makumba.DataDefinitionParseError p) {
            throw new SemanticException(p.getMessage());
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
                lbl = findLabel(frm, lbl);
                iterator = iterator.substring(i + 1);
                String field = iterator;
                i = iterator.indexOf('.');
                if (i == -1) {
                    join(lbl, field, label, joinType);
                    break;
                }
                field = iterator.substring(0, i);
                lbl = join(lbl, field, null, joinType);
            }
        } else {
            if (findLabelType(frm) == null)
                throw new antlr.SemanticException("could not find type \"" + frm + "\"");
            aliases.put(label, frm);
        }

    }

    private String findLabel(String frm, String lbl) throws SemanticException {
        if (labels.get(lbl) == null) {
            String lbl1 = (String) aliases.get(lbl);
            if (lbl1 == null)
                if (parent != null)
                    lbl1 = parent.findLabel(frm, lbl);
                else
                    throw new SemanticException("could not find type \"" + frm + "\" or label \"" + lbl + "\"");
            lbl = lbl1;
        }
        return lbl;
    }

    private void setLabelType(String label, DataDefinition type) throws SemanticException {
        if (findLabelType(label) != null)
            throw new antlr.SemanticException("label defined twice: " + label);
        labels.put(label, type);
    }

    DataDefinition findLabelType(String label) {
        DataDefinition d = labels.get(label);
        if (d == null && parent != null)
            return parent.findLabelType(label);
        return d;
    }

    /** writes the iterator definitions (FROM part) */
    protected void writeFrom(TextList ret) {
        boolean comma = false;

        for (Enumeration<String> e = fromLabels.keys(); e.hasMoreElements();) {
            String label = (String) e.nextElement();

            if (comma)
                ret.append(" JOIN ");
            comma = true;

            ret.append(getTableName(label))
            // .append(" AS ")
            .append(" ").append(label);
        }
    }

    /** return the database-level name of the type of the given label */
    DataDefinition getTableName(String label) {
        return (DataDefinition) findLabelType(label);
    }

    /** return the database-level name of the given field of the given label */
    String getFieldName(String label, String field, NameResolver nr) {
        DataDefinition ri = (DataDefinition) findLabelType(label);
        try {
            // return ((org.makumba.db.sql.TableManager) d.getTable(ri)).getFieldDBName(field);
            return nr.resolveFieldName(ri, field);
        } catch (NullPointerException e) {
            return field;
        }
    }

    /** write the translator-generated joins */
    protected void writeJoins(TextList ret) {
        // boolean and = false;
        for (Enumeration<Join> e = joins.elements(); e.hasMoreElements();) {
            Join j = (Join) e.nextElement();
            // if (and)
            // ret.append(" AND ");
            // and = true;
            if (!isCorrelated(j)) {
                // if this join is not correlating with a label from a superquery
                switch (j.joinType) {
                    case HqlSqlTokenTypes.LEFT_OUTER:
                        ret.append(" LEFT");
                    case HqlSqlTokenTypes.RIGHT_OUTER:
                        ret.append(" RIGHT");
                    case HqlSqlTokenTypes.FULL:
                        ret.append(" FULL");
                }
                ret.append(" JOIN ");
            }

            ret.append(getTableName(j.label2))
            // .append(" AS ")
            .append(" ").append(j.label2);
            
            TextList cond= ret;
            if (!isCorrelated(j))
                // if this join is not correlating with a label from a superquery
                ret.append(" ON ");
            else{
                // if we are correlated, we add a condition to the filters
                cond= new TextList();
                filters.add(cond);
            }
            
            joinCondition(cond, j);
        }
    }

    private boolean isCorrelated(Join j) {
        return labels.get(j.label1) == null;
    }

    private void joinCondition(TextList ret, Join j) {
        ret.append(j.label1).append(".").append(getTableName(j.label1), j.field1) //
        .append("= ") //
        .append(j.label2).append(".").append(getTableName(j.label2), j.field2);
    }

}
