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

package org.makumba.analyser.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.analyser.interfaces.JavaAnalyzer;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;

/**
 * This class performs a rudimentary detection of Java syntax elements in a Java class.
 * 
 * @author Rudolf Mayer
 */
public class JavaParseData implements SourceSyntaxPoints.PreprocessorClient {

    private class DefinitionPoint implements Comparable {
        String className;

        int position;

        public DefinitionPoint(String className, int position) {
            this.className = className;
            this.position = position;
        }

        public int compareTo(Object arg0) {
            return (new Integer(position).compareTo(new Integer(((DefinitionPoint) arg0).position)));
        }
        
        public String toString() {
            return position + ":" + className;
        }
    }

    public static boolean isCommentSyntaxPoint(String type) {
        return (Arrays.asList(JavaCommentPatternNames).contains(type));
    }

    public static boolean isClassUsageSyntaxPoint(String type) {
        return (Arrays.asList(JavaClassUsagePatternNames).contains(type));
    }
    
    public static boolean isPrimitiveType(String type) {
        return primitiveTypes.contains(type);
    }

    /** Cache of all page analyses. */
    public static int analyzedPages = NamedResources.makeStaticCache("Java page analyses", new NamedResourceFactory() {
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

    private static String[] JavaClassUsagePatternNames = { "JavaVariableDefinition", "JavaNewInstance",
        "JavaParameter", "JavaClassCast", "JavaMethodReturn", "JavaThrows", "JavaCatch", "JavaExtends" };

    private static Pattern[] JavaClassUsagePatterns;

    /** The patterns used to parse the page. */
    private static Pattern JavaDocCommentPattern, JavaBlockCommentPattern, JavaLineCommentPattern;

    private static Pattern JavaModifierPattern, JavaReservedWordPattern;

    private static Pattern JavaStringLiteral;

    private static Pattern JavaVariableDefinition, JavaNewInstance, JavaParameter, JavaClassCast, JavaMethodReturn,  
            JavaThrows, JavaCatch, JavaExtends;

    private static Pattern JavaImportPackage;

    private static Pattern JavaMethodInvocation;

    private static Pattern MakumbaFormHandler;
    
    private static List primitiveTypes = Arrays.asList(new String[] {"char", "byte", "short", "int", "long", "boolean", "float", "double", "void"});

    /** Initialiser for the class variables. */
    static {
        initPatterns();
    }

    private static void initPatterns() {
        // FIXME: this is not correct, could also start with _, $, ..
        String identifier = "\\w[\\w|\\d]*";
        String spaces = "[\\s]*";
        String minOneSpaces = "\\s" + spaces;

        JavaDocCommentPattern = Pattern.compile("/\\*\\*.*?[^\\*]\\*/", Pattern.DOTALL);

        JavaBlockCommentPattern = Pattern.compile("/\\*[^\\*]*\\*/", Pattern.DOTALL);
        JavaLineCommentPattern = Pattern.compile("//.*$", Pattern.MULTILINE);

        JavaModifierPattern = Pattern.compile("(public )|(private )|(protected )|(transient )|(static )|(void )");
        JavaReservedWordPattern = Pattern.compile("(class )|(int )|(boolean )|(double )|(float )|(short )|(long )|(byte )|(for )|(for\\()|(do )|(do\\{)|(while )|(while\\()|(switch )|(case )|(return )|(if )|(if\\()|(else )|(import\\s)|(package\\s)|(super\\.)|(super \\()|(super )|(inner\\.)|(outer\\.)|(extends )|(throws )");
        JavaImportPackage = Pattern.compile("(package" + minOneSpaces + "\\w[\\w\\d\\.]*" + spaces + ";)|(import"
                + minOneSpaces + "\\w[\\w\\d\\.]*" + "[\\.\\*]?" + spaces + ";)");
        JavaStringLiteral = Pattern.compile("\"[^\"]*\"");

        // find class usage.
        JavaNewInstance = Pattern.compile("new" + minOneSpaces + identifier + "[(| ]");
        JavaVariableDefinition = Pattern.compile("[" + identifier + "\\.]*" + identifier + minOneSpaces + identifier
                + spaces + "[;|=]");
        JavaParameter = Pattern.compile("[(|,]" + spaces + identifier + minOneSpaces + identifier);
        JavaClassCast = Pattern.compile("\\(" + spaces + identifier + spaces + "\\)" + spaces + identifier);
        // has problems with new|void, should be excluded ?
        JavaMethodReturn = Pattern.compile(spaces + identifier + minOneSpaces + identifier + spaces + "\\(");
        JavaThrows = Pattern.compile("throws" + spaces + identifier);
        JavaCatch = Pattern.compile("catch" + spaces + identifier);
        JavaExtends = Pattern.compile("extends" + spaces + identifier);
        
        JavaMethodInvocation = Pattern.compile(identifier + spaces + "\\." + spaces + identifier + "\\(");

        MakumbaFormHandler = Pattern.compile("on_(new|add|edit|delete)\\w+\\(");
        
        JavaCommentPatterns = new Pattern[] { JavaBlockCommentPattern, JavaDocCommentPattern, JavaLineCommentPattern };
        JavaClassUsagePatterns = new Pattern[] { JavaVariableDefinition, JavaNewInstance, JavaParameter, JavaClassCast,
                JavaMethodReturn, JavaThrows, JavaCatch, JavaExtends };
        
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

    /** The set of in this class imported packages. */
    HashSet<String> importedPackages = new HashSet<String>();

    private Hashtable<String, String> importedClasses = new Hashtable<String, String>();

    private String viewedClass = null;
    
    private String superClass = null;
    
    private Hashtable<String, ArrayList<DefinitionPoint>> definedObjects = new Hashtable<String, ArrayList<DefinitionPoint>>();

    /** Private constructor, construction can only be made by getParseData(). */
    protected JavaParseData(String path, JavaAnalyzer an, String uri) {
        // initPatterns(); // uncomment this if you want to test patterns
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

    /**
     * Gets the imported packages found in this java class.
     * 
     * @return A collection of Strings denoting package names.
     */
    public Collection getImportedPackages() {
        return importedPackages;
    }

    public Hashtable getImportedClasses() {
        return importedClasses;
    }

    public String[] getCommentPatternNames() {
        return JavaCommentPatternNames;
    }

    public Pattern[] getCommentPatterns() {
        return JavaCommentPatterns;
    }

    public String[] getLiteralPatternNames() {
        return new String[] { "JavaStringLiteral" };
    }

    public Pattern[] getLiteralPatterns() {
        return new Pattern[] { JavaStringLiteral };
    }

    public Pattern getIncludePattern() {
        return null;
    }

    public String getIncludePatternName() {
        return null;
    }

    public String getDefinedObjectClassName(String objectName, int position) {
        ArrayList points = (ArrayList) definedObjects.get(objectName);
        DefinitionPoint maxPoint = null;
        if (points != null) {
            for (int i = 0; i < points.size(); i++) {
                DefinitionPoint current = (DefinitionPoint) points.get(i);
                if (current.position < position) {
                    maxPoint = current;
                } else {
                    break;
                }
            }
            if (maxPoint != null) {
                return maxPoint.className;
            }
        }
        return null;
    }

    /**
     * @return Returns the superClass.
     */
    public String getSuperClass() {
        return superClass;
    }

    /**
     * @return Returns the viewedClass.
     */
    public String getViewedClass() {
        return viewedClass;
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

        // treat sting literals  
        treatJavaStringLiterals(syntaxPoints.getContent(), analyzer);

        // treat Java Modifiers
        treatJavaModifiers(syntaxPoints.getContent(), analyzer);

        // treat Java Reserved Words
        treatReservedWords(syntaxPoints.getContent(), analyzer);

        treatClassUsage(syntaxPoints.getContent(), analyzer);
        
        treatMethodUsage(syntaxPoints.getContent(), analyzer);
        
        treatMakumbaHandler(syntaxPoints.getContent(), analyzer);
        
        holder = analyzer.endPage(holder);

        java.util.logging.Logger.getLogger("org.makumba." + "javaparser.time").info(
                "analysis of " + uri + " took " + (new java.util.Date().getTime() - start) + " ms");
    }

    public void treatInclude(int position, String includeDirective, SourceSyntaxPoints host) {
    }

    /** Go thru the java import statments in the class. */
    void treatJavaImports(String content, JavaAnalyzer an) {
        Matcher m = JavaImportPackage.matcher(content);
        while (m.find()) {
            syntaxPoints.addSyntaxPoints(m.start(), m.end(), "JavaImport", null);
            String sub = content.substring(m.start(), m.end() - 1).trim();
            String importedPackage = sub.split("\\s")[1];
            if (sub.endsWith(".*") || content.substring(m.start(), m.end()).trim().startsWith("package")) {
                if (importedPackage.startsWith("package")) {
                    importedPackage = importedPackage.substring("package".length()).trim();
                }
                if (importedPackage.endsWith(".*")) {
                    importedPackage = importedPackage.substring(0, importedPackage.length() - 2);
                }
                importedPackages.add(importedPackage + ".");
            } else {
                String className = sub.substring(sub.lastIndexOf(".") + 1);
                importedClasses.put(className, importedPackage);
            }
        }
    }

    /** Go thru the reserved words in the class. */
    void treatReservedWords(String content, JavaAnalyzer an) {
        Matcher m = JavaReservedWordPattern.matcher(content);
        while (m.find()) {
             syntaxPoints.addSyntaxPoints(m.start(), m.end(), "JavaReservedWord", null);
            String s = content.substring(m.start(), m.end()).trim();
            if (s.equals("class") && viewedClass==null) {
                String c = content.substring(m.start()).trim();                
                c = c.substring(c.indexOf(" ")).trim();
                c = c.substring(0, c.indexOf(" "));
                viewedClass = c;
            } else if (s.equals("extends") && superClass == null) {
                String c = content.substring(m.start()).trim();                
                c = c.substring(c.indexOf(" ")).trim();
                c = c.substring(0, c.indexOf(" "));
                superClass = c;
            }
        }
    }

    void treatClassUsage(String content, JavaAnalyzer an) {
        for (int i = 0; i < JavaClassUsagePatterns.length; i++) {
            Matcher m = JavaClassUsagePatterns[i].matcher(content);
            while (m.find()) {
                String substring = content.substring(m.start(), m.end());
                if (JavaClassUsagePatterns[i] != JavaMethodReturn || !(substring.trim().startsWith("new"))) {
                    String className = extractClassName(substring, JavaClassUsagePatterns[i]);
                    int beginIndex = content.indexOf(className, m.start());
                    int endIndex = beginIndex + className.length();
                    String s = content.substring(beginIndex, endIndex);
                    if (!isPrimitiveType(s)) {
                        SyntaxPoint end = syntaxPoints.addSyntaxPoints(beginIndex, endIndex,
                            JavaClassUsagePatternNames[i], null);

                        // add defined objects --> store object name, class name and position in file, to be able to
                        // retrieve objects with the same name, but from a different class (e.g. "String a" in method
                        // "b", and "Integer a" in method "c").
                        if ((JavaClassUsagePatterns[i] == JavaVariableDefinition || JavaClassUsagePatterns[i] == JavaParameter)
                                && substring.trim().indexOf("new ") == -1) {
                            String objectName = substring.trim().substring(
                                substring.indexOf(className) + className.length()).replace('=', ' ').replace(';', ' ').trim();
                            ArrayList<DefinitionPoint> currentContent = definedObjects.get(objectName);
                            if (currentContent == null) {
                                currentContent = new ArrayList<DefinitionPoint>();
                            }
                            currentContent.add(new DefinitionPoint(className, end.getPosition()));
                            Collections.sort(currentContent);
                            definedObjects.put(objectName, currentContent);
                            java.util.logging.Logger.getLogger("org.makumba." + "javaparser").finest(
                                "Put defined object: " + objectName + ", values: " + currentContent);
                        }
                    }
                }
            }
        }
    }

    private static String extractClassName(String code, Pattern pattern) {
        code = code.replace('(', ' ').trim();
        code = code.replace(')', ' ').trim();
        code = code.replace(',', ' ').trim();

        StringTokenizer t = new StringTokenizer(code);
        ArrayList<String> s = new ArrayList<String>();
        while (t.hasMoreTokens()) {
            s.add(t.nextToken());
        }
        String[] parts = s.toArray(new String[s.size()]);

        if (pattern == JavaVariableDefinition || pattern == JavaParameter || pattern == JavaMethodReturn
                || pattern == JavaClassCast) {
            return parts[0];
        } else if (pattern == JavaNewInstance || pattern == JavaThrows || pattern == JavaCatch
                || pattern == JavaExtends) {
            return parts[1];
        } else {
            return "";
        }
    }

    /** Go thru the java String Literals in the class. */
    void treatJavaStringLiterals(String content, JavaAnalyzer an) {
        treatSimplePattern(content, an, JavaStringLiteral, "JavaStringLiteral");
    }

    /** Go thru the java modifiers in the class. */
    void treatJavaModifiers(String content, JavaAnalyzer an) {
        treatSimplePattern(content, an, JavaModifierPattern, "JavaModifier");
    }

    void treatMethodUsage(String content, JavaAnalyzer an) {
        treatSimplePattern(content, an, JavaMethodInvocation, "JavaMethodInvocation");
    }
    
    void treatMakumbaHandler(String content, JavaAnalyzer an) {
        treatSimplePattern(content, an, MakumbaFormHandler, "MakumbaFormHandler");
    }
    
    private void treatSimplePattern(String content, JavaAnalyzer an, Pattern pattern, String SyntaxPointName) {
        Matcher m = pattern.matcher(content);
        while (m.find()) {
            syntaxPoints.addSyntaxPoints(m.start(), m.end()-1, SyntaxPointName, null);
        }
    }

} // end class
