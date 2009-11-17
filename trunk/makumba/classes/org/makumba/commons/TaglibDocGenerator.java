package org.makumba.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * This class generates the taglib documentation JSPWiki files based on the taglib-documented.xml file
 * 
 * TODO finishing copying tag descs
 * TODO alphabetical order
 * 
 * @author Manuel Gay
 * @version $Id: TaglibDocGenerator.java,v 1.1 Nov 16, 2009 2:57:04 PM manu Exp $
 */
public class TaglibDocGenerator {

    private File outputDir;

    private HashMap<String, Element> processedElements = new HashMap<String, Element>();

    public static void main(String[] args) {

        String inputXMLPath = args[0];
        String outputPath = args[1];

        TaglibDocGenerator tdg = new TaglibDocGenerator(inputXMLPath, outputPath);
    }

    public TaglibDocGenerator(String inputXMLPath, String outputPath) {

        File xml = new File(inputXMLPath);
        if (!xml.exists()) {
            throw new RuntimeException("Could not find input file " + inputXMLPath);
        }

        outputDir = new File(outputPath);
        if (!outputDir.isDirectory() || !outputDir.exists()) {
            throw new RuntimeException("Output path " + outputPath + " does not exist or is not a directory");
        }

        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(xml);
        } catch (DocumentException e) {
            throw new RuntimeException("Could not read TLD XML file", e);
        }

