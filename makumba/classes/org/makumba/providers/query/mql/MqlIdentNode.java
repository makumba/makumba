package org.makumba.providers.query.mql;

import org.makumba.DataDefinition;
import org.makumba.commons.NameResolver.TextList;

import antlr.SemanticException;

/**
 * This is a label used in the SELECT or WHERE of a query TODO issue a warning, and advice to select label.id
 * 
 * @author Cristian Bogdan
 * @version $Id: MqlIdentNode.java,v 1.1 Aug 5, 2008 5:35:48 PM cristi Exp $
 */
public class MqlIdentNode extends MqlNode {
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
            throw new SemanticException("Unknown label: " + label);
        }

        String field = null;
        if (dd.getParentField() != null) {
            String stp = dd.getParentField().getType();
            if (stp.equals("setintEnum") || stp.equals("setcharEnum")) {
                field = "enum";
                setMakType(dd.getFieldDefinition(dd.getSetMemberFieldName()));
            }
        }
        if (field == null) {
            field = dd.getIndexPointerFieldName();
            setMakType(walker.currentContext.ddp.makeFieldDefinition("x", "ptr " + dd.getName()));
        }
        text = new TextList();
        text.append(label).append(".").append(dd, field);
    }
}
