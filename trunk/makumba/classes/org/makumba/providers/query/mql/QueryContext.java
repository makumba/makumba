package org.makumba.providers.query.mql;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NameResolver.TextList;
import org.makumba.providers.DataDefinitionProvider;

import antlr.SemanticException;
import antlr.collections.AST;

/**
 * FROM analysis code. Each query/subquery has its own QueryContext. 
 * This is ported from the old OQL parser. Additions:
 * 1) look in the parent query context for labels unknown here 
 * 2) more than just LEFT JOIN is supported
 * 
 * @author Cristian Bogdan
 * @version $Id: QueryContext.java,v 1.1 Aug 5, 2008 5:50:39 PM cristi Exp $
 */
public class QueryContext {
    private QueryContext parent;

    MqlSqlWalker walker;

    DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();
    
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
        addFrom(path, alias, joinType);

        if (inTree == null)
            return inTree = (MqlNode) ASTUtil.create(walker.fact, MqlSqlWalker.FROM_FRAGMENT, "fromFragment");
        else
            return null;
    }

    public void close() {
        if (inTree != null)
            inTree.setTextList(getFrom());
    }

    private TextList getFrom() {
        TextList tl = new TextList();
        writeFrom(tl);
        writeJoins(tl);

        return tl;
    }

    // //-----------

    /** associate each label to its makumba type */
    LinkedHashMap<String, DataDefinition> labels = new LinkedHashMap<String, DataDefinition>();

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
    Vector<TextList> filters = new Vector<TextList>();

    private Hashtable<String, TextList> labelText= new Hashtable<String, TextList>();

    /** labels that have fields selected, not just the primary key */
    private HashSet<String> labelFields= new HashSet<String>();

    /** labels defined explicitly in the FROM section of this subquery */
    HashSet<String> explicitLabels= new HashSet<String>();

    /** labels that have fields selected, not just the primary key */
    private HashSet<String> leftJoinedImplicit= new HashSet<String>();
    
    private boolean wroteRange;

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
     * make a new join with the name and associate the label with the type
     * 
     * @param joinType
     * @param location 
     * @throws SemanticException
     */
    String addJoin(String l1, String f1, String name, String f2, DataDefinition type, int joinType, AST location)
            throws SemanticException {
        addLabelField(l1);
        joins.addElement(new Join(l1, f1, name, f2, joinType));
        joinNames.put(l1 + "." + f1, name);
        setLabelType(name, type, location);
        if(leftJoinedImplicit.contains(l1) && joinType==HqlSqlTokenTypes.LEFT_OUTER)
            leftJoinedImplicit.add(name);
        return name;
    }

    private void addLabelField(String label) {
        if(labels.get(label)!=null)
            labelFields.add(label);
        else
            parent.addLabelField(label);
    }

    /**
     * produce a new label out of label.field, with the indicated labelf name for the result check if the indicated
     * field exists in the type of the label determine the type of the result label if more joins are necesary inbetween
     * (e.g. for sets), add these joins as well
     * 
     * @param joinType
     * @param  
     */
    String join(String label, String field, String labelf, int joinType, AST location) throws SemanticException {
        // need protection to avoid repeating a join
        String s = (String) joinNames.get(label + "." + field);
        if (s != null)
            return s;

        DataDefinition foreign = null, sub = null;
        DataDefinition type = (DataDefinition) findLabelType(label);
        String index = type.getIndexPointerFieldName();

        FieldDefinition fi = type.getFieldDefinition(field);
        if (fi == null)
            throw new SemanticException("no such field \"" + field + "\" in makumba type \"" + type.getName()
                    + "\"", "", location.getLine(), location.getColumn());

        // if the label is defined in a parent, and this is a pointer, we let the parent do the join
        if(fi.getType().startsWith("ptr") && labels.get(label)==null && parent!=null)
            return parent.join(label, field, labelf, joinType, location);

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
        if(joinType==-1 && leftJoinedImplicit.contains(label)){
            joinType= HqlSqlTokenTypes.LEFT_OUTER;            
        }

        if(joinType==-1){
            joinType= HqlSqlTokenTypes.INNER;

            if(walker.autoLeftJoin && fi.getType().startsWith("ptr") && !fi.isNotNull()){
                DataDefinition joinTable= fi.getType().equals("ptrOne")?sub:foreign;
                joinType= HqlSqlTokenTypes.LEFT_OUTER;
                String ret= addJoin(label, field, label2, joinTable.getIndexPointerFieldName(), joinTable, joinType, location);
                leftJoinedImplicit.add(ret);
                return ret;
            } 
        }
        if (fi.getType().equals("ptr") || fi.getType().equals("ptrRel"))
            return addJoin(label, field, label2, foreign.getIndexPointerFieldName(), foreign, joinType, location);
        else if (fi.getType().equals("ptrOne"))
            return addJoin(label, field, label2, sub.getIndexPointerFieldName(), sub, joinType, location);

        else if (fi.getType().equals("setComplex") || fi.getType().equals("setintEnum")
                || fi.getType().equals("setcharEnum"))
            return addJoin(label, index, label2, index, sub, joinType, location);
        else if (fi.getType().equals("set")) {
            label2 = label + "x";
            while (findLabelType(label2) != null)
                label2 += "x";

            addJoin(label, index, label2, index, sub, joinType, location);
            labels.put(label2, sub);
            if(joinType==HqlSqlTokenTypes.LEFT_OUTER && leftJoinedImplicit.contains(label))
                leftJoinedImplicit.add(label2);

            String label3 = label;
            if (labelf != null)
                label3 = labelf;
            while (findLabelType(label3) != null)
                label3 += "x";

            return addJoin(label2, sub.getSetMemberFieldName(), label3, foreign.getIndexPointerFieldName(), foreign,
                joinType, location);
        } else
            throw new SemanticException("\"" + field + "\" is not a set or pointer in makumba type \"" + type.getName()
                    + "\"", "", location.getLine(), location.getColumn());
    }

    public void addFrom(String frm, AST labelAST, int joinType) throws SemanticException {
        String label= labelAST.getText();
        String iterator = frm;
        explicitLabels.add(label);
        DataDefinition type = null;
        try {
            // if it's a type, we just add it as such
            type = ddp.getDataDefinition(iterator);
        } catch (org.makumba.DataDefinitionNotFoundError e) {
        } catch (org.makumba.DataDefinitionParseError p) {
            throw new SemanticException(p.getMessage(), "", labelAST.getLine(), labelAST.getColumn());
        }
        if (type != null) {
            setLabelType(label, type, labelAST);
            fromLabels.put(label, type);
            return;
        }

        // if it's a label.something, we add joins...
        int i = iterator.indexOf('.');

        if (i > 0) {
            String lbl = iterator.substring(0, i);
            while (true) {
                lbl = findLabel(frm, lbl, labelAST);
                iterator = iterator.substring(i + 1);
                String field = iterator;
                i = iterator.indexOf('.');
                if (i == -1) {
                    if(findLabelType(label)!=null)
                        throw new SemanticException("label defined twice: "+label, "", labelAST.getLine(), labelAST.getColumn());
                    join(lbl, field, label, joinType, labelAST);
                    break;
                }
                field = iterator.substring(0, i);
                lbl = join(lbl, field, null, joinType, labelAST);
            }
        } else {
            if (findLabelType(frm) == null)
                throw new antlr.SemanticException("could not find type \"" + frm + "\"", "", labelAST.getLine(), labelAST.getColumn());
            aliases.put(label, frm);
        }

    }

    private String findLabel(String frm, String lbl, AST location) throws SemanticException {
        if (labels.get(lbl) == null) {
            String lbl1 = (String) aliases.get(lbl);
            if (lbl1 == null)
                if (parent != null)
                    lbl1 = parent.findLabel(frm, lbl, location);
                else
                    throw new SemanticException("could not find type \"" + frm + "\" or label \"" + lbl + "\"", "", location.getLine(), location.getColumn());
            lbl = lbl1;
        }
        return lbl;
    }

    private void setLabelType(String label, DataDefinition type, AST location) throws SemanticException {
        if (findLabelType(label) != null)
            throw new antlr.SemanticException("label defined twice: " + label, "", location.getLine(), location.getColumn());
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
        wroteRange=false;
        for (Enumeration<String> e = fromLabels.keys(); e.hasMoreElements();) {
            String label = (String) e.nextElement();

            if (comma)
                ret.append(" JOIN ");
            comma = true;

            ret.append(getTableName(label))
            // .append(" AS ")
            .append(" ").append(label);
            wroteRange=true;
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
            if(walker.optimizeJoins && !isFieldUsed(j)){
                rewriteProjections(j);
                continue;
            }
            // if (and)
            // ret.append(" AND ");
            // and = true;
            if (wroteRange) {
                // if this join is not correlating with a label from a superquery
                switch (j.joinType) {
                    case HqlSqlTokenTypes.LEFT_OUTER:
                        ret.append(" LEFT");
                        break;
                    case HqlSqlTokenTypes.RIGHT_OUTER:
                        ret.append(" RIGHT");
                        break;
                    case HqlSqlTokenTypes.FULL:
                        ret.append(" FULL");
                        break;
                }
                ret.append(" JOIN ");
            }
            wroteRange=true;
            ret.append(getTableName(j.label2))
            // .append(" AS ")
            .append(" ").append(j.label2);

            TextList cond = ret;
            if (!isCorrelated(j))
                // if this join is not correlating with a label from a superquery
                ret.append(" ON ");
            else {
                // if we are correlated, we add a condition to the filters
                cond = new TextList();
                filters.add(cond);
            }

            joinCondition(cond, j);
        }
    }

    private void rewriteProjections(Join j) {
        TextList text= labelText.get(j.label2);
        if(text==null){
            if(!walker.isAnalysisQuery()){
                String msg= "unused label: "+j.label2 ;
                walker.addWarning(msg);
                java.util.logging.Logger.getLogger("org.makumba.db.query.compilation").warning(msg+ " in query "+walker.query);
            }
            return;
        }
        text.clear();
        text.append(makeTextList(j.label1, j.field1));
    }

    private TextList findLabelText(String label) {
        if(labels.get(label)==null)
            return parent.findLabelText(label);

       return addLabelText(label, labels.get(label).getIndexPointerFieldName());
    }

    private boolean isFieldUsed(Join j) {
        return labelFields.contains(j.label2);
    }

    private boolean isCorrelated(Join j) {
        return labels.get(j.label1) == null;
    }

    private void joinCondition(TextList ret, Join j) {
        ret.append(j.label1).append(".").append(getTableName(j.label1), j.field1) //
        .append("= ") //
        .append(j.label2).append(".").append(getTableName(j.label2), j.field2);
    }

    public TextList selectLabel(String label, MqlNode node) {
        DataDefinition dd = labels.get(label);
        if(dd==null)
            return parent.selectLabel(label, node);
        String field = null;
        if (dd.getParentField() != null) {
            String stp = dd.getParentField().getType();
            if (stp.equals("setintEnum") || stp.equals("setcharEnum")) {
                field = "enum";
                // there is no way to take out the joins for setintEnum or setcharEnum value selects
                labelFields.add(label);
                node.setMakType(dd.getFieldDefinition(dd.getSetMemberFieldName()));
            }
            if(stp.equals("setComplex"))
                // there is no way to take out the joins for setComplex label selects
                labelFields.add(label);                
        }
        if (field == null) {
            field = dd.getIndexPointerFieldName();
            node.setMakType(walker.currentContext.ddp.makeFieldDefinition("x", "ptr " + dd.getName()));
        }
        
        return addLabelText(label, field);
    }

    private TextList addLabelText(String label, String field) {
        TextList text = labelText.get(label);
        if(text==null){
            text = makeTextList(label, field);
            labelText.put(label, text);
        }
        return text;
    }

    private TextList makeTextList(String label, String field) {
        TextList text=new TextList();
        text.append(label).append(".").append(labels.get(label), field);
        return text;
    }

    public void selectField(String label, String field, MqlDotNode mqlDotNode) throws SemanticException{
        DataDefinition labelType = labels.get(label);
        if(labelType==null){
            parent.selectField(label, field, mqlDotNode);
            return;
        }

        if (field.equals("id") && labelType.getFieldDefinition("id") == null){
            mqlDotNode.setTextList(selectLabel(label, mqlDotNode));
            return;
        }
            
        if (labelType.getFieldDefinition(field) == null)
            throw new SemanticException("No such field " + field + " in " + labelType, "", mqlDotNode.getLine(), mqlDotNode.getColumn());
        
        labelFields.add(label);
        mqlDotNode.setTextList(makeTextList(label, field));
        mqlDotNode.setMakType(labelType.getFieldDefinition(field));
        
    }

    public Hashtable<String, MqlNode> getProjectionLabelSearch() {
        return projectionLabelSearch;
    }

    public LinkedHashMap<String, DataDefinition> getLabels() {
        return labels;
    }

}
