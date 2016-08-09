package org.makumba.providers.query.mql;

import antlr.SemanticException;

/**
 * Various binary operations. We need to treat them separately to compute parameter types and to rewrite some constants
 * 
 * @author Cristian Bogdan
 * @version $Id: MqlBinaryOperator.java,v 1.1 Aug 5, 2008 5:36:29 PM cristi Exp $
 */
public class MqlBinaryOperator extends MqlNode {
    private static final long serialVersionUID = 1L;

    @Override
    protected void oneMoreChild(MqlNode node) {
        if (this.getNumberOfChildren() != 2) {
            // this is the first child, we're not yet ready for analysis
            return;
        }
        analyze((MqlNode) getFirstChild(), node);
    }

    private void analyze(MqlNode left, MqlNode right) {
        if (walker.error != null) {
            return;
        }
        if (left.isFunctionCall() || right.isFunctionCall()) {
            return;
        }
        try {
            checkForOperandType(left);
            checkForOperandType(right);
            analyzeOperands(left, right);
            if (walker.error != null && walker.error instanceof SemanticException) {
                throw (SemanticException) walker.error;
            }
            analyzeOperands(right, left);
            if (walker.error != null && walker.error instanceof SemanticException) {
                throw (SemanticException) walker.error;
            }

            setMakType(left, right);
        } catch (SemanticException se) {
            walker.error = se;
        }
    }

    protected void analyzeOperands(MqlNode left, MqlNode right) throws SemanticException {
    }

    protected void setMakType(MqlNode left, MqlNode right) throws SemanticException {
    }

}
