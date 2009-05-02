package org.makumba.providers.datadefinition.mdd;

import antlr.ASTFactory;

public class MDDASTFactory extends ASTFactory {
    
    @Override
    public Class getASTNodeType(int tokenType) {

        switch(tokenType) {
            case MDDTokenTypes.BINARY:
            case MDDTokenTypes.BOOLEAN:
            case MDDTokenTypes.CHAR:
            case MDDTokenTypes.DATE:
            case MDDTokenTypes.FIELD:
            case MDDTokenTypes.FILE:
            case MDDTokenTypes.INT:
            case MDDTokenTypes.PTR:
            case MDDTokenTypes.REAL:
            case MDDTokenTypes.SET:
            case MDDTokenTypes.TEXT:
                return FieldNode.class;
            default:
                return super.getASTNodeType(tokenType);
        }
        
    }

}
