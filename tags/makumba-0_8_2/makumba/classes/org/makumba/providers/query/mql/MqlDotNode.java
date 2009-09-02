package org.makumba.providers.query.mql;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.providers.DataDefinitionProvider;

import antlr.SemanticException;

/**
 * This is the root of an a.b.c expression. If it occurs in SELECT or WHERE, it adds JOINs
 * 
 * @author Cristian Bogdan
 * @version $Id: MqlDotNode.java,v 1.1 Aug 5, 2008 5:34:23 PM cristi Exp $
 */
public class MqlDotNode extends MqlNode {
    private static final long serialVersionUID = 1L;

    public MqlDotNode() {
    }

    String label;

    String field;

    void processInExpression() throws SemanticException {
        // first we check if this sequence is maybe not a fully-qualified MDD type
        String path = ASTUtil.getPathText(this);
        DataDefinition type = null;
        try {
            type = DataDefinitionProvider.getInstance().getDataDefinition(path);
        } catch(DataDefinitionNotFoundError e) {
            // do nothing, we will test if type is not null
        }
        
        if(type != null) {
            int lastDot = path.lastIndexOf(".");
            String ptrIndex = lastDot > -1 ? path.substring(lastDot + 1) : path;
            setMakType(type.getFieldDefinition(ptrIndex));
        } else {
            // normally we just transcribe label.field_
            // but we keep the label and the field just in case we need another join

            field = getFirstChild().getNextSibling().getText();
            if (getFirstChild() instanceof MqlDotNode) {
                MqlDotNode son = (MqlDotNode) getFirstChild();
                label = walker.currentContext.join(son.label, son.field, null, -1, this);
            } else if (getFirstChild() instanceof MqlIdentNode) {
                label = ((MqlIdentNode) getFirstChild()).label;
            } else {
                throw new SemanticException("(expression).field not supported", "", getLine(), getColumn());
            }

            walker.currentContext.selectField(label, field, this);
        }
        
        
    }

    void processInFrom() throws SemanticException {
        // we simply compose the path
        // we work with normal text, not TextList as this is a type name, will be name-resolved later
        if (getFirstChild() instanceof MqlDotNode) {
            ((MqlDotNode) getFirstChild()).processInFrom();
        }
        setText(getFirstChild().getText() + "." + getFirstChild().getNextSibling().getText());
    }

}
