package org.makumba.view.jsptaglib;
import javax.servlet.jsp.tagext.TagLibraryValidator;
import javax.servlet.jsp.tagext.PageData;
import javax.servlet.jsp.tagext.ValidationMessage;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;

/** this class is not in use. to use it, copy servlet_context/WEB-INF/makumba.tld to servlet_context/WEB-INF/makumba.tld.1.1 and servlet_context/WEB-INF/makumba.tld.1.2 to servlet_context/WEB-INF/makumba.tld. make sure that makumba.tld declares within <taglib ...> :
	<validator>
		<validator-class>org.makumba.view.jsptaglib.PageValidator</validator-class>
	</validator>
	*/
 
public class PageValidator extends TagLibraryValidator
{
  static ThreadLocal pageStack= new ThreadLocal();

  public ValidationMessage[] validate(java.lang.String prefix,
				      java.lang.String uri,
				      javax.servlet.jsp.tagext.PageData page)
  {
    StringBuffer sb= new StringBuffer();
    String s;
    BufferedReader r= new BufferedReader(new InputStreamReader(page.getInputStream()));
    try{
      while((s=r.readLine())!=null)
	sb.append(s).append("\n");
    }catch(IOException e){System.err.println(e);}
    String pg= sb.toString();
    int n= pg.indexOf("mak:");
    if(n!=-1)
      System.out.println("######## pushing"+ pg.substring(n-1, n+40));
    Stack st= (Stack)pageStack.get();
    if(st==null)
      {st=new Stack(); pageStack.set(st);}
    st.push(pg);
    return null;
  }
}
