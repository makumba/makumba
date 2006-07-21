package org.makumba.db.hibernate.hql;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.hibernate.hql.ast.HqlParser;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.OQLAnalyzer;
import org.makumba.OQLParseError;
import antlr.SemanticException;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

public class HqlAnalyzer implements OQLAnalyzer {

    private DataDefinition result;

    private final static Map integerTypeMap = new HashMap();
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

    public HqlAnalyzer(String query) {
        this.query = query;

        HqlParser parser = HqlParser.getInstance(query);

        // Parse the input expression
        try {
            parser.statement();
            AST t1 = parser.getAST();
            
            /*
            if(t1!=null){ ASTFrame frame = new ASTFrame("normal", t1);
            frame.setVisible(true); }
            */
            
            //here I can display the tree and look at the tokens, then find them in the grammar and implement the function type detection

            // Print the resulting tree out in LISP notation
            if (t1 != null) {
                walker = new HqlAnalyzeWalker();
                walker.typeComputer = new MddObjectType();
                try {
                    walker.statement(t1);
                } catch (RuntimeException e) {
                    throw new OQLParseError("during analysis of query: " + query, e);
                }
                
                  //print the tree
                /*
                AST t = walker.getAST(); if(t!=null){ ASTFrame frame = new ASTFrame("analyzed", t);
                frame.setVisible(true); }
                */
                 
            }
        } catch (antlr.ANTLRException f) {
            throw new OQLParseError("during analysis of query: " + query, f);
        }
    }

    public String getOQL() {
        return query;
    }

    public synchronized DataDefinition getProjectionType() {
        if (result != null)
            return result;
        result = MakumbaSystem.getTemporaryDataDefinition("Projections for " + query);
        try {
            for (int i = 0; i < walker.getResult().size(); i++) {

                ExprTypeAST atom = (ExprTypeAST) walker.getResult().get(i);
                

                String name = atom.getIdentifier();
                if (name == null) {
                    name = "col" + (i + 1);
                }
                FieldDefinition fd=null;
                if (atom.getObjectType() == null) {
                    if(atom.getExtraTypeInfo()!=null)
                        fd=MakumbaSystem.makeFieldWithName(name, (FieldDefinition)atom.getExtraTypeInfo(), atom.getDescription());
                    else
                        fd= MakumbaSystem.makeFieldOfType(name, getTypeName(atom.getDataType()), atom
                            .getDescription());
                } else {
                    fd= MakumbaSystem.makeFieldDefinition(name, "ptr " + atom.getObjectType() + ";"
                            + atom.getDescription());
                }
                result.addField(fd);
            }
        } catch (RuntimeException e) {
            throw new OQLParseError("during analysis of query: " + query, e);
        }
        return result;
    }

    public DataDefinition getLabelType(String labelName) {
        return MakumbaSystem.getDataDefinition((String) walker.getLabelTypes().get(labelName));
    }

    public DataDefinition getParameterTypes() {
        // not implemented
        throw new UnsupportedOperationException("getParameterTypes");
    }

    public int parameterNumber() {
        throw new UnsupportedOperationException("getParameterNumber");
    }

    public int parameterAt(int index) {
        throw new UnsupportedOperationException("parameterAt");
    }

    String getTypeName(int i) {
        return (String) integerTypeMap.get(new Integer(i));
    }

    public String toString() {
        String result = "Query:\n" + this.getOQL() + "\n";
        Vector w = this.getProjectionType().getFieldNames();
        result += "Number of projections: " + w.size() + "\n";

        for (int i = 0; i < w.size(); i++) {
            result += (i + " FieldDef Name: " + (this.getProjectionType().getFieldDefinition(i).getName()) + "\n");
            result += (i + " FieldDef Type: " + (this.getProjectionType().getFieldDefinition(i).getType()) + "\n");
            result += (i + " FieldDef Comment: " + (this.getProjectionType().getFieldDefinition(i).getDescription()) + "\n");
        }

        return result;
    }
    
    public static void main(String[] args) {
        String q1 = "SELECT p as bullshit FROM test.Person p)";
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q1);
        
        System.out.println(oA.toString());
    }

}
