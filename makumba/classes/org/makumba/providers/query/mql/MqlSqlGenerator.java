package org.makumba.providers.query.mql;


import org.makumba.commons.NameResolver;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * Writing to SQL. This extends the class produced by mql-sql-gen.g which is adapted from Hibernate.
 * To simplify porting of new versions, the class only redefines methods declared in mql-sql-gen.g.
 * Operations redefined:
 * 1) writing is done to a NameRsolver.TextList instead of a StringBuffer to allow name-resolving later on
 * 2) minor separator redefinitions
 * @author Cristian Bogdan
 * @version $Id: MqlSqlGenerator.java,v 1.1 Aug 5, 2008 5:47:16 PM cristi Exp $
 */
public class MqlSqlGenerator extends MqlSqlGeneratorBase{

    NameResolver.TextList text= new NameResolver.TextList();
    
    protected void out(String s) {
        text.append(s);
    }

    protected void out(AST n) {
        ((MqlNode)n).writeTo(text);
    }

    public String toString(){
        return text.toString();
    }
    
    RecognitionException error;
  
    public void reportError(RecognitionException e) {
       error=e;
    }

    public void reportError(String s) {
        error= new RecognitionException(s);
    }

    public void reportWarning(String s) {
        System.out.println(s);
    }
    
    protected boolean hasText(AST a) {
        String t = a.getText();
        return t != null && t.length() > 0;
    }

    protected void fromFragmentSeparator(AST a) {
        AST next = a.getNextSibling();
        if (next != null) {
            if (a.getType() == FROM_FRAGMENT) {
                if (next.getType() == JOIN_FRAGMENT)
                    out( " " );
                else if (next.getType() == FROM_FRAGMENT)
                    out( hasText(next) ? ", " : "");
                else
                    out(" ");   // TODO: Should an exception be thrown here?
            }
            else
                out(" ");
        }
    }
    
    protected void separator(AST n, String sep) {
        if (n.getNextSibling() != null && n.getNextSibling().getType()!=HqlSqlTokenTypes.ALIAS_REF)
            out(sep);
    }

}
