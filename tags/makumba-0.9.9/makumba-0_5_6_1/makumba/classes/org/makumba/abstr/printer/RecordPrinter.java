package org.makumba.abstr.printer;
import org.makumba.abstr.*;

/** This class just prepares a more complex toString. The toString is still
  (like in the generic RecordHandler) a concatenation of the FieldHandlers toString, but those FieldHandlers are different in this handler family. The FieldHandlers are built according to rules in org.makumba/abstr/printer/redirectPrinter.properties :
<pre>
ptrIndex=Field
dateCreate=Field
dateModify=Field
date=Field
text=Field
int=Field

#not needed, but anyway
char= char          

#not needed, but anyway
charEnum= charEnum

#not needed, but anyway
intEnum= intEnum

#not needed, but anyway
ptr=ptr

#not needed, but anyway
ptrRel=ptrRel

ptrOne=subtable
set=subtable
setcharEnum=subtable
setintEnum=subtable
setComplex=subtable
</pre>
* @see org.makumba.abstr.printer.FieldPrinter
* @see org.makumba.abstr.printer.charPrinter
* @see org.makumba.abstr.printer.charEnumPrinter
* @see org.makumba.abstr.printer.intEnumPrinter
* @see org.makumba.abstr.printer.ptrPrinter
* @see org.makumba.abstr.printer.ptrRelPrinter
* @see org.makumba.abstr.printer.subtablePrinter
 */
public class RecordPrinter extends org.makumba.abstr.RecordHandler
{
  String separator= super.toStringSeparator();
    
  public RecordPrinter(String path)  
  {
    this(RecordInfo.getRecordInfo(path));
  }

  public RecordPrinter(RecordInfo ri)
  {
    super(ri);
    separator= System.getProperty("line.separator")+ri.fieldPrefix();
  }

  protected String toStringBefore() 
  { return getRecordInfo().getName()+" title: "+getRecordInfo().getTitleField()+separator; }
  
  protected String toStringSeparator() { return separator; }
  
  public static void main(String [] argv) 
  {
    try{
      if(argv.length==0)
	return;
      System.out.println(new RecordPrinter(argv[0]));  
    }
    catch(org.makumba.MakumbaError e){System.err.println(e.getMessage()); }
    
  }
}
