package org.makumba.controller.http;
import javax.servlet.*;
import javax.servlet.http.*;
import org.makumba.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Date;

/** The filter that controls each makumba HTTP access. Performs login, form response, exception handling. */
public class ControllerFilter implements Filter
{
  public static final String ORIGINAL_REQUEST="org.makumba.originalRequest";

  static FilterConfig conf;
  public void init(FilterConfig c) { conf=c; }
  
  /** the filtering method, basically puts some wrappers around the request and the response */
  public void doFilter(ServletRequest req, ServletResponse resp,
		       FilterChain chain)
        throws ServletException, java.io.IOException
  {
    boolean filter= shouldFilter((HttpServletRequest)req);
   try{
      if(filter){
	try{
	  RequestAttributes.getAttributes((HttpServletRequest)req);
	}catch(Throwable e)
	  { 
	    treatException(e, (HttpServletRequest)req, (HttpServletResponse)resp);
	    return; 
	  }
	
	Responder.response((HttpServletRequest)req, (HttpServletResponse)resp);
	if(wasException((HttpServletRequest)req))
	  return;
      }
      chain.doFilter(req, resp);
    }finally{ if(filter)setLastAccess((HttpServletRequest)req); }
  }

  /** decide if we filter or not */
  public boolean shouldFilter(HttpServletRequest req)
  {
    String file=null;
    try{
      file= new URL(req.getRequestURL().toString()).getFile();
    }catch(java.net.MalformedURLException e) { } // can't be

    if(file.endsWith(".jspx"))
      {
	// all images and stuff referred by jspxes are not filtered
	setLastAccess((HttpServletRequest)req); 
	return false;
      }
    return !((file.endsWith(".css")||file.endsWith(".jpg")||file.endsWith(".gif"))
	     && refererAccessedRecently(req));
  }

  static final String accessTable="ACCESS TABLE";

  /** keep all access times to filtered pages in the session */
  public void setLastAccess(HttpServletRequest req)
  {
    Hashtable accesses= (Hashtable)req.getSession(true).getAttribute(accessTable);
    if(accesses==null)
      req.getSession(true).setAttribute(accessTable, accesses=new Hashtable());
    String qs=req.getQueryString();
    
    accesses.put(req.getRequestURI()+((qs!=null)?("?"+qs):""), new Date());
  }

  static final long RECENTLY=10000;

  /** check if the referrer of this request was accessed recently */
  public boolean refererAccessedRecently(HttpServletRequest req)
  {
    Hashtable accesses=null;
    String referer= req.getHeader("Referer");
    URL u=null;
    try{
	u= new URL(referer);
    }catch(java.net.MalformedURLException e) { } // can't be

    /* we build the URI from the referer and just check if we are on the 
       same host as the referer access
       we don't check the port because ports may change in HTTP redirections...
    */
    if(!u.getHost().equals(req.getServerName()))
	referer=null;
    else
	{
	    referer=referer.substring(7);
	    int n=referer.indexOf("/");
	    if(n==-1)
		referer="/";
	    else
		referer=referer.substring(n);
	}

    Date lastAccess=null;
    boolean b= (accesses=(Hashtable)req.getSession(true).getAttribute(accessTable))!=null 
      && referer!=null
      && (lastAccess=(Date)accesses.get(referer))!=null
      && (new Date()).getTime()- lastAccess.getTime() < RECENTLY;
    // System.out.println("\n"+req.getRequestURI()+" "+referer+" "+lastAccess+" "+b+"\n"+accesses+"\n");
    return b;
  }


  public void destroy(){}
  

  //------------- treating exceptions ------------------
  /** treat an exception that occured during the request */
  static public void treatException(Throwable t, HttpServletRequest req, HttpServletResponse resp) 
  {
    req.setAttribute(javax.servlet.jsp.PageContext.EXCEPTION, t);
    if(req.getAttribute("org.makumba.exceptionTreated")==null && !((t instanceof UnauthorizedException) && login(req, resp)))
      {
	try{
	  req.getRequestDispatcher("/servlet/org.makumba.devel.TagExceptionServlet").forward(req, resp); 
	}catch(Throwable q){ q.printStackTrace(); throw new MakumbaError(q); }
      }
    setWasException(req);
    req.setAttribute("org.makumba.exceptionTreated", "yes");
  }

  /** signal that there was an exception during the request, so some operations can be skipped */
  public static void setWasException(HttpServletRequest req)
  {
    req.setAttribute("org.makumba.wasException", "yes");
  }

  /** test if there was an exception during the request */
  public static boolean wasException(HttpServletRequest req)
  {
    return "yes".equals(req.getAttribute("org.makumba.wasException"));
  }

  //---------------- login ---------------
  /** compute the login page from a servletPath */
  public static String getLoginPage(String servletPath)
  {
    String root= conf.getServletContext().getRealPath("/");    
    String virtualRoot="/";
    String login="/login.jsp";
    
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
  protected static boolean login(HttpServletRequest req, HttpServletResponse resp)
  {
    // find the original request... the inverse of the VHostWrapper
    while(req instanceof HttpServletRequestWrapper)
      req=(HttpServletRequest)((HttpServletRequestWrapper) req).getRequest();

    req.setAttribute(ORIGINAL_REQUEST, req);
    
    String login= getLoginPage(req.getServletPath());

    if(login==null)
      return false;
    try{
      req.getRequestDispatcher(login).forward(req, resp);
    }catch(Throwable q){ q.printStackTrace(); return false; }
    return true;
  }
}
