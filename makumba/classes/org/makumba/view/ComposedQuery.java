///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.view;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.makumba.Attributes;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.InvalidFieldTypeException;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.OQLAnalyzer;
import org.makumba.db.hibernate.HibernateOqlAnalyzer;
import org.makumba.util.ArgumentReplacer;
import org.makumba.util.MultipleKey;
import org.makumba.util.NamedResourceFactory;
import org.makumba.util.NamedResources;

/** An OQL query composed from various elements found in script pages. 
 * It can be enriched when a new element is found. 
 * It has a prepared Qyuery correspondent in a makumba database 
 * It may be based on a super query.
 */
public class ComposedQuery
{
  public static interface Evaluator{
    String evaluate(String s);
  }

  /** constructor 
 * @param usesHQL */
  public ComposedQuery(String[] sections, boolean usesHQL)
  {
    this.sections=sections;
    this.derivedSections= sections;
    this.useHibernate= usesHQL;
  }
  
  /** the subqueries of this query */
  Vector subqueries= new Vector();
  
  /** the projections made in this query */
  Vector projections= new Vector();

  /** the expression associated to each projection */
  Hashtable projectionExpr= new Hashtable();
  
  /** standard indexes for each query sections, so they can be passed as one single object */
  public static final int FROM=0;
  public static final int WHERE=1;
  public static final int GROUPBY=2;
  public static final int ORDERBY=3;
  public static final int VARFROM=4;

  /** section texts, encoded with the standard indexes */
  String[] sections;

  /** derived section texts, made from the sections of this query and the sections of its
   * superqueries */
  String[] derivedSections;

  String typeAnalyzerOQL;
  String fromAnalyzerOQL;

  /** the keyset defining the primary key for this query. Normally the primary key is made of the keys declared in FROM, in this query and all the parent queries. Keys are kept as integers (indexes) */
  Vector keyset;
  
  /** the keyset of all the parent queries */
  Vector previousKeyset;

  /** the labels of the keyset */
  Vector keysetLabels;
  
  /** do we use hibernate for execution or do we use makumba? */
  boolean useHibernate;
  
  /** a Vector containing and empty vector. Used for empty keysets */
  static Vector empty;
  static
  {
    empty=new Vector();
    empty.addElement(new Vector());
  }

      
    public DataDefinition getResultType() {
        if (typeAnalyzerOQL == null) {
            return null;
        } else {
            return getOQLAnalyzer(typeAnalyzerOQL).getProjectionType();
        }
    }

    private OQLAnalyzer getOQLAnalyzer(String type) {
        OQLAnalyzer oqa= null;
        if (useHibernate) {
            //TODO: use a cache for this
            oqa= HibernateOqlAnalyzer.getOqlAnalyzer(type, HibernateSFManager.getSF());                         
        } else {
           oqa= MakumbaSystem.getOQLAnalyzer(type);
        }
        return oqa;
    }
    
    public DataDefinition getLabelType(String s) {
        if (typeAnalyzerOQL == null) {
            return null;
        } else {
            return getOQLAnalyzer(typeAnalyzerOQL).getLabelType(s);
        }
    }

  /** initialize the object, template method */
  public void init()
  {
    initKeysets();
    fromAnalyzerOQL= "SELECT nil ";
    if(derivedSections[FROM]!=null)
      fromAnalyzerOQL+="FROM "+derivedSections[FROM];
  }

  /** initialize the keysets. previousKeyset is "empty" */
  protected void initKeysets()
  {
    previousKeyset= empty;
    keyset= new Vector();
    keysetLabels= new Vector();
  }

  /** add a subquery to this query. make it aware that it has subqueries at all. make it be able to announce its subqueries about changes (this will be needed when unique=true will be possible */
  protected void addSubquery(ComposedSubquery q)
  {
    if(subqueries.size()==0)
      prependFromToKeyset();
    subqueries.addElement(q);
  }

