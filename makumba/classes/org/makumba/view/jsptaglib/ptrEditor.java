package org.makumba.view.jsptaglib;
import org.makumba.*;
import java.util.*;

public class ptrEditor extends choiceEditor
{
  String db;
  String query;

  public void onStartup(RecordEditor re)
  {
    db=re.database;
    query= "SELECT choice as choice, choice."+getTitleField()+ " as title FROM "+getPointedType().getName()+" choice ORDER BY title";
  }

  public Object getOptions()
  {
    Database dbc= MakumbaSystem.getConnectionTo(db);
    try{
      return dbc.executeQuery(query, null); 
    }finally{dbc.close(); }
  }

  public int getOptionsLength(Object opts){ return ((Vector)opts).size(); }

  public Object getOptionValue(Object options, int i)
  { return ((Dictionary)((Vector)options).elementAt(i)).get("choice"); }

  public String formatOptionValue(Object opts, int i, Object val)
  { return ((Pointer)val).toExternalForm(); }
  
  public String formatOptionTitle(Object options, int i)
  { return ""+((Dictionary)((Vector)options).elementAt(i)).get("title"); }

  public String getMultiple() { return ""; }

  public int getDefaultSize() { return 1; }
}
