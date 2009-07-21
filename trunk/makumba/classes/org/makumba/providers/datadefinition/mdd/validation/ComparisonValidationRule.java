package org.makumba.providers.datadefinition.mdd.validation;

import java.util.Date;
import java.util.LinkedHashMap;

import org.makumba.InvalidValueException;
import org.makumba.NullObject;
import org.makumba.providers.datadefinition.mdd.FieldNode;
import org.makumba.providers.datadefinition.mdd.FieldType;
import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.MDDTokenTypes;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.ValidationType;

import antlr.collections.AST;

/**
 * FIXME the whole Lhs/Rhs thing is a very clumsy implementation.
 * @version $Id: ComparisonValidationRule.java,v 1.1 Jul 10, 2009 2:25:21 PM manu Exp $
 */
public class ComparisonValidationRule extends ValidationRuleNode {

    private static final long serialVersionUID = -3236085075060228473L;

    public ComparisonValidationRule(MDDNode mdd, AST originAST, ValidationType type, FieldNode parentField) {
        super(mdd, originAST, type, parentField);
    }
    
    @Override
    public String getRuleName() {
        return "compare() { " + comparisonExpression.toString() + " } : " + message + " (line " + getLine() + ")";
    }
    
    @Override
    public boolean validate(Object value) throws InvalidValueException {
        
        LinkedHashMap<String, Object> values = null;
        
        if(!(value instanceof LinkedHashMap)) {
            throw new RuntimeException("can't validate multi-field validation rule without right argument type, dude!");
        } else {
            values = (LinkedHashMap<String, Object>) value;
        }
        
        Object left = null;
        Object right = null;
        int compare = -1;
        
        
        // treat functions
        if(values.containsKey(comparisonExpression.getLhs())) {
            left = values.get(comparisonExpression.getLhs());
            if(comparisonExpression.getLhs_type() == MDDTokenTypes.UPPER) {
                left = ((String)left).toUpperCase();
            } else if(comparisonExpression.getLhs_type() == MDDTokenTypes.LOWER) {
                left = ((String)left).toLowerCase();
            }
        }
        if(values.containsKey(comparisonExpression.getRhs())) {
            right = values.get(comparisonExpression.getRhs());
            if(comparisonExpression.getRhs_type() == MDDTokenTypes.UPPER) {
                right = ((String)right).toUpperCase();
            } else if(comparisonExpression.getRhs_type() == MDDTokenTypes.LOWER) {
                right = ((String)right).toLowerCase();
            }
        }
        
        switch(comparisonExpression.getComparisonType()) {
            
            case DATE:
                if(left == null) {
                    left = comparisonExpression.getLhs_date();
                }
                if(left instanceof NullObject) {
                    left = FieldType.DATE.getEmptyValue();
                }
                
                if(right == null) {
                    right = comparisonExpression.getRhs_date();
                }
                if(right instanceof NullObject) {
                    right = FieldType.DATE.getEmptyValue();
                }
                
                compare = ((Date)left).compareTo(((Date)right));
                break;
                
            case NUMBER:
                if(left == null) {
                    left = comparisonExpression.getLhs();
                }
                if(right == null) {
                    right = comparisonExpression.getRhs();
                }
                compare = Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
                break;
                
            case STRING:
                if (((String) left).length() > 0 && ((String) right).length() > 0) {
                    compare = ((String) left).compareTo((String) right);
                } else {
                    return true;
                }
                break;
        }
        
        int compareOperator = comparisonExpression.getOperatorType();

        if (compareOperator == MDDTokenTypes.LT) {
            return throwException(compare < 0);
        } else if (compareOperator == MDDTokenTypes.LE) {
            return throwException(compare < 0 || compare == 0);
        } else if (compareOperator == MDDTokenTypes.EQ) {
            return throwException(compare == 0);
        } else if (compareOperator == MDDTokenTypes.GT) {
            return throwException(compare > 0);
        } else if (compareOperator == MDDTokenTypes.GE) {
            return throwException(compare > 0 || compare == 0);
        } else if (compareOperator == MDDTokenTypes.NE) {
            return throwException(compare != 0);
        }
        return false; // TODO: think of throwing some "cannot validate exception"        
    
    }
    
    protected boolean throwException(boolean b) throws InvalidValueException {
        if (!b) {
            throw new InvalidValueException(arguments.get(0), getErrorMessage());
        } else {
            return b;
        }
    }


}