  /** All keys from the FROM section are added to the keyset, and their labels to the keyLabels. 
   * They are all added as projections (this has to change) */
  protected void prependFromToKeyset()
  {
    projectionExpr.clear();
    Enumeration e=((Vector)projections.clone()).elements();
    projections.removeAllElements();

    // add the previous keyset
    for(int i=0; i<keyset.size(); i++)
      checkProjectionInteger((String)e.nextElement());

    for(StringTokenizer st= new StringTokenizer(sections[FROM]==null?"":sections[FROM],","); 
	st.hasMoreTokens();)
      {
	String label= st.nextToken().trim();
	int j= label.lastIndexOf(" ");
	if(j==-1)
	  throw new RuntimeException("invalid FROM");
	label=label.substring(j+1).trim();
	keysetLabels.addElement(label);
	keyset.addElement(addProjection(label));
      }

    while(e.hasMoreElements())
      checkProjectionInteger((String)e.nextElement());
  }

  public String getProjectionAt(int n)
  {
    return (String)projections.elementAt(n);
  }
  
  /** add a projection with the given expression */
  Integer addProjection(String expr)
  {
    Integer index=new Integer(projections.size());
    projections.addElement(expr);
    projectionExpr.put(expr, index);
    return index;
  }
  
  /** check if a projection exists, if not, add it. return its index */
  public Integer checkProjectionInteger(String expr)
  {
    Integer index= (Integer)projectionExpr.get(expr);
    if(index==null)
      {
	addProjection(expr);
	// FIXME: if DISTINCT is true, need to recompute the keyset and notify the subqueries to recompute their previous keyset 
	return null;
      }
    return index;
  }

  /** check if a projection exists, if not, add it. return its column name */
  String checkProjection(String expr)
  {
    Integer i= checkProjectionInteger(expr);
    if(i==null)
      return null;
    return columnName(i);
  }
  
  /** return the name of a column indicated by index */
  public static String columnName(Integer n)
  {
    return "col"+(n.intValue()+1);
  }

  /** check the orderBy or groupBy expressions to see if they are already selected, if not add a projection. Only group by and order by labels */
  String checkExpr(String str)
  {
    if(str==null)
      return null;
    if(str.trim().length()==0)
      return null;
    //    if(projections.size()==1)
    // new Throwable().printStackTrace();

    StringBuffer ret= new StringBuffer();
    String sep="";
    for(StringTokenizer st= new StringTokenizer(str, ","); st.hasMoreTokens(); )
      {
	ret.append(sep);
	sep=",";
	String s=st.nextToken().trim();
	String rest="";
	int i= s.indexOf(" ");
	if(i!=-1)
	  {
	    rest= s.substring(i);
	    s=s.substring(0, i);
	  }
	String p=checkProjection(s);
	if(p==null)
	  p=checkProjection(s);
	ret.append(p).append(rest);
      }
    return ret.toString();
  }
  
  /** compute the query from its sections */
  protected String computeQuery(String derivedSections[], boolean typeAnalysisOnly)
  {
    String groups= null;
    String orders= null;
    if(!typeAnalysisOnly)
      {
	groups=checkExpr((String)derivedSections[GROUPBY]);
	orders=checkExpr((String)derivedSections[ORDERBY]);
      }

    StringBuffer sb= new StringBuffer();
    sb.append("SELECT ");
    String sep="";
    
    int i=0;

    for(Enumeration e= projections.elements(); e.hasMoreElements(); )
      {
	sb.append(sep);
	sep=",";
	sb.append(e.nextElement()).append(" AS ").append(columnName(new Integer(i++)));
      }
    Object o;

    if((o=derivedSections[FROM])!=null)
      {
	sb.append(" FROM ");
	sb.append(o);

	// there can be no VARFROM without FROM
	// VARFROM is not part of type analysis 
	// (i.e. projections don't know about it)
	if(!typeAnalysisOnly && derivedSections.length==5 && derivedSections[VARFROM]!=null && derivedSections[VARFROM].trim().length()>0)
	  sb.append(",").append(derivedSections[VARFROM]);
      }
    if(!typeAnalysisOnly)
      {
	if((o=derivedSections[WHERE]) !=null && derivedSections[WHERE].trim().length()>0)
	  {
	    sb.append(" WHERE ");
	    sb.append(o);
	  }
	if(groups !=null)
	  {
	    sb.append(" GROUP BY ");
	    sb.append(groups);
	  }
	if(orders !=null)
	  {
	    sb.append(" ORDER BY ");
	    sb.append(orders);
	  }
      }
    String ret=sb.toString();
    if(!typeAnalysisOnly)
      return ret;

    // replace names with numbers
    ArgumentReplacer ar= new ArgumentReplacer(ret);
    Dictionary d= new Hashtable();
    int j=1;
    for(Enumeration e= ar.getArgumentNames(); e.hasMoreElements(); )
      d.put(e.nextElement(), "$"+(j++));
    return ar.replaceValues(d);
  }

