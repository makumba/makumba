package org.makumba.providers.datadefinition.mdd;

import java.io.Reader;
import java.io.StringReader;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

public class MDDParserTest {

    public static void main(String[] args) {
        
        Reader reader = new StringReader("name = type type");
        MDDLexer lexer = new MDDLexer(reader);
        MDDParser parser = new MDDParser(lexer);
               
        try
        {
            parser.declaration();
            AST tree = parser.getAST();
            System.out.println(tree.toStringTree());

            
            if(tree!=null) {
                ASTFrame frame = new ASTFrame("normal", tree);
                frame.setVisible(true);
            }

        }
        catch (RecognitionException e)
        {
            System.err.println(e.toString());
        }
        catch (TokenStreamException e)
        {
            System.err.println(e.toString());
        }

        
    }
    
}
