package org.makumba.util;

public class NameCase
{
  public static String transformCase(String s)
  {
    s=s.trim();
    StringBuffer sb= new StringBuffer();
    boolean wasSpace=true;
    boolean wasSpaceChar=false;
    boolean wasLowerCase = false;
    for(int i=0; i<s.length(); i++)
      {
	char c= s.charAt(i);
	if ((c=='-') || (c=='\''))
	  {
	    wasSpace=true;
	    wasSpaceChar=false;
	    wasLowerCase=false;
	    sb.append(c);
	  }
	else if(c==' ')
	  if(!wasSpaceChar)
	    {
	      wasSpace=true;
	      wasSpaceChar=true;
	      wasLowerCase=false;
	      sb.append(c);
	    }
	  else;
	else
	  {
	    if(wasSpace)
	    {
	      sb.append(Character.toUpperCase(c));
	      wasLowerCase=false;
	    }
	    else
	    {
		if ((wasLowerCase==true) && (Character.toUpperCase(c)==c))
		{
		    /* uncomment this if you want BauerPartnerHeimer -> Bauer Partner Heimer */
		    /* sb.append(' '); */
		    sb.append(Character.toUpperCase(c));
		    wasLowerCase=false;
		}
		else
		{
		   sb.append(Character.toLowerCase(c));
		   wasLowerCase= (Character.toLowerCase(c)==c);
		}
	    }
	    wasSpace=false;
	    wasSpaceChar=false;
	  }
      }
    String ret=sb.toString();
    if(!s.equals(ret))
      org.makumba.MakumbaSystem.getMakumbaLogger("import").info("org.makumba.util.NameCase: "+ s+" -> "+ret);
    return ret;
  }

  public static void main(String argv[])
  {
    System.out.println(transformCase(argv[0]));
  }
}
