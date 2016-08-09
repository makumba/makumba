package org.makumba.providers.datadefinition.mdd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.makumba.forms.html.dateEditor;

import antlr.TokenStream;
import antlr.collections.AST;

/**
 * MDD expression parser. We need this parser because we cannot do all the parsing inside of the initial parser, since
 * in some cases we want to skip parsing (e.g. for function bodies).
 * 
 * @author Manuel Gay
 * @version $Id: MDDExpressionParser.java,v 1.1 08.07.2009 11:24:51 gaym Exp $
 */
public class MDDExpressionParser extends MDDExpressionBaseParser {

    private MDDFactory factory;
    private AST originalExpression;
    private String typeName;

    public MDDExpressionParser(TokenStream lexer, MDDFactory factory, String typeName, AST originalExpression) {
        super(lexer);
        this.factory = factory;
        this.typeName = typeName;
        this.originalExpression = originalExpression;
    }
    
    @Override
    protected void assignPart(ComparisonExpressionNode ce, AST part) {
        
        switch(part.getType()) {
            case DATE:
            case NOW:
            case TODAY:
                if(ce.getLhs() == null) {
                    ce.setLhs(part.getText());
                    ce.setLhs_date(handleDate(ce, part));
                    ce.setLhs_type(DATE);
                } else {
                    ce.setRhs(part.getText());
                    ce.setRhs_date(handleDate(ce, part));
                    ce.setRhs_type(DATE);
                }
                break;
                
            default:
                if(ce.getLhs() == null) {
                    ce.setLhs(part.getText());
                    ce.setLhs_type(part.getType());
                } else {
                    ce.setRhs(part.getText());
                    ce.setRhs_type(part.getType());
                }
        }
        
        
        
        
    }
    
    

    private Date handleDate(ComparisonExpressionNode ce, AST part) {
        GregorianCalendar c = new GregorianCalendar();
        c.set(Calendar.MILLISECOND, 0);
        
        switch(part.getType()) {
            case NOW:
            case TODAY:
                handleConstant(c, part);
                break;
            case DATE:
                int level = 0;
                AST arg = part.getFirstChild();
                while(arg.getNextSibling() != null && level < 10) {
                    processDate(c, arg, level);
                    level++;
                    if(level > 6) {
                        MDDAST argMDD = (MDDAST) arg;
                        MDDAST originalMDD = (MDDAST) originalExpression;
                        argMDD.setLine(originalMDD.getLine());
                        argMDD.setCol(argMDD.getColumn() + originalMDD.getColumn());
                        factory.doThrow(typeName, "date() function cannot have more than 6 arguments", argMDD);
                    }
                    arg = arg.getNextSibling();

                }
        }
        
        return c.getTime();
        
    }
    
    private void processDate(GregorianCalendar c, AST arg, int level) {
        
        switch(arg.getType()) {
            case PLUS:
            case MINUS:
                AST lhs = arg.getFirstChild();
                AST rhs = lhs.getNextSibling();
                int lhs_val = getSummandValue(c, level, lhs);
                int rhs_val = getSummandValue(c, level, rhs);
                
                if(arg.getType() == MINUS) {
                    c.set(dateEditor.components[level], lhs_val - rhs_val);
                } else {
                    c.set(dateEditor.components[level], lhs_val + rhs_val);
                }
                break;
            case NOW:
                // nothing to do
                break;
        }
    }

    private int getSummandValue(GregorianCalendar c, int level, AST lhs) {
        if(lhs.getType() == NOW) {
            return c.get(dateEditor.components[level]);
        } else if(lhs.getType() == POSITIVE_INTEGER || lhs.getType() == NEGATIVE_INTEGER) {
            return Integer.parseInt(lhs.getText());
        } else {
            throw new RuntimeException("expecting NOW or NUMBER but got " + lhs.getType());
        }
    }

    private void handleConstant(Calendar c, AST constant) {
        switch(constant.getType()) {
            case NOW:
                // we will just use the calendar
                break;
            case TODAY:
                // we use today, i.e. this dates, but 00:00.00 for the time
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                break;
        }
    }
    
}
