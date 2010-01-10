package org.makumba.providers.query.mql;

import org.makumba.MakumbaError;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.datadefinition.mdd.MakumbaDumpASTVisitor;

import antlr.collections.AST;

/**
 * Walks over a first-pass tree and prints the result as MQL query
 * 
 * FIXME finish implementing it!
 * FIXME this is not fully tested
 * 
 * @author Manuel Gay
 * @version $Id: MqlTreePrinter.java,v 1.1 Aug 5, 2009 10:49:05 AM manu Exp $
 */
public class MqlTreePrinter {
    
    StringBuffer sb = new StringBuffer();
    
    QueryAnalysisProvider qp;
    
    MakumbaDumpASTVisitor v = new MakumbaDumpASTVisitor(false);
    
    public MqlTreePrinter(QueryAnalysisProvider qp) {
        this.qp = qp;
    }

    
    public String printTree(AST tree) {
        
        
        // for the moment we only handle SELECT queries
        // query [83] 
        //     SELECT_FROM [86] 
        //        FROM [22]
        //        SELECT [45] 
        //     WHERE [53] 
        //     order [41]                    

        try {
            handleAST(tree);
        } catch(Throwable npe) {
        // FIXME this is ugly because it might pop up at any time
        // but the only way to construct this printer is to account for any kind of mql query that can be constructructed
        // one way to accelerate the implementation might be to run the handleAST method against generated trees of all types
            System.out.println("Error while printing MQL tree: " + npe.getMessage());
            npe.printStackTrace();
            v.visit(tree);
        }
        
        
        String result =  sb.toString();
        
        sb = new StringBuffer();
        return result;
    }
    
    
    private void handleAST(AST a) {
        
        switch(a.getType()) {
            
            // special cases
            
            case HqlTokenTypes.QUERY:
                handleQuery(a);
                break;
            case HqlTokenTypes.SELECT_FROM:
                handleSelectFrom(a);
                break;
            case HqlTokenTypes.FROM:
                handleFrom(a);
                break;
            case HqlTokenTypes.SELECT:
                handleSelect(a);
                break;
            case HqlTokenTypes.WHERE:
                handleWhere(a);
                break;
            case HqlTokenTypes.GROUP:
                handleGroup(a);
                break;
            case HqlTokenTypes.HAVING:
                handleHaving(a);
                break;
            case HqlTokenTypes.ORDER:
                handleOrder(a);
                break;
            case HqlTokenTypes.CASE:
                handleCase(a);
                break;
            case HqlTokenTypes.CASE2:
                handleCase2(a);
                break;
            case HqlTokenTypes.WHEN:
                handleWhen(a);
                break;
            case HqlTokenTypes.ELSE:
                handleElse(a);
                break;
            case HqlTokenTypes.BETWEEN:
                handleBetween(a);
                break;
            case HqlTokenTypes.LIKE:
                handleLike(a);
                break;
            case HqlTokenTypes.NOT_LIKE:
                handleNotLike(a);
                break;
            case HqlTokenTypes.NOT_BETWEEN:
                handleNotBetween(a);
                break;
            case HqlTokenTypes.IN:
                handleIn(a);
                break;
            case HqlTokenTypes.NOT_IN:
                handleNotIn(a);
                break;
            case HqlTokenTypes.COUNT:
                handleCount(a);
                break;
            case HqlTokenTypes.METHOD_CALL:
                handleMethodCall(a);
                break;
            case HqlTokenTypes.COLON:
                handleColon(a);
                break;
            case HqlTokenTypes.AS:
                handleAs(a);
                break;
            case HqlTokenTypes.AGGREGATE:
                handleAggregate(a);
                break;
            case HqlTokenTypes.ALL:
                handleAll(a);
                break;

                
            // constants
                
            case HqlTokenTypes.ALIAS:
            case HqlTokenTypes.NUM_DOUBLE:
            case HqlTokenTypes.NUM_FLOAT:
            case HqlTokenTypes.NUM_INT:
            case HqlTokenTypes.NUM_LONG:
            case HqlTokenTypes.QUOTED_STRING:
            case HqlTokenTypes.CONSTANT:
            case HqlTokenTypes.JAVA_CONSTANT:
            case HqlTokenTypes.TRUE:
            case HqlTokenTypes.FALSE:
            case HqlTokenTypes.IDENT:
            case HqlTokenTypes.WEIRD_IDENT:
                out(a.getText());
                break;

            // middle operators
                
            case HqlTokenTypes.DOT:
            case HqlTokenTypes.PLUS:
            case HqlTokenTypes.MINUS:
            case HqlTokenTypes.DIV:
            case HqlTokenTypes.STAR:
            case HqlTokenTypes.AND:
            case HqlTokenTypes.OR:
                handleAST(a.getFirstChild());
                printOperator(a);
                handleAST(a.getFirstChild().getNextSibling());
                break;
                
            // operators that require parentheses
                
            case HqlTokenTypes.EQ:
            case HqlTokenTypes.NE:
            case HqlTokenTypes.LT:
            case HqlTokenTypes.GT:
            case HqlTokenTypes.LE:
            case HqlTokenTypes.GE:
                out("(");
                handleAST(a.getFirstChild());
                printOperator(a);
                handleAST(a.getFirstChild().getNextSibling());
                out(")");
                break;
                
            // ending operators
                
            case HqlTokenTypes.IS_NOT_NULL:
            case HqlTokenTypes.IS_NULL:
                handleAST(a.getFirstChild());
                printOperator(a);
                break;
                
            // starting operators with parentheses
                
            case HqlTokenTypes.ANY:
            case HqlTokenTypes.SOME:
            case HqlTokenTypes.EXISTS:
            case HqlTokenTypes.NOT:
            case HqlTokenTypes.IN_LIST:
                printOperator(a);
                out("(");
                handleAST(a.getFirstChild());
                out(")");
                break;
                
            // starting operators without parentheses
                
            case HqlTokenTypes.ESCAPE:
            case HqlTokenTypes.UNARY_MINUS:
            case HqlTokenTypes.UNARY_PLUS:
                printOperator(a);
                handleAST(a.getFirstChild());
                break;
                
            // constant operators
                
            case HqlTokenTypes.ASCENDING:
            case HqlTokenTypes.DESCENDING:
            case HqlTokenTypes.ROW_STAR:
            case HqlTokenTypes.DISTINCT:
            case HqlTokenTypes.INNER:
                printOperator(a);
                break;
        
            default:
                throw new MakumbaError("AST type " + a.getType() + " not implemented!");
        }
                
    }


