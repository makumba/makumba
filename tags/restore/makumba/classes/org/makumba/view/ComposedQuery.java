package org.makumba.view;
import java.util.*;
import org.makumba.util.*;
import org.makumba.*;

/** An OQL query composed from various elements found in script pages. 
 * It can be enriched when a new element is found. 
 * It has a prepared Qyuery correspondent in a makumba database 
 * It may be based on a super query.
 */
public class ComposedQuery extends Observable
{
  /** a cache for composed queries */
  static int queries = NamedResources.makeStaticCache
      ("JSP composed queries",
   new NamedResourceFactory(){
    protected Object getHashObject(Object nm) 
      {
	return ""+((Object[])nm)[0]; 
      }
  
    protected Object makeResource(Object name, Object hashName) 
      {
	ComposedQuery parent=(ComposedQuery)((Object[])name)[2];
	String[] sections=(String[])((Object[])name)[1];
	ComposedQuery ret=(parent==null?new ComposedQuery(sections)
			   :new ComposedSubquery(sections, parent));
	ret.init();
	return ret;
      }
  });
  
  /** return a composed query that will associated to the given key. 
   */
  public static ComposedQuery getQuery(Object key, String[] sections, ComposedQuery parent)
  {
    Object arg[]= {key, sections, parent};
    return (ComposedQuery)NamedResources.getStaticCache(queries).
      getResource(arg);
  }
  
  /** constructor */
  ComposedQuery(String[] sections)
  {
    this.sections=sections;
    this.derivedSections= sections;
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

  /** section texts, encoded with the standard indexes */
  String[] sections;

  /** derived section texts, made from the sections of this query and the sections of its
   * superqueries */
  String[] derivedSections;

  /** the OQL query as computed from the derived sections */
  String oqlQuery;

  /** the keyset defining the primary key for this query. Normally the primary key is made of the keys declared in FROM, in this query and all the parent queries. Keys are kept as integers (indexes) */
  Vector keyset;
  
  /** the keyset of all the parent queries */
  Vector previousKeyset;

  /** the labels of the keyset */
  Vector keysetLabels;
  
  /** the parametrizer of arguments */
  MultipleAttributeParametrizer attrParam= null;

  /** a Vector containing and empty vector. Used for empty keysets */
  static Vector empty;
  static
  {
    empty=new Vector();
    empty.addElement(new Vector());
  }

  public DataDefinition getResultType()
  { return attrParam==null?null:attrParam.getResultType(); }

  public DataDefinition getLabelType(String s)
  { return attrParam==null?null:attrParam.getLabelType(s); }

  /** return the version, one version is added for each change */
  public int getVersion(){ return projections.size(); }

  /** initialize the object, template method */
  protected void init()
  {
    initKeysets();
  }

  /** initialize the keysets. previousKeyset is "empty" */
  protected void initKeysets()
  {
    previousKeyset= empty;
    keyset= new Vector();
    keysetLabels= new Vector();
  }

  /** export the parent's keyset to the class clients. It is used to group the results of this query */
  public Vector getPreviousKeyset() { return previousKeyset; }

  /** returns a primary key for the result */
  public Vector getKeyset() { return keyset; }

  /** add a subquery to this query. make it aware that it has subqueries at all. make it be able to announce its subqueries about changes (this will be needed when unique=true will be possible */
  protected void addSubquery(ComposedSubquery q)
  {
    if(subqueries.size()==0)
      {
	prependFromToKeyset();
	recomputeQuery();
      }
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
      checkProjectionInteger((String)e.nextElement(), false);

    for(StringTokenizer st= new StringTokenizer((String)sections[FROM],","); st.hasMoreTokens();)
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
      checkProjectionInteger((String)e.nextElement(), false);
  }

  /** return a clone of all the projections */
  public Dictionary getProjections()
  {
    return (Dictionary)projectionExpr.clone();
  }

  /** get the index of the indicated projection */
  public Integer getProjectionIndex(String expr)
  {
    return (Integer)projectionExpr.get(expr);
  }

  public String getProjectionAt(int n)
  {
    return (String)projections.elementAt(n);
  }
  
  /** add a projection with the given expression */
  protected Integer addProjection(String expr)
  {
    Integer index=new Integer(projections.size());
    projections.addElement(expr);
    projectionExpr.put(expr, index);
    return index;
  }
  
  /** check if a projection exists, if not, add it. return its index */
  public Integer checkProjectionInteger(String expr)
  {
    return checkProjectionInteger(expr, true);
  }

  /** check if a projection exists, if not, add it. return its index */
  public synchronized Integer checkProjectionInteger(String expr, boolean recompute)
  {
    Integer index= getProjectionIndex(expr);
    if(index==null)
      {
	addProjection(expr);
	if(recompute)
	  recomputeQuery();
	// if UNIQUE is true, need to recompute the keyset and notify the subqueries to recompute their previous keyset 
	return null;
      }
    return index;
  }

  /** check if a projection exists, if not, add it. return its column name */
  public String checkProjection(String expr)
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

  /** a change has come up, recompute the query */
  protected synchronized void recomputeQuery() 
  {
    oqlQuery=computeQuery();
    attrParam=null;
  }

  /** check the orderBy or groupBy expressions to see if they are already selected, if not add a projections. Only group by and order by labels */
  String checkExpr(String str)
  {
    if(str==null)
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
  protected String computeQuery()
  {
    String groups= checkExpr((String)derivedSections[GROUPBY]);
    String orders= checkExpr((String)derivedSections[ORDERBY]);

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
    sb.append(" FROM ");
    sb.append(derivedSections[FROM]);
    Object o;
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
    return sb.toString();
  }

  // ------------
  /** execute the contained query in the given database */ 
  public Grouper execute(org.makumba.Database db, Attributes a) 
       throws LogicException
  {
    prepare(a);
    return new Grouper(getPreviousKeyset(), attrParam.execute(db, a).elements());
  }
  
  /** prepare the query */
  public synchronized void prepare(Attributes a)
       throws LogicException
  {
    if(oqlQuery==null)
      {
	prependFromToKeyset();
	recomputeQuery();
      }
    if(attrParam==null)
      {
	attrParam=new MultipleAttributeParametrizer(getOQLQuery(), a);
	setChanged();
	notifyObservers(null);
      }
  }

  /** return the OQL Query for debugging purposes*/
  protected String getOQLQuery()
  {
    return oqlQuery;
  }

  MultipleAttributeParametrizer fromTypes=null;

  /** check if an expression is valid, nullable or set  */
  public Object checkExpr(String s, Attributes a) 
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
      Object nl= checkId(s.substring(m, n), a);
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

  MultipleAttributeParametrizer getFromTypes(Attributes a)
  {
    if(fromTypes==null)
      {
	try{
	  fromTypes= new MultipleAttributeParametrizer("SELECT \" \" FROM "+derivedSections[FROM], a);
	}catch(LogicException e) 
	  { 
	    return null; // this will be an error later on 
	  }
      }
    return fromTypes;
  }
  /** check if an id is nullable, and if so, return the path to the null pointer */
  public Object checkId(String s, Attributes a) 
  {
    int dot=s.indexOf(".");
    if(dot==-1)
      return null;
    
    if(getFromTypes(a)==null)
	return null; // this will be an error later on 
    
    DataDefinition dd= fromTypes.getLabelType(s.substring(0, dot));
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
	dd=((org.makumba.abstr.FieldInfo)fd).getPointedType();
	dot=dot1;
      }
  }
}

