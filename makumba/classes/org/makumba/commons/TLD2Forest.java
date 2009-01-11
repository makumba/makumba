package org.makumba.commons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
 * This mechanism allows for continuous documentation based on the TLD file TODO finish the XML generation according to
 * the template file<br>
 * tag.XML - [path to the output directory of XMLs]\<b>tags</b>\tag.xml <br>
 * tagExample.XML - [path to the output directory of XMLs]\<b>examples</b>\tagExample.xml <br>
 * 
 * @author Manuel Gay
 * @author Anet Aselmaa (12 Oct 2008)
 * @version $Id: TLD2Forest.java,v 1.1 Oct 3, 2008 11:11:51 PM manu Exp $
 */
public class TLD2Forest {
    final static String EXAMPLES_FOLDER = "examples";

    final static String TAGS_FOLDER = "taglib";

    final static int CREATE = 1;// action is step 1-creating all the needed files (separate tag.xml & tagExample.xml)

    final static int UPDATE = 2;// action is step 2 - merging all tag.xml and tagExample.xml files

    final static String EXAMPLE_SECTION_ID = "example";

    public static String errorMsg;

    public static HashMap<String, Element> processedTags = new HashMap<String, Element>();

    public static final String[] attributeTags = { "name", "required", "rtexprvalue", "description" };

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
                if (!s.equals("create") && !s.equals("update") && parsedAction != CREATE && parsedAction != UPDATE) {
                    System.err.println("args[2] must be 'create', 'update', '1' or '2'");
                    return;
                } else if (s.equals("create"))
                    action = CREATE;
                else if (s.equals("update")) {
                    action = UPDATE;
                } else {
                    action = parsedAction;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("continuing with creating new files");
            }
        }

        switch (action) {
            case CREATE:// it is step 1, creating separate 2 files
                System.out.println("doing STEP 1");
                generateAllTagFiles(tldFilePath, taglibDirectory);
                generateAllTagExampleFiles(tldFilePath, exampleDirectory);
                break;
            case UPDATE:
                System.out.println("doing STEP 2");
                // generateAllTagWithExampleFiles(tldFilePath, outputDirectory);
                generateAllTagsWithExampleFile2(tldFilePath, taglibDirectory, exampleDirectory);
                break;
            default:
                break;
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
            if (e.getName().equals("tag")) {
                generateTagFile(taglibDirectory, e);
            }
        }
    }

    /**
     * @param tldFileDirectory
     * @param exampleDirectory
     */
    public static void generateAllTagExampleFiles(String tldFileDirectory, String exampleDirectory) {
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
                generateTagExampleFile(exampleDirectory, e.elementText("name"));
            }
        }
    }

    /**
     * @param taglibDirectory
     * @param tagName
     * @return
     */
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
        titleElement.setText("mak:" + tagName + " tag documentation");
        Element bodyElement = docElement.addElement("body");

        // tag description
        Element infoSection = bodyElement.addElement("section");
        infoSection.addAttribute("id", "description");
        Element infoSectionTitle = infoSection.addElement("title");
        infoSectionTitle.setText("Description");
        Element description = infoSection.addElement("p");
        String desc = new String();
        for (Iterator<Element> tagElementIter = tag.elementIterator(); tagElementIter.hasNext();) {
            Element tagElement = tagElementIter.next();
            if (tagElement.getName().equals("description")) {
                desc = tagElement.getText();
                break;
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
        titleElement2.setText("Attributes");
        /* creating the table */
        Element table = sectionElement.addElement("table");
        Element headerRow = table.addElement("tr");
        /* the header row */
        Element nameTh = headerRow.addElement("th");
        nameTh.setText("Name");
        Element requiredTh = headerRow.addElement("th");
        requiredTh.setText("Required");
        Element rtexprvalueTh = headerRow.addElement("th");
        rtexprvalueTh.setText("rt expression value");
        Element descriptionTh = headerRow.addElement("th");
        descriptionTh.setText("Description");
        /* the content */
        /* the iterator of elements of a tag */
        for (Iterator<Element> tagElementIter = tag.elementIterator(); tagElementIter.hasNext();) {
            Element tagElement = tagElementIter.next();
            /* looking for attributes */
            if (tagElement.getName().equals("attribute")) {
                if (tagElement.attributeValue("name") != null || tagElement.attributeValue("specifiedIn") != null) {
                    // have a referring attribute
                    Element attribute = MakumbaTLDGenerator.getReferencedAttributes(processedTags, errorMsg, tag,
                        tagName, tagElement);
                    processAttribute(table, attributeTags, attribute);
                } else { // normal attribute
                    processAttribute(table, attributeTags, tagElement);
                }
            }
        }
        Element exampleSection = bodyElement.addElement("section");
        exampleSection.addAttribute("id", EXAMPLE_SECTION_ID);
        exampleSection.addElement("title");

        // tagXML.addDocType("document", "-//APACHE//DTD Documentation V2.0//EN",
        // "document-v20-mak.dtd");

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

    private static void processAttribute(Element table, String[] attributeTags, Element tagElement) {
        int cellAddedCount = 0;
        /* if attribute is found then start writing new row for a table */
        Element tr = table.addElement("tr");
        /* going thru all the different data of attributes */
        for (Iterator<Element> tagElementAttributeIter = tagElement.elementIterator(); tagElementAttributeIter.hasNext();) {
            Element dataElement = tagElementAttributeIter.next();
            // System.out.println("tagelement attribute: "+dataElement.getText());
            String tagElementAttributeName = dataElement.getName();
            /* looking only specified data of an attribute, currently there are 4 tags to look for */
            for (String attributeName : attributeTags) {
                /*
                 * if specified attribute is found then write the corresponding data to a table cell
                 */
                if (tagElementAttributeName.equals(attributeName)) {
                    // Element dataElement = tagElementAttribute.element(attributeName);
                    /*
                     * content of a current data tag
                     */
                    String elementText = dataElement.getText();
                    /*
                     * if a text of the tag is empty a check to avoid nullpointer Exception. Is it possible to have
                     * null????
                     */
                    elementText = (elementText != null ? elementText : "");
                    Element td = tr.addElement("td");
                    td.setText(elementText);
                    cellAddedCount++;
                }
            }
        }
        for (int j = cellAddedCount; j < attributeTags.length; j++) {
            Element td = tr.addElement("td");
            td.setText("");
        }
    }

    /**
     * Method for checking if example file exists if not an example.xml with empty structure is generated. If there
     * already exists corresponding example file then just the full path file name is returned.
     * 
     * @param exampleDirectory
     *            - name of the output directory where to put the generated example file
     * @param tagName
     *            - tag for which the example file is generated
     * @return the name of the example file
     */
    public static String generateTagExampleFile(String exampleDirectory, String tagName) {
        String exampleFilePath = exampleDirectory + File.separator + "mak" + tagName + "Example" + ".xml";

        File f = new File(exampleFilePath);
        if (!f.exists()) {
            // empty generated tagExample.xml file
            Document exampleXML = DocumentHelper.createDocument();
            Element exampleSection = exampleXML.addElement("section");
            exampleSection.addAttribute("id", EXAMPLE_SECTION_ID);
            Element exampleTitle = exampleSection.addElement("title");
            exampleTitle.setText("Example");
            Element exampleTextParagraph = exampleSection.addElement("p");
            exampleTextParagraph.setText("");
            Element exampleCodeParagraph = exampleSection.addElement("p");
            Element exampleCode = exampleCodeParagraph.addElement("code");
            exampleCode.setText("");
            try {
                if (!(new File(exampleDirectory)).exists()) {
                    boolean success = (new File(exampleDirectory)).mkdir();
                    if (success) {
                        System.out.println("Directory: " + exampleDirectory + " created");
                    }
                }
                XMLWriter output = new XMLWriter(new FileWriter(new File(exampleFilePath)),
                        new OutputFormat("  ", true));
                output.write(exampleXML);
                output.close();
            } catch (IOException e1) {
                System.out.println(e1.getMessage());
            }
        }
        return exampleFilePath;
    }

    /**
     * Step 2 method that merges separate tag.xml and tagExample.xml file
     * 
     * @return
     */
    public static String generateAllTagsWithExampleFile2(String tldFilePath, String taglibDirectory,
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
            if (e.getName().equals("tag")) {
                generateTagWithExampleFile(taglibDirectory, exampleDirectory, e);
            }
        }
        return null;
    }

    /**
     * Step 2 method that merges separate tag.xml and tagExample.xml file
     * 
     * @return
     */
    public static void generateTagWithExampleFile(String tagsDir, String exampleDir, Element tag) {
        String tagName = tag.elementText("name");
        SAXReader saxReader = new SAXReader();

        String tagFileName = "mak" + tagName + ".xml";
        String tagFilePath = tagsDir + File.separator + tagFileName;

        String exampleFileName = "mak" + tagName + "Example" + ".xml";
        String exampleFilePath = exampleDir + File.separator + exampleFileName;

        File exampleFile = new File(exampleFilePath);
        if (exampleFile.exists()) {
            // find the correct place in tag.xml where to add the tagExample.xml code
            /*
             * reading the tag.XML file
             */
            try {
                Document exampleXML = saxReader.read(exampleFile);
                DefaultElement exampleSection = (DefaultElement) exampleXML.getRootElement().selectObject("//section");

                Document tagXML = saxReader.read(new File(tagFilePath));

                // add the docType
                tagXML.addDocType("document", "-//MAKUMBA//DTD Documentation V2.0//EN", "document-v20-mak.dtd");

                // Element tagExampleSection = tagXML.elementByID(EXAMPLE_SECTION_ID);
                Element tagRoot = tagXML.getRootElement();
                Element tagExampleSection = null;
                List l = (List) tagRoot.selectObject("//document//body//section");
                if (l.size() > 0) {
                    for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                        Element el = (Element) iterator.next();
                        if (EXAMPLE_SECTION_ID.equals(el.attributeValue("id"))) {
                            System.out.println("writing file " + tagFilePath);
                            tagExampleSection = el;
                            Element parent = tagExampleSection.getParent();
                            parent.remove(tagExampleSection);
                            // adding the needed part from the exampleXML
                            parent.add(exampleSection);
                            XMLWriter writer = new XMLWriter(new FileWriter(new File(tagFilePath)), new OutputFormat(
                                    "  ", false));
                            writer.write(tagXML);
                            writer.close();
                        }
                    }
                    if (tagExampleSection == null) {
                        System.err.println("There was no section with ID=" + EXAMPLE_SECTION_ID);
                    }
                } else
                    System.err.println("Couldn't find elementById " + EXAMPLE_SECTION_ID + " from file " + tagFilePath);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

}
