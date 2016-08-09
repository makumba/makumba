package org.makumba.commons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

/**
 * This class takes a TLD XML file and transforms it in a bunch of XML files understandable by Apache Forest, in two
 * steps.<br>
 * <ul>
 * <li>Step 1 takes care of generating the tag documentation files and separate example XML files in which examples can
 * be provided</li>
 * <li>Step 2 merges the generated tag files and the corresponding example files</li>
 * </ul>
 * This mechanism allows for continuous documentation based on the TLD file.<br>
 * tag.XML - [path to the output directory of XMLs]\<b>tags</b>\tag.xml <br>
 * tagExample.XML - [path to the output directory of XMLs]\<b>examples</b>\tagExample.xml <br>
 * 
 * @author Manuel Gay
 * @author Anet Aselmaa (12 Oct 2008)
 * @author Rudolf Mayer
 * @version $Id: TLD2Forest.java,v 1.1 Oct 3, 2008 11:11:51 PM manu Exp $
 */
public class TLD2Forest {
    private final static int CREATE = 1;// action is step 1-creating all the needed files (separate tag.xml &

    // tagExample.xml)

    private final static int UPDATE = 2;// action is step 2 - merging all tag.xml and tagExample.xml files

    private final static int BOTH = 3;// both actions
    
    private static enum TagFileType { 
        
        EXAMPLE("Example"), DESCRIPTION("Description");
        
        String typeName;
        
        public String getTypeName() {
            return typeName;
        }
        
        TagFileType(String name) { this.typeName = name; }

    }

    private static String errorMsg;

    private static HashMap<String, Element> processedTags = new HashMap<String, Element>();

    private static final String[] attributeTags = { "name", "required", "rtexprvalue", "description", "comments" };

    private static final String[] attributeClassesAlignment = { null, "center", "center", null, null };

    private static final String[][] attributeHighlightValues = { null, { "true" }, null,
            { "Document me please", "FIXME" }, { "Document me please" } };

    private static final String[][] genericAttributes = {
            { "Form-specific HTML tag attribute",
                    "The content is copied to the resulting <form...> tag. Careful with (escaping) quotes." },
            { "Generic HTML tag attribute",
                    "The content is copied to the resulting html tag. Careful with (escaping) quotes." },
            { "Input-specific HTML tag attribute",
                    "The content is copied to the resulting <input...> tag. Careful with (escaping) quotes." } };

    private static final String[] attributeHighlightClasses = { null, "required", null, "missingDoc", "missingDoc" };

    // stores the name of that latest generic attribute encountered
    private static String genericAttributeName = null;

    // counts how many rows of generic attributes with the same name we have seen
    private static int genericAttributesCount = 0;