        generateTaglibDocumentation(document);

    }

    /**
     * Generates all the taglib documentation based on the <tag> and <function> elements found in the tld-documented XML
     * 
     * @param document
     *            the DOM representation of the XML document
     */
    private void generateTaglibDocumentation(Document document) {

        Element root = document.getRootElement();
        List<String> tagNames = new ArrayList<String>();
        List<String> functionNames = new ArrayList<String>();
        
        // get all the tag and function elements
        for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
            Element e = i.next();
            
            // keep names in separate places
            if(e.getName().equals("tag") && !e.elementText("name").equals("rickroll")) {
                tagNames.add(e.elementText("name"));
            }
            if(e.getName().equals("function")) {
                functionNames.add(e.elementText("name"));
            }
            
            // generate tag doc
            try {
                if(e.getName().equals("tag") && !e.elementText("name").equals("rickroll")) {
                    generateTagFile(e, true);
                    
                    // we keep a reference to the processed attributes because we need this for the attribute referer resolution
                    processedElements.put(e.elementText("name"), e);
                }
            } catch (FileNotFoundException io) {
                throw new RuntimeException("Cannot find generated file", io);
            } catch (IOException io2) {
                throw new RuntimeException("Cannot create generated file", io2);

            }
        }
        
        
        Collections.sort(tagNames);
        Collections.sort(functionNames);
        
        // generate index page for all tags
        File taglibIndexFile = new File(this.outputDir.getAbsolutePath() + File.separator + "TagIndex.txt");

        try {
            if (!taglibIndexFile.exists()) {
                taglibIndexFile.createNewFile();
            }

            PrintStream s = new PrintStream(new FileOutputStream(taglibIndexFile));
            s.println("!!!makumba tag library");
            s.println();
            
            for (String el : tagNames) {
                String tagName = processedElements.get(el).elementText("name");
                s.println("* [mak:" + tagName + "|" + getWikiTagName(tagName) + "]");
            }

            for(String f : functionNames) {
                s.println("* [mak:" + f + "()|" + getWikiTagName(f) + "]");
            }


        } catch (IOException io) {
            throw new RuntimeException("Cannot create index file", io);
        }

    }

    /**
     * Generates one tag file
     * 
     * @param element
     *            the XML element representing this tag or function
     * @param isTag
     *            whether this is a tag or a function
     * @throws IOException
     *             if the generated file can't be created
     */
    private void generateTagFile(Element element, boolean isTag) throws IOException {

        String elementName = element.elementText("name");
        String generatedFileName = getWikiTagName(elementName);

        File generatedFile = new File(this.outputDir.getAbsoluteFile() + File.separator + generatedFileName + ".txt");
        if (!generatedFile.exists()) {
            generatedFile.createNewFile();
        }

        PrintStream s = new PrintStream(new FileOutputStream(generatedFile));

        // generate the header
        if (isTag) {
            s.println("!!!Taglib documentation for tag mak:" + elementName);
        } else {
            s.println("!!!Taglib documentation for EL function mak:" + elementName);
        }

        s.println();

        // generate the description
        generateDescription(element, s);
        s.println();

        // then the attributes
        generateAttributes(element, s);
        s.println();

        // then the "see also" section
        generateSeeAlso(element, s);
        s.println();

        // then the examples
        generateExamples(element, s);

    }

    private String getWikiTagName(String elementName) {
        return elementName.substring(0, 1).toUpperCase() + elementName.substring(1, elementName.length()) + "Tag";
    }

    private void generateExamples(Element element, PrintStream s) {
        s.println("!!Examples");
        s.println();

        Node examples = checkNodeExists(element, "example");
        StringTokenizer tk = new StringTokenizer(examples.getText(), ",");

        if (tk.countTokens() == 0) {
            s.println("%%(color:red) No example page provided for this tag!!%%");
        }

        while (tk.hasMoreElements()) {
            s.println(getPageInsert(tk.nextToken()));
            s.println();
        }
    }

    private void generateSeeAlso(Element element, PrintStream s) {
        s.println("!!See also");
        s.println();

        Node seeAlso = checkNodeExists(element, "see");
        StringTokenizer tk = new StringTokenizer(seeAlso.getText(), ",");
        while (tk.hasMoreElements()) {

            String reference = tk.nextToken().trim();

            Object referredElement = element.getDocument().getRootElement().selectObject(
                "//taglib//tag['@name=" + reference + "']");
            if (referredElement == null) {
                throw new RuntimeException("Error: see also reference " + reference + " in tag definition "
                        + element.elementText("name") + " does not exist.");
            }

            String referenceWikiName = reference.substring(0, 1).toUpperCase()
                    + reference.substring(1, reference.length()) + "Tag";

            s.print("[mak:" + reference + "|" + referenceWikiName + "]");
            if (tk.hasMoreTokens()) {
                s.print(", ");
            }
        }
        s.println();
    }

    private void generateAttributes(Element element, PrintStream s) {
        s.println("!!Attributes");

        List<Element> attributes = element.elements("attribute");
        if (attributes.size() == 0) {
            s.println("This tag has no attributes");
        } else {

            // generate the table header
            s.println("[{Table");
            s.println(); // empty line necessary or the plugin doesn't work
            s.println("||Name||Required||Request-time||Description||Comments");

            for (Element a : attributes) {

                if (a.attribute("name") != null && a.attribute("specifiedIn") != null) {

                    Element includedAttribute = MakumbaTLDGenerator.getReferencedAttributes(this.processedElements,
                        "Error processing attribute " + a.attributeValue("name") + " of tag "
                                + element.elementText("name") + ": ", element, element.elementText("name"), a);
                    generateAttributeRow(includedAttribute, s);
                } else {
                    generateAttributeRow(a, s);
                }
            }
            s.println("}]");
        }
    }

    private void generateAttributeRow(Element attribute, PrintStream s) {

        // TODO handle all the case of generic attributes, see TLD2Forest
        // TODO also handle deprecation etc.

        String name = attribute.elementText("name");
        String required = attribute.elementText("required");
        String runtimeExpr = attribute.elementText("rtexprvalue");

        String description = getOrInsertElement(attribute, "description");
        String comments = getOrInsertElement(attribute, "comments");

        // FIXME remove line breaks in description and comments
        // as a workaround we escape them but that's very ugly
        description = "{{{" + description + "}}}";
        comments = "{{{" + comments + "}}}";

        s.println("|" + name + "|" + required + "|" + runtimeExpr + "|" + description + "|" + comments);
    }

    /**
     * Generates the description for this document. Checks if the <description> element in taglib-documented exists
     */
    private void generateDescription(Element element, PrintStream s) {
        s.println("!!Description");
        s.println();
        s.println(getOrInsertElement(element, "description"));
    }

    /**
     * Figures out if there is a page specified for the element documentation and returns the insert syntax it if yes.
     * If not, checks if there is a normal version of the element and inserts its content. If none of both are provided,
     * throws an exception.
     */
    private String getOrInsertElement(Element element, String elementName) {
        Element e = element.element(elementName);
        Element elementPage = element.element(elementName + "Page");

        if (elementPage != null && elementPage.hasContent()) {
            return getPageInsert(elementPage.getText());
        } else if (e != null && e.hasContent()) {
            return e.getText();
        } else if (e != null && !e.hasContent()) {
            return "%%(color:red) DOCUMENT ME PLEASE! %%";
        } else if (e == null && elementPage == null) {
            throw new RuntimeException("No " + elementName + " for element " + element.elementText("name"));
        }
        return null;
    }

    private Node checkNodeExists(Element element, String nodeName) {
        Node n = element.element(nodeName);
        if (n == null) {
            throw new RuntimeException("No <" + nodeName + "> element found in taglib-documented.xml for element "
                    + element.elementText("name"));
        }
        return n;
    }

    private String getPageInsert(String pageName) {
        return "[{InsertPage page=" + pageName + "}]";

    }

}
