package org.makumba.view;
import java.util.*;
import java.math.BigInteger;
import org.makumba.util.*;
import org.makumba.*;

/** this class takes a Query with arguments, and tries to find the maximum set of arguments that
 * can be parametrized in OQL sense. 
 * For the non-parametrized arguments, it keeps a Hashtable of prepared database Queries */
public class AttributeParametrizer
{
  /** names of all arguments, to keep an order */
  Vector argumentNames= new Vector();
  
  /** each bit represents an argument: 1= nonparametric, 0= parametric */
  BigInteger mask;
  
  /** replacer of values to arguments */
  ArgumentReplacer ar;
  
  /** the parameters in the form returned by parametrized() */
  Hashtable parameters;

  static final String undefLabel="undefined label: \"";

  OQLAnalyzer example;

    NamedResources asts;

  /** build a parametrizer from an OQL query, in the given database and with the given example arguments */
  public AttributeParametrizer(String oql, Dictionary a)
       throws LogicException
  {
      asts=new NamedResources("JSP attribute parametrizer objects",
			      astFactory);
    ar= new ArgumentReplacer(oql);
    
    for(Enumeration e=ar.getArgumentNames(); e.hasMoreElements(); )
      argumentNames.addElement(e.nextElement());
    BigInteger max= BigInteger.ONE.shiftLeft(argumentNames.size());
    OQLAnalyzer q=null;
    
    String ignored="";

    // we try all possibilities param/non-param, for all arguments
    // we go in increasing order of the number of non-parametrized arguments
  all:
    for(int nonparam=0; nonparam<=argumentNames.size(); nonparam++)
  loop:
    for(mask=BigInteger.ZERO; mask.compareTo(max)<0;mask=mask.add(BigInteger.ONE))
      {
	int np=0;
	// we count the number of non-parametrized in this mask
	for(int ones=0; ones<argumentNames.size(); ones++)
	  if(mask.testBit(ones))
	    np++;
	// if too much or too little, we try another mask
	if(np!=nonparam)
	  continue;
	// this mask has the right number of non-parametrized
	parameters= new Hashtable();
	parametrized(parameters);
	try{
	  Dictionary d= (Dictionary)parameters.clone();
	  nonParametrized(a, d);
	  q= findAST(d);
	  // we found something!!
	  break all;
	}catch(OQLParseError e) 
	   // There are a number of typical parse exceptions that occur due to a 
	   // non-parametrizable argument being parametrized, or vice-versa.
	   // These exceptions are ignored and other things are tried out.
	   // This proces is rather guessy, but works for most cases.
	  {
	    String m=e.getMessage();
	    if(m.indexOf("unexpected token: $")!=-1 ||
	       m.indexOf("found '$'")!=-1)
	      {
		ignored+=m+"\n";
		continue;
	      }
	    int n=m.indexOf(undefLabel);
	    if(n!=-1)
	      {
		n+=undefLabel.length();
		String argValue=m.substring(n, m.indexOf('\"', n));
		for(int j=0; j<argumentNames.size(); j++)
		  if(a.get((String)argumentNames.elementAt(j)).equals(argValue)
		     && mask.testBit(j))
		    {
		      ignored+=m+"\n";
		      continue loop;
		    }
	      }
	    throw new LogicException(e);
	  }
      }
    
    if(q==null)
      throw new LogicException("cannot validate query arguments "+ oql
			       +"\npossible sources:\n"+ignored);
    example=q;
  }

  public DataDefinition getResultType() { return example.getProjectionType(); }

  public DataDefinition getLabelType(String label){ return example.getLabelType(label); }

  
    NamedResourceFactory astFactory=new NamedResourceFactory(){
    protected Object getHashObject(Object nm) 
      {
	return nm.toString();
      }
    
    protected Object makeResource(Object nm, Object hashName) 
      throws Exception
      {
	return  MakumbaSystem.getOQLAnalyzer(ar.replaceValues((Dictionary)nm));
     }
  };

  /** find the query corresponding to the given database and arguments */
  OQLAnalyzer findAST(Dictionary dic) throws LogicException
  {
    try{
      return (OQLAnalyzer)asts.getResource(dic);
    }catch(RuntimeWrappedException e)
      {
        Throwable t= e.getReason();
	if(t instanceof OQLParseError)
	  throw (OQLParseError)t;
	if(t instanceof AttributeNotFoundException) 
	  throw (AttributeNotFoundException)t;
	throw e;
      }
  }

  /** find and execute the query corresponding to the non-parametrized arguments and 
   * pass the parametrizable ones */
  public Vector execute(Database db, Dictionary a) 
       throws LogicException
  {
    Object args[]= new Object[parameters.size()];
    int j=0;
    for(int i=0; i<argumentNames.size(); i++)
      if(!mask.testBit(i))
	args[j++]=a.get((String)argumentNames.elementAt(i));
    Dictionary d= (Dictionary)parameters.clone();
    nonParametrized(a, d);
    return db.executeQuery(findAST(d).getOQL(), args);
  }

  /** fill a dictionary with pairs like {argName= argValue } for nonparametrized arguments */
  void nonParametrized(Dictionary a, Dictionary d) throws LogicException
  {
    for(int i=0; i<argumentNames.size(); i++)
      if(mask.testBit(i))
	{
	  String s= (String)argumentNames.elementAt(i);
	  d.put(s, a.get(s));
	}
  }
  
  /** fill a dictionary with pairs like {argName= $n } for parametrized arguments */
  void parametrized(Dictionary d)
  {
    int j=1;
    for(int i=0; i<argumentNames.size(); i++)
      if(!mask.testBit(i))
	d.put(argumentNames.elementAt(i), "$"+(j++));
  }
}


