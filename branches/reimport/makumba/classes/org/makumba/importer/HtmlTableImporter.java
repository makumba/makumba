package org.makumba.importer;
import org.makumba.*;
import org.makumba.abstr.*;
import org.makumba.util.HtmlTagEnumerator;
import java.io.*;
import java.util.*;
import java.net.URL;

public class HtmlTableImporter 
{
  RecordImporter imp;
  boolean inRow=false;
  boolean inCell=false;
  String text;
  Vector data;
  String fieldOrder[];
  String type;
  Database db;

  void endOfCell()
  {
    if(inCell)
      data.addElement(text);
    text=null;
  }

  void endOfRow()
  {
    endOfCell();
    if(data!=null && ! data.isEmpty())
      if(data.size()!=fieldOrder.length)
	MakumbaSystem.getMakumbaLogger("import").severe(type+": invalid HTML table row length: "+data.size()+"\r\nin: "+data);
      else
	  try {
	db.insert(type, importVector());
	  } catch (InvalidValueException e)
	      {MakumbaSystem.getMakumbaLogger("import").warning("record not inserted --> "+e.getMessage());}
  }

  public HtmlTableImporter(Database db, RecordInfo type, Reader r, String tableStartTag, String[] fieldOrder)
       throws IOException
  {
    this.imp= new RecordImporter(type, true);
    this.fieldOrder=fieldOrder;
    this.type= type.getName();
    this.db=db;
    String[] tables= {type.getName()};
    MakumbaSystem._delete(db.getName(), db.getName(), tables); 
    HtmlTagEnumerator e= new HtmlTagEnumerator(r);
    while(e.next() && !e.getTag().equals(tableStartTag))
      ;
  
    String s;

    while(e.next())
      {
	if(e.getTagType().toLowerCase().equals("tr"))
	  {
	    endOfRow();
	    inRow=true;
	    inCell=false;
	    data=new Vector();
	  }
	else if(inRow && e.getTagType().toLowerCase().equals("td"))
	  {
	    endOfCell();
	    inCell=true;
	  }
	else if(inCell && (s=e.getNonHtml())!=null &&s.length()>0)
	  text=s;
	if(e.getTagType().toLowerCase().equals("/table"))
	  {
	    endOfRow();
	    MakumbaSystem.getMakumbaLogger("import").severe("end of table encountered");
	    return;
	  }
      }
   MakumbaSystem.getMakumbaLogger("import").severe("end of table missing");
  }
  
  Dictionary importVector()
  {
    Dictionary d= new Hashtable();
    Vector v1= new Vector();

    for(int i=0; i<fieldOrder.length; i++)
      if(data.elementAt(i)!=null)
	{
	  Object o= imp.getValue(fieldOrder[i], (String)data.elementAt(i), db, null);
	  if(o!=null)
	    d.put(fieldOrder[i], o);
	  v1.addElement(o);
	}
    MakumbaSystem.getMakumbaLogger("import").finest(v1.toString());

    return d;
  }

  public static void main(String[] argv) throws IOException
  {
    String[]args= new String[argv.length-4];
    System.arraycopy(argv, 4, args, 0, args.length);

    new HtmlTableImporter(
			  MakumbaSystem.getDatabase(argv[0]),
			  RecordInfo.getRecordInfo(argv[1]),
			  new BufferedReader(new InputStreamReader(new FileInputStream(argv[2]))),
			  argv[3],
			  args);
  }
}
