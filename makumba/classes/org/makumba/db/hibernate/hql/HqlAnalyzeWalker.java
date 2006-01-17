/*
 * Created on 24-Jul-2005
 */
package org.makumba.db.hibernate.hql;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import antlr.collections.AST;

public class HqlAnalyzeWalker extends HqlAnalyzeBaseWalker {
    
    private List result;
    
    AST deriveArithmethicExpr(AST ae) {
        String operator = ae.getText();
        AST firstValue = ae.getFirstChild();
        AST secondValue = firstValue.getNextSibling();
        System.out.println(firstValue);
        System.out.println(secondValue);
        
//      TODO check if this is valid with ObjectTypeAST
        //MddObjectTypeAST typeAnalyzer = new MddObjectTypeAST(firstValue, secondValue, Map aliasTypes);
        
        
        return new ExprTypeAST(ExprTypeAST.INT);
       
    }
    
    
    protected void setAlias(AST se, AST i) {
        if(se instanceof ExprTypeAST) {
            ((ExprTypeAST) se).setIdentifier(i.getText());
        }
    }
    
    public void getReturnTypes(AST r) {
        result = new LinkedList();
        AST a = r.getFirstChild();
        System.out.println();
        for(/*a = a.getNextSibling()*/; a != null; a = a.getNextSibling()) {
            result.add(a);
        }
        
        
    }

    public Map getLabelTypes(){ return aliasTypes; }

    public List getResult() {
        return result;
    }
}