    private void handleAll(AST a) {
        printOperator(a);
        if(a.getFirstChild() != null) {
            out("(");
            handleAST(a.getFirstChild());
            out(")");
        }
    }


    private void handleAggregate(AST a) {
        out(a.getText());
        out("(");
        handleAST(a.getFirstChild());
        out(")");
    }


    private void handleHaving(AST a) {
        out(" having ");
        handleAST(a.getFirstChild());
    }


    private void handleGroup(AST a) {
        out(" group by ");
        a = a.getFirstChild();
        handleAST(a);
        while(a.getNextSibling() != null) {
            out(", ");
            handleAST(a.getNextSibling());
            a = a.getNextSibling();
        }
        
        
    }


    private void handleAs(AST a) {
        handleAST(a.getFirstChild());
        sb.append(" as ");
        sb.append(a.getFirstChild().getNextSibling());
    }


    private void handleColon(AST a) {
        int sep = a.getFirstChild().getText().indexOf("###");
        out(qp.getParameterSyntax() + a.getFirstChild().getText().substring(0, sep));
    }
    
    private void handleCount(AST a) {
        out("count ");
        a = a.getFirstChild();
        if(a.getType() == HqlTokenTypes.DISTINCT || a.getType() == HqlTokenTypes.ALL) {
            handleAST(a);
            a = a.getNextSibling();
        }
        handleAST(a);
        
    }


