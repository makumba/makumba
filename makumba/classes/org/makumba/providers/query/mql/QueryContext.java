package org.makumba.providers.query.mql;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.hibernate.hql.ast.util.ASTUtil;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.commons.NameResolver;
import org.makumba.providers.DataDefinitionProvider;

import antlr.SemanticException;
import antlr.collections.AST;

public class QueryContext {
    private DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    private QueryContext parent;

    MqlSqlWalker walker;

    AST inTree = null;

    public QueryContext(MqlSqlWalker mqlSqlWalker) {
        walker = mqlSqlWalker;
    }

    public void setParent(QueryContext currentContext) {
        parent = currentContext;

    }

    public QueryContext getParent() {
        return parent;
    }

    public AST createFromElement(String path, AST alias) throws SemanticException {
        addFrom(path, alias.getText(), false);
        
        if (inTree == null)
            return inTree = ASTUtil.create(walker.fact, MqlSqlWalker.FROM_FRAGMENT, "");
        else
            return null;
    }

    public void close() {
        if(inTree!=null)
            inTree.setText(getFrom());        
    }

    private String getFrom() {
     
        StringBuffer sb=new StringBuffer();
        writeFrom(walker.nr, sb);
        writeJoins(walker.nr, sb);
        
        return sb.toString();
    }

    ////-----------
    
    
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
            this.leftJoin = leftJoin;
        }

        public String toString() {
            return label1 + "." + field1 + " JOIN " + label2 + "." + field2;
        }
    }

    /**
     * make a new join with the name and associate teh label with the type
     * 
     * @param leftJoin
     * @throws SemanticException
     */
    String addJoin(String l1, String f1, String name, String f2, DataDefinition type, boolean leftJoin)
            throws SemanticException {
        joins.addElement(new Join(l1, f1, name, f2, leftJoin));
        joinNames.put(l1 + "." + f1, name);
        setLabelType(name, type);
        return name;
    }

    /**
     * produce a new label out of label.field, with the indicated labelf name for the result check if the indicated
     * field exists in the type of the label determine the type of the result label if more joins are necesary inbetween
     * (e.g. for sets), add these joins as well
     * 
     * @param leftJoin
     */
    String join(String label, String field, String labelf, boolean leftJoin) throws SemanticException {
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
            return addJoin(label, field, label2, foreign.getIndexPointerFieldName(), foreign, leftJoin);
        else if (fi.getType().equals("ptrOne"))
            return addJoin(label, field, label2, sub.getIndexPointerFieldName(), sub, leftJoin);

        else if (fi.getType().equals("setComplex") || fi.getType().equals("setintEnum")
                || fi.getType().equals("setcharEnum"))
            return addJoin(label, index, label2, index, sub, leftJoin);
        else if (fi.getType().equals("set")) {
            label2 = label + "x";
            while (findLabelType(label2) != null)
                label2 += "x";

            // FIXME: pointers from set tables are never null, so probably leftJoin should always be false for sets
            addJoin(label, index, label2, index, sub, false);
            labels.put(label2, sub);

            String label3 = label;
            if (labelf != null)
                label3 = labelf;
            while (findLabelType(label3) != null)
                label3 += "x";

            return addJoin(label2, sub.getSetMemberFieldName(), label3, foreign.getIndexPointerFieldName(), foreign,
                leftJoin);
        } else
            throw new SemanticException("\"" + field + "\" is not a set or pointer in makumba type \""
                    + type.getName() + "\"");
    }


    
    public void addFrom(String frm, String label, boolean leftJoin) throws SemanticException {
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
                    join(lbl, field, label, leftJoin);
                    break;
                }
                field = iterator.substring(0, i);
                lbl = join(lbl, field, null, leftJoin);
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
                if(parent!=null)
                    lbl1=parent.findLabel(frm, lbl);
                else
                    throw new SemanticException("could not find type \"" + frm + "\" or label \"" + lbl
                        + "\"");
            lbl = lbl1;
        }
        return lbl;
    }

    private void setLabelType(String label, DataDefinition type) throws SemanticException {
        if (findLabelType(label) != null)
            throw new antlr.SemanticException("label defined twice: " + label);
        labels.put(label, type);
    }

    private DataDefinition findLabelType(String label) {
        DataDefinition d= labels.get(label);
        if(d==null && parent!=null)
            return parent.findLabelType(label);
        return d;
    }

    /** writes the iterator definitions (FROM part) */
    protected void writeFrom(NameResolver nr, StringBuffer ret) {
        boolean comma = false;

        for (Enumeration<String> e = fromLabels.keys(); e.hasMoreElements();) {
            String label = (String) e.nextElement();

            if (comma)
                ret.append(" JOIN ");
            comma = true;

            ret.append(getTableName(label, nr))
            // .append(" AS ")
            .append(" ").append(label);
        }
    }

    /** return the database-level name of the type of the given label */
    protected String getTableName(String label, NameResolver nr) {
        DataDefinition ri = (DataDefinition) findLabelType(label);
        try {
            // return ((org.makumba.db.sql.TableManager) d.getTable(ri)).getDBName();
            return nr.resolveTypeName(ri);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return ri.getName();
        }
    }

    /** return the database-level name of the given field of the given label */
    protected String getFieldName(String label, String field, NameResolver nr) {
        DataDefinition ri = (DataDefinition) findLabelType(label);
        try {
            // return ((org.makumba.db.sql.TableManager) d.getTable(ri)).getFieldDBName(field);
            return nr.resolveFieldName(ri, field);
        } catch (NullPointerException e) {
            return field;
        }
    }

    /** write the translator-generated joins */
    protected void writeJoins(NameResolver nr, StringBuffer ret) {
        // boolean and = false;
        for (Enumeration<Join> e = joins.elements(); e.hasMoreElements();) {
            Join j = (Join) e.nextElement();
            // if (and)
            // ret.append(" AND ");
            // and = true;

            if (j.leftJoin)
                ret.append(" LEFT");
            ret.append(" JOIN ");
            ret.append(getTableName(j.label2, nr))
            // .append(" AS ")
            .append(" ").append(j.label2);
            ret.append(" ON ");
            ret.append(j.label1).append(".").append(getFieldName(j.label1, j.field1, nr)).append("= ").append(j.label2).append(
                ".").append(getFieldName(j.label2, j.field2, nr));
        }
    }

    

}
