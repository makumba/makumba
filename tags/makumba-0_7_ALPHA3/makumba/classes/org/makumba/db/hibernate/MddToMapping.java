package org.makumba.db.hibernate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.cfg.Configuration;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.commons.NameResolver;
import org.makumba.providers.DataDefinitionProvider;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class MddToMapping {
    private Set<String> mddsDone = new HashSet<String>();

    private LinkedList<DataDefinition> mddsToDo = new LinkedList<DataDefinition>();

    private String generatedMappingPath = "";

    private String prefix = "";
    
    private DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    private NameResolver nr;

    public MddToMapping(Vector v, Configuration cfg, String generationPath, String prefix, NameResolver nr)
            throws TransformerConfigurationException, SAXException {
        managePaths(generationPath, prefix);
        
        this.nr = nr;
        
        for (int i = 0; i < v.size(); i++)
            generateMapping(ddp.getDataDefinition((String) v.elementAt(i)), cfg);
        while (!mddsToDo.isEmpty())
            generateMapping(mddsToDo.removeFirst(), cfg);
    }

    public MddToMapping(DataDefinition dd, Configuration cfg, String generationPath, String prefix)
            throws TransformerConfigurationException, SAXException {
        // TODO: generate only if file doesn't exist already
        managePaths(generationPath, prefix);
        this.generatedMappingPath = generationPath;
        generateMapping(dd, cfg);

        /* generate the mappings for the related mdd files */
        while (!mddsToDo.isEmpty()) {
            generateMapping(mddsToDo.removeFirst(), cfg);
        }
    }

    private void managePaths(String generationPath, String prefix) {
        this.generatedMappingPath = generationPath + File.separator + prefix;
        this.prefix = prefix;
        new File(generatedMappingPath).mkdirs();
    }

    SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

    /**
     * Creates an xml file for the given DataDefinition and adds it to the configuration resource
     * 
     * @param dd
     *            DataDefinition that needs to be mapped
     * @param cfg
     *            Configuration in which it will be used
     */
    public void generateMapping(DataDefinition dd, Configuration cfg) throws TransformerConfigurationException,
            SAXException {
        if (mddsDone.contains(dd.getName()))
            return;
        mddsDone.add(dd.getName());

        takenColumnNames = new HashSet();
        columnNames = new HashMap();
        String filename = nr.arrowToDoubleUnderscore(dd.getName()) + ".hbm.xml";
        
        /*
        //checks if the MDD has to be generated
        File checkFile = new File(generatedMappingPath + File.separator + filename);
        File mddFile = new File(((RecordInfo) dd).getOrigin().getFile());
        
        if(checkFile.exists()) {
            cfg.addResource(prefix + File.separator + filename);
            return;
        }
        */
            
        
        Writer w = null;
        try {
            w = new FileWriter(generatedMappingPath + File.separator + filename);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        StreamResult streamResult = new StreamResult(w);

        // SAX2.0 ContentHandler
        TransformerHandler hd = tf.newTransformerHandler();
        Transformer serializer = hd.getTransformer();
        serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//Hibernate/Hibernate Mapping DTD 3.0//EN");
        serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");

        hd.setResult(streamResult);
        hd.startDocument();

        AttributesImpl atts = new AttributesImpl();

        /* hibernate mapping */
        // no auto-import to allow for classes with same name in different packages
        atts.addAttribute("", "", "auto-import", "", "false");
        hd.startElement("", "", "hibernate-mapping", atts);

        /* class definition */
        atts.clear();
        atts.addAttribute("", "", "name", "", nr.arrowToDoubleUnderscore(dd.getName()));
        // TODO: might actually work without toLowerCase()
        atts.addAttribute("", "", "table", "", nr.resolveTypeName(dd));
        hd.startElement("", "", "class", atts);

        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldDefinition fd = dd.getFieldDefinition(i);
            atts.clear();
            switch (fd.getIntegerType()) {
            case FieldDefinition._int:
            case FieldDefinition._real:
            case FieldDefinition._charEnum:
            case FieldDefinition._intEnum:
            case FieldDefinition._dateModify:
            case FieldDefinition._dateCreate:
            case FieldDefinition._date:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                atts.addAttribute("", "", "column", "", columnName(dd, fd.getName()));
                hd.startElement("", "", "property", atts);
                hd.endElement("", "", "property");
                break;
            case FieldDefinition._char:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                hd.startElement("", "", "property", atts);
                atts.clear();
                atts.addAttribute("", "", "name", "", columnName(dd, fd.getName()));
                atts.addAttribute("", "", "length", "", String.valueOf(fd.getWidth()));
                hd.startElement("", "", "column", atts);
                hd.endElement("", "", "column");
                hd.endElement("", "", "property");
                break;
            case FieldDefinition._ptr:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                atts.addAttribute("", "", "column", "", columnName(dd, fd.getName()));
                atts.addAttribute("", "", "cascade", "", "all");
                atts.addAttribute("", "", "class", "", nr.arrowToDoubleUnderscore(fd.getPointedType().getName()));
                hd.startElement("", "", "many-to-one", atts);
                hd.endElement("", "", "many-to-one");
                mddsToDo.add(fd.getPointedType());
                break;
            case FieldDefinition._ptrOne:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                atts.addAttribute("", "", "column", "", columnName(dd, fd.getName()));
                atts.addAttribute("", "", "cascade", "", "all");
                atts.addAttribute("", "", "unique", "", "true");
                hd.startElement("", "", "many-to-one", atts);
                hd.endElement("", "", "many-to-one");
                mddsToDo.add(fd.getPointedType());
                break;
            case FieldDefinition._ptrIndex:
                atts.addAttribute("", "", "name", "", "primaryKey");
                atts.addAttribute("", "", "column", "", nr.resolveFieldName(dd, fd.getName()));
                hd.startElement("", "", "id", atts);
                atts.clear();
                atts.addAttribute("", "", "class", "", "identity");
                hd.startElement("", "", "generator", atts);
                hd.endElement("", "", "generator");
                hd.endElement("", "", "id");
                break;
            case FieldDefinition._text:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                atts.addAttribute("", "", "type", "", "org.makumba.db.hibernate.TextUserType");
                hd.startElement("", "", "property", atts);
                atts.clear();
                atts.addAttribute("", "", "name", "", columnName(dd, fd.getName()));
                atts.addAttribute("", "", "sql-type", "", "longtext");
                hd.startElement("", "", "column", atts);
                hd.endElement("", "", "column");
                hd.endElement("", "", "property");
                break;
            case FieldDefinition._binary:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                atts.addAttribute("", "", "type", "", "org.makumba.db.hibernate.TextUserType");
                hd.startElement("", "", "property", atts);
                atts.clear();
                atts.addAttribute("", "", "name", "", columnName(dd, fd.getName()));
                atts.addAttribute("", "", "sql-type", "", "longblob");
                hd.startElement("", "", "column", atts);
                hd.endElement("", "", "column");
                hd.endElement("", "", "property");
                break;
            case FieldDefinition._boolean:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                hd.startElement("", "", "property", atts);
                atts.clear();
                atts.addAttribute("", "", "name", "", columnName(dd, fd.getName()));
                hd.startElement("", "", "column", atts);
                hd.endElement("", "", "column");
                hd.endElement("", "", "property");
                break;
            case FieldDefinition._set:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                atts.addAttribute("", "", "table", "", nr.resolveTypeName(fd.getSubtable()));
                atts.addAttribute("", "", "cascade", "", "all"); //""
                atts.addAttribute("", "", "inverse", "", "false"); //"all-delete-orphan"
                hd.startElement("", "", "bag", atts);
                atts.clear();
                atts.addAttribute("", "", "column", "", columnName(dd, dd.getIndexPointerFieldName()));
                hd.startElement("", "", "key", atts);
                hd.endElement("", "", "key");
                atts.clear();
                atts.addAttribute("", "", "class", "", nr.arrowToDoubleUnderscore(fd.getPointedType().getName()));

                // TODO: "formula" works around hibernate bug 572
                // http://opensource2.atlassian.com/projects/hibernate/browse/HHH-572
                DataDefinition set= fd.getSubtable();
                atts.addAttribute("", "", "column", "", columnName(set, set.getSetMemberFieldName()));
                hd.startElement("", "", "many-to-many", atts);
                hd.endElement("", "", "many-to-many");
                hd.endElement("", "", "bag");
                mddsToDo.add(fd.getPointedType());
                mddsToDo.add(set);
                break;
            case FieldDefinition._setComplex:
            case FieldDefinition._setCharEnum:
            case FieldDefinition._setIntEnum:
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                atts.addAttribute("", "", "inverse", "", "true");
                atts.addAttribute("", "", "cascade", "", "none");
                hd.startElement("", "", "bag", atts);
                atts.clear();
                atts.addAttribute("", "", "column", "", columnName(dd, dd.getIndexPointerFieldName()));
                hd.startElement("", "", "key", atts);
                hd.endElement("", "", "key");
                atts.clear();
                atts.addAttribute("", "", "class", "", nr.arrowToDoubleUnderscore(fd.getPointedType().getName()));
                hd.startElement("", "", "one-to-many", atts);
                hd.endElement("", "", "one-to-many");
                hd.endElement("", "", "bag");
                mddsToDo.add(fd.getSubtable());
                break;

            /* ptrRel is the pointer type used in case of sets (ie pointing to two tables) */
            case FieldDefinition._ptrRel:
                /* do we need to add a mapping to the parent field? */
                atts.clear();
                atts.addAttribute("", "", "name", "", nr.checkReserved(fd.getName()));
                atts.addAttribute("", "", "column", "", columnName(dd, fd.getName()));
                atts.addAttribute("", "", "class", "", nr.arrowToDoubleUnderscore(fd.getPointedType().getName()));
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

        try {
            w.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cfg.addResource(prefix + File.separator + filename);
    }

    Set takenColumnNames;

    Map columnNames;

    private String columnName(DataDefinition dd, String name) {
        
        /*
        String cn = (String) columnNames.get(name);
        if (cn != null)
            return cn;

        cn = name + "_";
        while (takenColumnNames.contains(cn.toLowerCase()))
            cn += "_";
        takenColumnNames.add(cn.toLowerCase());
        columnNames.put(name, cn);
        return arrowToDoubleUnderscore(cn);
        
        */
        
        return nr.resolveFieldName(dd, name);
    }
}
