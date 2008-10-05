package org.makumba.commons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * This class takes a TLD XML file and transforms it in a bunch of XML files understandable by Apache Forest.<br>
 * TODO see if tomcat still understands the TLD if we add some custom tags
 * TODO make this an actual Forest plugin<br>
 * TODO finish the XML generation according to the template file<br>
 * 
 * @author Manuel Gay
 * @version $Id: TLD2Forest.java,v 1.1 Oct 3, 2008 11:11:51 PM manu Exp $
 */
public class TLD2Forest {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Arguments needed: [path to the TLD file] [path to the output directory of XMLs]  (absolute paths");
        }

        // read the TLD file entered as first argument
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new File(args[0]));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // let's go thru all the tag elements and fetch useful stuff there
        Element root = document.getRootElement();
        for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
            Element e = i.next();
            if (e.getName().equals("tag")) {

                Element name = e.element("name");
                String tagName = name.getText();

                // create a new XML file for this tag
                Document tagXML = DocumentHelper.createDocument();
                Element docElement = tagXML.addElement("document");
                Element headerElement = docElement.addElement("header");
                Element titleElement = headerElement.addElement("title");
                titleElement.setText("mak:" + tagName + " tag documentation");
                Element bodyElement = docElement.addElement("body");
                bodyElement.addAttribute("id", "attributes");
                Element titleElement2 = bodyElement.addElement("title");
                titleElement2.setText("Attributes");
                
                // your turn Anet ;-)
                
                
                

                tagXML.addDocType("document", "-//APACHE//DTD Documentation V2.0//EN",
                    "http://forrest.apache.org/dtd/document-v20.dtd");

                
                // now we write our new guy to the disk
                String path = args[1] + File.separator + "mak" + tagName + ".xml";
                System.out.println("Writing XML for tag " + tagName + " at path " + path);
                try {
                    XMLWriter output = new XMLWriter(new FileWriter(new File(path)), new OutputFormat("  ", true));
                    output.write(tagXML);
                    output.close();
                } catch (IOException e1) {
                    System.out.println(e1.getMessage());
                }
            }
        }
    }
}
