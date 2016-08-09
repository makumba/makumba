package org.makumba.commons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * This class generates the Makumba TLD files based on the documented TLD XML file.
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaTLDGenerator.java,v 1.1 Oct 5, 2008 4:09:45 PM manu Exp $
 */
public class MakumbaTLDGenerator {

    private static final String TAGLIB_MAK = "taglib.tld";
    private static final String TAGLIB_HIBERNATE = "taglib-hibernate.tld";
    private static final String HIBERNATE_TLD_URI = "http://www.makumba.org/view-hql";
    private static final String TAGLIB_DOCUMENTED_XML = "taglib-documented.xml";

    public static void main(String[] args) {

        // read the documented XML files
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new File(args[0]) + File.separator + TAGLIB_DOCUMENTED_XML);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // remove all the <web-description> tags inside <tag> elements
        Element root = document.getRootElement();
        for (Element tag : (List<Element>) root.elements()) {
            if (tag.getName().equals("tag")) {

                Element name = tag.element("name");
                String tagName = name.getText();

                for (Element tagContent : (List<Element>) tag.elements()) {

                    if (tagContent.getName().equals("web-description")) {
                        tagContent.getParent().remove(tagContent);
                    }

                    if (tagContent.getName().equals("attribute")) {

                        for (Element attributeContent : (List<Element>) tagContent.elements()) {
                            // remove all the <description> and <comments> tags inside <attribute> elements
                            if (attributeContent.getName().equals("description")
                                    || attributeContent.getName().equals("comments")) {
                                attributeContent.getParent().remove(attributeContent);
                            }
                        }
                    }
                }
            }
        }
        // generate the clean TLD
        String tldPath = args[0] + File.separator + TAGLIB_MAK;
        System.out.println("Writing general Makumba TLD at path " + tldPath);
        try {
            XMLWriter output = new XMLWriter(new FileWriter(new File(tldPath)), new OutputFormat("", false));
            output.write(document);
            output.close();
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        }
        
        // generate hibernate TLD
        
        Document hibernateTLD = document;
        hibernateTLD.getRootElement().element("uri").setText(HIBERNATE_TLD_URI);
        
        String hibernateTldPath = args[0] + File.separator + TAGLIB_HIBERNATE;
        System.out.println("Writing hibernate Makumba TLD at path " + hibernateTldPath);
        try {
            XMLWriter output = new XMLWriter(new FileWriter(new File(hibernateTldPath)), new OutputFormat("", false));
            output.write(document);
            output.close();
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        }
        
        // here we could now also generate the list-hql, forms-hql, ... TLDs
        // this would be easily done by defining an array of tags that should be in each of them, iterate over all the tags in the doc
        // and take only the right ones
        // and then write this with the right URL
        
    }

}
