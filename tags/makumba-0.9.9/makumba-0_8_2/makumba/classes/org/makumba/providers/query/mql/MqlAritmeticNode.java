package org.makumba.providers.query.mql;

import antlr.SemanticException;

/** We need to treat arithmetic operations specially because
 * * an operation with a parameter will determine the parameter type
 * @author Cristian Bogdan
 * @version $Id: MqlComparisonNode.java,v 1.1 Aug 5, 2008 5:31:13 PM cristi Exp $
 */
public class MqlAritmeticNode extends MqlBinaryOperator {
    private static final long serialVersionUID = 1L;

    MqlNode returnType;

    public MqlAritmeticNode() {
    }

    @Override
    protected void analyzeOperands(MqlNode left, MqlNode right) throws SemanticException {
        // FIXME: we make sure that int+real= real and real+int= real, maybe only the latter is needed
        if(returnType!=null)
            return;
        returnType=left;
        if (checkParam(left, right))
            return;
        if(right.getMakType().getType().equals("int")&&
                left.getMakType().getType().equals("real")){
            returnType=left;
            return;
        }
        checkOperandTypes(left, right);
    }
    
    @Override
    protected void setMakType(MqlNode left, MqlNode right){
        boolean hasType = returnType!=null && returnType.getMakType() != null;
        if(!hasType && left.getMakType() != null) {
            returnType=left;
        } else if(!hasType && right.getMakType() != null) {
            returnType = right;
        } else if(!hasType) {
            throw new RuntimeException("could not compute return type of arithmetic expression");
        }
        setMakType(returnType.getMakType());
    }

}
