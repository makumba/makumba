package org.makumba.providers.query.hql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.hibernate.hql.ast.HqlParser;
import org.hibernate.hql.ast.ParseErrorHandler;
import org.hibernate.hql.ast.QuerySyntaxException;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.OQLParseError;
import org.makumba.commons.NameResolver;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.query.Pass1ASTPrinter;

import antlr.RecognitionException;
import antlr.collections.AST;

public class HqlAnalyzer implements QueryAnalysis {

    private DataDefinition projTypes;
    private DataDefinition paramTypes;
    private Map<String, DataDefinition> computedLabelTypes;
    private DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    private final static Map<Integer, String> integerTypeMap = new HashMap<Integer, String>();
    static {
        integerTypeMap.put(new Integer(FieldDefinition._ptr), "ptr");
        integerTypeMap.put(new Integer(FieldDefinition._ptrRel), "ptrRel");
        integerTypeMap.put(new Integer(FieldDefinition._ptrOne), "ptrOne");
        integerTypeMap.put(new Integer(FieldDefinition._ptrIndex), "ptrIndex");
        integerTypeMap.put(new Integer(FieldDefinition._int), "int");
        integerTypeMap.put(new Integer(FieldDefinition._intEnum), "intEnum");
        integerTypeMap.put(new Integer(FieldDefinition._char), "char");
        integerTypeMap.put(new Integer(FieldDefinition._charEnum), "charEnum");
        integerTypeMap.put(new Integer(FieldDefinition._text), "text");
        integerTypeMap.put(new Integer(FieldDefinition._date), "date");
        integerTypeMap.put(new Integer(FieldDefinition._dateCreate), "dateCreate");
        integerTypeMap.put(new Integer(FieldDefinition._dateModify), "dateModify");
        integerTypeMap.put(new Integer(FieldDefinition._set), "set");
        integerTypeMap.put(new Integer(FieldDefinition._setComplex), "setComplex");
        integerTypeMap.put(new Integer(FieldDefinition._nil), "nil");
        integerTypeMap.put(new Integer(FieldDefinition._real), "real");
        integerTypeMap.put(new Integer(FieldDefinition._setCharEnum), "setcharEnum");
        integerTypeMap.put(new Integer(FieldDefinition._setIntEnum), "setintEnum");
        integerTypeMap.put(new Integer(ExprTypeAST.PARAMETER), "parameter");
    }

    private String query;

    private HqlAnalyzeWalker walker;
    
    private AST parsedHQL;
    
    public HqlAnalyzer(AST pass1, DataDefinition knownLabels){
        init(pass1, knownLabels);
    }
    public HqlAnalyzer(String query1) {
        java.util.Date d = new java.util.Date();
        this.query = query1;
        
        query = QueryAnalysisProvider.checkForFrom(query);

        AST parsed= QueryProvider.getQueryAnalzyer("oql").inlineFunctions(query);
        
        boolean noFrom = QueryAnalysisProvider.reduceDummyFrom(parsed);

        init(parsed, null);
        
        
        long diff = new java.util.Date().getTime() - d.getTime();
        java.util.logging.Logger.getLogger("org.makumba.db.query.compilation").fine("HQL to SQL: " + diff + " ms: " + query);

    }

    private void init(AST t1, DataDefinition knownLabels) {
        if (t1 != null) {
            parsedHQL = t1;
            if(query==null)
                query= Pass1ASTPrinter.printAST(t1).toString();
            walker = new HqlAnalyzeWalker();
            walker.knownLabels= knownLabels;
            walker.typeComputer = new MddObjectType();
            try {
                walker.setDebug(query);
                walker.statement(t1);
            } catch(RuntimeWrappedException e1){
                throw new OQLParseError(" during analysis of query: " + query, e1.getCause()); 
            }
            catch (RuntimeException e) {
                throw new OQLParseError(" during analysis of query: " + query, e);
            } catch (RecognitionException f) {
                throw new OQLParseError("during analysis of query: " + query, f);
            }
            
              //print the tree
            /*
            AST t = walker.getAST(); if(t!=null){ ASTFrame frame = new ASTFrame("analyzed", t);
            frame.setVisible(true); }                
            */
        }

        
    }
    public synchronized DataDefinition getProjectionType() {
        if (projTypes != null)
            return projTypes;
        projTypes = ddp.getVirtualDataDefinition("Projections for " + query);
        try {
            for (int i = 0; i < walker.getResult().size(); i++) {

                ExprTypeAST atom = (ExprTypeAST) walker.getResult().get(i);
                

                String name = atom.getIdentifier();
                if (name == null) {
                    name = "col" + (i + 1);
                }
                projTypes.addField(makeField(name, atom, null));
            }
        }catch(RuntimeWrappedException e1){
            throw new OQLParseError(" during analysis of query: " + query, e1.getCause()); 
        } catch (RuntimeException e) {
            throw new OQLParseError(" during analysis of query: " + query, e);
        }
        return projTypes;
    }

    public DataDefinition getLabelType(String labelName) {
        String labelTypeName = (String) walker.getLabelTypes().get(labelName);
        if(labelTypeName==null)
            throw new OQLParseError(" unknown label "+labelName+ " in query "+query);
        return ddp.getDataDefinition(labelTypeName);
    }

