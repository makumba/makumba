package org.makumba.view.jsptaglib;
import org.makumba.abstr.*;
import org.makumba.*;
import org.makumba.util.*;
import org.makumba.view.*;

import javax.servlet.jsp.*;
import java.util.*;
import java.io.*;
 
public abstract class FormResponder implements java.io.Serializable
{
  int identity;
  String database;
  Object controller;
  
  RecordInfo dd= new RecordInfo();
  int max=0;

  RecordEditor editor;
  
  String subjectLabel;
  String message;
  
  Hashtable fieldParameters= new Hashtable();
  Hashtable fieldNames= new Hashtable();
  
  String operation;
  String type;
  String pointerType;
  String formKey;
  boolean simpleForm;
  String handler; 

  public void init(FormTagBase ftb) throws LogicException
  {
    controller=HttpAttributes.getAttributes(ftb.getPageContext()).controller;
    database=ftb.getDatabaseName();
    operation=ftb.getClass().getName();
    message=ftb.getMessage();
    subjectLabel=ftb.getSubjectLabel();
    formKey=ftb.getFormKey();
    handler=ftb.handler;
  }

  public final static String responderName="__makumba__responder__";
  public final static String basePointerName="__makumba__base__";


  public void writeInput(JspWriter pw, String type) throws IOException
  {
    pw.print("<input type=\"hidden\" name=\""+responderName+"\" value=\""+getIdentity(type)+"\">");
  }
  
  Hashtable indexes=new Hashtable();

  public String format(String fname, Object ftype, Object fval, Dictionary formatParams)
  {
    Integer i=(Integer)indexes.get(fname);
    if(i!=null)
      return editor.format(i.intValue(), fval, formatParams);

    indexes.put(fname, new Integer(max));
    String colName=("col"+max);
    fieldNames.put(colName, fname);
    fieldParameters.put(colName, (Dictionary)((Hashtable)formatParams).clone());
    dd.addField(FieldInfo.getFieldInfo(colName, ftype, true));
    editor= new RecordEditor(dd, fieldNames);
    editor.database=database;
    editor.config();
    return editor.format(max++, fval, formatParams);
  }

  public String responderKey()
  { 
    return operation+formKey+fieldNames+fieldParameters+subjectLabel+database;
  }

  static int cache= NamedResources.makeStaticCache
      ("Http controller form responders",
   new NamedResourceFactory()
   {
     {
       supplementary= new Hashtable();
     }
     public Object getHashObject(Object o)
       {
	 return ((FormResponder)o).responderKey();
       }

     public Object makeResource(Object name, Object hashName)
       {
	 FormResponder f= (FormResponder)name;
	 f.identity= hashName.hashCode();
	 ((Hashtable)supplementary).put(new Integer(f.identity), name);
	 return name;
       }
   });

  public int getIdentity(String type) 
  {
    if(type==null && !simpleForm)
      return -1;
    this.type=type;
    return getSimpleIdentity();
  }

  public int getSimpleIdentity() { return ((FormResponder)NamedResources.getStaticCache(cache).getResource(this)).identity; }

  public static Integer responseId(PageContext pc)
  {
    Object o= (String)HttpAttributes.getParameters(pc).getParameter(responderName);
    if(o==null)
      return null;
    if(!(o instanceof String))
      throw new RuntimeException("Multiple responses??? "+o);
    return new Integer(Integer.parseInt((String)o));
  }

  public static FormResponder getFormResponder(Integer i)
  {
    FormResponder fr= ((FormResponder)((Hashtable)NamedResources.getStaticCache(cache).getSupplementary()).get(i));
    if(fr==null)
      throw new org.makumba.InvalidValueException("Form responder cannot be found, probably due to server restart. Please reload the form page.");
    return fr;
  }

  public Attributes makeAttributes(PageContext pc) throws LogicException
  {
    return new HttpAttributes(controller, pc, database);
  }

  public Dictionary getHttpData(PageContext pc)
  {
    if(editor!=null)
      return editor.readFrom(pc);
    else
      return new Hashtable(1);
  }

  public void writeBasePointer(JspWriter pw, String s) throws IOException
  {
    pw.print("<input type=\"hidden\" name=\""+basePointerName+"\" value=\""+s+"\">");
  }

  public Pointer getHttpBasePointer(PageContext pc)
  {
    return new Pointer(pointerType, (String)HttpAttributes.getParameters(pc).getParameter(basePointerName));
  }

  public abstract Object respondTo(PageContext pc) throws LogicException; 

  public String getSubjectLabel() { return subjectLabel; }
  public String getMessage() { return message; }

}