    // reference to the first row of the generic attribute. needed to afterwards modify the rowspan
    private static Element genericAttributeFirstRow = null;

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Arguments needed: [path to the TLD file] [path to the output directory of example XMLs] [path to the output directory of taglib XMLs for Forrest]  (absolute paths)");
        }
        String tldFilePath = args[0]; // currently used 'makumba\classes\META-INF\taglib-documented.xml'
        errorMsg = "Error processing '" + tldFilePath + "': ";
        String exampleDirectory = args[1];
        String taglibDirectory = args[2];
        int action = CREATE;// default
        if (args.length == 4) {
            String s = args[3];
            try {
                int parsedAction = Integer.parseInt(s);
                if (!s.equals("create") && !s.equals("update") && parsedAction != CREATE && parsedAction != UPDATE
                        && parsedAction != BOTH) {
                    System.err.println("args[2] must be 'create', 'update', 'both', '1', '2' or '3' ");
                    return;
                } else if (s.equals("create")) {
                    action = CREATE;
                } else if (s.equals("update")) {
                    action = UPDATE;
                } else if (s.equals("both")) {
                    action = BOTH;
                } else {
                    action = parsedAction;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("continuing with creating new files");
            }
        }

        if (action == CREATE || action == BOTH) { // it is step 1, creating separate 2 files
            System.out.println("doing STEP 1");
            generateAllTagFiles(tldFilePath, taglibDirectory);
            generateAllTagSpecificFiles(tldFilePath, exampleDirectory);
        }
        if (action == UPDATE || action == BOTH) {
            System.out.println("doing STEP 2");
            mergeAllTagsWithSpecificFiles(tldFilePath, taglibDirectory, exampleDirectory);
        }

    }

    /**
     * @param tldFileDirectory
     * @param taglibDirectory
     */
    public static void generateAllTagFiles(String tldFileDirectory, String taglibDirectory) {
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new File(tldFileDirectory));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // let's go thru all the tag elements and fetch useful stuff there
        Element root = document.getRootElement();
        for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
            Element e = i.next();
            if (e.getName().equals("tag") || e.getName().equals("function")) {
                generateTagFile(taglibDirectory, e);
            }
        }
    }

    public static void generateAllTagSpecificFiles(String tldFileDirectory, String tagDirectory) {
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new File(tldFileDirectory));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // let's go thru all the tag elements and fetch useful stuff there
        Element root = document.getRootElement();
        for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
            Element e = i.next();
            if (e.getName().equals("tag")) {
                generateTagSpecificFile(tagDirectory, e.elementText("name"), TagFileType.EXAMPLE);
                generateTagSpecificFile(tagDirectory, e.elementText("name"), TagFileType.DESCRIPTION);
            }
        }
    }

    public static String generateTagFile(String taglibDirectory, Element tag) {
        String tagName = tag.elementText("name");
        String tagFilePath = taglibDirectory + File.separator + "mak" + tagName + ".xml";
        System.out.println("TLD2Forest.generateTagFile(): file name -" + tagFilePath);
        // read the TLD file entered as first argument

        // create a new XML file for this tag
        Document tagXML = DocumentHelper.createDocument();
        Element docElement = tagXML.addElement("document");
        Element headerElement = docElement.addElement("header");
        Element titleElement = headerElement.addElement("title");
        if (tag.getName().equals("tag")) {
            titleElement.setText("mak:" + tagName + " tag documentation");
        } else {
            titleElement.setText("mak:" + tagName + "() function documentation");
        }
        Element bodyElement = docElement.addElement("body");

        // tag description
        Element infoSection = bodyElement.addElement("section");
        infoSection.addAttribute("id", TagFileType.DESCRIPTION.toString().toLowerCase());
        Element infoSectionTitle = infoSection.addElement("title");
        infoSectionTitle.setText(TagFileType.DESCRIPTION.getTypeName());
        Element description = infoSection.addElement("p");
        description.addAttribute("class", "tagDescription");

        String desc = new String();
        String see = new String();

        for (Iterator<Element> tagElementIter = tag.elementIterator(); tagElementIter.hasNext();) {
            Element tagElement = tagElementIter.next();
            if (tagElement.getName().equals("see")) {
                see = tagElement.getText();
            }
        }
        if (desc.trim().length() != 0) {
            description.setText(desc);
        } else {
            description.setText("FIXME: no description for this tag in taglib-documented.xml!");
        }

        // attributes
        Element sectionElement = bodyElement.addElement("section");
        sectionElement.addAttribute("id", "attributes");
        Element titleElement2 = sectionElement.addElement("title");
        if (tag.getName().equals("tag")) { // create attribute section only for tags, not for functions
            titleElement2.setText("Attributes");
            /* creating the table */
            if (hasAttributes(tag, "attribute")) { // create table only if this tag has attributes
                Element table = sectionElement.addElement("table");
                Element headerRow = table.addElement("tr");
                headerRow.addElement("th").setText("Name");
                headerRow.addElement("th").setText("Required");
                headerRow.addElement("th").setText("Request-time");
                headerRow.addElement("th").setText("Description");
                headerRow.addElement("th").setText("Comments");
                /* the content */
                /* the iterator of elements of a tag */
                for (Iterator<Element> tagElementIter = tag.elementIterator(); tagElementIter.hasNext();) {
                    Element tagElement = tagElementIter.next();
                    /* looking for attributes */
                    if (tagElement.getName().equals("attribute")) {
                        if (tagElement.attributeValue("name") != null
                                || tagElement.attributeValue("specifiedIn") != null) {
                            // have a referring attribute
                            Element attribute = MakumbaTLDGenerator.getReferencedAttributes(processedTags, errorMsg,
                                tag, tagName, tagElement);
                            processAttribute(table, attribute);
                        } else { // normal attribute
                            processAttribute(table, tagElement);
                        }
                    }
                }
                // clean up potential generic attributes at the end of the table
                modifyGenericAttributeFirstRow();
                genericAttributeName = null;
                genericAttributeFirstRow = null;
                genericAttributesCount = 0;

            } else { // default message if the tag has no attributes
                sectionElement.addElement("p").setText("This tag has no attributes");
            }
        }
        Element exampleSection = bodyElement.addElement("section");
        exampleSection.addAttribute("id", TagFileType.EXAMPLE.toString().toLowerCase());
        exampleSection.addElement("title");

        // see also
        if (see.length() != 0) {
            see = see.trim();
            Element seeAlsoSection = bodyElement.addElement("section");
            seeAlsoSection.addAttribute("id", "seeAlso");
            Element seeAlsoSectionTitle = seeAlsoSection.addElement("title");
            seeAlsoSectionTitle.setText("See also");
            Element seeAlso = seeAlsoSection.addElement("ul");

            String links = new String();

            StringTokenizer st = new StringTokenizer(see, ",");
            while (st.hasMoreTokens()) {
                String reference = st.nextToken().trim();
                // check if referred tag exists
                List referredElement = (List) tag.getDocument().getRootElement().selectObject(
                    "//taglib//tag['@name=" + reference + "']");
                if (referredElement == null) {
                    throw new RuntimeException("Error: see also reference " + reference + " in tag definition "
                            + tagName + " does not exist.");
                }

                Element refElement = seeAlso.addElement("li");
                Element link = refElement.addElement("a");
                link.addAttribute("href", "/doc/taglib/" + "mak" + reference + ".html");
                link.setText("mak:" + reference);
            }
            seeAlso.setText(links);
        }

        // now we write our new guy to the disk
        System.out.println("Writing XML for tag " + tagName + " at path " + tagFilePath);
        try {
            XMLWriter output = new XMLWriter(new FileWriter(new File(tagFilePath)), new OutputFormat("  ", true));
            output.write(tagXML);
            output.close();
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        }
        processedTags.put(tagName, tag);
        return tagFilePath;
    }

    private static boolean hasAttributes(Element tag, String attributeName) {
        for (Element e : (List<Element>) tag.elements()) {
            if (e.getName().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    private static void processAttribute(Element table, Element tagElement) {
        int cellAddedCount = 0;
        /* if attribute is found then start writing new row for a table */
        Element tr = table.addElement("tr");

        boolean isDeprecated = isDeprecated(tagElement);
        if (isDeprecated) {
            tr.addAttribute("class", "deprecated");
        }

        Element[] elements = new Element[attributeTags.length];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = getChildElement(tagElement, attributeTags[i]);
        }
        String thisGenericAttributeName = null;

        for (String[] generic : genericAttributes) {
            if (generic[0].equals(elements[3].getTextTrim()) && generic[1].equals(elements[4].getTextTrim())) {
                thisGenericAttributeName = generic[0];
                // if this attribute is a generic attribute
                if (genericAttributeName == null) { // if there was no previous generic attribute, set start values
                    // System.out.println("new generic attribute: " + thisGenericAttributeName);
                    genericAttributeName = thisGenericAttributeName;
                    genericAttributeFirstRow = tr;
                    genericAttributesCount = 1;
                } else if (genericAttributeName.equals(thisGenericAttributeName)) {
                    // we have another generic attribute as before
                    // System.out.println("continued generic attribute: " + thisGenericAttributeName);
                    genericAttributesCount++;
                } else { // we have a new generic attribute
                    // System.out.println("change of generic attribute: " + thisGenericAttributeName);
                    modifyGenericAttributeFirstRow();
                    genericAttributeFirstRow = tr;
                    genericAttributesCount = 1;
                }
            }
        }
        // if the attribute is no generic attribute
        if (thisGenericAttributeName == null && genericAttributeName != null) {
            // process the generic attributes from before
            System.out.println("change of generic attribute: " + thisGenericAttributeName);
            modifyGenericAttributeFirstRow();
            genericAttributeName = null;
            genericAttributeFirstRow = null;
            genericAttributesCount = 0;
        }

        /* looking only specified data of an attribute, currently there are 4 tags to look for */
        for (int i = 0; i < elements.length; i++) {
            if (genericAttributesCount > 1 && (i == 3 || i == 4)) {
                // if we are in the second or later occurrence of a generic attribute, don't write the last two tables
                // System.out.println("skipping writing td element " + i + "/" + elements.length);
                continue;
            }

            final Element dataElement = elements[i];
            String attributeName = attributeTags[i];
            Element td = tr.addElement("td");
            // td.addAttribute("rowspan", "1");
            StringBuffer cssClasses = new StringBuffer();

            if (isDeprecated) {
                appendClass(cssClasses, "deprecated");
            }
            // content of a current data tag
            String elementText = dataElement != null ? dataElement.getText() : "FIXME";

            // apply special formatting
            if (StringUtils.isNotBlank(attributeClassesAlignment[i])) {
                appendClass(cssClasses, attributeClassesAlignment[i]);
            }
            if (org.makumba.commons.StringUtils.equalsAny(elementText, attributeHighlightValues[i])) {
                appendClass(cssClasses, attributeHighlightClasses[i]);
            }

            // special treatment for deprecated attributes
            if (isDeprecated) {
                if (attributeName.equals("name")) {
                    elementText += " (deprecated)";
                }
                // appendClass(cssClasses, "deprecated");
            }
            td.setText(elementText);
            if (StringUtils.isNotBlank(cssClasses.toString())) {
                td.addAttribute("class", cssClasses.toString());
            }
            cellAddedCount++;
        }
    }

    private static void modifyGenericAttributeFirstRow() {
        if (genericAttributesCount > 1) { // only do it if we have more than one generic attribute
            final List<Element> elements2 = genericAttributeFirstRow.elements();
            elements2.get(3).addAttribute("rowspan", String.valueOf(genericAttributesCount));
            elements2.get(4).addAttribute("rowspan", String.valueOf(genericAttributesCount));
            elements2.get(3).addAttribute("class", "generic");
            elements2.get(4).addAttribute("class", "generic");
        }
    }

    private static void appendClass(StringBuffer cssClasses, String cssClass) {
        if (cssClasses.length() > 0) {
            cssClasses.append(" ");
        }
        cssClasses.append(cssClass);
    }

    private static boolean isDeprecated(Element e) {
        for (Iterator<Element> tagElementAttributeIter = e.elementIterator(); tagElementAttributeIter.hasNext();) {
            Element dataElement = tagElementAttributeIter.next();
            if (dataElement.getName().equals("deprecated")) {
                return StringUtils.equals(dataElement.getText(), "true");
            }
        }
        return false;
    }

    private static Element getChildElement(Element e, String name) {
        for (Element dataElement : (List<Element>) e.elements()) {
            if (dataElement.getName().equals(name)) {
                return dataElement;
            }
        }
        return null;
    }

    /**
     * Method for checking if tag-specific file (example, description) exists. If not an XML with empty structure is generated. If there
     * already exists corresponding tag-specific file then just the full path file name is returned.
     * 
     * @param directory
     *            name of the output directory where to put the generated file
     * @param tagName
     *            tag for which the file is generated
     * @param type type of the file that is going to be generated
     * 
     * @return the name of the example file
     */
    public static String generateTagSpecificFile(String directory, String tagName, TagFileType type) {
        
        String filePath = directory + File.separator + "mak" + tagName + type.getTypeName() + ".xml";
            
        File f = new File(filePath);
        if (!f.exists()) {
            // empty generated tagExample.xml or tagDescription.xml file
            Document specificXML = DocumentHelper.createDocument();
            Element specificSection = specificXML.addElement("section");
            specificSection.addAttribute("id", type.toString().toLowerCase());
            Element specificTitle = specificSection.addElement("title");
            specificTitle.setText(type.getTypeName());
            Element specificTextParagraph = specificSection.addElement("p");
            specificTextParagraph.setText(type.getTypeName());
            try {
                if (!(new File(directory)).exists()) {
                    boolean success = (new File(directory)).mkdir();
                    if (success) {
                        System.out.println("Directory: " + directory + " created");
                    }
                }
                XMLWriter output = new XMLWriter(new FileWriter(new File(filePath)),
                        new OutputFormat("  ", true));
                output.write(specificXML);
                output.close();
            } catch (IOException e1) {
                System.out.println(e1.getMessage());
            }
        }
        return filePath;
    }

    /**
     * Step 2 method that merges separate tag.xml and tagExample.xml file
     * 
     * @return
     */
    public static String mergeAllTagsWithSpecificFiles(String tldFilePath, String taglibDirectory,
            String exampleDirectory) {
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new File(tldFilePath));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // let's go thru all the tag elements and fetch useful stuff there
        Element root = document.getRootElement();
        for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
            Element e = i.next();
            if (e.getName().equals("tag") || e.getName().equals("function")) {

                SAXReader reader = new SAXReader();

                String tagName = e.elementText("name");
                String tagFileName = "mak" + tagName + ".xml";
                String tagFilePath = taglibDirectory + File.separator + tagFileName;
                
                try {
                    Document tagXML = reader.read(new File(tagFilePath));
                    
                    mergeTagWithSpecificFile(taglibDirectory, exampleDirectory, e, TagFileType.DESCRIPTION, tagXML);
                    mergeTagWithSpecificFile(taglibDirectory, exampleDirectory, e, TagFileType.EXAMPLE, tagXML);

                    // finally, add the docType to our finalized XML
                    tagXML.addDocType("document", "-//MAKUMBA//DTD Documentation V2.0//EN", "document-v20-mak.dtd");
                    
                    XMLWriter writer = new XMLWriter(new FileWriter(new File(tagFilePath)), new OutputFormat("  ", false));
                    writer.write(tagXML);
                    writer.close();

                    
                } catch (DocumentException e1) {
                    System.err.println("Couldn't read tag file");
                    e1.printStackTrace();
                } catch (IOException e2) {
                    System.err.println("Couldn't write tag file");
                    e2.printStackTrace();
                }
                
            }
        }
        

        return null;
    }

    /**
     * Step 2 method that merges separate tag.xml, tagExample.xml and tagDescription.xml file
     * @param type TODO
     * @param tagDocument TODO
     * @return
     */
    public static void mergeTagWithSpecificFile(String tagFilePath, String specificDir, Element tag, TagFileType type, Document tagDocument) {
        SAXReader saxReader = new SAXReader();

        String tagName = tag.elementText("name");

        String specificFileName = "mak" + tagName + type.getTypeName() + ".xml";
        String specificFilePath = specificDir + File.separator + specificFileName;

        File specificFile = new File(specificFilePath);
        if (specificFile.exists()) {
            // find the correct place in tag.xml where to add the tag-specific XML code
            /*
             * reading the tag.XML file
             */
            try {
                Document specificXML = saxReader.read(specificFile);
                DefaultElement specificSection = (DefaultElement) specificXML.getRootElement().selectObject("//section");

                // Element tagExampleSection = tagXML.elementByID(EXAMPLE_SECTION_ID);
                Element tagRoot = tagDocument.getRootElement();
                Element tagSpecificSection = null;
                List l = (List) tagRoot.selectObject("//document//body//section");
                if (l.size() > 0) {
                    for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                        Element el = (Element) iterator.next();
                        if (type.toString().toLowerCase().equals(el.attributeValue("id"))) {
                            tagSpecificSection = el;
                            
                            // remove existing children
                            for(Iterator it = tagSpecificSection.elementIterator(); it.hasNext(); ) {
                                tagSpecificSection.remove((Element) it.next());
                            }
                            
                            // add children of the other guy
                            for(Iterator itNew = specificSection.elementIterator(); itNew.hasNext(); ) {
                                Element newChild = (Element) itNew.next();
                                Node n = newChild.detach();
                                tagSpecificSection.add(n);
                            }
                        }
                    }
                    if (tagSpecificSection == null) {
                        System.err.println("There was no section with ID=" + type.toString().toLowerCase());
                    }
                } else
                    System.err.println("Couldn't find elementById " + type.toString() + " from file " + tagFilePath);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

}
