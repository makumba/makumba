package org.makumba.providers.datadefinition.mdd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;

import antlr.MismatchedTokenException;
import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * MDD factory, used to turn a .mdd file into a {@link DataDefinition}.<br>
 * This class calls the inital MDD parser, then walks over it and performs the analysis and finally transforms it again
 * in order to produce the {@link DataDefinition} and its content.
 * 
 * @author Manuel Gay
 * @version $Id: MDDAnalyzer.java,v 1.1 Apr 29, 2009 8:59:46 PM manu Exp $
 */
public class MDDFactory {

    private static MDDASTFactory astFactory = new MDDASTFactory();

    private HashMap<String, BufferedReader> errorReaders = new HashMap<String, BufferedReader>();

    private MDDFactory() {

    }
    
    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder {
        static MDDFactory singleton = new MDDFactory();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }
    
    
    public static MDDFactory getInstance() {
        return SingletonHolder.singleton;
    }

    /**
     * Gets the dataDefinition for a given MDD type
     */
    public DataDefinition getDataDefinition(String typeName) {

        // step 0 - reset common fields
        errorReaders = new HashMap<String, BufferedReader>();

        // step 1 - parse the MDD
        URL u = getDataDefinitionURL(typeName, "mdd");

        AST tree = parse(typeName, u);

        return buildDataDefinition(typeName, u, tree);

    }
    
    /**
     * Gets the dataDefinition given a definition text
     */
    public DataDefinition getVirtualDataDefinition(String typeName, String definition) {
        
        // step 0 - reset common fields
        errorReaders = new HashMap<String, BufferedReader>();

        // step 1 - parse the definition
        AST tree = parse(typeName, definition);

        return buildDataDefinition(typeName, null, tree);
        
    }

    private DataDefinition buildDataDefinition(String typeName, URL u, AST tree) {
        // step 2 - analysis
        MDDAnalyzeWalker analysisWalker = null;
        try {
            analysisWalker = new MDDAnalyzeWalker(typeName, u, this);
            analysisWalker.setASTFactory(astFactory);
            analysisWalker.dataDefinition(tree);
        } catch (Throwable e) {
            doThrow(e, analysisWalker.getAST(), typeName);
        }
        doThrow(analysisWalker.error, tree, typeName);

        //System.out.println("**** Analysis walker ****");
        MakumbaDumpASTVisitor visitor2 = new MakumbaDumpASTVisitor(false);
        //visitor2.visit(analysisWalker.getAST());

        // step 3 - build the resulting DataDefinition and FieldDefinition
        MDDBuildWalker builder = null;
        try {
            builder = new MDDBuildWalker(typeName, analysisWalker.mdd, analysisWalker.typeShorthands, this);
            builder.dataDefinition(analysisWalker.getAST());
        } catch (Throwable e) {
            doThrow(e, builder.getAST(), typeName);
        }
        doThrow(builder.error, analysisWalker.getAST(), typeName);

        //System.out.println("**** Build walker ****");
        MakumbaDumpASTVisitor visitor3 = new MakumbaDumpASTVisitor(false);
        //visitor3.visit(builder.getAST());

        //System.out.println(builder.mdd.toString());

        // step 4 - make the DataDefinitionImpl object, together with its FieldDefinitionImpl objects
        DataDefinitionImpl result = new DataDefinitionImpl(builder.mdd);
        //System.out.println(result.toString());

        return result;
    }

    /**
     * parses a MDD text
     */
    protected AST parse(String typeName, String text) {

        InputStream o = new ByteArrayInputStream(text.getBytes());
        InputStream o1 = new ByteArrayInputStream(text.getBytes());

        return parse(typeName, o, o1);

    }

    private AST parse(String typeName, URL u) {

        InputStream o = null;
        InputStream o1 = null;

        try {
            o = (InputStream) u.getContent();
            o1 = (InputStream) u.getContent();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return parse(typeName, o, o1);
    }

    /** the main parser method **/
    private AST parse(String typeName, InputStream o, InputStream o1) {
        // first pass - simply parse the MDD file
        Reader reader = new InputStreamReader(o);
        MDDLexer lexer = new MDDLexer(reader);

        // create reader for error handling
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(o1));
        errorReaders.put(typeName, errorReader);

        MDDParser parser = null;
        try {
            parser = new MDDParser(lexer, this);
            parser.setASTFactory(astFactory);
            parser.dataDefinition();
            parser.postProcess();

        } catch (Throwable t) {
            doThrow(t, parser.getAST(), typeName);
        }
        doThrow(parser.error, parser.getAST(), typeName);

        AST tree = parser.getAST();

//        System.out.println("**** Parser ****");
//        MakumbaDumpASTVisitor visitor = new MakumbaDumpASTVisitor(false);
//        visitor.visit(tree);

        return tree;
    }

    /**
     * parses an included data definition (.idd)
     */
    protected AST parseIncludedDataDefinition(String includedName) {
        URL idd = getDataDefinitionURL(includedName, "idd");
        return parse(includedName, idd);
    }

    /**
     * Throws a {@link DataDefinitionParseError} at parse time
     */
    private void doThrow(Throwable t, AST debugTree, String typeName) {
        if (t == null)
            return;

        // we already have a DataDefinitionParse error, just throw it
        if (t instanceof DataDefinitionParseError) {
            throw new RuntimeException(t);
        }

        if (t instanceof RuntimeException) {
            t.printStackTrace();
            throw (RuntimeException) t;
        }

        String line = "";
        int column = 0;

        if (t instanceof MismatchedTokenException) {
            MismatchedTokenException mte = (MismatchedTokenException) t;
            line = getLine(mte.getLine(), typeName);
            column = mte.getColumn();
        } else if (t instanceof RecognitionException) {
            RecognitionException re = (RecognitionException) t;
            if (re.getColumn() > 0) {
                column = re.getColumn();
                line = getLine(re.getLine(), typeName);
            }
        }

        throw new DataDefinitionParseError(typeName, t.getMessage(), line, column - 1);
    }

    /**
     * Throws a {@link DataDefinitionParseError} based on the information returned by the {@link MDDAST}
     */
    protected void doThrow(String typeName, String message, AST ast) {
        int line = ((MDDAST) ast).getLine();
        int col = ((MDDAST) ast).getColumn();
        throw new DataDefinitionParseError(typeName, message, getLine(line, typeName), col);
    }

    protected String getLine(int lineNumber, String typeName) {
        String line = "";
        for (int i = 0; i < lineNumber; i++) {
            try {
                line = errorReaders.get(typeName).readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;

    }

    private URL getDataDefinitionURL(String typeName, String extension) throws DataDefinitionNotFoundError {
        URL u = MDDProvider.findDataDefinition(typeName, extension);
        if (u == null) {
            throw new DataDefinitionNotFoundError(typeName);
        }
        return u;
    }

    public void release() {
        // TODO Auto-generated method stub
        
    }

}
