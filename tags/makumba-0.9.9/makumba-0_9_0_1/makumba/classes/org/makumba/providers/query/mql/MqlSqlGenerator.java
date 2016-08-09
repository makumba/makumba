package org.makumba.providers.query.mql;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

import org.makumba.commons.NameResolver;
import org.makumba.commons.NameResolver.TextList;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * Writing to SQL. This extends the class produced by mql-sql-gen.g which is adapted from Hibernate. To simplify porting
 * of new versions, the class only redefines methods declared in mql-sql-gen.g. Operations redefined: 1) writing is done
 * to a NameResolver.TextList instead of a StringBuffer to allow name-resolving later on 2) minor separator
 * redefinitions
 * 
 * @author Cristian Bogdan
 * @version $Id: MqlSqlGenerator.java,v 1.1 Aug 5, 2008 5:47:16 PM cristi Exp $
 */
public class MqlSqlGenerator extends MqlSqlGeneratorBase {

    NameResolver.TextList text = new NameResolver.TextList();

    private Stack<NameResolver.TextList> textListStack = new Stack<NameResolver.TextList>();

    private Stack<MQLFunctionDefinition> functionStack = new Stack<MQLFunctionDefinition>();

    @Override
    protected void out(String s) {
        // if we get an unexpected value, do some logging
        if (s == null) {
            java.util.logging.Logger.getLogger("org.makumba.db.query.compilation").log(Level.SEVERE,
                "Got 'null' to append to TextList.", new Throwable());
        }
        text.append(s);
    }

    @Override
    protected void out(AST n) {
        ((MqlNode) n).writeTo(text);
    }

    @Override
    public String toString() {
        return text.toString();
    }

    RecognitionException error;

    @Override
    public void reportError(RecognitionException e) {
        error = e;
    }

    @Override
    public void reportError(String s) {
        error = new RecognitionException(s);
    }

    @Override
    public void reportWarning(String s) {
        System.out.println(s);
    }

    @Override
    protected boolean hasText(AST a) {
        String t = a.getText();
        return t != null && t.length() > 0;
    }

    @Override
    protected void fromFragmentSeparator(AST a) {
        AST next = a.getNextSibling();
        if (next != null) {
            if (a.getType() == FROM_FRAGMENT) {
                if (next.getType() == JOIN_FRAGMENT) {
                    out(" ");
                } else if (next.getType() == FROM_FRAGMENT) {
                    out(hasText(next) ? ", " : "");
                } else {
                    out(" "); // TODO: Should an exception be thrown here?
                }
            } else {
                out(" ");
            }
        }
    }

    @Override
    protected void separator(AST n, String sep) {
        if (n.getNextSibling() != null && n.getNextSibling().getType() != HqlSqlTokenTypes.ALIAS_REF) {
            out(sep);
        }
    }

    // @Override
    // protected void nestedFromFragment(AST d, AST parent) {
    // // check a set of parent/child nodes in the from-clause tree
    // // to determine if a comma is required between them
    // if ( d != null && hasText( d ) ) {
    // if ( parent != null && hasText( parent ) ) {
    // // again, both should be FromElements
    // FromElement left = ( FromElement ) parent;
    // FromElement right = ( FromElement ) d;
    // if ( right.getRealOrigin() == left ) {
    // // right represents a joins originating from left...
    // if ( right.getJoinSequence() != null && right.getJoinSequence().isThetaStyle() ) {
    // out( ", " );
    // }
    // else {
    // out( " " );
    // }
    // }
    // else {
    // // not so sure this is even valid subtree. but if it was, it'd
    // // represent two unrelated table references...
    // out( ", " );
    // }
    // }
    // out( d );
    // }
    // }

    public static class FunctionArgumentWriter extends TextList {

        private int argInd;

        private final List<TextList> args = new ArrayList<TextList>(3);

        public List<TextList> getArgs() {
            return args;
        }

        @Override
        public TextList append(Object o) {
            // FIXME: we should probably use the methods in the super class for processing the argument
            if (argInd == args.size()) {
                TextList textList = new TextList();
                textList.append(o);
                args.add(textList);
            } else {
                args.get(argInd).append(o);
            }
            return this;
        }

        public TextList commaBetweenParameters(String comma) {
            ++argInd;
            return this;
        }

    }

    @Override
    protected void beginFunctionTemplate(AST m, AST i) {
        String name = i.getText();
        MQLFunctionDefinition function = MQLFunctionRegistry.findMQLFunction(name);
        if (function == null) { // this should actually never happen, as all our functions are defined...
            super.beginFunctionTemplate(m, i);
        } else {
            // this function has a class that will render it -> redirect output and catch the arguments
            textListStack.push(text);
            functionStack.push(function);
            text = new FunctionArgumentWriter();
        }
    }

    @Override
    protected void endFunctionTemplate(AST m) {
        if (textListStack.isEmpty()) { // this should actually never happen, as all our functions are defined...
            super.endFunctionTemplate(m);
        } else {
            FunctionArgumentWriter w = (FunctionArgumentWriter) text;
            text = textListStack.pop();
            MQLFunctionDefinition template = functionStack.pop();
            text.append(template.render(w.getArgs())); // render the function
        }
    }

    @Override
    protected void commaBetweenParameters(String comma) {
        if (text instanceof FunctionArgumentWriter) {
            ((FunctionArgumentWriter) text).commaBetweenParameters(comma);
        } else {
            super.commaBetweenParameters(comma);
        }
    }
}
