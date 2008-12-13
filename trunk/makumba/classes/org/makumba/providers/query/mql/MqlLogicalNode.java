package org.makumba.providers.query.mql;


import antlr.SemanticException;

/** We need to treat logical operations specially because
 * * an operation with a parameter will determine the parameter type
 * @author Cristian Bogdan
 * @version $Id: MqlComparisonNode.java,v 1.1 Aug 5, 2008 5:31:13 PM cristi Exp $
 */
public class MqlLogicalNode extends MqlBinaryOperator {
    private static final long serialVersionUID = 1L;

    public MqlLogicalNode() {
    }

    @Override
    protected void analyzeOperands(MqlNode left, MqlNode right) throws SemanticException {
        if(right.isParam()){
            right.setMakType(makeBooleanFieldDefinition());
            return;
        }
        checkOperandTypes(left, right);
    }

    @Override
    protected void setMakType(MqlNode left, MqlNode right){
        setMakType(makeBooleanFieldDefinition());
    }

}
