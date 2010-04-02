package org.makumba.providers.query.mql;

import java.util.ArrayList;

import org.makumba.DataDefinition;

import antlr.SemanticException;

/**
 * This is a label used in the SELECT or WHERE of a query TODO issue a warning, and advice to select label.id
 * 
 * @author Cristian Bogdan
 * @version $Id: MqlIdentNode.java,v 1.1 Aug 5, 2008 5:35:48 PM cristi Exp $
 */
public class MqlIdentNode extends MqlNode {
    private static final long serialVersionUID = 1L;

    public String label;

    public MqlIdentNode() {
    }

    public void resolve() throws SemanticException {
        if (walker.error != null)
            return;
        if (label != null)
            // we've analyzed already
            return;
        label = getText();
        DataDefinition dd = walker.currentContext.findLabelType(label);
        if (dd == null) {
            MqlNode selectExpr = walker.currentContext.projectionLabelSearch.get(label);
            if(selectExpr!=null){
                checkAsIds= new ArrayList<String>();
                checkAsIds.add(label);
                // if we do this, we practically copy the select expression here
                // then we don't need to check a label any more
                //setTextList(selectExpr.text);
                setMakType(selectExpr.getMakType());
                return;
            }
            throw new SemanticException("Unknown label: " + label, "", getLine(), getColumn());
        }
        
        setTextList(walker.currentContext.selectLabel(label, this));
    }
}
