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

package org.makumba.db;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DBError;
import org.makumba.DataTransformer;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.abstr.FieldHandler;
import org.makumba.abstr.RecordHandler;
import org.makumba.abstr.RecordInfo;

/** This is a generic database table RecordHandler. Upon building, it uses the rules in org.makumba/db/redirectHandler.properties :
<pre>
ptr=foreign
ptrRel=foreign
ptrOne=subtable
set=foreign
setcharEnum=subtable
setintEnum=subtable
setComplex=subtable
</pre>
... where foreignHandler and subtableHandler use different techniques to add their foreign table to this table's foreign table list, and then let the unrelying packages define their own handlers of the respective type, by adding DB to the type name (e.g. ptr will lead to ptrDB).
* @see org.makumba.db.Database#getTable(org.makumba.abstr.RecordInfo)
* @see org.makumba.db.foreignHandler
* @see org.makumba.db.subtableHandler
*/
public abstract class Table extends RecordHandler
{
  public Table(){}

  org.makumba.db.Database db;

  /** What database does this table belong to */
  public org.makumba.db.Database getDatabase() { return db; }

  /** set the RecordInfo */
  protected void setRecordInfo(RecordInfo ri){ super.setRecordInfo(ri); }

  Hashtable relatedTables= new Hashtable();

  /** get the related table for the field indicated by name (of any set or ptr type) */
  public Table getRelatedTable(String field)
  {
    return getDatabase().getTable((RecordInfo)relatedTables.get(field));
    //    return (Table)relatedTables.get(field);
  }

  public Enumeration getRelatedTableFields()
  {
    return relatedTables.keys();
  }

  /** does the table exist in the database ? */
  public abstract boolean canAdmin();

  /** does the table exist in the database ? */
  public abstract boolean exists();

  /** delete all the records created within the indicated database and return their number */
  public abstract int deleteFrom(DBConnection here, DBConnection source); 

  /** does the field exist in the database ? */
  public abstract boolean exists(String fieldName);

  String selectAllWithDbsv; 
  Object[] selectLimits= new Object[2];

  static final int BAR= 75;
  /** copies all records from the table1 to table2 */
  void copyFrom(DBConnection dest, Table source, DBConnection sourceDB) 
  {
    final String nm= getRecordInfo().getName();
    if(!source.exists()|| nm.equals("org.makumba.db.Catalog"))
      // catalog is never copied
      return;

    if(selectAllWithDbsv==null)
      {
        StringBuffer list=new StringBuffer();
	String comma="";
	
	for(Enumeration e=handlerOrder.elements(); e.hasMoreElements();)
	  {
	    list.append(comma);
	    comma=", ";
	    String name=((FieldHandler)e.nextElement()).getName(); 
	    list.append("t.").append(name);
	  }
	String indexName= getRecordInfo().getIndexName();
	selectAllWithDbsv= "SELECT "+list+" FROM "+nm+" t WHERE t."+indexName+ ">=$1 AND t."+indexName+" <=$2";

	final int dbsv=sourceDB.getHostDatabase().getDbsv();
	selectLimits[0]=new Pointer(){
	  public String getType(){ return nm; }
	  public long longValue(){ return dbsv<<MASK_ORDER; }
	};
	selectLimits[1]=new Pointer(){
	  public String getType(){ return nm; }
	  public long longValue(){ return ((dbsv+1)<<MASK_ORDER)-1;}
	};
      }    
      
    Vector v=sourceDB.executeQuery(selectAllWithDbsv, selectLimits);
    if(v.size()==0)
      {
	MakumbaSystem.getMakumbaLogger("db.admin.copy").info(nm+": no records to copy");
	return;
      }

    MakumbaSystem.getMakumbaLogger("db.admin.copy").info(nm+": starting copying "+v.size()+" records");
    
    
    System.out.print("|");
    for(int b=0; b<BAR; b++)
      System.out.print("-");
    System.out.print("|\n "); System.out.flush();
    float step=((float)v.size()/BAR);

    int stars=0;
    Hashtable data= new Hashtable(23);
    Hashtable nameKey= new Hashtable(23);

    for(int f=0; f<handlerOrder.size(); f++)
      nameKey.put("col"+(f+1), ((FieldHandler)handlerOrder.elementAt(f)).getName());

    for(int j=0; j<v.size(); j++)
      {
	Dictionary d= (Dictionary)v.elementAt(j);
	for(Enumeration e= d.keys(); e.hasMoreElements();)
	  {
	    Object k= e.nextElement();
	    data.put(nameKey.get(k), d.get(k));
	  }

	dest.insert(getRecordInfo().getName(), data);
	
	// free up some memory
	data.clear();
	v.setElementAt(null, j);

	// display progress bar
	int nstars= (int)(((float)j+1)/step);
	while(nstars>stars)
	  {
	    System.out.print("*"); 
	    System.out.flush();
	    stars++;
	  }
      }
    System.out.println();
  }
  
  /**
    Prepares everything needed for database management. identifies the database adapter that will be used, the type of connection manager, etc.
    Might call create.
    Looks if secondary tables (from a one-to-many, sets) need to be opened or created.
    Looks if the opened database actually respects the org.makumba file (if not, provides functionality to convert the database to the new format).
    Toto: Your brain is a mess where your stupidity is swimming.
    */
  protected abstract void open(Properties p) ;

  DataTransformer insertHook;

  void computeInsertHook()
  {
    if(insertHook==null)
      {
	String s=getDatabase().getConfiguration("insert#"+getRecordInfo().getName());
	if(s!=null)
	  {
	    try{
	      insertHook= (DataTransformer)Class.forName(s).newInstance();
	    }catch(Exception e) { throw new DBError(e);}
	  }
      }
  }

  /** insert a record, return the pointer to it */
  public Pointer insertRecord(DBConnection c, Dictionary d)
  {
    return insertRecordImpl(c, d);
  }

  public abstract Pointer insertRecordImpl(DBConnection c, Dictionary d);
}


