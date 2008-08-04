package org.makumba.providers.query.mql;

import org.makumba.DataDefinition;

import antlr.SemanticException;

public class MqlIdentNode extends MqlNode {
    public String label;

    public MqlIdentNode() {
    }

    public void resolve() throws SemanticException {
        // this is a label used in the query
        // TODO issue a warning, and advice to select label.id
        if(walker.error!=null)
            return;
        if(label!=null)
            // we've analyzed already
            return;
        label = getText();
        DataDefinition dd = walker.currentContext.findLabelType(label);
        if(dd==null){
            throw new SemanticException("Unknown label: "+label);
        }
        
        String field=null;
        if (dd.getParentField() != null) {
            String stp = dd.getParentField().getType();
            if (stp.equals("setintEnum") || stp.equals("setcharEnum")){
                field = "enum";
                setMakType(dd.getFieldDefinition(dd.getSetMemberFieldName()));
            }
        }
        if (field == null){
            field =dd.getIndexPointerFieldName();
            setMakType(walker.currentContext.ddp.makeFieldDefinition("x", "ptr "+dd.getName()));
        }
        setText(label + "." + walker.currentContext.getFieldName(label, field, walker.nr));
    }
}
