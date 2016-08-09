package org.makumba.view.jsptaglib;
import org.makumba.view.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import org.makumba.*;
import org.makumba.util.*;

/** this class provides utility methods for all makumba tags */
public abstract class MakumbaTag extends TagSupport implements TagStrategy
{
  static final String ROOT_DATA_NAME="makumba.root.data";
  static final String RESPONSE_STRING_NAME="makumba.response";

  TagStrategy strategy;

  public PageContext getPageContext() {return pageContext; }

  /** identifies the closest parent of the class desired by getParentClass() */
  public void setParent(Tag t)
  { 
    super.setParent(t); 
    if(getMakumbaParent()==null)
      {
	pageContext.setAttribute(ROOT_DATA_NAME, new RootData(this, pageContext), PageContext.PAGE_SCOPE);
	canBeRoot();
	getRootData().buffer=makeBuffer();
      }
  }

  /** make common data for all queries to be stored in the root data */
  public Object makeBuffer() { return null; }

  /** return true if this tag can be root tag, else throw an exception */
  protected abstract boolean canBeRoot();

  /** what kind of makumba tag should the parent tag be? */
  protected abstract Class getParentClass();

  /** get the makumba parent, of the desired class */
  protected MakumbaTag getMakumbaParent() 
  { return (MakumbaTag)findAncestorWithClass(this, getParentClass()); }

  static public RootData getRootData(PageContext pc)
  {
    return (RootData)pc.getAttribute(ROOT_DATA_NAME, PageContext.PAGE_SCOPE);
  }

  /** get the root data */
  protected RootData getRootData() 
  { 
    return getRootData(pageContext);
  }

  /** make a strategy, returns this object by default */
  public TagStrategy makeNonRootStrategy(Object key) { return this; }

  /** factory method, make a root strategy if this tag is root */
  protected RootTagStrategy makeRootStrategy(Object key) { throw new MakumbaError("must be redefined"); }

  public TagStrategy makeStrategy(Object key)
  {
    if(getMakumbaParent()==null)
      return makeRootStrategy(key);
    else
      return makeNonRootStrategy(key);
  }

  /** return a key for registration in root. If null is returned, registration in root is not made */
  public Object getRootRegistrationKey() throws LogicException { return null; }

  /** return a key for registration in root. If null is returned, registration in root is not made */
  public Object getRegistrationKey() throws LogicException { return null; }

  /** get the closest enclosing query, or null if nothing */
  public QueryStrategy getEnclosingQuery()
  {
    QueryTag qt= (QueryTag)findAncestorWithClass(this, QueryTag.class);
    if(qt==null)
      return null;
    return ((QueryTagStrategy)qt.strategy).getQueryStrategy();
  }

  /** only works for tags that have query parents */
  public QueryStrategy getParentQueryStrategy() 
  {
    return ((QueryTagStrategy)getMakumbaParent().strategy).getQueryStrategy();
  }

  public HttpAttributes getAttributes()
  {
    try{
      return HttpAttributes.getAttributes(pageContext);
    }catch(Throwable e){ throw new MakumbaError("should not have error here "+e); } 
  }

  /** find the strategy, then delegate it to execute the preamble */
  public int doStartTag() throws JspException
  {
    try{
      HttpAttributes.getAttributes(pageContext);
    }catch(Throwable e){ treatException(e); return SKIP_BODY; }
    response();
    if(wasException())
      return SKIP_PAGE;    
    try{
      Object key=null;
      if(getMakumbaParent()==null)
	key= getRootRegistrationKey();
      else
	key= getRegistrationKey();
    
      if(key==null)
	strategy=makeStrategy(null);
      else
	getRootData().setStrategy(key, this);
      strategy.setPage(pageContext);
      return strategy.doStart();
    }catch(Throwable t){ treatException(t); return SKIP_BODY; }
  }

  /** delegate the strategy to end */
  public int doEndTag() throws JspException
  {
    if(wasException())
      return SKIP_PAGE;
    return strategy.doEnd();
  }

