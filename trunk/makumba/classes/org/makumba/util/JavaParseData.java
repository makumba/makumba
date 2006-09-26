///////////////////////////////
//Makumba, Makumba tag library
//Copyright (C) 2000-2003  http://www.makumba.org
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//-------------
//$Id$
//$Name$
/////////////////////////////////////

package org.makumba.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class performs a rudimentary detection of Java syntax elements in a Java class.
 * 
 * @author Rudolf Mayer
 */
public class JavaParseData implements SourceSyntaxPoints.PreprocessorClient {

    /** The interface of a Java analyzer. */
    public interface JavaAnalyzer {

        /**
         * the end of the page
         * 
         * @return the result of the analysis
         */
        Object endPage(Object status);

        /**
         * make a status holder, which is passed to all other methods
         * 
         * @param initStatus
         *            an initial status to be passed to the JavaAnalyzer. for example, the pageContext for an example-based analyzer
         */
        Object makeStatusHolder(Object initStatus);
    }

    /** Cache of all page analyses. */
    static int analyzedPages = NamedResources.makeStaticCache("Java page analyses", new NamedResourceFactory() {
		private static final long serialVersionUID = 1L;

		public Object getHashObject(Object o) {
            Object[] o1 = (Object[]) o;
            return ((String) o1[0]) + o1[1].getClass().getName();
        }

        public Object makeResource(Object o, Object hashName) throws Throwable {
            Object[] o1 = (Object[]) o;
            return new JavaParseData((String) o1[0], (JavaAnalyzer) o1[1], (String) o1[2]);
        }
    }, true);

    private static String[] JavaCommentPatternNames = { "JavaBlockComment", "JavaDocComment", "JavaLineComment" };

    private static Pattern[] JavaCommentPatterns;

    /** The patterns used to parse the page. */
    static Pattern JavaDocCommentPattern, JavaBlockCommentPattern, JavaLineCommentPattern, JavaModifierPattern,
            JavaReservedWordPattern, JavaImports, JavaStringLiteral;