    private void handleNotLike(AST a) {
        handleAST(a.getFirstChild());
        out(" not like ");
        handleAST(a.getFirstChild().getNextSibling());
        if(a.getFirstChild().getNextSibling().getNextSibling() != null) {
            handleAST(a.getFirstChild().getNextSibling().getNextSibling());
        }
    }

    private void handleLike(AST a) {
        handleAST(a.getFirstChild());
        out(" like ");
        handleAST(a.getFirstChild().getNextSibling());
        if(a.getFirstChild().getNextSibling().getNextSibling() != null) {
            handleAST(a.getFirstChild().getNextSibling().getNextSibling());
        }
        
    }


    private void handleIn(AST a) {
        handleAST(a.getFirstChild());
        out(" in ");
        if(a.getFirstChild().getNextSibling().getType() == HqlTokenTypes.QUERY) {
            handleQuery(a.getFirstChild().getNextSibling());
        } else {
            a = a.getFirstChild().getNextSibling();
            handleList(a);
        }
    }


    private void handleList(AST a) {
        out("(");
        handleAST(a);
        while(a.getNextSibling() != null) {
            out(", ");
            handleAST(a.getNextSibling());
            a = a.getNextSibling();
        }
        out(")");
    }
    
    private void handleNotIn(AST a) {
        handleAST(a.getFirstChild());
        out(" not in ");
        if(a.getFirstChild().getNextSibling().getType() == HqlTokenTypes.QUERY) {
            handleQuery(a.getFirstChild().getNextSibling());
        } else {
            a = a.getFirstChild().getNextSibling();
            handleList(a);
        }
    }


    private void handleBetween(AST a) {
        handleAST(a.getFirstChild());
        out(" between ");
        handleAST(a.getFirstChild().getNextSibling());
        out(" and ");
        handleAST(a.getFirstChild().getNextSibling().getNextSibling());
    }
    
    private void handleNotBetween(AST a) {
        handleAST(a.getFirstChild());
        out(" not between ");
        handleAST(a.getFirstChild().getNextSibling());
        out(" and ");
        handleAST(a.getFirstChild().getNextSibling().getNextSibling());
    }



    private void handleMethodCall(AST a) {
        handleAST(a.getFirstChild());
        out("(");
        AST arg = a.getFirstChild().getNextSibling().getFirstChild();
        if(arg != null) {
            handleAST(arg);
            while(arg.getNextSibling() != null) {
                out(", ");
                handleAST(arg.getNextSibling());
                arg = arg.getNextSibling();
            }
        }
        out(")");
        
    }


    private void handleCase(AST a) {
        out("case ");
        AST when = a.getFirstChild();
        handleAST(when);
        while(when.getNextSibling() != null) {
            handleAST(when.getNextSibling());
            when = when.getNextSibling();
        }
        out(" end");
    }
    
    private void handleWhen(AST a) {
        out(" when ");
        handleAST(a.getFirstChild());
        out(" then ");
        handleAST(a.getFirstChild().getNextSibling());
    }
    
    private void handleElse(AST a) {
        out(" else ");
        handleAST(a.getFirstChild());
    }
    
    private void handleCase2(AST a) {
        out("case ");
        handleAST(a.getFirstChild());
        AST when = a.getFirstChild().getNextSibling();
        while(when.getNextSibling() != null) {
            handleAST(when.getNextSibling());
            when = when.getNextSibling();
        }
        out(" end");
    }

    private void handleOrder(AST a) {
        out(" order by ");
        handleAST(a.getFirstChild());
        a = a.getFirstChild();
        while(a.getNextSibling() != null) {
            if(a.getNextSibling().getType() != HqlTokenTypes.ASCENDING && a.getNextSibling().getType() != HqlTokenTypes.DESCENDING) {
                out(", ");
            }
            handleAST(a.getNextSibling());
            a = a.getNextSibling();
        }
    }


