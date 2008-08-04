package org.makumba.providers.query.mql;

import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.commons.RuntimeWrappedException;

import antlr.SemanticException;
import antlr.collections.AST;

public class MqlComparisonNode extends MqlNode {
    public MqlComparisonNode() {
    }

    @Override
    public void setFirstChild(AST a) {
        super.setFirstChild(a);
        ((MqlNode) a).setFather(this);
    }

    @Override
    protected void analyze(MqlNode left, MqlNode right) {
        // FIXME: from here on, this one should be simetrical

        if(walker.error!=null)
            return;
        if(right.getType()==HqlSqlTokenTypes.PARAM|| right.getType()==HqlSqlTokenTypes.NAMED_PARAM)
            // TODO: set param type
            return;
        
        if(right.getMakType()!=null && left.getMakType()!=null)
            if(right.getMakType().isAssignableFrom(left.getMakType()))
                    return;
            else{
                walker.reportError(new SemanticException("incompatible operands "+right.getText()+" and "+left.getText()));
                return;
            }
        String s = right.getText();
        Object arg=s;
        if (right.getType() == HqlSqlTokenTypes.QUOTED_STRING)
            arg = s.substring(1, s.length() - 1);
        if (right.getType() == HqlSqlTokenTypes.NUM_INT)
            arg = Integer.parseInt(s);
        
        Object o = null;
        try {
            o = ((FieldDefinition) left.getMakType()).checkValue(arg);
        } catch (org.makumba.InvalidValueException e) {
           //walker.printer.showAst(right, walker.pw);
            walker.reportError(new antlr.SemanticException(e.getMessage()));
            return;
        }
        if (o instanceof Pointer) {
            o = new Long(((Pointer) o).longValue());
        }
        if (o instanceof Number) {
            right.setText(o.toString());
        } else
            right.setText("\'" + o + "\'");
    }

}
