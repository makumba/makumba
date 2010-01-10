package org.makumba.commons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * This class generates the taglib documentation JSPWiki files based on the taglib-documented.xml file
 * 
 * @author Manuel Gay
 * @version $Id: TaglibDocGenerator.java,v 1.1 Nov 16, 2009 2:57:04 PM manu Exp $
 */
public class TaglibDocGenerator {

    private File outputDir;

    private HashMap<String, Element> processedElements = new HashMap<String, Element>();
    
    private static final String[][] genericAttributes = {
        { "Form-specific HTML tag attribute",
                "The content is copied to the resulting <form...> tag. Careful with (escaping) quotes." },
        { "Generic HTML tag attribute",
                "The content is copied to the resulting html tag. Careful with (escaping) quotes." },
        { "Input-specific HTML tag attribute",
                "The content is copied to the resulting <input...> tag. Careful with (escaping) quotes." } };

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
                } else if(e.getName().equals("function")) {
                    generateTagFile(e, false);
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
            
            s.println();
            s.println("%%(display:none;)[Category Documentation]%%");

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

        // wtf?
        if(System.getProperty("os.name").startsWith("Mac OS X")) {
            System.setProperty("line.separator","\r\n");
        }

        FileOutputStream fos = new FileOutputStream(generatedFile);
        BufferedWriter s = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
        
        // generate the header
        if (isTag) {
            s.append("!!!Taglib documentation for tag mak:" + elementName);
            s.newLine();
        } else {
            s.append("!!!Taglib documentation for EL function mak:" + elementName);
            s.newLine();

        }

        s.newLine();

        // generate the description
        generateDescription(element, s);
        s.newLine();

        // then the attributes
        generateAttributes(element, s);
        s.newLine();

        // then the "see also" section
        generateSeeAlso(element, s);
        s.newLine();

        // then the examples
        generateExamples(element, s);
        
        // finally append the category
        s.newLine();
        s.append("%%(display:none;)[Category Documentation]%%");
        s.flush();
        s.close();
        fos.close();

    }

    private String getWikiTagName(String elementName) {
        return elementName.substring(0, 1).toUpperCase() + elementName.substring(1, elementName.length()) + "Tag";
    }

    private void generateExamples(Element element, BufferedWriter s) throws IOException {
        s.append("!!Examples");
        s.newLine();
        s.newLine();

        Node examples = checkNodeExists(element, "example");
        StringTokenizer tk = new StringTokenizer(examples.getText(), ",");

        if (tk.countTokens() == 0) {
            s.append("%%(color:red) No example page provided for this tag!!%%");
            s.newLine();
        }

        while (tk.hasMoreElements()) {
            s.append(getPageInsert(tk.nextToken()));
            s.newLine();
            s.newLine();
        }
    }

    private void generateSeeAlso(Element element, BufferedWriter s) throws IOException {
        Node seeAlso = checkNodeExists(element, "see");
        StringTokenizer tk = new StringTokenizer(seeAlso.getText(), ",");
        if(tk.countTokens() > 0) {
            s.append("!!See also");
            s.newLine();
            s.newLine();
        }
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

            s.append("[mak:" + reference + "|" + referenceWikiName + "]");
            if (tk.hasMoreTokens()) {
                s.append(", ");
            }
        }
        if(tk.countTokens() > 0) {
            s.newLine();
        }
    }

    private void generateAttributes(Element element, BufferedWriter s) throws IOException {
        s.append("!!Attributes");
        s.newLine();

        List<Element> attributes = element.elements("attribute");
        if (attributes.size() == 0) {
            s.append("This tag has no attributes");
            s.newLine();
        } else {
            s.newLine();
            s.append("%%(display:none) makumba hackers: DO NOT CHANGE THESE ATTRIBUTES HERE since your changes will be lost! You have to edit the taglib-documented.xml file and re-generate the taglib doc! %%");
            s.newLine();
            s.newLine();
            
            // generate the table header
            s.append("[{Table");
            s.newLine();
            s.newLine(); // empty line necessary or the plugin doesn't work
            s.append("||Name||Required||Request-time||Description||Comments ");
            s.newLine();
            s.newLine();
            
            // in order not to generate the same content for generic attributes (HTML, form-specific, ...)
            // we keep the first attribute of the kind and only write out all the rows if another kind of attribute is met
            GenericAttributeTuple genericAttributeTuple = new GenericAttributeTuple();

            for (Element a : attributes) {

                if (a.attribute("name") != null && a.attribute("specifiedIn") != null) {

                    Element includedAttribute = MakumbaTLDGenerator.getReferencedAttributes(this.processedElements,
                        "Error processing attribute " + a.attributeValue("name") + " of tag "
                                + element.elementText("name") + ": ", element, element.elementText("name"), a);
                    generateAttributeRow(includedAttribute, s, genericAttributeTuple);
                    
                } else {
                    generateAttributeRow(a, s, genericAttributeTuple);
                }
            }
            
            // in the end we flush the tuple
            if(genericAttributeTuple.getGenericAttributeName() != null) {
                genericAttributeTuple.print(s);
                genericAttributeTuple.reset();
            }
            
            s.newLine(); // empty line necessary or the plugin chomps one character of each cell
            s.append("}]");
            s.newLine();
        }
    }

    private void generateAttributeRow(Element attribute, BufferedWriter s, GenericAttributeTuple genericAttributeTuple) throws IOException {

        String name = attribute.elementText("name");
        String required = attribute.elementText("required");
        String runtimeExpr = attribute.elementText("rtexprvalue");

        String description = getOrInsertElement(attribute, "description");
        String comments = getOrInsertElement(attribute, "comments");

        String deprecated = attribute.elementText("deprecated");
        boolean isDeprecated = deprecated != null && deprecated.equals("true");
        String deprecatedStyle = isDeprecated ? "(deprecated) " : "";
        
        // check if this is a generic attribute
        boolean isGenericAttribute = false;
        
        for(String[] generic : genericAttributes) {
            
            // if this is a generic attribute
            if(description.equals(generic[0]) && comments.equals(generic[1])) {
                isGenericAttribute = true;
                
                if(genericAttributeTuple.getGenericAttributeName() == null) {
                    // this is the first occurrence of the generic attribute so we build it
                    genericAttributeTuple.setFirstGenericAttribute(name, required, runtimeExpr, description + " (generic)", comments + " (generic)");
                    genericAttributeTuple.setGenericAttributeName(generic[0]);
                } else if(genericAttributeTuple.getGenericAttributeName().equals(generic[0])) {
                    genericAttributeTuple.addAttribute(name, required, runtimeExpr);
                } else {
                    // we have a different generic attribute, so we print the previous one, clear the tuple and set the new one
                    genericAttributeTuple.print(s);
                    genericAttributeTuple.reset();
                    genericAttributeTuple.setFirstGenericAttribute(name, required, runtimeExpr, description + " (generic)", comments + " (generic)");
                }
            }
        }
        
        // if the attribute is no generic attribute process the generic attribute from before
        if(!isGenericAttribute && genericAttributeTuple.getGenericAttributeName() != null) {
            genericAttributeTuple.print(s);
            genericAttributeTuple.reset();
        } else if(!isGenericAttribute) {
            if(isDeprecated) {
                s.append("|" + deprecatedStyle + name + " (deprecated) ");
            } else {
                s.append("|" + deprecatedStyle + name+" ");
            }
            s.newLine();
            s.append("|" + deprecatedStyle + required+" ");
            s.newLine();
            s.append("|" + deprecatedStyle + runtimeExpr+" ");
            s.newLine();
            s.append("|" + deprecatedStyle + description+" ");
            s.newLine();
            s.append("|" + deprecatedStyle + comments+" ");
            s.newLine();
            s.newLine(); // empty line, row is over
        }
        
    }

    /**
     * Generates the description for this document. Checks if the <description> element in taglib-documented exists
     * @throws IOException 
     */
    private void generateDescription(Element element, BufferedWriter s) throws IOException {
        s.append("!!Description");
        s.newLine();
        s.newLine();
        s.append(getOrInsertElement(element, "description"));
        s.newLine();

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
            return e.getTextTrim();
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
    
    class GenericAttributeTuple {
        
        private String[] firstGenericAttribute;
        private String genericAttributeName;
        private List<String[]> attributes = new ArrayList<String[]>();
        
        public void addAttribute(String name, String required, String runtimeExpr) {
            String[] att = new String[3];
            att[0] = name;
            att[1] = required;
            att[2] = runtimeExpr;
            attributes.add(att);
        }
                
        public void print(BufferedWriter s) throws IOException {
            for(String str : firstGenericAttribute) {
                s.append("|"+str+" ");
                s.newLine();
            }
            
            s.newLine(); //first row is over, new line
            
            for(String[] att : attributes) {
                s.append("|"+att[0]+" ");
                s.newLine();
                s.append("|"+att[1]+" ");
                s.newLine();
                s.append("|"+att[2]+" ");
                s.newLine();
                s.append("|^ ");
                s.newLine();
                s.append("|^ ");
                s.newLine();
                
                s.newLine();
            }
        }
        
        public void reset() {
            firstGenericAttribute = null;
            genericAttributeName = null;
            attributes = new ArrayList<String[]>();
        }
        
        public void setFirstGenericAttribute(String name, String required, String runtimeExpr, String description, String comments) {
            this.firstGenericAttribute = new String[5];
            this.firstGenericAttribute[0] = name;
            this.firstGenericAttribute[1] = required;
            this.firstGenericAttribute[2] = runtimeExpr;
            this.firstGenericAttribute[3] = description;
            this.firstGenericAttribute[4] = comments;
            
        }
        public String getGenericAttributeName() {
            return genericAttributeName;
        }
        public void setGenericAttributeName(String genericAttributeName) {
            this.genericAttributeName = genericAttributeName;
        }
    }

}
