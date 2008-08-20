package org.makumba.providers.query.mql;

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
        rewroteOperand= checkAndRewriteOperand(left, right);
    }

    @Override
    protected void setMakType(MqlNode left, MqlNode right) throws SemanticException {
        setMakType(makeBooleanFieldDefinition());
    }
}
