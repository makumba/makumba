package org.makumba.providers.query.mql;


import antlr.RecognitionException;
import antlr.collections.AST;
public class MqlSqlGenerator extends MqlSqlGeneratorBase{

    public String toString(){ return getStringBuffer().toString(); } 

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
