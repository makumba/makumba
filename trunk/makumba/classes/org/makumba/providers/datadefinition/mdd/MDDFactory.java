package org.makumba.providers.datadefinition.mdd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.MakumbaError;

import antlr.DumpASTVisitor;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

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
    
    private MDDASTFactory astFactory = new MDDASTFactory();
    
    public MDDFactory(String name) {
        
        // TODO introduce some caching here
        
        URL u = findDataDefinition(name, "mdd");
        if (u == null) {
            throw new DataDefinitionNotFoundError((String) name);
        }
        
        
        Object o;
        
        try {
            
            o = u.getContent();
            
            // first pass - simply parse the MDD file
            Reader reader = new InputStreamReader((InputStream) o);
            MDDLexer lexer = new MDDLexer(reader);
            MDDParser parser = new MDDParser(lexer);
            parser.setASTFactory(astFactory);
                   
            parser.dataDefinition();
            AST tree = parser.getAST();

            System.out.println("**** Parser ****");
            DumpASTVisitor visitor = new DumpASTVisitor();
            visitor.visit(tree);
            
            MDDAnalyzeWalker walker = new MDDAnalyzeWalker();
            walker.declaration(tree);
            
            System.out.println("**** Analysis walker ****");
            DumpASTVisitor visitor2 = new DumpASTVisitor();
            visitor2.visit(walker.getAST());
            
            
            
            
        }
        catch (RecognitionException e)
        {
            System.err.println(e.toString());
        }
        catch (TokenStreamException e)
        {
            System.err.println(e.toString());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
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
