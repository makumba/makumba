package org.makumba.providers.datadefinition.mdd.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.InvalidValueException;
import org.makumba.providers.datadefinition.mdd.FieldNode;
import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;

import antlr.collections.AST;

public class RegExpValidationRule extends ValidationRuleNode {

    private static final long serialVersionUID = 1L;
    
    private Pattern regExpPattern;

    public RegExpValidationRule(MDDNode mdd, AST originAST, FieldNode field) {
        super(mdd, originAST, field);
    }
    
    @Override
    public String getRuleName() {
        return "matches(" + field.getName() + ") {" + expression + "} : " + message + " (line " + getLine() + ")";
    }
        
    @Override
    public boolean validate(Object value) throws InvalidValueException {
        
        if (!(value instanceof String)) {
            return false;// TODO: think of throwing some "cannot validate exception"
        }
        
        if(regExpPattern == null) {
            regExpPattern = Pattern.compile(expression);
        }
        
        Matcher matcher = regExpPattern.matcher((String) value);
        if (!matcher.matches()) {
            // throw new InvalidValueException(fieldName, "does not match regular expression '" + regExp + "'");
            throwException();
            return false;
        } else {
            return true;
        }
        
    }

}
