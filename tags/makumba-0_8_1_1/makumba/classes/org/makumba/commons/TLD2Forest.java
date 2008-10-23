package org.makumba.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
 * This class takes a TLD XML file and transforms it in a bunch of XML files understandable by Apache Forest.<br>
 * Created a separate XML with described tags
 * TODO make this an actual Forest plugin<br>
 * TODO finish the XML generation according to the template file<br>
 * 
 * tag.XML - [path to the output directory of XMLs]\<b>tags</b>\tag.xml <br>
 * tagExample.XML - [path to the output directory of XMLs]\<b>examples</b>\tagExample.xml <br>
 * tagWithExample.XML - [path to the output directory of XMLs]\tagWithExample.xml <br>
 * 
 * @author Manuel Gay
 * @author Anet Aselmaa (12 Oct 2008)
 * @version $Id: TLD2Forest.java,v 1.1 Oct 3, 2008 11:11:51 PM manu Exp $
 */
public class TLD2Forest {
    final static String EXAMPLES_FOLDER = "examples";
    final static String TAGS_FOLDER = "tags";
    final static int CREATE = 1;//action is step 1-creating all the needed files (separate tag.xml & tagExample.xml)
    final static int UPDATE = 2;//action is step 2 - merging all tag.xml and tagExample.xml files
    final static String EXAMPLE_SECTION_ID = "example";
    
    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Arguments needed: [path to the TLD file] [path to the output directory of XMLs]  (absolute paths");
        }
        String tldFilePath = args[0]; //currently used 'makumba\classes\META-INF\taglib-documented.xml' 
        String outputDirectory = args[1];
        int action = CREATE;//default
        if(args.length==3){
            String s = args[2];
            try {
                int parsedAction = Integer.parseInt(s);
                if(!s.equals("create") && !s.equals("update") && 
                        parsedAction!=CREATE && parsedAction!=UPDATE){
                    System.err.println("args[2] must be 'create', 'update', '1' or '2'");
                    return;
                }
                else if(s.equals("create"))
                    action = CREATE;
                else if( s.equals("update")){
                    action = UPDATE;
                }
                else{
                    action = parsedAction;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("continuing with creating new files");
            }
        }
        
        switch (action) {
            case CREATE://it is step 1, creating separate 2 files
                System.out.println("doing STEP 1");
                generateAllTagFiles(tldFilePath, outputDirectory);
                generateAllTagExampleFiles(tldFilePath, outputDirectory);
                break;
            case UPDATE:
                System.out.println("doing STEP 2");
//                generateAllTagWithExampleFiles(tldFilePath, outputDirectory);
                generateAllTagsWithExampleFile2(tldFilePath, outputDirectory);
                break;
            default:
                break;
        }
              
    }
    /**
     * 
     * @param tldFileDirectory
     * @param outputDirectory
     */
    public static void generateAllTagFiles(String tldFileDirectory, String outputDirectory){
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
                generateTagFile(outputDirectory, e);
            }
        }
    }
    /**
     * 
     * @param tldFileDirectory
     * @param outputDirectory
     */
    public static void generateAllTagExampleFiles(String tldFileDirectory, String outputDirectory){
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
                generateTagExampleFile(outputDirectory, e.elementText("name"));
            }
        }
    }

    
    /**
     * 
     * @param outputDirectory
     * @param tagName
     * @return
     */
    public static String generateTagFile(String outputDirectory, Element tag){
        String tagName = tag.elementText("name");
        String tagFilePath = outputDirectory + File.separator + TAGS_FOLDER + File.separator + "mak" + tagName + ".xml";
        System.out.println("TLD2Forest.generateTagFile(): file name -"+tagFilePath);
        // read the TLD file entered as first argument
    
    
        // create a new XML file for this tag
        Document tagXML = DocumentHelper.createDocument();
        Element docElement = tagXML.addElement("document");
        Element headerElement = docElement.addElement("header");
        Element titleElement = headerElement.addElement("title");
        titleElement.setText("mak:" + tagName + " tag documentation");
        Element bodyElement = docElement.addElement("body");
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
        String[] attributeTags = {"name","required","rtexprvalue","description"};
        /* the iterator of elements of a tag*/
        for (Iterator<Element> tagElementIter = tag.elementIterator(); tagElementIter.hasNext();) {
            Element tagElement = tagElementIter.next();
    //                        System.out.println("tagelement: "+tagElement.getName());
            /* looking for attributes*/
            if(tagElement.getName().equals("attribute")){
                int cellAddedCount = 0;
                /* if attribute is found then start writing new row for a table */
                Element tr = table.addElement("tr");
                /* going thru all the different data of attributes */
                for (Iterator<Element> tagElementAttributeIter = tagElement.elementIterator(); tagElementAttributeIter.hasNext();) {
                    Element dataElement = tagElementAttributeIter.next();
    //                                System.out.println("tagelement attribute: "+dataElement.getText());
                    String tagElementAttributeName = dataElement.getName();
                    /* looking only specified data of an attribute, currently there are 4 tags to look for */
                    for (String attributeName : attributeTags) {
                        /* 
                         * if specified attribute is found then write the corresponding data to a table cell 
                         */
                        if(tagElementAttributeName.equals(attributeName)){
    //                                    Element dataElement = tagElementAttribute.element(attributeName);
                            /* 
                             * content of a current data tag 
                             */
                            String elementText = dataElement.getText();
                            /* 
                             * if a text of the tag is empty a check to avoid nullpointer Exception. Is it possible to have null???? 
                             */
                            elementText = (elementText!=null ? elementText : "");
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
            } //end of tag attributes part
            
        } 
        Element exampleSection = bodyElement.addElement("section");
        exampleSection.addAttribute("id", EXAMPLE_SECTION_ID);
        exampleSection.addElement("title");
        
        tagXML.addDocType("document", "-//APACHE//DTD Documentation V2.0//EN",
            "http://forrest.apache.org/dtd/document-v20.dtd");
    
        
        // now we write our new guy to the disk
        System.out.println("Writing XML for tag " + tagName + " at path " + tagFilePath);
        try {
            String tagsDir = outputDirectory+ File.separator+TAGS_FOLDER;
            if(!(new File(tagsDir)).exists()){
                boolean success = (new File(tagsDir)).mkdir();
                if (success) {
                    System.out.println("Directory: " + tagsDir + " created");
                } 
            }

            XMLWriter output = new XMLWriter(new FileWriter(new File(tagFilePath)), new OutputFormat("  ", true));
            output.write(tagXML);
            output.close();
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        }
        return tagFilePath;
    }

    /**
     * Method for checking if example file exists if not an example.xml with empty structure 
     * is generated. If there already exists corresponding example file then just the full path 
     * file name is returned.
     * @param outputDirectory - name of the output directory where to put the generated example file
     * @param tagName - tag for which the example file is generated
     * @return the name of the example file
     */
    public static String generateTagExampleFile(String outputDirectory, String tagName){
        String exampleFilePath = outputDirectory+ File.separator+EXAMPLES_FOLDER+File.separator + "mak" + tagName +"Example"+ ".xml";
        //TODO check if file already exists
        File f = new File(exampleFilePath);
        if(!f.exists()){
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
                String exampleDir = outputDirectory+ File.separator+EXAMPLES_FOLDER;
                if(!(new File(exampleDir)).exists()){
                    boolean success = (new File(exampleDir)).mkdir();
                    if (success) {
                        System.out.println("Directory: " + exampleDir + " created");
                    } 
                }
                XMLWriter output = new XMLWriter(new FileWriter(new File(exampleFilePath)), new OutputFormat("  ", true));
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
     * @return
     */
    public static String generateAllTagsWithExampleFile2(String tldFilePath, String outputDirectory){
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
                generateTagWithExampleFile(outputDirectory, e);
            }
        }
        return null;
    }

    /**
     * Step 2 method that merges separate tag.xml and tagExample.xml file
     * @return
     */
    public static void generateTagWithExampleFile(String outputDirectory, Element tag){
        String tagName = tag.elementText("name");
        SAXReader saxReader = new SAXReader();

        String tagsDir = outputDirectory + File.separator + TAGS_FOLDER;
        String tagFileName = "mak" + tagName + ".xml";
        String tagFilePath = tagsDir + File.separator + tagFileName;
        
        String exampleDir = outputDirectory + File.separator + EXAMPLES_FOLDER;
        String exampleFileName = "mak" + tagName +"Example"+ ".xml";
        String exampleFilePath = exampleDir + File.separator + exampleFileName;
        
        //final file has the same name as a tag.xml without the example part
        String tagWithExampleFilePath = outputDirectory+File.separator+tagFileName; 
        
        //TODO check if exists tagExample.xml
        File exampleFile = new File(exampleFilePath);
        if(exampleFile.exists()){
            //TODO find the correct place in tag.xml where to add the tagExample.xml code
            /*
             * reading the tag.XML file
             */
            try {        
                Document exampleXML = saxReader.read(exampleFile);
                DefaultElement exampleSection = (DefaultElement) exampleXML.getRootElement().selectObject( "//section" );
                
                Document tagXML = saxReader.read(new File(tagFilePath));
//            Element tagExampleSection = tagXML.elementByID(EXAMPLE_SECTION_ID);
                Element tagRoot = tagXML.getRootElement();
                Element tagExampleSection = null;
                List l = (List) tagRoot.selectObject( "//document//body//section" );
                if(l.size()>0){
                    for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                        Element el = (Element) iterator.next();
                        if(EXAMPLE_SECTION_ID.equals(el.attributeValue("id"))){ 
                            System.out.println("writing file "+tagWithExampleFilePath);
                            tagExampleSection = el;
                            Element parent = tagExampleSection.getParent();
                            parent.remove(tagExampleSection);
                            //adding the needed part from the exampleXML
                            parent.add(exampleSection);
                            XMLWriter writer = new XMLWriter(new FileWriter(new File(tagWithExampleFilePath)), new OutputFormat("  ", true));
                            writer.write(tagXML);
                            writer.close();
                        }
                    }
                    if(tagExampleSection==null){
                        System.err.println("There was no section with ID="+EXAMPLE_SECTION_ID);
                    }
                }
                else
                    System.err.println("Couldn't find elementById "+EXAMPLE_SECTION_ID+" from file "+tagFilePath);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
        }
    }
        
        
 
}
