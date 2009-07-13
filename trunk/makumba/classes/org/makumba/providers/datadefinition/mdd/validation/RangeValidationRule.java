package org.makumba.providers.datadefinition.mdd.validation;

import org.makumba.InvalidValueException;
import org.makumba.Text;
import org.makumba.providers.datadefinition.mdd.FieldNode;
import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.ValidationType;

import antlr.collections.AST;

public class RangeValidationRule extends ValidationRuleNode {
    
    private static final long serialVersionUID = 3906776093230956372L;

    private Number lowerLimit;
    
    private Number upperLimit;
    
    public RangeValidationRule(MDDNode mdd, AST originAST, FieldNode field, ValidationType type) {
        super(mdd, originAST, field);
        this.type = type;
    }
    
    @Override
    public boolean validate(Object value) throws InvalidValueException {
        
        if(lowerLimit == null || upperLimit == null) {
            
            if (lowerBound.equals("?")) {
                lowerLimit = new Double(Double.MIN_VALUE); // FIXME: use the min value makumba can handle
            } else {
                lowerLimit = Double.valueOf(lowerBound);
            }
            if (upperBound.equals("?")) {
                upperLimit = new Double(Double.MAX_VALUE); // FIXME: use the max value makumba can handle
            } else {
                upperLimit = Double.valueOf(upperBound);
            }
            
        }
        
        switch(type) {
            case LENGTH:
                if (!(value instanceof Number)) {
                    return false;// TODO: think of throwing some "cannot validate exception"
                }
                if ((lowerLimit.doubleValue() <= ((Number) value).doubleValue() && ((Number) value).doubleValue() <= upperLimit.doubleValue())) {
                    return true;
                } else {
                    throwException();
                    return false;
                }
 
                
            case RANGE:
                if (!(value instanceof String || value instanceof Text)) {
                    return false;// TODO: think of throwing some "cannot validate exception"
                }
                
                String s;
                if(value instanceof Text)
                // FIXME: we actually only need the length of the Text, not the getString()
                    s=((Text)value).getString();
                else
                    s= (String)value;
                if (lowerLimit.intValue() <= s.length() && s.length() <= upperLimit.intValue()) {
                    return true;
                } else {
                    throwException();
                    return false;
                }
            default:
                throw new RuntimeException("should not be here");
        }
        
    }
    
    @Override
    public String getRuleName() {
        return type.name().toLowerCase() + "(" + field.getName() + ") {" + lowerBound + ".." +  upperBound+ "} : " + message  + " (line " + getLine() + ")";
    }

}
