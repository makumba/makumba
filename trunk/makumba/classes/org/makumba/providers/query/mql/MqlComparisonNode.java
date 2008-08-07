package org.makumba.providers.query.mql;

import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import antlr.SemanticException;

/** We need to treat comparisons specially because:
 * 1) a comparison with 'blabla' of an intEnum will be transformed into its int
 * 2) a comparison to a '7charPointer' will be transformed into that value
 * 3) a comparison to a parameter will determine the parameter type
 * @author Cristian Bogdan
 * @version $Id: MqlComparisonNode.java,v 1.1 Aug 5, 2008 5:31:13 PM cristi Exp $
 */
public class MqlComparisonNode extends MqlBinaryOperator {
    private boolean rewroteOperand;

    public MqlComparisonNode() {
    }

    @Override
    protected void analyzeOperands(MqlNode left, MqlNode right) throws SemanticException{
        if (checkParam(left, right))
            return;
        if(rewroteOperand)
            return;
        String s = right.getText();
        Object arg = s;
        if (right.getType() == HqlSqlTokenTypes.QUOTED_STRING && !left.isParam()) {
            arg = s.substring(1, s.length() - 1);

            Object o = null;
            try {
                o = ((FieldDefinition) left.getMakType()).checkValue(arg);
            } catch (org.makumba.InvalidValueException e) {
                // walker.printer.showAst(right, walker.pw);
               throw new SemanticException(e.getMessage());
            }
            if (o instanceof Pointer) {
                o = new Long(((Pointer) o).longValue());
            }
            if (o instanceof Number) {
                right.setText(o.toString());
            } else
                right.setText("\'" + o + "\'");
            rewroteOperand= true;
        }else{
            checkOperandTypes(left, right);
        }
    }

    @Override
    protected void setMakType(MqlNode left, MqlNode right) throws SemanticException {
        setMakType(walker.currentContext.ddp.makeFieldDefinition("x", "boolean"));
    }

    void checkOperandTypes(MqlNode left, MqlNode right)  throws SemanticException{
        if (!left.isParam() &&
                !right.getMakType().isAssignableFrom(left.getMakType()) &&
                !(right.getMakType().isNumberType() && left.getMakType().isNumberType())
                )
            throw new SemanticException("incompatible operands " + left.getText() + " and "
                    + right.getText());
    }    
}