    public DataDefinition getParameterTypes() {
        if (paramTypes != null)
            return paramTypes;
        paramTypes = ddp.getVirtualDataDefinition("Parameters for " + query);
        try {
            int parameterCounter = 0;
            for (Iterator<Map.Entry<String, ExprTypeAST>> i=walker.getParameterTypes().entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, ExprTypeAST> e= i.next();
                
                paramTypes.addField(makeField(e.getKey(), e.getValue(), parameterCounter));
                parameterCounter++;
            }
        } catch(RuntimeWrappedException e1){
            throw new OQLParseError(" during analysis of query: " + query, e1.getCause()); 
        }catch (RuntimeException e) {
            throw new OQLParseError("during analysis of query: " + query, e);
        }
        return paramTypes;
    }

    private FieldDefinition makeField(String name, ExprTypeAST expr, Integer count) {
        FieldDefinition fd=null;
        if (expr.getObjectType() == null) {
            if(expr.getExtraTypeInfo()!=null)
                fd=ddp.makeFieldWithName(name, (FieldDefinition)expr.getExtraTypeInfo(), expr.getDescription());
            else
                fd= ddp.makeFieldOfType(name, getTypeName(expr.getDataType()), expr
                    .getDescription());
        } else {
            // if this is a query with not named parameters
            if(name.equals("?")) {
                if(count == null) {
                    throw new OQLParseError("Unexpected projection name during HQL query analysis"); // should not happen
                }
                name = "param"+count.toString();
            }
            fd= ddp.makeFieldDefinition(name, "ptr " + expr.getObjectType() + ";" + expr.getDescription());
        }
        return fd;
    }
    
   
    public int parameterNumber() {
        return walker.getParameterTypes().size();
    }

    public int parameterAt(int index) {
        throw new UnsupportedOperationException("parameterAt");
    }
    
    public java.util.List<String> getOrderedParameterNames() {
        throw new UnsupportedOperationException("getOrderedParameterNames");
    }

    String getTypeName(int i) {
        return (String) integerTypeMap.get(new Integer(i));
    }

    public String toString() {
        String result = "Query:\n" + this.getQuery() + "\n";
        Vector<String> w = this.getProjectionType().getFieldNames();
        result += "Number of projections: " + w.size() + "\n";

        for (int i = 0; i < w.size(); i++) {
            result += (i + " FieldDef Name: " + (this.getProjectionType().getFieldDefinition(i).getName()) + "\n");
            result += (i + " FieldDef Type: " + (this.getProjectionType().getFieldDefinition(i).getType()) + "\n");
            result += (i + " FieldDef Comment: " + (this.getProjectionType().getFieldDefinition(i).getDescription()) + "\n");
        }

        return result;
    }
    
    
    private AST getSection(String sectionName) {
        boolean found = false;
        AST child = parsedHQL.getFirstChild();
        if(child == null) return null;
        while(!child.getText().equals(sectionName) || !found) {
            child = child.getNextSibling();
            if(child == null) return null;
            if (child.getText().equals(sectionName)) {
                found = true;
            }
        }
        
        return found ? child : null;
    }
    
    // workaround for Hibernate bug HHH-2390
    // see http://opensource.atlassian.com/projects/hibernate/browse/HHH-2390
    public String getHackedQuery(String query) {
        
        // first we check if there's actually an orderBy in this query, if not return the initial one
        if(getSection("order") == null) {
            return query;
        }
        
        String selectFrom = query.substring(7, query.toLowerCase().indexOf("from")).toLowerCase();
        
        // we generate a hashtable containing the corresponding elements
        // col1|general.Person p
        // col2|p.name
        // etc...
        
        Hashtable<String, String> translator = new Hashtable<String, String>();
        StringTokenizer st = new StringTokenizer(selectFrom, ",");
        while(st.hasMoreTokens()) {
            String[] split = st.nextToken().trim().split("\\s[a|A][s|S]\\s");
            String beforeAS = split[0];
            String afterAS = split[1];
            translator.put(afterAS, beforeAS);
        }
        
        // now we need to replace the col1, col2... in the "order by" part of our query by the corresponding elements we just found
        // as we know our orderBy, we're going to build a new one and append ASC or DESC
        
        String newOrderBy = new String();
        
        boolean done = false;
        AST arg = getSection("order").getFirstChild();
        while(!done) {
            newOrderBy += translator.get(arg.getText());
            arg = arg.getNextSibling();
            if(arg.getText().toUpperCase().equals("ASC") || arg.getText().toUpperCase().equals("DESC")) {
                newOrderBy += " " + arg.getText();
                break;
            } else {
                newOrderBy += ", ";
            }
        }
        
        // now we just replace in our initial query...
        int afterOrderByIndex = query.toLowerCase().indexOf("order by") + 8;
        String result = query.substring(0, afterOrderByIndex) + " " + newOrderBy;
        
        return result;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, DataDefinition> getLabelTypes() {
        if(computedLabelTypes != null) {
            return computedLabelTypes;
        }
        computedLabelTypes = new HashMap<String, DataDefinition>();
        for(String label : walker.getLabelTypes().keySet()) {
            DataDefinition type = null;
            try {
                type = ddp.getDataDefinition(walker.getLabelTypes().get(label));
            } catch(DataDefinitionNotFoundError ddnfe) {
                throw new RuntimeException("Could not find data definition for type "+walker.getLabelTypes().get(label) + " of label "+label+" in query "+getQuery());
            }
            computedLabelTypes.put(label, type);
        }
        
        return computedLabelTypes;
        
    }

    public String writeInSQLQuery(NameResolver nr) {
        throw new RuntimeException("not implemented");
    }
    public AST getPass1Tree() {
        return parsedHQL;
    }
    public Collection<String> getWarnings() {
        // no warnings supported for now
        return null;
    }

}
