package org.makumba.providers.datadefinition.mdd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.HashMap;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;

import antlr.ANTLRException;
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
        MDDParser parser = parse(typeName, u, false);
        return buildDataDefinition(typeName, u, parser, true, false);

    }
    
    /**
     * Gets the dataDefinition given a definition text
     */
    public DataDefinition getVirtualDataDefinition(String typeName, String definition) {
        
        // step 0 - reset common fields
        errorReaders = new HashMap<String, BufferedReader>();

        // step 1 - parse the definition
        MDDParser parser = parse(typeName, definition);
        
        return buildDataDefinition(typeName, null, parser, false, true);
        
    }

    private DataDefinition buildDataDefinition(String typeName, URL u, MDDParser parser, boolean strictTypeCheck, boolean virtual) {
        // step 2 - analysis
        MDDAnalyzeWalker analysisWalker = null;
        try {
            analysisWalker = new MDDAnalyzeWalker(typeName, u, this, parser, strictTypeCheck);
            analysisWalker.setASTFactory(astFactory);
            analysisWalker.dataDefinition(parser.getAST());
        } catch (ANTLRException t) {
            doThrow(t, parser.getAST(), typeName);
        } catch(DataDefinitionParseError dpe) {
            throw dpe;
        } catch(NullPointerException npe) {
            if(analysisWalker.error != null) {
                doThrow(analysisWalker.error, parser.getAST(), typeName);
            } else {
                throw npe;
            }
        } catch(Throwable t) {
            doThrow(t, parser.getAST(), typeName);
        }
        doThrow(parser.error, parser.getAST(), typeName);
        
        if(analysisWalker.getAST() == null && parser.getAST() != null) {
            throw new DataDefinitionParseError("Could not parse " + typeName + ": analyser returned empty AST. Please report to developers.");
        }
        
        
//        System.out.println("**** Analysis walker for " + typeName + " ****");
//        MakumbaDumpASTVisitor visitor2 = new MakumbaDumpASTVisitor(false);
//        visitor2.visit(analysisWalker.getAST());

        
        
        // step 3 - build the resulting DataDefinition and FieldDefinition
        MDDPostProcessorWalker postProcessor = null;
        try {
            postProcessor = new MDDPostProcessorWalker(typeName, analysisWalker.mdd, analysisWalker.typeShorthands, this);
            postProcessor.dataDefinition(analysisWalker.getAST());
        } catch (ANTLRException t) {
            doThrow(t, parser.getAST(), typeName);
        } catch(DataDefinitionParseError dpe) {
            throw dpe;
        } catch(NullPointerException npe) {
            if(postProcessor.error != null) {
                doThrow(postProcessor.error, parser.getAST(), typeName);
            } else {
                throw npe;
            }
        } catch(Throwable t) {
            doThrow(t, parser.getAST(), typeName);
        }
        doThrow(parser.error, parser.getAST(), typeName);

        if(postProcessor.getAST() == null && analysisWalker.getAST() != null) {
            throw new DataDefinitionParseError("Could not parse " + typeName + ": postprocessor returned empty AST. Please report to developers.");
        }
        
        
        //System.out.println("**** Postprocessor walker ****");
        //MakumbaDumpASTVisitor visitor3 = new MakumbaDumpASTVisitor(false);
        //visitor3.visit(postProcessor.getAST());

        // step 4 - make the DataDefinitionImpl object, together with its FieldDefinitionImpl objects
        DataDefinitionImpl result = new DataDefinitionImpl(postProcessor.mdd);;
        if(!virtual) {
            // at this point, the DataDefinitionImpl object is not yet initialised, but will be by the NamedResource creation
            return result;
        } else {
            result.build();
            return result;
        }
    }

    /**
     * parses a MDD text
     */
    protected MDDParser parse(String typeName, String text) {

        InputStream o = new ByteArrayInputStream(text.getBytes());
        InputStream o1 = new ByteArrayInputStream(text.getBytes());

        return parse(typeName, o, o1, false);

    }

    private MDDParser parse(String typeName, URL u, boolean included) {

        InputStream o = null;
        InputStream o1 = null;

        try {
            o = (InputStream) u.getContent();
            o1 = (InputStream) u.getContent();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return parse(typeName, o, o1, included);
    }

    /** the main parser method **/
    private MDDParser parse(String typeName, InputStream o, InputStream o1, boolean included) {
        // this is an ugly workaround to the fact that the grammar has problems handling EOF
        // so we just add a new line by hand
        // TODO we might be able to check for EOF in the lexer with EOF_CHAR
        InputStream newLine1 = new ByteArrayInputStream("\n".getBytes());
        InputStream newLine2 = new ByteArrayInputStream("\n".getBytes());
        SequenceInputStream seq1 = new SequenceInputStream(o, newLine1); 
        SequenceInputStream seq2 = new SequenceInputStream(o1, newLine2); 
        
        // first pass - simply parse the MDD file
        Reader reader = new InputStreamReader(seq1);
        MDDLexer lexer = new MDDLexer(reader);
        
        // create reader for error handling
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(seq2));
        errorReaders.put(typeName, errorReader);

        MDDParser parser = null;
        try {
            parser = new MDDParser(lexer, this, typeName, included);
            parser.setASTFactory(astFactory);
            parser.dataDefinition();
            parser.postProcess();
        } catch (ANTLRException t) {
            doThrow(t, parser.getAST(), typeName);
        } catch(DataDefinitionParseError dpe) {
            throw dpe;
        } catch(NullPointerException npe) {
            if(parser.error != null) {
                doThrow(parser.error, parser.getAST(), typeName);
            } else {
                throw npe;
            }
        } catch(Throwable t) {
            doThrow(t, parser.getAST(), typeName);
        }
        doThrow(parser.error, parser.getAST(), typeName);
        
//        AST tree = parser.getAST();
//        System.out.println("**** Parser ****");
//        MakumbaDumpASTVisitor visitor = new MakumbaDumpASTVisitor(false);
//        visitor.visit(tree);

        return parser;
    }

    /**
     * parses an included data definition (.idd)
     */
    protected MDDParser parseIncludedDataDefinition(String includedName) {
        URL idd = getDataDefinitionURL(includedName, "idd");
        return parse(includedName, idd, true);
    }

    /**
     * Throws a {@link DataDefinitionParseError} at parse time
     */
    protected void doThrow(Throwable t, AST debugTree, String typeName) throws DataDefinitionParseError {
        if (t == null)
            return;

        // we already have a DataDefinitionParse error, just throw it further
        if (t instanceof DataDefinitionParseError) {
            throw (DataDefinitionParseError) t;
        }

        String line = "";
        int column = 0;

        if (t instanceof RecognitionException) {
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
        int col = ((MDDAST) ast).getColumn() - 1; // for some fishy reason the cursor gets too far
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

}
