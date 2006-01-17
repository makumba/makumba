/*
 * Created on 24-Jul-2005
 */
package org.makumba.db.hibernate.hql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

public class HqlAnalyzeWalker extends HqlAnalyzeBaseWalker {

    private List result;

    void setAliasType(AST alias, String type) throws antlr.RecognitionException {
        if (aliasTypes.get(alias.getText()) != null)
            throw new antlr.SemanticException("alias " + alias.getText() + " defined twice");

        if(typeComputer.determineType(type, null)!=null)
            aliasTypes.put(alias.getText(), type);
        else
        {
         // we have a from section in the form a.b.c that is not a type!
         // a must be a recognizable label
            int dot= type.indexOf('.');
            if(dot==-1)
                throw new SemanticException("unknown OQL type: "+type);
            String label= type.substring(0, dot);
            String labelType= (String)aliasTypes.get(label);
            if(labelType==null)
                throw new SemanticException("unknown OQL label/alias: "+label);
            while(true){
                int dot1= type.indexOf('.', dot+1);
                String field;
                if(dot1==-1)
                    field= type.substring(dot+1);
                else
                    field= type.substring(dot+1, dot1);
                System.out.println(field);
                Object tp= typeComputer.determineType(labelType, field);
                if(!(tp instanceof String))
                    throw new SemanticException("composite type expected in FROM expression "+type+". "+field +" is a non-composite field of type "+labelType);
                labelType=(String)tp;
                dot=dot1;
                if(dot1==-1)
                    break;
            }
            aliasTypes.put(alias.getText(), labelType);            
        }
        System.out.println(alias.getText()+" "+aliasTypes.get(alias.getText()));
    }

    AST deriveArithmethicExpr(AST ae) {
        String operator = ae.getText();
        AST firstValue = ae.getFirstChild();
        AST secondValue = firstValue.getNextSibling();
        System.out.println(firstValue);
        System.out.println(secondValue);

        // TODO check if this is valid with ObjectTypeAST
        // ObjectTypeAST typeAnalyzer = new ObjectTypeAST(firstValue, secondValue, Map aliasTypes);

        return new ExprTypeAST(ExprTypeAST.INT);
    }

    protected void setAlias(AST se, AST i) {
        if (se instanceof ExprTypeAST) {
            ((ExprTypeAST) se).setIdentifier(i.getText());
        }
    }

    /** this method is called when the SELECT section (projections) is parsed */
    void getReturnTypes(AST r) throws RecognitionException {
        result = new ArrayList();
        for (AST a = r.getFirstChild(); a != null; a = a.getNextSibling()) {
            result.add(a);
        }
    }

    public Map getLabelTypes() {
        return aliasTypes;
    }

    public List getResult() {
        return result;
    }
}
