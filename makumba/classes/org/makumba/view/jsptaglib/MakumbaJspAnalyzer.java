package org.makumba.view.jsptaglib;

import org.makumba.util.JspParseData;
import org.makumba.util.RuntimeWrappedException;
import org.makumba.util.MultipleKey;

import org.makumba.view.ComposedQuery;
import org.makumba.view.ComposedSubquery;

import org.makumba.LogicException;
import org.makumba.MakumbaError;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

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

  static
  {
    for(int i=0; i<tags.length; i+=2)
      try
      {
	tagClasses.put(tags[i], Class.forName(tags[i+1]));
      }catch(Throwable t){ t.printStackTrace(); }
  }

  class Types extends HashMap
  {
    public void setType(Object key, Object value)
    {
      Object o= get(key);
      // FIXME: this should do type compatibility check
      if(o!=null)
	System.out.println("changing type for "+key+" from "+o+" to "+value);
      put(key, value);
    }
  }

  class PageCache
  {
    HashMap formatters= new HashMap();
    HashMap valueComputers= new HashMap();
    Types types= new Types();
    HashMap queries= new HashMap();
    HashMap inputTypes= new HashMap();
    HashMap basePointerTypes= new HashMap();

    public ComposedQuery getQuery(Object key)
    {
      ComposedQuery ret= (ComposedQuery)queries.get(key);
      if(ret==null)
	throw new MakumbaError("unknown query for key "+key);
      return ret;
    }

    /** return a composed query that will associated to the given key. 
     */
    public ComposedQuery cacheQuery(Object key, String[] sections, MultipleKey parentKey)
    {
      ComposedQuery ret= (ComposedQuery)queries.get(key);
      if(ret!=null)
	return ret;
      ret=parentKey==null? new ComposedQuery(sections):
	new ComposedSubquery(sections, getQuery(parentKey));
      
      ret.init();
      queries.put(key,ret);
      return ret;
    }
  }

  class ParseStatus
  {
    String makumbaPrefix;
    List tags= new ArrayList();
    List parents= new ArrayList();
    PageCache pageCache= new PageCache();

    void addTag(MakumbaTag t, JspParseData.TagData td)
    {
      if(!parents.isEmpty())
	t.setParent((MakumbaTag)parents.get(parents.size()-1));
      else
	t.setParent(null);

      t.pageCache=pageCache;

      JspParseData.fill(t, td.attributes);
      t.setTagKey();
      t.doStartAnalyze();
      tags.add(t);
    }

    public void start(MakumbaTag t)
    {
      if(t==null)
	return;
      if(!(t instanceof QueryTag) && ! (t instanceof FormTagBase))
	throw new RuntimeException("body tag expected");
      parents.add(t);
    }

    public void end(JspParseData.TagData td)
    {
      String tagName= td.name;
      if(!tagName.startsWith(makumbaPrefix))
	return;

      if (parents.isEmpty()) {
          StringBuffer sb= new StringBuffer();
          sb.append("Error: Closing tag never opened:\ntag \"").
             append(td.name).
             append("\" at line ");
          JspParseData.tagDataLine(td, sb);
          throw new org.makumba.ProgrammerError(sb.toString());
      }
      tagName= tagName.substring(makumbaPrefix.length()+1);
      MakumbaTag t= (MakumbaTag)parents.get(parents.size()-1);
      if(!(t instanceof QueryTag) && ! (t instanceof FormTagBase))
	throw new RuntimeException("body tag expected");
      if(!t.getClass().equals(tagClasses.get(tagName)))
	  {
	      StringBuffer sb= new StringBuffer();
	      sb.append("Body tag nesting error:\ntag \"").
		  append(t.tagData.name).
		  append("\" at line ");
	      JspParseData.tagDataLine(t.tagData, sb);

	      sb.append("\n\ngot incorrect closing \"").append(td.name).
		  append("\" at line ");
	      JspParseData.tagDataLine(td, sb);
	      
	      throw new org.makumba.ProgrammerError(sb.toString());
	  }

      parents.remove(parents.size()-1);
    }

    public void endPage()
    {
      for(Iterator i= tags.iterator(); i.hasNext(); )
	((MakumbaTag)i.next()).doEndAnalyze();
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
    td.tagObject=t;
    t.tagData=td;
    ((ParseStatus)status).addTag(t, td);
  }

  public void startTag(JspParseData.TagData td, Object status)
  {
    simpleTag(td, status);
    ((ParseStatus)status).start((MakumbaTag)td.tagObject);
  }

  public void endTag(JspParseData.TagData td, Object status)
  {
    ((ParseStatus)status).end(td);
  }

  public Object makeStatusHolder(Object initialStatus)
  { 
    return new ParseStatus();
  } 
  
  public Object endPage(Object status)
  {
    ((ParseStatus)status).endPage();
    return ((ParseStatus)status).pageCache;
  }
}
