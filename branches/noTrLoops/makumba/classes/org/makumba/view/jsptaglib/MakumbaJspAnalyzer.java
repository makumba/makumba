package org.makumba.view.jsptaglib;
import org.makumba.util.JspParseData;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

// this should go away when the cache becomes per-page and not example-based
import javax.servlet.jsp.PageContext;
import org.makumba.util.RuntimeWrappedException;
import org.makumba.LogicException;

public class MakumbaJspAnalyzer implements JspParseData.JspAnalyzer
{
  static String[] tags= {
    "value", "org.makumba.view.jsptaglib.ValueTag"
    ,"list", "org.makumba.view.jsptaglib.QueryTag"
    ,"object", "org.makumba.view.jsptaglib.ObjectTag"
    ,"form", "org.makumba.view.jsptaglib.FormTagBase"
    ,"newForm", "org.makumba.view.jsptaglib.NewTag"
    ,"addForm", "org.makumba.view.jsptaglib.AddTag"
    ,"editForm", "org.makumba.view.jsptaglib.EditTag"
    ,"deleteLink", "org.makumba.view.jsptaglib.DeleteTag"
    ,"input", "org.makumba.view.jsptaglib.InputTag"
    ,"action", "org.makumba.view.jsptaglib.ActionTag"
  };
  
  static final Map tagClasses= new HashMap();
    //   checkInclude(td.name);

  static
  {
    for(int i=0; i<tags.length; i+=2)
      try
      {
	tagClasses.put(tags[i], Class.forName(tags[i+1]));
      }catch(Throwable t){ t.printStackTrace(); }
  }

  class ParseStatus
  {
    String makumbaPrefix;
    Map tags= new HashMap();
    List parents= new ArrayList();
    
    // should go away:
    PageContext pageContext;

    void addTag(MakumbaTag t)
    {
      // should go away:
      t.setPageContext(pageContext);
      
      Object key;
      if(!parents.isEmpty())
	t.setParent((MakumbaTag)parents.get(parents.size()-1));
      else
	t.setParent(null);
      
      if(t.getMakumbaParent()!=null)
	{
	  key=t.getRegistrationKey();
	  if(key!=null)
	    tags.put(key, t);
	}
      else
	{
	  key=t.getRootRegistrationKey();
	  if(key!=null)
	    tags.put(key, t);
	}

      try{
	if(key!=null)
	  t.getRootData().setStrategy(key, t);
	else
	  t.strategy= t.makeStrategy(null);
	t.setPage(pageContext);
	t.strategy.doAnalyze();
      }catch(Exception e)
	{ throw new RuntimeWrappedException(e); }

      if(t instanceof MakumbaBodyTag)
	parents.add(t);
    }

    public void end(String tagName)
    {
      if(!tagName.startsWith(makumbaPrefix))
	return;
      tagName= tagName.substring(makumbaPrefix.length()+1);
      MakumbaTag t= (MakumbaTag)parents.get(parents.size()-1);
      if(!t.getClass().equals(tagClasses.get(tagName)))
	throw new RuntimeException("tag closed incorrectly: "+tagName+" should have got "+
				   tagClasses.get(tagName)+" and it was "+t.getClass());
      parents.remove(parents.size()-1);
    }
  }


  private MakumbaJspAnalyzer(){}
  public static final JspParseData.JspAnalyzer singleton= new MakumbaJspAnalyzer();

  public void systemTag(JspParseData.TagData td, Object status)
  {
    // FIXME: should treat @include here...
    if(!td.name.equals("taglib") ||
       !td.attributes.get("uri").equals("http://www.makumba.org/presentation"))
      return;
    ((ParseStatus)status).makumbaPrefix= (String)td.attributes.get("prefix");
  }
 
  public void simpleTag(JspParseData.TagData td, Object status)
  {
    String prefix= ((ParseStatus)status).makumbaPrefix+":";
    if(!td.name.startsWith(prefix))
      return;
    Class c= (Class)tagClasses.get(td.name.substring(prefix.length()));
    if(c==null)
      return;
    
    MakumbaTag t=null;
    try{ t= (MakumbaTag)c.newInstance();}catch(Throwable thr){ thr.printStackTrace(); }
    JspParseData.fill(t, td.attributes);
    td.tagObject=t;
    t.template=true;
    ((ParseStatus)status).addTag(t);
  }

  public void startTag(JspParseData.TagData td, Object status)
  {
    simpleTag(td, status);
  }

  public void endTag(String tagName, Object status)
  {
    ((ParseStatus)status).end(tagName);
  }

  public Object makeStatusHolder(Object pageContext)
  { 
    ParseStatus status= new ParseStatus();
    status.pageContext=(PageContext)pageContext;
    return status;
  } 
  
  public Object endPage(Object status)
  {
    return ((ParseStatus)status).tags;
  }
}