  // ------------
  /** execute the contained query in the given database */ 
  public Grouper execute(AbstractQueryRunner db, Attributes a, Evaluator v, int offset, int limit) 
       throws LogicException
  {
    analyze();
    String []vars= new String[5];
    vars[0]=derivedSections[FROM];
    for(int i=1; i<5; i++)
      vars[i]=derivedSections[i]==null?null:v.evaluate(derivedSections[i]);
    
    return new Grouper
      (previousKeyset, 
       ((MultipleAttributeParametrizer)queries.getResource(new MultipleKey(vars)))
	.execute(db, a, offset, limit).elements());
  }
  
  NamedResources queries=new NamedResources("Composed queries", 
					    new NamedResourceFactory(){
    /**
							 * 
							 */
							private static final long serialVersionUID = 1L;

	protected Object makeResource(Object nm, Object hashName) 
      {
	MultipleKey mk=(MultipleKey)nm;
	String[] sections= new String[5];
	for(int i=0; i<5; i++){
	  if(mk.elementAt(i) instanceof String) // not "null key memeber"
	    sections[i]= (String)mk.elementAt(i);
	}
	return new MultipleAttributeParametrizer(computeQuery(sections, false));
     }
  });

  public synchronized void analyze()
  {
    if(projections.isEmpty())
      prependFromToKeyset();
    if(typeAnalyzerOQL==null)
      typeAnalyzerOQL= computeQuery(derivedSections, true);
  }

  /** check if an expression is valid, nullable or set  */
  public Object checkExprSetOrNullable(String s) 
  {
    int n=0;
    int m=0;
    while(true){
      while(n<s.length()&& !isMakId(s.charAt(n)))
	n++;
      
      if(n==s.length())
	return null;
      m=n;
      while(n<s.length()&& isMakId(s.charAt(n)))
	n++;
      Object nl= checkId(s.substring(m, n));
      if(nl!=null)
	return nl;
      if(n==s.length())
	return null;
    }
  }

  static boolean isMakId(char c)
  {
    return Character.isJavaIdentifierPart(c) || c=='.';
  }

  /** check if an id is nullable, and if so, return the path to the null pointer */
  public Object checkId(String s) 
  {
    int dot=s.indexOf(".");
    if(dot==-1)
      return null;
    DataDefinition dd= getOQLAnalyzer(fromAnalyzerOQL).getLabelType(s.substring(0, dot));
    if(dd==null)
      throw new org.makumba.InvalidValueException("no such label "+s.substring(0, dot));
    while(true)
      {
	int dot1=s.indexOf(".", dot+1);
	if(dot1==-1)
	  {
	    FieldDefinition fd=dd.getFieldDefinition(s.substring(dot+1));
	    if(fd==null)
	      throw new org.makumba.NoSuchFieldException(dd, s.substring(dot+1));
	    if(fd.getType().equals("set"))
	      return fd;
	    return null;
	  }
	FieldDefinition fd=dd.getFieldDefinition(s.substring(dot+1, dot1));
	if(fd==null)
	  throw new org.makumba.NoSuchFieldException(dd, s.substring(dot+1, dot1));
	if(!fd.getType().startsWith("ptr"))
	  throw new InvalidFieldTypeException(fd, "pointer");
	if(!fd.isNotNull())
	  return s.substring(0, dot1);
	dd=fd.getPointedType();
	dot=dot1;
      }
  }
}

