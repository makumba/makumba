package org.makumba.db.hibernate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.cfg.Configuration;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class MddToMapping extends HibernateUtils {
    public static final String generatedMappingPath="generated";
	private List mddsDone = new ArrayList();
	private LinkedList mddsToDo = new LinkedList();
	
	public MddToMapping(DataDefinition dd, Configuration cfg) throws TransformerConfigurationException, SAXException {
		generateMapping(dd, cfg);
		
		/* generate the mappings for the related mdd files */
		while (!mddsToDo.isEmpty()) {
			generateMapping((DataDefinition)mddsToDo.removeFirst(), cfg);	
		}
	}

	/**
	 * Creates an xml file for the given DataDefinition
	 * @param dd DataDefinition that needs to be mapped   
	 **/
	public void generateMapping(DataDefinition dd, Configuration cfg) throws TransformerConfigurationException, SAXException { 
		if (mddsDone.contains(dd.getName())) 
            return;
			
			mddsDone.add(dd.getName());
			
			String filename = arrowToDot(dd.getName()) + ".hbm.xml";
			StreamResult streamResult = new StreamResult(generatedMappingPath+"/" + filename);
			SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
			// SAX2.0 ContentHandler
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//Hibernate/Hibernate Mapping DTD 3.0//EN");
			serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			hd.startDocument();
			
			AttributesImpl atts = new AttributesImpl();
			
			/* hibernate mapping */
			hd.startElement("", "", "hibernate-mapping", atts);
			
			/* class definition */
			atts.clear();
			atts.addAttribute("","","name","", arrowToDot(dd.getName()));
			atts.addAttribute("","","table","", dotToUnderscore(arrowToDoubleDot(dd.getName())) + "_");
			hd.startElement("", "", "class", atts);
			
			for (int i = 0; i < dd.getFieldNames().size(); i++) {
				FieldDefinition fd = dd.getFieldDefinition(i);
				atts.clear();
				System.out.println(dd.getFieldDefinition(i).getName() + " : " + dd.getFieldDefinition(i).getType());
				switch (fd.getIntegerType()) {
					case FieldDefinition._int:
					case FieldDefinition._real:
					case FieldDefinition._charEnum:				
					case FieldDefinition._intEnum:
					case FieldDefinition._dateModify:
					case FieldDefinition._dateCreate:
					case FieldDefinition._date:
						atts.addAttribute("", "", "name", "", fd.getName());
						atts.addAttribute("", "", "column", "", fd.getName()+"_");
						hd.startElement("", "", "property", atts);
						hd.endElement("","","property");
						break;
					case FieldDefinition._char:
						atts.addAttribute("", "", "name", "", fd.getName());
						hd.startElement("", "", "property", atts);
						atts.clear();
						atts.addAttribute("", "", "name", "", fd.getName()+"_");
						atts.addAttribute("", "", "length", "", String.valueOf(fd.getWidth()));
						hd.startElement("", "", "column", atts);
						hd.endElement("","","column");
						hd.endElement("","","property");
						break;
					case FieldDefinition._ptr:
						atts.addAttribute("", "", "name", "", fd.getName());
						atts.addAttribute("", "", "column", "", fd.getName()+"_");
						atts.addAttribute("", "", "cascade", "", "all");
						hd.startElement("", "", "many-to-one", atts);
						hd.endElement("","","many-to-one");
						atts.clear();
						atts.addAttribute("", "", "name", "", "hibernate_" + fd.getName());
						atts.addAttribute("", "", "type", "", "org.makumba.db.hibernate.customtypes.PointerUserType");
						atts.addAttribute("", "", "column", "", fd.getName()+"_");
						atts.addAttribute("", "", "insert", "", "false");
						atts.addAttribute("", "", "update", "", "false");
						atts.addAttribute("", "", "access", "", "org.makumba.db.hibernate.propertyaccess.HibernatePrimaryKey");
						hd.startElement("", "", "property", atts);
						hd.endElement("","","property");
						mddsToDo.add(fd.getPointedType());
						break;
					case FieldDefinition._ptrOne:
						atts.addAttribute("", "", "name", "", fd.getName());
						atts.addAttribute("", "", "column", "", fd.getName()+"_");
						atts.addAttribute("", "", "cascade", "", "all");
						atts.addAttribute("", "", "unique", "", "true");
						hd.startElement("", "", "many-to-one", atts);
						hd.endElement("","","many-to-one");
						mddsToDo.add(fd.getPointedType());
						break;
					case FieldDefinition._ptrIndex:
						atts.addAttribute("", "", "name", "", "id");
						atts.addAttribute("", "", "column", "", fd.getName()+"_");
						hd.startElement("", "", "id", atts);
						atts.clear();
						atts.addAttribute("", "", "class", "", "increment");
						hd.startElement("", "", "generator", atts);
						hd.endElement("","","generator");
						hd.endElement("","","id");
						break;
					case FieldDefinition._text:
						atts.addAttribute("", "", "name", "", fd.getName());
						atts.addAttribute("", "", "type", "", "org.makumba.db.hibernate.customtypes.TextUserType");
						hd.startElement("", "", "property", atts);
						atts.clear();
						atts.addAttribute("", "", "name", "", fd.getName()+"_");
						atts.addAttribute("", "", "sql-type", "", "longblob");
						hd.startElement("", "", "column", atts);
						hd.endElement("","","column");
						hd.endElement("","","property");
						break;
					case FieldDefinition._set:
						atts.addAttribute("", "", "name", "", fd.getName());
						atts.addAttribute("", "", "table", "", dd.getName().replace(".","_").replace("->", "__") + "__" + fd.getName() + "_");
						atts.addAttribute("", "", "cascade", "", "all-delete-orphan");
						hd.startElement("", "", "bag", atts);
						atts.clear();
						atts.addAttribute("", "", "column", "", dd.getIndexPointerFieldName()+"_");
						hd.startElement("", "", "key", atts);
						hd.endElement("","","key");
						atts.clear();
						atts.addAttribute("", "", "class", "", fd.getPointedType().getName());
						atts.addAttribute("", "", "column", "", fd.getPointedType().getIndexPointerFieldName()+"_");
						hd.startElement("", "", "many-to-many", atts);
						hd.endElement("","","many-to-many");
						hd.endElement("","","bag");
						mddsToDo.add(fd.getPointedType());
						break;
					case FieldDefinition._setComplex:
					case FieldDefinition._setCharEnum:
					case FieldDefinition._setIntEnum:
						atts.addAttribute("", "", "name", "", fd.getName());
						atts.addAttribute("", "", "inverse", "", "true");
						atts.addAttribute("", "", "cascade", "", "none");
						hd.startElement("", "", "bag", atts);
						atts.clear();
						atts.addAttribute("", "", "column", "", dd.getIndexPointerFieldName()+"_");
						hd.startElement("", "", "key", atts);
						hd.endElement("","","key");
						atts.clear();
						atts.addAttribute("", "", "class", "", arrowToDot(fd.getPointedType().getName()));
						hd.startElement("", "", "one-to-many", atts);
						hd.endElement("","","one-to-many");
						hd.endElement("","","bag");
						mddsToDo.add(fd.getSubtable());
						break;
					case FieldDefinition._ptrRel:
						/* do we need to add a mapping to the parent field? */
						atts.clear();
						atts.addAttribute("", "", "name", "", getBaseName(getArrowBaseName(dd.getName())).toLowerCase());
						atts.addAttribute("", "", "column", "", getBaseName(getArrowBaseName(dd.getName()) + "_"));
						atts.addAttribute("", "", "class", "", arrowToDot(getArrowBaseName(dd.getName())));
						hd.startElement("", "", "many-to-one", atts);
						hd.endElement("", "", "many-to-one");
						break;
					default:
						try {
							throw new Exception("Unmapped type: " + fd.getName() + "-" + fd.getType());
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
			hd.endElement("", "", "class");
			hd.endElement("", "", "hibernate-mapping");
			hd.endDocument();
            cfg.addResource(filename);	
	}
}
