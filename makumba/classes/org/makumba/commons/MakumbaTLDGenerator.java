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

package org.makumba.commons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.makumba.version;

/**
 * This class generates the Makumba TLD files based on the documented TLD XML file.
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public class MakumbaTLDGenerator {

    public static final String TAGLIB_SKELETON = "taglib-skeleton.tld";

    private static final String TAGLIB_MAK = "taglib.tld";

    private static final String TAGLIB_HIBERNATE = "taglib-hibernate.tld";

    private static final String HIBERNATE_TLD_URI = "http://www.makumba.org/view-hql";

    public static void main(String[] args) {
        HashMap<String, Element> processedTags = new HashMap<String, Element>();

        // read the documented XML files
        SAXReader saxReader = new SAXReader();
        Document document = null;
        final String sourcePath = new File(args[0]) + File.separator + TAGLIB_SKELETON;
        final String documentationPath = new File(args[2]).getAbsolutePath();
        try {
            document = saxReader.read(sourcePath);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        final String errorMsg = "Error processing '" + sourcePath + "': ";

        Element root = document.getRootElement();
        for (Element tag : getElementList(root)) {
            if (tag.getName().equals("description")) {
                // update makumba version place-holder
                tag.setText(tag.getText().replace("@version@", version.getVersion()));
            }
            if (StringUtils.equalsAny(tag.getName(), "tag", "function")) {
                boolean isTag = tag.getName().equals("tag");
                String tagName = tag.element("name").getText();

                for (Element tagContent : getElementList(tag)) {

                    if (tagContent.getName().equals("attribute")) {
                        if (tagContent.attributeValue("name") != null
                                || tagContent.attributeValue("specifiedIn") != null) { // have a referring attribute
                            replaceReferencedAttribute(processedTags, errorMsg, tagName, tagContent);
                        } else { // normal attribute
                            for (Element attributeContent : getElementList(tagContent)) {
                                String inheritedFrom = null;
                                if (attributeContent.getName().equals("inheritedFrom")) {
                                    inheritedFrom = attributeContent.getText();
                                }

                                // remove all the <comments> tags inside <attribute> elements
                                if (StringUtils.equalsAny(attributeContent.getName(), "comments", "deprecated",
                                    "descriptionPage", "commentsPage", "inheritedFrom")) {
                                    attributeContent.getParent().remove(attributeContent);
                                }

                                if (attributeContent.getName().equals("description")) {

                                    // insert the description
                                    String descriptionFileName = "";
                                    if (inheritedFrom != null) {
                                        descriptionFileName += org.apache.commons.lang.StringUtils.capitalize(inheritedFrom)
                                                + (isTag ? "Tag" : "Function");
                                    } else {
                                        descriptionFileName += org.apache.commons.lang.StringUtils.capitalize(tagName)
                                                + (isTag ? "Tag" : "Function");
                                    }
                                    descriptionFileName += "Attribute"
                                            + org.apache.commons.lang.StringUtils.capitalize(tagContent.elementText("name"))
                                            + "AttributeDescription";

                                    File d = new File(documentationPath + File.separator + descriptionFileName + ".txt");
                                    if (!d.exists()) {
                                        System.err.println("Could not find attribute description file " + d);
                                    }

                                    String desc = "";

                                    if (d.exists()) {
                                        try {
                                            desc = readFileAsString(d.getAbsolutePath());
                                        } catch (IOException e) {
                                            System.err.println("Could not read attribute description file " + d);
                                        }
                                    }
                                    attributeContent.setText(desc.trim());
                                }
                            }
                        }
                    }

                    // remove the invalid tags
                    if (StringUtils.equalsAny(tagContent.getName(), "see", "example")) {
                        tagContent.getParent().remove(tagContent);
                    }

                    // if we have a descriptionPage instead of a raw text, use the content of that page
                    if (tagContent.getName().equals("descriptionPage")) {
                        String descriptionFileName = org.apache.commons.lang.StringUtils.capitalize(tagName)
                                + (isTag ? "Tag" : "Function") + "Description";
                        String desc = "";
                        try {
                            desc = readFileAsString(documentationPath + File.separator + descriptionFileName + ".txt");
                        } catch (IOException e1) {
                            System.err.println("Could not read tag description file " + documentationPath
                                    + File.separator + descriptionFileName + ".txt");
                        }
                        Element d = null;
                        if ((d = tagContent.getParent().element("description")) == null) {
                            d = tagContent.getParent().addElement("description");
                        }
                        d.setText(desc.trim());
                        tagContent.getParent().remove(tagContent);
                    }
                }
                processedTags.put(tagName, tag);
            }
        }

        // generate the clean TLD
        String tldPath = args[1] + File.separator + TAGLIB_MAK;
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

        String hibernateTldPath = args[1] + File.separator + TAGLIB_HIBERNATE;
        System.out.println("Writing hibernate Makumba TLD at path " + hibernateTldPath);
        try {
            XMLWriter output = new XMLWriter(new FileWriter(new File(hibernateTldPath)), new OutputFormat("", false));
            output.write(document);
            output.close();
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        }

        // here we could now also generate the list-hql, forms-hql, ... TLDs
        // this would be easily done by defining an array of tags that should be in each of them, iterate over all the
        // tags in the doc
        // and take only the right ones
        // and then write this with the right URL

    }

    @SuppressWarnings("unchecked")
    private static List<Element> getElementList(Element list) {
        return list.elements();
    }

    /**
     * Replaces the content of a tag attribute using "specifiedIn" by the actual content
     * 
     * @param processedTags
     *            the hashmap of already processed tags
     * @param errorMsg
     *            the error message appearing in case the referenced attribute can't be found
     * @param tagName
     *            the name of the parent tag
     * @param attributeTagContent
     *            the content of the attribute
     */
    public static void replaceReferencedAttribute(HashMap<String, Element> processedTags, final String errorMsg,
            String tagName, Element attributeTagContent) {
        Element newTag = getReferencedAttributes(processedTags, errorMsg, tagName, attributeTagContent, false);
        attributeTagContent.setAttributes(newTag.attributes());
        final List<Element> elements = getElementList(newTag);
        for (Element element : elements) {
            attributeTagContent.add((Element) element.clone());
        }
    }

    /**
     * Retrieves the attribute referenced via "specifiedIn"
     * 
     * @param processedTags
     *            the hashmap of already processed tags
     * @param errorMsg
     *            the error message appearing in case the referenced attribute can't be found
     * @param tagName
     *            the name of the parent tag
     * @param attributeTagContent
     *            the content of the attribute
     * @param addOrigin
     *            TODO
     * @param tag
     *            the parent tag
     * @return a copy of the attribute Element, enriched with the "inheritedFrom" attribute to track origin
     */
    public static Element getReferencedAttributes(HashMap<String, Element> processedTags, final String errorMsg,
            String tagName, Element attributeTagContent, boolean addOrigin) {
        String specifiedIn = attributeTagContent.attributeValue("specifiedIn");
        String attribute = attributeTagContent.attributeValue("name");
        Element copyFrom = processedTags.get(specifiedIn);
        if (copyFrom == null) {
            System.out.println(errorMsg + "tag '" + tagName
                    + "' specifies an invalid source tag to copy attributes from: '" + specifiedIn
                    + "'. Make sure that the source tag exists and is defined BEFORE the tag copying!");
            System.exit(-1);
        }
        Element element = getAttributeFromTag(copyFrom, attribute);
        if (element == null) {
            System.out.println(errorMsg + "tag '" + tagName + "' specifies an not-existing source attribute '"
                    + attribute + "' to copy from tag '" + specifiedIn + "'!");
            System.exit(-1);
        }

        if (addOrigin) {
            // enrich the element so it knows where it was copied from
            element.addAttribute("inheritedFrom", specifiedIn);
        }

        return element;
    }

    private static Element getAttributeFromTag(Element tag, String attribute) {
        for (Element tagContent : getElementList(tag)) {
            if (tagContent.getName().equals("attribute")) {
                if (tagContent.element("name") != null && tagContent.element("name").getText().equals(attribute)) {
                    return (Element) tagContent.clone();
                }
            }
        }
        return null;
    }

    public static String readFileAsString(String filePath) throws java.io.IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
        f.read(buffer);
        return new String(buffer);
    }

}
