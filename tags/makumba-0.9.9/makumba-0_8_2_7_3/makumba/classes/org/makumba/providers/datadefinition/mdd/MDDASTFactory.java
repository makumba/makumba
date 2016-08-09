package org.makumba.providers.datadefinition.mdd;

import antlr.ASTFactory;

public class MDDASTFactory extends ASTFactory {
    
    @Override
    public Class getASTNodeType(int tokenType) {

        switch(tokenType) {
            case MDDTokenTypes.TITLEFIELD:
            case MDDTokenTypes.TITLEFIELDFIELD:
            case MDDTokenTypes.TITLEFIELDFUNCTION:    
                return TitleFieldNode.class;
            default:
                return MDDAST.class;
        }
        
    }

}
