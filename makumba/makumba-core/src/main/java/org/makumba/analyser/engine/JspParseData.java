///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.analyser.engine;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.tagext.Tag;

import org.makumba.analyser.ELData;
import org.makumba.analyser.ElementData;
import org.makumba.analyser.TagData;
import org.makumba.analyser.interfaces.JspAnalyzer;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RegExpUtils;
import org.makumba.commons.RuntimeWrappedException;

/**
 * This class performs a rudimentary detection of JSP-relevant tags in a JSP page.
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class JspParseData implements SourceSyntaxPoints.PreprocessorClient {
    /** The JSP file path */
    File file;

    /** The analyzer plugged in. */
    JspAnalyzer analyzer;

    /** The syntax points of this page. */
    SourceSyntaxPoints syntaxPoints;

    /** The holder of the analysis status, and partial results. */
    Object holder;

    /** The JSP URI, for debugging purposes. */
    String uri;

    /** Indicates whether the JSP file uses the Hibernate implementation or not. * */
    private boolean usingHibernate = false;

    /** the path of the webapp root */
    private String root;

    /** The patterns used to parse the page. */
    static private Pattern JspSystemTagPattern, JspTagPattern, HTMLTagPattern, JspCommentPattern, JspScriptletPattern,
            JspIncludePattern, JspTagAttributePattern, JspExpressionLanguagePattern, JSPELFunctionPattern,
            JsfExpressionLanguagePattern, Word, TagName, MapExpression, DotExpression;

    static private String[] JspCommentPatternNames = { "JspComment", "JspScriptlet" };

    static private Pattern[] JspCommentPatterns;

    /** Cache of all page analyses. */
    static int analyzedPages = NamedResources.makeStaticCache("JSP page analyses", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        @Override
        public Object getHashObject(Object o) {
            Object[] o1 = (Object[]) o;
            return (String) o1[0] + o1[2] + o1[1].getClass().getName();
        }

        @Override
        public Object makeResource(Object o, Object hashName) throws Throwable {
            Object[] o1 = (Object[]) o;
            return new JspParseData((String) o1[0], (String) o1[2], (JspAnalyzer) o1[1]);
        }
    }, false);

    static String attribute(String attName) {
        return "(" + attribute(attName, "\"") + "|" + attribute(attName, "\'") + ")";
    }

    /**
     * This helps to create regex for the 'attribute' pattern easily.
     * 
     * @param attName
     *            the name of the attribute
     * @param quote
     *            the kind of quote used. Is either " or ' .
     */
    static String attribute(String attName, String quote) {
        String bs = "\\"; // backslash in a java String (escaped)
        String q = bs + quote;
        String backslash = bs + bs; // backslash in a regex in a java String (escaped)

        // BAD TRY: pattern is (?s)\s*\w+\s*=\s*"(.*?[^\\])??" or idem with single quote '
        // the pattern is \s*\w+\s*=\s*"(\\.|[^"\\])*?" or idem with single quote '
        return bs + "s*" + attName + bs + "s*=" + bs + "s*" + q + "(" + backslash + ".|[^" + q + backslash + "])*?" + q;
    }

    /**
     * Initialiser for the class variables.
     */
    static {
        String attribute = attribute("\\w+");
        String attColon = attribute("\\w+(:\\w+)?");

        try {
            JspTagAttributePattern = Pattern.compile(attColon);
            JspSystemTagPattern = Pattern.compile("<%@\\s*\\w+(" + attribute + ")*\\s*%>");
            JspIncludePattern = Pattern.compile("<%@\\s*include" + attribute("file") + "\\s*%>");
            JspTagPattern = Pattern.compile("<((\\s*\\w+:\\w+(" + attColon + ")*\\s*)/?|(/\\w+:\\w+\\s*))>");
            HTMLTagPattern = Pattern.compile("<((\\s*\\w+(" + attribute + ")*\\s*)/?|(/\\w+\\s*))>");

            // JspCommentPattern= Pattern.compile("<%--([^-]|(-[^-])|(--[^%])|(--%[^>]))*--%>", Pattern.DOTALL);
            JspCommentPattern = Pattern.compile("<%--.*?[^-]--%>", Pattern.DOTALL);
            JspScriptletPattern = Pattern.compile("<%[^@].*?%>", Pattern.DOTALL);
            JspExpressionLanguagePattern = Pattern.compile("\\$\\{[^\\}]*\\}");

            // FIXME: the function accepts only quoted literal as parameters
            // There might be a need to also accept $parameters or expressions/labels
            String functionParamElement = RegExpUtils.LineWhitespaces + "(" + "\\'.+\\'" + ")"
                    + RegExpUtils.LineWhitespaces;
            String functionParamElementRepeatment = "(?:" + RegExpUtils.LineWhitespaces + "," + "(?:"
                    + functionParamElement + "))*";
            String functionParamRegExp = RegExpUtils.LineWhitespaces + "(?:" + functionParamElement + ")?"
                    + functionParamElementRepeatment + RegExpUtils.LineWhitespaces;
            JSPELFunctionPattern = Pattern.compile("\\w+:\\w+\\(" + functionParamRegExp + "?\\)");

            JsfExpressionLanguagePattern = Pattern.compile("\\#\\{[^\\}]*\\}");
            Pattern[] cp = { JspCommentPattern, JspScriptletPattern };
            JspCommentPatterns = cp;
            MapExpression = Pattern.compile("[A-Za-z]\\w*\\[[^]]*\\]");
            Word = Pattern.compile("\\w+");
            TagName = Pattern.compile("\\w+:\\w+");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Releases parsing data that is useful during analysis and for error handling, but not needed anymore afterwards
     */
    public synchronized void discardParsingData() {
        // if they were not already discarded
        if (!syntaxPoints.isDiscarded()) {
            syntaxPoints.discardPoints();
        }
    }

    /**
     * Performs the analysis if not performed already, or if the file has changed. The method is synchronized, so other
     * accesses are blocked if the current access determines that an analysis needs be performed
     * 
     * @param initStatus
     *            an initial status to be passed to the JspAnalyzer. For example, the pageContext for an example-based
     *            analyzer
     */
    public synchronized Object getAnalysisResult(Object initStatus) {
        if (getSyntaxPoints() == null || !getSyntaxPoints().unchanged()) {
            getSyntaxPointArray(initStatus);
        }
        return holder;
    }

    @Override
    public synchronized SyntaxPoint[] getSyntaxPointArray(Object initStatus) {
        try {
            parse(initStatus);
            return syntaxPoints.getSyntaxPoints();
        } catch (Error e) {
            holder = e;
            throw e;
        } catch (RuntimeException re) {
            holder = re;
            throw re;
        }
    }

    /**
     * Returns the pageData of the page at the given path in the given webapp. This is the only way for clients of this
     * class to obtain instances of JspPageData.
     * 
     * @param webappRoot
     *            the root of the webapp
     * @param path
     *            the path to the file
     * @param the
     *            JspAnalyzer used to parse the page
     */
    static public JspParseData getParseData(String webappRoot, String path, JspAnalyzer an) {
        Object arg[] = { webappRoot, an, path };
        return (JspParseData) NamedResources.getStaticCache(analyzedPages).getResource(arg);
    }

    /**
     * Gets the collection of syntax points.
     */
    @Override
    public SourceSyntaxPoints getSyntaxPoints() {
        return syntaxPoints;
    }

    /**
     * Private constructor, construction can only be made by getParseData().
     * 
     * @param path
     *            the path to the page
     * @param uri
     *            the uri, for debugging purposes
     * @param an
     *            the analyzer used for analysis
     */
    public JspParseData(String path, String uri, JspAnalyzer an) {
        this.root = path;
        this.file = new File(root + uri);
        this.uri = uri;
        this.analyzer = an;
    }

    /**
     * Parses the file.
     * 
     * @param initStatus
     *            an initial status to be passed to the JspAnalyzer. For example, the pageContext for an example-based
     *            analyzer
     */
    void parse(Object initStatus) {
        long start = new java.util.Date().getTime();

        // we need to create the holder before the syntaxPoints, because we need it if there are include directives in
        // the file
        holder = analyzer.makeStatusHolder(initStatus);
        syntaxPoints = new SourceSyntaxPoints(file, this);

        // handles the page elements, i.e. tags and expression language
        treatPageContent(syntaxPoints.getContent(), analyzer);

        holder = analyzer.endPage(holder);

        java.util.logging.Logger.getLogger("org.makumba.jspparser.time").info(
            "analysis of " + uri + " took " + (new java.util.Date().getTime() - start) + " ms");
    }

    /**
     * Identifies tag attributes from a tag string and puts them in a Map. Sets the attribute syntax points.
     * 
     * @param s
     *            the tag string to be parsed
     * @param origin
     *            the origin of the string in the parsed page
     */
    Map<String, String> parseAttributes(String s, int origin) {
        Map<String, String> attributes = new HashMap<String, String>(13);
        // System.out.println("tag = " + s); //debugging

        Matcher m = JspTagAttributePattern.matcher(s);
        while (m.find()) {
            // here we have an attributeName="attributeValue"
            String attr = s.substring(m.start(), m.end());
            int n = attr.indexOf('='); // position of the equal sign
            String attName = attr.substring(0, n).trim();
            String attValQuoted = attr.substring(n + 1).trim(); // the part after the equal sign.
            char chQuote = attValQuoted.charAt(0);

            // the following assertion must be ensured by the attributePattern matching
            if (attValQuoted.charAt(0) != attValQuoted.charAt(attValQuoted.length() - 1)) {
                throw new RuntimeException("Properly quoted string expected, found " + attValQuoted);
            }

            // unescape the "escaped quotes" in the attributeValue
            if (chQuote == '\"') {
                attValQuoted = attValQuoted.replaceAll("\\\\\"", "\""); // replace \" by "
            } else if (chQuote == '\'') {
                attValQuoted = attValQuoted.replaceAll("\\\\\'", "\'"); // replace \' by '
            }
            attValQuoted = attValQuoted.replaceAll("(\\\\){2}", "\\\\"); // replace \\ by \

            String attValue = attValQuoted.substring(1, attValQuoted.length() - 1);
            attributes.put(attName, attValue);

            if (origin != -1) {
                // syntax points, only set if the origin is given
                syntaxPoints.addSyntaxPoints(origin, origin + n, "JSPTagAttributeName", null);
                syntaxPoints.addSyntaxPoints(origin + n, origin + n + 1, "JSPTagAttributeEquals", null);
                syntaxPoints.addSyntaxPoints(origin + n + 1, origin + s.length(), "JSPTagAttributeValue", null);
            }
            // debug
            Logger log = java.util.logging.Logger.getLogger("org.makumba.jspparser.tags.attribute");
            log.finest("< Attribute : " + attr);
            log.finest("> AttrParse : " + attName + " = " + attValue);
        }
        return attributes;
    }

    /**
     * Treats include directives in the page
     * 
     * @param position
     *            position of the include directive
     * @param includeDirective
     *            include directive to be treated
     * @param host
     *            SourceSyntaxPoints object that is going to host this included page
     */
    @Override
    public void treatInclude(int position, String includeDirective, SyntaxPoint start, SyntaxPoint end,
            SourceSyntaxPoints host) {

        treatIncludeDirective(includeDirective, start, end, analyzer);

        Map<String, String> m1 = parseAttributes(includeDirective, -1);
        String fileName = m1.get("file");
        String dir = fileName.startsWith("/") ? root : host.file.getParent();
        host.include(new File(dir, fileName), position, includeDirective);
    }

    @Override
    public Pattern[] getCommentPatterns() {
        return JspCommentPatterns;
    }

    @Override
    public String[] getCommentPatternNames() {
        return JspCommentPatternNames;
    }

    @Override
    public Pattern getIncludePattern() {
        return JspIncludePattern;
    }

    @Override
    public String getIncludePatternName() {
        return "JspInclude";
    }

    /**
     * Goes through the page and matches tags, system tags, and EL expressions in such a way that the calls to the
     * handling methods happen in sequential order. TODO add support for JSF EL expressions
     * 
     * @param content
     *            the content of the page
     * @param an
     *            a {@link JspAnalyzer} implementation
     */
    void treatPageContent(String content, JspAnalyzer an) {

        Matcher[] match = { JspTagPattern.matcher(content), JspSystemTagPattern.matcher(content),
                JspExpressionLanguagePattern.matcher(content), HTMLTagPattern.matcher(content) };

        int start[] = { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };

        // put mx=4 for html and otehr tags to be analyzed as well! 
        int mx = 3;
        for (int i = 0; i < mx; i++) {
            if (match[i].find()) {
                start[i] = match[i].start();
            }
        }

        while (true) {
            int minimum = Integer.MAX_VALUE;
            for (int i = 0; i < mx; i++) {
                if (start[i] < minimum) {
                    minimum = start[i];
                }
            }
            if (minimum == Integer.MAX_VALUE) {
                break;
            }

            for (int i = 0; i < mx; i++) {
                if (start[i] == minimum) {
                    switch (i) {
                        case 0:
                            treatTag(match[i], content, an, "Jsp", TagName);
                            break;
                        case 1:
                            treatSystemTag(match[i], content, an);
                            break;
                        case 2:
                            treatELExpression(match[i], content, an, false);
                            break;
                        case 3:
                            treatTag(match[i], content, an, "HTML", Word);
                    }
                    start[i] = Integer.MAX_VALUE;
                    if (match[i].find()) {
                        start[i] = match[i].start();
                    }
                }
            }
        }

    }

    /**
     * Treats a single EL expression, extracts all the maps of the kind Expr[...] TODO also parse Expr.some.thing TODO
     * parse attributes for methods, if these exist
     * 
     * @param m
     *            the Matcher used to parse the expression
     * @param content
     *            the content of the page
     * @param an
     *            the JspAnalyzer used to analyze the page
     * @param isJsf
     *            whether this is a JSP EL value ${...} or a JSF method #{...}
     */
    void treatELExpression(Matcher m, String content, JspAnalyzer an, boolean isJsf) {

        SyntaxPoint end = syntaxPoints.addSyntaxPoints(m.start(), m.end(), "ExpressionLanguage", null);
        SyntaxPoint start = (SyntaxPoint) end.getOtherInfo();
        int elContentStart = m.start() + 2;
        String elContent = content.substring(elContentStart, m.end() - 1);
        Matcher map = MapExpression.matcher(elContent);
        while (map.find()) {
            SyntaxPoint mapEnd = syntaxPoints.addSyntaxPoints(m.start() + map.start(), m.start() + map.end(),
                "ExpressionLanguageMap", null);
            SyntaxPoint mapStart = (SyntaxPoint) mapEnd.getOtherInfo();

            String mapContent = elContent.substring(map.start(), map.end());

            ELData elData = new ELData(mapContent, mapStart, mapEnd);
            an.elExpression(elData, holder);
        }

        // search for functions inside the expression
        Matcher jspELFunction = JSPELFunctionPattern.matcher(elContent);
        while (jspELFunction.find()) {
            SyntaxPoint jspELFunctionsEnd = syntaxPoints.addSyntaxPoints(elContentStart + jspELFunction.start(),
                elContentStart + jspELFunction.end(), "ExpressionLanguageFunction", null);
            SyntaxPoint jspELFunctionsStart = (SyntaxPoint) jspELFunctionsEnd.getOtherInfo();
            // find the function name
            int beginIndex = elContent.indexOf(":", jspELFunction.start()) + 1;
            String jspELFunctionsContent = elContent.substring(beginIndex, elContent.indexOf("(", beginIndex));
            // find the function arguments
            ArrayList<String> arguments = new ArrayList<String>();
            if (jspELFunction.groupCount() > 0) {
                for (int j = 1; j <= jspELFunction.groupCount(); j++) {
                    if (jspELFunction.group(j) != null) {
                        arguments.add(jspELFunction.group(j).trim());
                    }
                }
            }
            ELData elData = new ELData(jspELFunctionsContent, arguments, jspELFunctionsStart, jspELFunctionsEnd);
            an.elExpression(elData, holder);
        }
    }

    /**
     * Treats a jsp or taglib tag: parses its different parts and stores the analysis.
     * 
     * @param m
     *            the Matcher used to parse the tag
     * @param content
     *            the content of the page
     * @param an
     *            the JspAnalyzer used to analyze the page
     * @param tagType
     */
    void treatTag(Matcher m, String content, JspAnalyzer an, String tagType, Pattern pattern) {
        String tag = content.substring(m.start(), m.end());
        boolean tagEnd = tag.startsWith("</");
        boolean tagClosed = tag.endsWith("/>");
        Matcher m1 = pattern.matcher(tag);
        m1.find();
        syntaxPoints.addSyntaxPoints(m.start() + m1.start(), m.start() + m1.end(), tagType + "TagName", null);

        String type = tagEnd ? tagType + "TagEnd" : tagClosed ? tagType + "TagSimple" : tagType + "TagBegin";

        SyntaxPoint end = syntaxPoints.addSyntaxPoints(m.start(), m.end(), type, null);
        SyntaxPoint start = (SyntaxPoint) end.getOtherInfo();

        String tagName = tag.substring(m1.start(), m1.end());

        TagData td = new TagData(tagName, start, end, !tagEnd ? parseAttributes(tag, m.start()) : null);
        // System.out.println(td);

        Logger log = java.util.logging.Logger.getLogger("org.makumba.jspparser.tags");

        // we avoid evaluation of the logging expression
        if (log.isLoggable(Level.FINE)) {
            log.fine(uri + ":" + start.line + ":" + start.column + ": "
                    + (tagEnd ? "/" + tagName : td.name + " " + td.attributes));
        }
        if (pattern == TagName) { // if this is a JSP tag...
            if (tagEnd) {
                an.endTag(td, holder);
                return;
            }

            if (tagClosed) {
                an.simpleTag(td, holder);
            } else {
                an.startTag(td, holder);
            }
        }
    }

    /**
     * Treats a system tag: parses its different parts and stores the analysis. FIXME this contains mak-specific code
     * and should move together with the makJspAnalyser
     * 
     * @param m
     *            the Matcher used to parse the tag
     * @param content
     *            the content of the page
     * @param an
     *            the JspAnalyzer used to analyze the page
     */
    void treatSystemTag(Matcher m, String content, JspAnalyzer an) {
        String tag = content.substring(m.start(), m.end());
        SyntaxPoint end = syntaxPoints.addSyntaxPoints(m.start(), m.end(), "JSPSystemTag", null);

        Matcher m1 = Word.matcher(tag);
        m1.find();
        syntaxPoints.addSyntaxPoints(m.start() + m1.start(), m.start() + m1.end(), "JSPSystemTagName", null);
        SyntaxPoint start = (SyntaxPoint) end.getOtherInfo();

        TagData td = new TagData(tag.substring(m1.start(), m1.end()), start, end, parseAttributes(tag, m.start()));

        // FIXME this should be in the analyzer and not in here
        if (td.name.equals("taglib")) { // find out whether we have a taglib tag
            if (td.attributes.get("uri") != null
                    && td.attributes.get("uri").toString().startsWith("http://www.makumba.org/")) {
                if (!td.attributes.get("uri").equals("http://www.makumba.org/presentation")) {
                    // every other makumba.org tag-lib than www.makumba.org/presentation is treated to be hibernate
                    usingHibernate = true;
                }
            }
        }

        Logger log = java.util.logging.Logger.getLogger("org.makumba.jspparser.tags");

        // we avoid evaluation of the logging expression
        if (log.isLoggable(Level.FINE)) {
            log.fine(uri + ":" + start.line + ":" + start.column + ": " + td.name + " " + td.attributes);
        }

        an.systemTag(td, holder);
    }

    /**
     * Treats an include directive: parses its different parts and stores the analysis.
     * 
     * @param m
     *            the Matcher used to parse the directive
     * @param directive
     *            the content of the page
     * @param an
     *            the JspAnalyzer used to analyze the page
     */
    void treatIncludeDirective(String directive, SyntaxPoint start, SyntaxPoint end, JspAnalyzer an) {

        // we don't add a syntax point because this was already done when the include was expanded

        Matcher m1 = Word.matcher(directive);
        m1.find();

        TagData td = new TagData("include", start, end, parseAttributes(directive, -1));

        Logger log = java.util.logging.Logger.getLogger("org.makumba.jspparser.tags");

        // we avoid evaluation of the logging expression
        if (log.isLoggable(Level.FINE)) {
            log.fine(uri + ":" + start.line + ":" + start.column + ": " + td.name + " " + td.attributes);
        }

        an.systemTag(td, holder); // change?
    }

    /**
     * Prints the line of a tag and points the beginning of the tag. Seems to be a nice illustration for parse-error
     * messages.
     * 
     * @param td
     *            the TagData of the tag
     * @param sb
     *            the StringBuffer used to print out
     */
    public static void tagDataLine(ElementData td, StringBuffer sb) {
        sb.append("\n").append(td.getSourceSyntaxPoints().getLineText(td.getStartLine())).append('\n');
        for (int i = 1; i < td.getStartColumn(); i++) {
            sb.append(' ');
        }
        sb.append('^');
    }

    /**
     * Fills the data for a tag by invoking the setter method through Java reflexion
     * 
     * @param t
     *            the tag to be filled with data
     * @param attributes
     *            the attributes of the tag
     */
    public static void fill(Tag t, Map<String, String> attributes) {
        Class<?> c = t.getClass();
        Class<?>[] argTypes = { String.class };
        Object[] args = new Object[1];

        for (Entry<String, String> entry : attributes.entrySet()) {
            Entry<String, String> me = entry;
            String s = me.getKey();
            String methodName = "set" + Character.toUpperCase(s.charAt(0)) + s.substring(1);
            try {
                Method m = c.getMethod(methodName, argTypes);
                args[0] = me.getValue();
                m.invoke(t, args);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                java.util.logging.Logger.getLogger("org.makumba.jspparser").warning(
                    ("error invoking method " + methodName + " on object of class " + c.getName() + " with argument " + args[0]));
                throw new RuntimeWrappedException(ite.getTargetException());
            } catch (Throwable thr) {
                java.util.logging.Logger.getLogger("org.makumba.jspparser").warning(
                    "error invoking method " + methodName + " on object of class " + c.getName() + " with argument "
                            + args[0]);
                throw new RuntimeWrappedException(thr);
            }
        }
    }

    // ==========================================================================================
    //
    // THIS CLASS ALSO HAS AN INNER INTERFACE, AND INNER CLASS DEFINITION:
    //
    // ==========================================================================================

    public boolean isUsingHibernate() {
        return usingHibernate;
    }

    @Override
    public String[] getLiteralPatternNames() {
        return null;
    }

    @Override
    public Pattern[] getLiteralPatterns() {
        return null;
    }

} // end class