    /** Initialiser for the class variables. */
    static {
        try {
            JavaDocCommentPattern = Pattern.compile("/\\*\\*.*?[^\\*]\\*/", Pattern.DOTALL);

            JavaBlockCommentPattern = Pattern.compile("/\\*[^\\*]*\\*/", Pattern.DOTALL);
            JavaLineCommentPattern = Pattern.compile("//.*$", Pattern.MULTILINE);

            JavaModifierPattern = Pattern.compile("(public )|(private )|(protected )|(transient )|(static )|(void )|(super.)|(super \\()|(super )|(inner.)|(outer.)");
            JavaReservedWordPattern = Pattern.compile("(class )|(int )|(boolean )|(double )|(float )|(short )|(long )|(byte )|(for )|(for\\()|(do )|(do\\{)|(while )|(while\\()|(switch )|(case )|(return )|(if )|(if\\()|(else )|(else\\()");
            JavaImports = Pattern.compile("(package )|(import )");
            JavaStringLiteral = Pattern.compile("\"[^\"]*\"");

            JavaCommentPatterns = new Pattern[] { JavaBlockCommentPattern, JavaDocCommentPattern, JavaLineCommentPattern };
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Return the pageData of the class at the given path in the given webapp. This is the only way for clients of this class to obtain instances of
     * JavaPageData
     */
    static public JavaParseData getParseData(String webappRoot, String path, JavaAnalyzer an) {
        Object arg[] = { webappRoot + path, an, path };
        return (JavaParseData) NamedResources.getStaticCache(analyzedPages).getResource(arg);
    }

    /** The analyzer plugged in. */
    JavaAnalyzer analyzer;

    /** The Java file path */
    File file;

    /** The holder of the analysis status, and partial results. */
    Object holder;

    /** The syntax points of this page. */
    SourceSyntaxPoints syntaxPoints;

    /** The Java URI, for debugging purposes. */
    String uri;

    /** Private constructor, construction can only be made by getParseData(). */
    protected JavaParseData(String path, JavaAnalyzer an, String uri) {
        this.file = new File(path);
        this.uri = uri;
        this.analyzer = an;
    }

    /**
     * This method will perform the analysis if not performed already, or if the file has changed. the method is synchronized, so other accesses are
     * blocked if the current access determines that an analysis needs be performed
     * 
     * @param initStatus
     *            an initial status to be passed to the JavaAnalyzer. for example, the pageContext for an example-based analyzer
     */
    public synchronized Object getAnalysisResult(Object initStatus) {
        if (getSyntaxPoints() == null || !getSyntaxPoints().unchanged())
            try {
                parse(initStatus);
            } catch (Error e) {
                holder = e;
                throw e;
            } catch (RuntimeException re) {
                holder = re;
                throw re;
            }
        return holder;
    }

    public String[] getCommentPatternNames() {
        return JavaCommentPatternNames;
    }

    public Pattern[] getCommentPatterns() {
        return JavaCommentPatterns;
    }

    public Pattern getIncludePattern() {
        return null;
    }

    public String getIncludePatternName() {
        return null;
    }

    /** Gets the collection of syntax points. */
    public SourceSyntaxPoints getSyntaxPoints() {
        return syntaxPoints;
    }

    /** Parses the file. */
    void parse(Object initStatus) {
        long start = new java.util.Date().getTime();
        syntaxPoints = new SourceSyntaxPoints(file, this);

        holder = analyzer.makeStatusHolder(initStatus);

        // 
        treatJavaImports(syntaxPoints.getContent(), analyzer);

        // 
        treatJavaStringLiterals(syntaxPoints.getContent(), analyzer);

        // treat Java Modifiers
        treatJavaModifiers(syntaxPoints.getContent(), analyzer);

        // treat Java Reserved Words
        treatReservedWords(syntaxPoints.getContent(), analyzer);

        holder = analyzer.endPage(holder);

        org.makumba.MakumbaSystem.getMakumbaLogger("javaparser.time").info(
                "analysis of " + uri + " took " + (new java.util.Date().getTime() - start) + " ms");
    }

    public void treatInclude(int position, String includeDirective, SourceSyntaxPoints host) {
    }

    /** Go thru the java import statments in the class. */
    void treatJavaImports(String content, JavaAnalyzer an) {
        Matcher m = JavaImports.matcher(content);
        while (m.find()) {
            SyntaxPoint end = syntaxPoints.addSyntaxPoints(m.start(), m.end(), "JavaImport", null);
            SyntaxPoint start = (SyntaxPoint) end.getOtherInfo();
        }
    }

    /** Go thru the java String Literals in the class. */
    void treatJavaStringLiterals(String content, JavaAnalyzer an) {
        Matcher m = JavaStringLiteral.matcher(content);
        while (m.find()) {
            SyntaxPoint end = syntaxPoints.addSyntaxPoints(m.start(), m.end(), "JavaStringLiteral", null);
            SyntaxPoint start = (SyntaxPoint) end.getOtherInfo();
        }
    }

    /** Go thru the java modifiers in the class. */
    void treatJavaModifiers(String content, JavaAnalyzer an) {
        Matcher m = JavaModifierPattern.matcher(content);
        while (m.find()) {
            SyntaxPoint end = syntaxPoints.addSyntaxPoints(m.start(), m.end(), "JavaModifier", null);
            SyntaxPoint start = (SyntaxPoint) end.getOtherInfo();
        }
    }

    /** Go thru the java modifiers in the class. */
    void treatReservedWords(String content, JavaAnalyzer an) {
        Matcher m = JavaReservedWordPattern.matcher(content);
        while (m.find()) {
            SyntaxPoint end = syntaxPoints.addSyntaxPoints(m.start(), m.end(), "JavaReservedWord", null);
            SyntaxPoint start = (SyntaxPoint) end.getOtherInfo();
        }
    }

} // end class
