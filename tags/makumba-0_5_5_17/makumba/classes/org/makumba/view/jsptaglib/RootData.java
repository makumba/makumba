package org.makumba.view.jsptaglib;
import java.util.*;
import org.makumba.util.*;
import javax.servlet.http.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;
import org.makumba.*;

public class RootData
{
  //  static final String attrNameAttr="org.makumba.attributeName"; 
  String header, footer, db;
  MakumbaTag rootTag;
  Dictionary subtagData= new Hashtable();
  Dictionary subtagDataNormalKeys= new Hashtable();
  long stamp;
  int ntags=0;
  PageContext pageContext;
  Object buffer;

  public RootData(MakumbaTag t, PageContext pageContext){
    this.rootTag=t; 
    subtagData=new Hashtable();
    MakumbaSystem.getMakumbaLogger("taglib.performance").fine("---- tag start ---");
    stamp= new Date().getTime();
    this.pageContext=pageContext;
    if(pageContext.getAttribute(pageContext.EXCEPTION, pageContext.REQUEST_SCOPE)!=null)
      t.setWasException();

    //    String attName[]=pageContext.getRequest().getParameterValues(attrNameAttr);
    //if(pageContext.getAttribute(attrNameAttr, PageContext.SESSION_SCOPE) == null && attName!=null)
    //      pageContext.setAttribute(attrNameAttr, attName[0], PageContext.SESSION_SCOPE);
  }

  public void setStrategy(Object key, MakumbaTag tag) throws LogicException
  {
    ntags++;
    tag.strategy= (TagStrategy)subtagData.get(key);
    if(tag.strategy==null)
      {
	subtagData.put(key, tag.strategy=tag.makeStrategy(key));
	tag.strategy.init(rootTag, tag, key);
	((RootTagStrategy)rootTag.strategy).onInit(tag.strategy);
      }
    tag.strategy.loop();
  }

  /** release resources kept by all the tags */
  protected void close()
  {
    if(subtagData==null)
      return;
    MakumbaSystem.getMakumbaLogger("taglib.performance").fine("tag time: "+(new Date().getTime()-stamp)+" ms "+ntags+" query tags");
    for(Enumeration e= subtagData.elements(); e.hasMoreElements();)
      ((TagStrategy)e.nextElement()).rootClose();
    subtagData=null;
  }
}
