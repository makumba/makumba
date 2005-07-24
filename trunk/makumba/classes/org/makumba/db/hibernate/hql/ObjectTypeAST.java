/*
 * Created on 21-Jul-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.hibernate.hql;



import java.util.Map;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

public class ObjectTypeAST extends CommonAST {

    public ObjectTypeAST(AST lhs, AST rhs, Map aliasTypes) throws SemanticException{
        String type= null;
        if(lhs instanceof ObjectTypeAST){
            type= lhs.getText();
        }
        else{
            type= (String)aliasTypes.get(lhs.getText());
            if(type==null){
                throw new SemanticException("unknown alias: "+lhs.getText()+ " in property reference: "+lhs.getText()+"."+rhs.getText());
            }
        }
        
        setText(type+"->"+rhs.getText());
        System.out.println(getText());
    }

}