    private void handleWhere(AST a) {
        out(" where ");
        handleAST(a.getFirstChild());
    }


    private void handleSelect(AST a) {
        out("select ");
        handleAST(a.getFirstChild());
        a = a.getFirstChild();
        while(a.getNextSibling() != null) {
            out(", ");
            handleAST(a.getNextSibling());
            a = a.getNextSibling();
        }
    }


    private void handleFrom(AST a) {
        out(" from ");
        handleRange(a.getFirstChild());
        
    }
    
    private void handleRange(AST a) {
        handleAST(a.getFirstChild());
        space();
        handleAST(a.getFirstChild().getNextSibling());
        
        if(a.getNextSibling() != null) {
            // not sure if that's okay all the time...
            sb.append(", ");
            handleRange(a.getNextSibling());
        }
    }

    private void handleSelectFrom(AST a) {
        if(a.getFirstChild().getNextSibling() != null) {
            handleSelect(a.getFirstChild().getNextSibling());
        }
        handleFrom(a.getFirstChild());
    }


    private void handleQuery(AST a) {
        
        boolean subquery = sb.length() > 0;
        
        if(subquery) {
            sb.append("(");
        }
        
        AST q = a.getFirstChild();
        handleAST(q);
        while(q.getNextSibling() != null) {
            handleAST(q.getNextSibling());
            q = q.getNextSibling();
        }
        
        if(subquery) {
            sb.append(")");
        }

    }
    
    
    
    private void printOperator(AST a) {
        switch(a.getType()) {
            case HqlTokenTypes.DOT:
                out(".");
                break;
            case HqlTokenTypes.PLUS:
                out("+");
                break;
            case HqlTokenTypes.MINUS:
                out("-");
                break;
            case HqlTokenTypes.DIV:
                out("/");
                break;
            case HqlTokenTypes.STAR:
                out("*");
                break;
            case HqlTokenTypes.UNARY_MINUS:
                out("-");
                break;
            case HqlTokenTypes.UNARY_PLUS:
                out("+");
                break;
            case HqlTokenTypes.AND:
                out(" and ");
                break;
            case HqlTokenTypes.OR:
                out(" or ");
                break;
            case HqlTokenTypes.NOT:
                out("not ");
                break;
            case HqlTokenTypes.EQ:
                out("=");
                break;
            case HqlTokenTypes.NE:
                out("!=");
                break;
            case HqlTokenTypes.LT:
                out("<");
                break;
            case HqlTokenTypes.GT:
                out(">");
                break;
            case HqlTokenTypes.LE:
                out("<=");
                break;
            case HqlTokenTypes.GE:
                out(">=");
                break;
            case HqlTokenTypes.ASCENDING:
                out(" asc");
                break;
            case HqlTokenTypes.DESCENDING:
                out(" desc");
                break;
            case HqlTokenTypes.IS_NOT_NULL:
                out(" is not null");
                break;
            case HqlTokenTypes.IS_NULL:
                out(" is null");
                break;
            case HqlTokenTypes.ANY:
                out("any ");
                break;
            case HqlTokenTypes.SOME:
                out("some ");
                break;
            case HqlTokenTypes.ALL:
                out("all ");
                break;
            case HqlTokenTypes.EXISTS:
                out("exists ");
                break;
            case HqlTokenTypes.ESCAPE:
                out(" escape ");
                break;
            case HqlTokenTypes.DISTINCT:
                out("distinct ");
                break;
            case HqlTokenTypes.ROW_STAR:
                out("*");
                break;
            case HqlTokenTypes.INNER:
                out("inner");
                break;
            case HqlTokenTypes.IN_LIST:
                out("in");
                break;
                
                default:
                    throw new MakumbaError("Operator " + a.getText() + " not defined!");

        }
        
    }

    
    private void space() {
        sb.append(" ");
    }
    
    private void out(String t) {
        sb.append(t);
    }

}