  public void release(){ if(strategy!=null)strategy.doRelease(); }

  /** obtain the makumba database; this can be more complex (accept arguments, etc) */
  public String getDatabaseName() {return getDatabaseName(pageContext); }

  public static String getDatabaseName(PageContext pc) 
  {
    if(getRootData(pc).db==null)
      return MakumbaSystem.getDefaultDatabaseName();
    return getRootData(pc).db;
  }


  //--- dummy implementation of TagStrategy
  public void init(MakumbaTag root, MakumbaTag tag, Object key){}
  public void setBody(BodyContent bc){}
  public void setPage(PageContext pc){}
  public BodyContent getBody(){ return null; }
  public void loop(){}
  public int doStart() throws JspException, LogicException { return Tag.SKIP_BODY; }
  public int doAfter() throws JspException { return Tag.SKIP_BODY; }
  public int doEnd() throws JspException{ return Tag.EVAL_PAGE; } 
  public void doRelease() {}
  public void rootClose() {}

  
  // ------------------------   
  // ------------  only root arguments
  // ----------------------
  /** throw an exception if this is not the root tag */
  protected void onlyRootArgument(String s) 
  {
    if(getMakumbaParent()!=null)
      treatException(new MakumbaJspException
		     (this, "the "+s+" argument cannot be set for non-root makumba tags"));   
  }

  /** set the database argument */
  public void setDb(String db) throws JspException
  {
    onlyRootArgument("db");
    getRootData().db=db;
  }

  public void setHeader(String s) throws JspException  
  { 
    onlyRootArgument("header"); 
    getRootData().header=s;
  }

  public void setFooter(String s) throws JspException  
  {
    onlyRootArgument("footer"); 
    getRootData().footer=s;
  }
  
  //------------------ form response
  public void response()
  {    
    if(pageContext.getRequest().getAttribute(RESPONSE_STRING_NAME)!=null)
      return;
    pageContext.getRequest().setAttribute(RESPONSE_STRING_NAME, "");
    Integer i=FormResponder.responseId(pageContext);
    if(i==null)
      return;
    String s="";
    FormResponder fr= null;
    try{
      fr=FormResponder.getFormResponder(i);
      Object p=fr.respondTo(pageContext);
      s="<font color=green>"+fr.getMessage()+"</font>";
      if(p!=null)
	pageContext.setAttribute(fr.getSubjectLabel(), p, pageContext.PAGE_SCOPE);
      pageContext.getRequest().setAttribute("makumba.successfulResponse", "yes");
    }
    catch(AttributeNotFoundException anfe)
      {
	// attribute not found is a programmer error and is reported
	treatException(anfe); 
	return ;
      }
    catch(LogicException e){
      MakumbaSystem.getMakumbaLogger("controller.logicError").log(java.util.logging.Level.INFO, "logic exception", e);
      s=errorMessage(e);
      pageContext.setAttribute(fr.getSubjectLabel(), Pointer.Null, pageContext.PAGE_SCOPE);
    }
    catch(Throwable t){
      // all included error types should be considered here
      treatException(t);
    }
    pageContext.getRequest().setAttribute(RESPONSE_STRING_NAME, s);
  }

  /** root tag properties */
  static final String ROOT_PREFIX="makumba.root.prefix.";

  public Object getRootProperty(String s)
  {
    return pageContext.getAttribute(ROOT_PREFIX+s, pageContext.REQUEST_SCOPE);
  }


  public void setRootProperty(String s, Object o)
  {
    pageContext.setAttribute(ROOT_PREFIX+s, o, pageContext.REQUEST_SCOPE);
  }

  public void removeRootProperty(String s)
  {
    pageContext.removeAttribute(ROOT_PREFIX+s, pageContext.REQUEST_SCOPE);
  }

  public boolean wasException()
  {
    return getRootProperty("wasException")!=null;
  }

