package org.makumba.providers.datadefinition.mdd;

import antlr.Token;
import antlr.collections.AST;

/**
 * Custom AST type that stores useful information for MDD parsing and analysis, such as token line and column and makumba type
 * 
 * @author Manuel Gay
 * @version $Id: MDDAST.java,v 1.1 May 8, 2009 1:44:43 PM manu Exp $
 */
public class MDDAST extends antlr.CommonAST {
    
    // makumba field type
    protected FieldType makumbaType;
    
    // whether this AST was included (for fields)
    protected boolean wasIncluded;
    
    private int col = 0, line = 0;
    
    @Override
    public void initialize(Token tok) {
        super.initialize(tok);
        line = tok.getLine();
        col = tok.getColumn();
    }
    
    @Override
    public void initialize(AST t) {
        super.initialize(t);
        if(t instanceof MDDAST) {
            line = t.getLine();
            col = t.getColumn();
        }
    }
    
    @Override
    public int getColumn() {
        return col;
    }
    
    @Override
    public int getLine() {
        return line;
    }
    
    public void setLine(int line) {
        this.line = line;
    }
    
    public void setCol(int col) {
        this.col = col;
    }

    
}
