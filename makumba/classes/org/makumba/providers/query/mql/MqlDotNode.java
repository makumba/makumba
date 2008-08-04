package org.makumba.providers.query.mql;

import org.makumba.DataDefinition;

import antlr.SemanticException;

public class MqlDotNode extends MqlNode{
    public MqlDotNode(){}
    
    String label;
    String field;
    void processInExpression() throws SemanticException {
        // normally we just transcribe label.field_
        // but we keep the label and the field just in case we need another join
        // TODO: treat the id field
        
        field= getFirstChild().getNextSibling().getText();
        if(getFirstChild() instanceof MqlDotNode){
            MqlDotNode son=(MqlDotNode) getFirstChild();
            label=walker.currentContext.join(son.label, son.field, null, HqlSqlTokenTypes.INNER);
        }
        else  if(getFirstChild() instanceof MqlIdentNode){
            label=((MqlIdentNode)getFirstChild()).label;    
        }
        else throw new SemanticException("(expression).field not supported");
            
        DataDefinition labelType = walker.currentContext.findLabelType(label);
        if(field.equals("id") && labelType.getFieldDefinition("id")==null)
            field=labelType.getIndexPointerFieldName();
        String fname=walker.currentContext.getFieldName(label, 
            field,
            walker.nr);
        if(fname==null)
            throw new SemanticException("No such field " + field + " in " + labelType);
        setText(label+"."+fname);
        setMakType(labelType.getFieldDefinition(field));
    }
    
    void processInFrom() throws SemanticException{
        // we simply compose the path
        if(getFirstChild() instanceof MqlDotNode)
            ((MqlDotNode)getFirstChild()).processInFrom();
        setText(getFirstChild().getText()+"."+getFirstChild().getNextSibling().getText());
    }

}
