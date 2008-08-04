package org.makumba.providers.query.mql;


import org.makumba.FieldDefinition;

import antlr.CommonAST;
import antlr.collections.AST;
public class MqlNode extends CommonAST {
    MqlSqlWalker walker;
    private MqlNode father;
    private FieldDefinition makType;
    
    protected MqlSqlWalker getWalker() {
        return walker;
    }

    protected void setWalker(MqlSqlWalker walker) {
        this.walker = walker;
    }

    public MqlNode(){}

    public void setFather(MqlNode node) {
        father=node;        
    }
    @Override
    public void setNextSibling(AST a){
        super.setNextSibling(a);
        if(father!=null)
            father.analyze(this, (MqlNode)a);
    }

    protected void analyze(MqlNode left, MqlNode right) {}
    
    protected void setMakType(FieldDefinition fieldDefinition) {
        makType= fieldDefinition;
    }
    protected FieldDefinition getMakType(){
        return makType;
    }
}
