package org.makumba.providers.datadefinition.mdd;

import antlr.ASTFactory;

/**
 * FIXME not sure whether this will ever be actually needed
 * @author Manuel Gay
 * 
 * @version $Id: MDDASTFactory.java,v 1.1 May 3, 2009 10:16:47 PM manu Exp $
 */
public class MDDASTFactory extends ASTFactory {
    
    @Override
    public Class getASTNodeType(int tokenType) {

        switch(tokenType) {
//            case MDDTokenTypes.FIELD:
//                return FieldNode.class;
            default:
                return super.getASTNodeType(tokenType);
        }
        
    }
    
    

}
