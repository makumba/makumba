package org.makumba.abstr.translator;
import org.makumba.abstr.*;

/** This class translates a Data Definition to a new language (such as a new version of the Data Definition)
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
  { return ""; }

  protected String toStringSeparator() { return ""; }

  public static void main(String [] argv)
  {
    if(argv.length==0)
      return;
    System.out.println(new RecordPrinter(argv[0]));
  }
}
