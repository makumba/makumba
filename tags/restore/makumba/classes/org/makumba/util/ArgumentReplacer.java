package org.makumba.util;
import java.util.*;

/** parse a string and identify the arguments, to allow operations with them 
 * for now, arguments are of the form $javaid[$] but the class can be extended for them to take 
 * other forms
 */
public class ArgumentReplacer 
{
  Vector text= new Vector();
  Dictionary argumentNames= new Hashtable();
  Vector argumentOrder=new Vector();

  /** return the arguments list */
  public Enumeration getArgumentNames()
  {
    return argumentNames.keys();
  }

  /** return a string with the respective values replaced */
  public String replaceValues(Dictionary d)
  {
    StringBuffer sb= new StringBuffer();
    Enumeration f= argumentOrder.elements();
    Enumeration e= text.elements();
    while(true)
      {
	sb.append(e.nextElement());
	if(f.hasMoreElements())
	  {
	    Object nm= f.nextElement();
	    Object o= d.get(nm);
	    if(o==null)
	      throw new RuntimeException(nm+" "+d);
	    sb.append(o);
	  }
	else
	  break;
      }
    return sb.toString();
  }

  /* make a list of all arguments and where they are... */
  public ArgumentReplacer(String s)
  {
    int dollar;
    String prev="";
    boolean doubledollar;
    int n;
    String argname;

    while(true)
      {
	dollar=s.indexOf('$');
	if(dollar==-1 || s.length()==dollar+1)
	  {
	    text.addElement(prev+s);
	    break;
	  }
     
	dollar++;
	if( (doubledollar=s.charAt(dollar)=='$') 
	    || !Character.isJavaIdentifierStart(s.charAt(dollar)))
	  {
	    prev=s.substring(0, dollar);
	    if(doubledollar)
	      dollar++;
	    if(s.length()>dollar)
	      {
		s=s.substring(dollar);
		continue;
	      }
	    else
	      {
		text.addElement(prev);
		break;
	      }
	  }
	text.addElement(prev+s.substring(0, dollar-1));
	prev="";

	for(n=dollar+1; n<s.length() && s.charAt(n)!='$' && Character.isJavaIdentifierPart(s.charAt(n)); n++);
	argname=s.substring(dollar, n);	
	if(n<s.length() && s.charAt(n)=='$')
	  n++;
	argumentNames.put(argname, "");
	argumentOrder.addElement(argname);
	if(n<s.length())
	  {
	    s=s.substring(n);
	    continue;
	  }
	else
	  {
	    text.addElement("");
	    break;
	  }
      }
  }
}
