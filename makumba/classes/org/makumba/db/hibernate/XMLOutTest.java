package org.makumba.db.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XMLOutTest {
	private List mddsDone = new ArrayList();

	public XMLOutTest(DataDefinition dd) throws TransformerConfigurationException, SAXException {
		generateMappingFile(dd, null);
	}
	
	private String getBaseName(String name) {
		int dot;
		dot = name.lastIndexOf(".");
		if (dot != -1)
			return name.substring(dot+1);
		return name;
	}
	private String getArrowBaseName(String name) {
		int dot;
		dot = name.lastIndexOf("->");
		if (dot != -1)
			return name.substring(0, dot);
		return name;
	}
	private String dotToUnderscore(String name) {
		return name.replaceAll("\\.", "_");
	}
	private String arrowToDot(String name) {
		return name.replaceAll("->", ".");
	}
	private String arrowToDoubleDot(String name) {
		return name.replaceAll("->", "..");
	}
	
	public void generateMappingFile(DataDefinition dd, DataDefinition subtable) throws TransformerConfigurationException, SAXException { 
		if (!mddsDone.contains(dd.getName())) {

			mddsDone.add(dd.getName());
			
			String filename = arrowToDot(dd.getName()) + ".hbm.xml";
			HibernateTest.allmdds.add(filename);
			StreamResult streamResult = new StreamResult("/home/jpeeters/eclipse/antwerpen/makumba/classes/" + HibernateTest.packageprefix + "/" + filename);
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
			atts.addAttribute("","","name","", HibernateTest.packageprefix + "." + arrowToDot(dd.getName()));
			atts.addAttribute("","","table","", dotToUnderscore(arrowToDoubleDot(dd.getName())) + "_");
			hd.startElement("", "", "class", atts);
			
			addMapping(hd, dd);
			/* do we need to add a mapping to the parent field? */
			if (subtable != null) {
				atts.clear();
				atts.addAttribute("", "", "name", "", getBaseName(getArrowBaseName(dd.getName())).toLowerCase());
				atts.addAttribute("", "", "column", "", getBaseName(getArrowBaseName(dd.getName()) + "_"));
				atts.addAttribute("", "", "class", "", HibernateTest.packageprefix + "." + arrowToDot(getArrowBaseName(dd.getName())));
				hd.startElement("", "", "many-to-one", atts);
				hd.endElement("", "", "many-to-one");
			}
			
			hd.endElement("", "", "class");
			hd.endElement("", "", "hibernate-mapping");
			hd.endDocument();
		}
	}
	
	public void addMapping(TransformerHandler hd, DataDefinition dd) throws SAXException, TransformerConfigurationException {
		Vector test = dd.getFieldNames();
		for (int i = 0; i < dd.getFieldNames().size(); i++) {
			AttributesImpl atts = new AttributesImpl();
			FieldDefinition fd = dd.getFieldDefinition(i);
			atts.clear();
//			System.out.println(dd.getFieldDefinition(i).getName() + " : " + dd.getFieldDefinition(i).getType());
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
					generateMappingFile(fd.getPointedType(), null);
					break;
				case FieldDefinition._ptrOne:
					atts.addAttribute("", "", "name", "", fd.getName());
					atts.addAttribute("", "", "column", "", fd.getName()+"_");
					atts.addAttribute("", "", "cascade", "", "all");
					atts.addAttribute("", "", "unique", "", "true");
					hd.startElement("", "", "many-to-one", atts);
					hd.endElement("","","many-to-one");
					generateMappingFile(fd.getPointedType(), null);
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
					atts.addAttribute("", "", "table", "", dd.getName() + "__" + fd.getName() + "_");
					atts.addAttribute("", "", "cascade", "", "all-delete-orphan");
					hd.startElement("", "", "bag", atts);
					atts.clear();
					atts.addAttribute("", "", "column", "", dd.getIndexPointerFieldName()+"_");
					hd.startElement("", "", "key", atts);
					hd.endElement("","","key");
					atts.clear();
					atts.addAttribute("", "", "class", "", HibernateTest.packageprefix + "." + fd.getPointedType().getName());
					atts.addAttribute("", "", "column", "", fd.getPointedType().getIndexPointerFieldName()+"_");
					hd.startElement("", "", "many-to-many", atts);
					hd.endElement("","","many-to-many");
					hd.endElement("","","bag");
					generateMappingFile(fd.getPointedType(), null);
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
					atts.addAttribute("", "", "class", "", HibernateTest.packageprefix + "." + arrowToDot(fd.getPointedType().getName()));
					hd.startElement("", "", "one-to-many", atts);
					hd.endElement("","","one-to-many");
					hd.endElement("","","bag");			
					generateMappingFile(fd.getSubtable(), fd.getSubtable());
					break;
				case FieldDefinition._ptrRel:
					break;
				default:
					try {
						throw new Exception("Unmapped type: " + fd.getName() + "-" + fd.getType());
					} catch (Exception e) {
						e.printStackTrace();
					}
			}				
		}
	}
}