  public void setWasException()
  {
    setRootProperty("wasException", "yes");
  }

  // ----------------------
  // -----  forwarding stuff: login, exceptions 
  // ---------------------- 

  public static String errorMessage(Throwable t)
  {
    return "<font color=red>"+t.getMessage()+"</font>";
  }

  protected void treatException(Throwable t) 
  {
    if(getRootProperty("exceptionTreated")==null && !((t instanceof UnauthorizedException) && login(t)))
      {
	pageContext.setAttribute(pageContext.EXCEPTION, t, pageContext.REQUEST_SCOPE);
	try{
	  pageContext.forward("/servlet/org.makumba.devel.TagExceptionServlet"); 
	}catch(Throwable q){ q.printStackTrace(); throw new MakumbaError(q); }
      }
    setRootProperty("exceptionTreated", "yes");
    setRootProperty("wasException", "yes");
  }

  public static String getLoginPage(String servletPath, javax.servlet.ServletConfig svc)
  {
    String root= svc.getServletContext().getRealPath("/");    
    String virtualRoot="/";
    String login=null;
    
    java.util.StringTokenizer st= new java.util.StringTokenizer(servletPath, "/");
    while(st.hasMoreElements())
      {
	try{ 
	  new java.io.FileReader(root+"login.jsp"); 
	  login=virtualRoot+"login.jsp";
	}catch(java.io.IOException e) {}
	String s=st.nextToken()+"/";
	root+=s;
	virtualRoot+=s;
      }
    return login;
  }

  /** find the closest login.jsp and forward to it */
  protected boolean login(Throwable t)
  {
    //    pageContext.setAttribute(attrNameAttr, actorAttribute, pageContext.SESSION_SCOPE);
      HttpServletRequest req=(HttpServletRequest)pageContext.getRequest();

      String servletPath= req.getServletPath();

      // find the original request... the inverse of the VHostWrapper
      while(req instanceof HttpServletRequestWrapper)
	  req=(HttpServletRequest)((HttpServletRequestWrapper) req).getRequest();

/*      String pathInfo=req.getPathInfo();
      if(pathInfo==null)
	  pathInfo="";
      String varInfo=req.getQueryString();
      if(varInfo==null) 
          varInfo=""; 
      System.out.println(varInfo+"\n");
      pageContext.setAttribute(LoginTag.pageAttr, req.getServletPath()+pathInfo+"?"+varInfo, pageContext.REQUEST_SCOPE); */


      pageContext.setAttribute(LoginTag.pageAttr, req, pageContext.REQUEST_SCOPE);
//      req.setAttribute(LoginTag.pageAttr, req);

      // pageContext.getRequest().setAttribute(RESPONSE_STRING_NAME, errorMessage(t));
      pageContext.setAttribute(pageContext.EXCEPTION, t, pageContext.REQUEST_SCOPE);
      
      String login= getLoginPage(servletPath, pageContext.getServletConfig());
      if(login==null)
	  return false;
      try{
           System.out.println("Forwarding to:"+login);
	  pageContext.forward(login);
      }catch(Throwable q){ q.printStackTrace(); return false; }
      return true;
  }


  ///---------------- arguments for simple tags 
  /** get the strategy of the root */
  protected RootQueryBuffer getRootQueryBuffer() 
  { 
    return (RootQueryBuffer)getRootData().buffer;
  }


  public void setUrlEncode(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("urlEncode", s); 
  }

  public void setHtml(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("html", s); 
  }
  
  public void setFormat(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("format", s); 
  }

  public void setType(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("type", s); 
  }

  public void setSize(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("size", s); 
  }

  public void setMaxlength(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("maxlength", s); 
  }

  public void setRows(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("rows", s); 
  }

  public void setCols(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("cols", s); 
  }

  public void setLineSeparator(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("lineSeparator", s); 
  }

  public void setLongLineLength(String s) 
  { 
    getRootQueryBuffer().bufferParams.put("longLineLength", s); 
  }
}
