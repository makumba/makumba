package org.makumba.providers.datadefinition.mdd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;
import org.makumba.MakumbaError;

import antlr.MismatchedTokenException;
import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * MDD factory, used to turn a .mdd file into a {@link DataDefinition}.<br>
 * This class calls the inital MDD parser, then walks over it and performs the analysis
 * and finally transforms it again in order to produce the {@link DataDefinition} and its content. 
 * 
 * @author Manuel Gay
 * @version $Id: MDDAnalyzer.java,v 1.1 Apr 29, 2009 8:59:46 PM manu Exp $
 */
public class MDDFactory {
    
    private static String webappRoot;
    
    private String typeName = new String();
    
    private URL origin;
    
    private BufferedReader errorReader = null;
    
    private MDDASTFactory factory = new MDDASTFactory();
    
    public MDDFactory(String name) {
        
        this.typeName = name;
        
        // TODO introduce some caching here
        
        URL u = findDataDefinition(name, "mdd");
        if (u == null) {
            throw new DataDefinitionNotFoundError((String) name);
        }
        
        this.origin = u;

        Object o = null;
        Object o1 = null;
        
        try {
            o = u.getContent();
            o1 = u.getContent();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
            
            // first pass - simply parse the MDD file
            Reader reader = new InputStreamReader((InputStream) o);
            MDDLexer lexer = new MDDLexer(reader);
            errorReader = new BufferedReader(new InputStreamReader((InputStream) o1));
            
            MDDParser parser = null;
            try {
                parser = new MDDParser(lexer);
                parser.setASTFactory(factory);
//                parser.setASTNodeClass("org.makumba.providers.datadefinition.mdd.MDDAST");
                parser.dataDefinition();
                
            } catch(Throwable t) {
                doThrow(t, parser.getAST());
            }
            doThrow(parser.error, parser.getAST());
            
            AST tree = parser.getAST();

            System.out.println("**** Parser ****");
            MakumbaDumpASTVisitor visitor = new MakumbaDumpASTVisitor(false);
            visitor.visit(tree);
            
            MDDAnalyzeWalker analysisWalker = null;
            try {
                analysisWalker = new MDDAnalyzeWalker(this.typeName, this.origin, this);
                analysisWalker.setASTFactory(factory);
//                analysisWalker.setASTNodeClass("org.makumba.providers.datadefinition.mdd.MDDAST");
                analysisWalker.dataDefinition(tree);
            } catch (Throwable e) {
                doThrow(e, analysisWalker.getAST());
            }
            doThrow(analysisWalker.error, parser.getAST());
                        
            System.out.println("**** Analysis walker ****");
            MakumbaDumpASTVisitor visitor2 = new MakumbaDumpASTVisitor(false);
            visitor2.visit(analysisWalker.getAST());
            
            MDDBuildWalker builder = null;
            try {
                builder = new MDDBuildWalker(this.typeName, analysisWalker.mdd, analysisWalker.typeShorthands, this);
                builder.dataDefinition(analysisWalker.getAST());
            } catch (Throwable e) {
                doThrow(e, builder.getAST());
            }
            doThrow(builder.error, parser.getAST());
            
            System.out.println("**** Build walker ****");
            MakumbaDumpASTVisitor visitor3 = new MakumbaDumpASTVisitor(false);
            visitor3.visit(builder.getAST());
            
            System.out.println(builder.mdd.toString());

        
    }
        
    /**
     * Throws a {@link DataDefinitionParseError} at parse time
     */
    private void doThrow(Throwable t, AST debugTree) {
        if (t == null)
            return;
        
        // we already have a DataDefinitionParse error, just throw it
        if(t instanceof DataDefinitionParseError) {
            throw new RuntimeException(t);
        }

        if (t instanceof RuntimeException) {
            t.printStackTrace();
            throw (RuntimeException) t;
        }
        
        String line = "";
        int column = 0;
        
        if (t instanceof RecognitionException) {
            RecognitionException re = (RecognitionException) t;
            if (re.getColumn() > 0) {
                column = re.getColumn();
                line = getLine(re.getLine());
            }
        }
        
        if(t instanceof MismatchedTokenException) {
            MismatchedTokenException mte = (MismatchedTokenException) t;
            line = getLine(mte.getLine());
            column = mte.getColumn();
        }
        
        throw new DataDefinitionParseError(this.typeName, t.getMessage(), line, column);
    }

    /**
     * Throws a {@link DataDefinitionParseError} based on the information returned by the {@link MDDAST}
     */
    protected void doThrow(String message, AST ast) {
        int line = ((MDDAST)ast).getLine();
        int col = ((MDDAST)ast).getColumn();
        throw new DataDefinitionParseError(typeName, message, getLine(line), col);
    }

    
    
   protected String getLine(int lineNumber) {
       String line = "";
       for(int i = 0; i < lineNumber; i++) {
           try {
            line = errorReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
       }
       return line;

   }

    /**
     * Finds a data definition, based on its name and extensions
     */
    private URL findDataDefinition(String s, String ext) {
        // must specify a filename, not a directory (or package), see bug 173
        java.net.URL u = findDataDefinitionOrDirectory(s, ext);
        if (u != null && (s.endsWith("/") || getResource(s + '/') != null)) {
            return null;
        }
        return u;
    }

    private URL getResource(String s) {
        return org.makumba.commons.ClassResource.get(s);
    }

    
    /**
     * Looks up a data definition. First tries to see if an arbitrary webapp root path was passed, if not uses the
     * classpath
     * 
     * @param s
     *            the name of the type
     * @param ext
     *            the extension (e.g. mdd)
     * @return a URL to the MDD file, null if none was found
     */
    private URL findDataDefinitionOrDirectory(String s, String ext) {
        java.net.URL u = null;
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        if (s.endsWith(".") || s.endsWith("//")) {
            return null;
        }

        // if a webappRoot was passed, we fetch the MDDs from there, not using the CP
        if (webappRoot != null) {
            File f = new File(webappRoot);
            if (!f.exists() || (f.exists() && !f.isDirectory())) {
                throw new MakumbaError("webappRoot " + webappRoot
                        + " does not appear to be a valid directory");
            }
            String mddPath = webappRoot + "/WEB-INF/classes/dataDefinitions/" + s.replace('.', '/') + "."
                    + ext;
            File mdd = new File(mddPath.replaceAll("/", File.separator));
            if (mdd.exists()) {
                try {
                    u = new java.net.URL("file://" + mdd.getAbsolutePath());
                } catch (MalformedURLException e) {
                    throw new MakumbaError("internal error while trying to retrieve URL for MDD "
                            + mdd.getAbsolutePath());
                }
            }
        }

        if (u == null) {
            u = getResource("dataDefinitions/" + s.replace('.', '/') + "." + ext);
            if (u == null) {
                u = getResource(s.replace('.', '/') + "." + ext);
            }
        }
        return u;
    }

}
