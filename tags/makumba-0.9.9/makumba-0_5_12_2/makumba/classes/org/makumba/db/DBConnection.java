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
import org.makumba.*;
import java.util.*;
import org.makumba.abstr.*;
import org.makumba.util.*;

public abstract class DBConnection implements org.makumba.Database
{
  protected org.makumba.db.Database db;
      
  protected DBConnection() {} // for the wrapper

  public DBConnection(Database database)
  { 
    this.db=database; 
  }

  public org.makumba.db.Database getHostDatabase(){ return db; }
  
  /** Get the name of the database in the form host[_port]_dbprotocol_dbname */
  public String getName(){ return db.getName(); }
  
  public abstract void close();
  
  public abstract void commit();

  public abstract void rollback();

  Map locks= new HashMap(13);
  Hashtable lockRecord= new Hashtable(5);
  
  public void lock(String symbol)
  {
    lockRecord.clear();
    lockRecord.put("name", symbol);
    locks.put(symbol, insert("org.makumba.db.Lock", lockRecord));
  }
  
  public void unlock(String symbol)
  {
    Pointer p= (Pointer)locks.get(symbol);
    if(p==null)
      throw new ProgrammerError(symbol+" not locked in connection "+ this);
    deleteLock(symbol);
  }

  protected void deleteLock(String symbol){
    locks.remove(symbol);
    // we need to delete after the lock name instead of the pointer
    // in order not to produce deadlock 
    delete("org.makumba.db.Lock l", "l.name=$1", symbol);
  }
  protected void unlockAll(){
    for(Iterator i= locks.keySet().iterator(); i.hasNext(); ){
      deleteLock((String)i.next());
    }
  }

  /** change the record pointed by the given pointer. Only fields indicated are changed to the respective values */
  public void update(Pointer ptr, java.util.Dictionary fieldsToChange)
  {
    DataHolder dh= new DataHolder(this,  fieldsToChange, ptr.getType());
    dh.checkUpdate();
    dh.update(ptr);
  }

  public Dictionary read(Pointer p, Object flds)
  {
    Enumeration e=null;
    if(flds==null)
      {
	RecordInfo ri= RecordInfo.getRecordInfo(p.getType());
	Vector v= new Vector();
	for(Enumeration f=ri.getFieldNames().elements(); f.hasMoreElements(); )
	  {
	    String s=(String)f.nextElement();
	    if(ri.getKeyIndex().get(s)!=null)
	      v.addElement(s);
	  }
	e=v.elements();
      }
    else if (flds instanceof Vector)
      e=((Vector)flds).elements();
    else if (flds instanceof Enumeration)
      e=(Enumeration)flds;
    else if( flds instanceof String[])
      {
	Vector v=new Vector();
	String [] fl=(String[])flds;
	for(int i=0; i<fl.length; i++)
	  v.addElement(fl[i]);
	e=v.elements();
      }
    else throw new InvalidValueException("read() argument must be Enumeration, Vector, String[] or null");
    StringBuffer sb= new StringBuffer();
    sb.append("SELECT ");
    String separator="";
    while(e.hasMoreElements())
      {
	String s=RecordInfo.getRecordInfo(p.getType()).checkFieldName(e.nextElement());
	sb.append(separator).append("p.").append(s).append(" as ").append(s);
	separator=",";
      }
    sb.append(" FROM "+p.getType()+" p WHERE p=$1");
    Object [] params= {p};
    Vector v= executeQuery(sb.toString(), params);
    if(v.size()==0)
      return null;
    if(v.size()>1)
      throw new org.makumba.MakumbaError("MAKUMBA DATABASE INCOSISTENT: Pointer not unique: "+p);
    return (Dictionary)v.elementAt(0); 
  }

  /** insert a record*/
  public Pointer insert(String type, Dictionary data)
  {
    Table t= db.getTable(type);
    t.computeInsertHook();
    
    if(t.insertHook!=null)
      {
	Hashtable h= new Hashtable();
	for(Enumeration e= data.keys(); e.hasMoreElements(); )
	  {
	    Object k= e.nextElement();
	    h.put(k, data.get(k));
	  }
	data=h;
      }

     if(t.insertHook==null || t.insertHook.transform(data,this))
	{
	  DataHolder dh= new DataHolder(this, data, type);
	  dh.checkInsert();
	  return dh.insert();
	}
     return null;
  }

  Object[] treatParam(Object args)
  {
    if(args instanceof Vector)
      {
	Vector v= (Vector)args;
	Object[] param=new Object[v.size()];
	v.copyInto(param);
	return param;
      }
    else if(args instanceof Object[])
      return (Object[])args;
    else
      {
	Object p[] = {args};
	return p;
      }
  }

  /** Execute a parametrized OQL query.
   * @return a Vector of Dictionaries */
  public java.util.Vector executeQuery(String OQL, Object args, int offset, int limit)
  {
    return ((Query)getHostDatabase().queries.getResource(OQL)).execute(treatParam(args), this, limit, offset); 
  }
  public java.util.Vector executeQuery(String OQL, Object args){
    return executeQuery(OQL, args, 0, -1);
  }

  /** Execute a parametrized update or delete. A null set means "delete"
   * @return a Vector of Dictionaries */
  public int executeUpdate(String type, String set, String where, Object args)
  {
    Object []multi= { type, set, where };

    return ((Update)getHostDatabase().updates.getResource(multi)).execute(this, treatParam(args)); 
  }

  /** Insert a record in a subset (1-N set) or subrecord (1-1 pointer) of the given record. For 1-1 pointers, if another subrecord existed, it is deleted.
   * @return a Pointer to the inserted record */ 
  public Pointer insert(Pointer base, String field, java.util.Dictionary data)
  {
    FieldInfo fi=RecordInfo.getRecordInfo(base.getType()).getField(field);
    
    if(fi.getType().equals("setComplex")) 
      {
	data.put(fi.getSubtable().getMainTablePointerName(), base);
	return insert(fi.getSubtable().getName(), data);
      }
    else throw new InvalidFieldTypeException(fi, "subset");
  }

  /** Delete the record pointed by the given pointer. If the pointer is a 1-1, the oringinal is set to null. All the subrecords and subsets are automatically deleted. */
  public void delete(Pointer ptr)
  {
    RecordInfo ri= RecordInfo.getRecordInfo(ptr.getType());
    FieldDefinition fi= ri.getParentField();

    // if this is a ptrOne, we nullify the pointer in the parent record
    if(fi!=null && fi.getType().equals("ptrOne"))
      executeUpdate(fi.getDataDefinition().getName()+" this", "this."+fi.getName()+"=nil", "this."+fi.getName()+"=$1", ptr);

    // then we do the rest of the delete job
    delete1(ptr);
  }

  void delete1(Pointer ptr)
  {
    RecordInfo ri= RecordInfo.getRecordInfo(ptr.getType());
    Object param[]={ptr};
    
    // delete the ptrOnes
    Vector ptrOnes= new Vector();

    for(Enumeration e=ri.getFieldNames().elements(); e.hasMoreElements(); )
      {
	String s= (String)e.nextElement();
	if(ri.getField(s).getType().equals("ptrOne"))
	  ptrOnes.addElement(s);
      }

    if(ptrOnes.size()>0)
      {
	Dictionary d=read(ptr, ptrOnes);
	for(Enumeration e= d.elements(); e.hasMoreElements(); )
	  delete1((Pointer)e.nextElement());
      }
    // delete all the subfields
    for(Enumeration e= ri.getFieldNames().elements(); e.hasMoreElements(); )
      {
	FieldInfo fi= ri.getField((String)e.nextElement());
	if(fi.getType().startsWith("set"))
	  if(fi.getType().equals("setComplex"))
	    executeUpdate(fi.getSubtype().getName()+" this", null, 
			  "this."+fi.getSubtype().getFieldDefinition(3).getName()+"= $1", param);
	  else deleteSet(ptr, fi);
      }    
    // delete the record
    executeUpdate(ptr.getType()+" this", null, "this."+MakumbaSystem.getDataDefinition(ptr.getType()).getIndexPointerFieldName()+"=$1", ptr);
  }

  // delete a set
  void deleteSet(Pointer base, FieldInfo fi)
  {
    executeUpdate(fi.getSubtable().getName()+" this", null, "this."+fi.getSubtable().getMainTablePointerName()+"=$1", base);
  }

  /** Update the given external set */
  void updateSet(Pointer base, String field, Object val)
  {
    FieldInfo fi= RecordInfo.getRecordInfo(base.getType()).getField(field);
    if(!fi.getType().equals("set") && !fi.getType().equals("setintEnum") && !fi.getType().equals("setcharEnum"))
      throw new InvalidFieldTypeException(fi, "set");
    
    deleteSet(base, fi);
    if(val==null || val == Pointer.NullSet || ((Vector)val).size()==0)
      return;
    Vector values=(Vector)val;

    Dictionary data= new Hashtable(10);
    data.put(fi.getSubtable().getMainTablePointerName(), base);
    for(Enumeration e= values.elements(); e.hasMoreElements(); )
      {
	data.put(fi.getSubtable().getSetMemberFieldName(), e.nextElement());
	db.getTable(fi.getSubtable()).insertRecord(this, data);
      }
  }

    /* update in the form update("general.Person p", "p.birthdate=$1", "p=$2", params)
     * NOTE that this method does not delete subrecords if their pointers are nullified
     * @return the number of records affected
     */
  public int update(String from, String set, String where, Object parameters)
    {
	return executeUpdate(from, set, where, parameters);
    }


    /* delete in the form delete("general.Person p", "p=$1", params)
     * NOTE that this method does not delete subsets and subrecords
     * @return the number of records affected
     */
  public int delete(String from, String where, Object parameters)
    {
	return executeUpdate(from, null, where, parameters);
    }
}


class DataHolder
{
  DBConnection d;
  Table t;
  Dictionary dt= new Hashtable();
  Dictionary others= new Hashtable(); // contains data holders
  Dictionary sets= new Hashtable();   // contains vectors

  DataHolder(DBConnection d, Dictionary data, String type)
  {
    this.d=d;
    t=d.db.getTable(RecordInfo.getRecordInfo(type).getName());
    
    for(Enumeration e=data.keys(); e.hasMoreElements(); )
       {
	 Object o= e.nextElement();
	 dt.put(o, data.get(o));
       }
     
    for(Enumeration e=data.keys(); e.hasMoreElements(); )
      {
	Object o= e.nextElement();
	if(!(o instanceof String))
	  throw new org.makumba.NoSuchFieldException(t.getRecordInfo(), "Dictionaries passed to makumba DB operations should have String keys. Key <"+o+"> is of type "+o.getClass()+t.getRecordInfo().getName());
	String s=(String)o;
	int dot= s.indexOf(".");
	if(dot==-1)
	  {
	    FieldInfo fi=t.getRecordInfo().getField(s);
	    if(fi==null)
	      throw new org.makumba.NoSuchFieldException(t.getRecordInfo(), (String)o);
	    if(fi.getType().equals("set") ||
	       fi.getType().equals("setintEnum")||
	       fi.getType().equals("setcharEnum")
	       )
	      {
		Object v= dt.remove(s);
		fi.checkValue(v);
		sets.put(s, v);
	      }
	  }
	else
	  {
	    String fld=s.substring(0, dot);
	    Dictionary oth= (Dictionary)others.get(fld);
	    if(oth==null)
	      others.put(fld, oth= new Hashtable());
	    oth.put(s.substring(dot+1), dt.remove(s));
	  }
      }
    Dictionary others1= others;
    others=new Hashtable();
    for(Enumeration e=others1.keys(); e.hasMoreElements(); )
      {
	String fld= (String)e.nextElement();
	FieldHandler fh=t.getFieldHandler(fld);
	if(fh==null)
	  throw new org.makumba.NoSuchFieldException(t.getRecordInfo(), fld);
	if(dt.get(fld)!=null)
	  throw new org.makumba.InvalidValueException(fh.getFieldInfo(), "you cannot indicate both a subfield and the field itself. Values for "+fld+"."+others.get(fld)+" were also indicated");
	if(!fh.getType().equals("ptrOne") &&  (!fh.isNotNull() || ! fh.isFixed()))
	  throw new InvalidFieldTypeException(fh.getFieldInfo(), "subpointer or base pointer, so it cannot be used for composite insert/edit");
	others.put(fld, new DataHolder(d, (Dictionary)others1.get(fld), fh.getPointedType().getName()));
      }
  }

  public String toString()
  {
    return "data: "+dt+" others: "+others;
  }

  void checkInsert()
  {
    for(Enumeration e=others.elements(); e.hasMoreElements(); )
      ((DataHolder)e.nextElement()).checkInsert();
    t.checkInsert(dt, others);
  }

  void checkUpdate()
  {
    for(Enumeration e=others.elements(); e.hasMoreElements(); )
      ((DataHolder)e.nextElement()).checkUpdate();
    t.checkUpdate(dt, others);
  }

  Pointer insert()
  {
    // insert the other pointers
    for(Enumeration e=others.keys(); e.hasMoreElements(); )
      {
	String fld=(String)e.nextElement();
	dt.put(fld, ((DataHolder)others.get(fld)).insert());
      }
    // insert the record
    Pointer p=t.insertRecord(d, dt);
    // insert the sets

    for(Enumeration e= sets.keys(); e.hasMoreElements();)
      {
	String fld=(String)e.nextElement();
	d.updateSet(p, fld, sets.get(fld));
      }
    return p;
  }
  
  void update(Pointer p)
  {
    // see if we have to read some pointers
    Vector ptrsx= new Vector();
    // we have to read the "other" pointers
    for(Enumeration e= others.keys(); e.hasMoreElements(); )
      ptrsx.addElement(e.nextElement());
    // we might have to read the ptrOnes that are nullified
    for(Enumeration e=dt.keys(); e.hasMoreElements(); )
      {
	String s= (String)e.nextElement();
	if(dt.get(s).equals(Pointer.Null) && t.getRecordInfo().getField(s).getType().equals("ptrOne"))
	  ptrsx.addElement(s);
      }
    // read the pointers if there any to read
    Dictionary ptrs=null;
    if(ptrsx.size()>0)
      ptrs= d.read(p, ptrsx);

    // update others
    for(Enumeration e= others.keys(); e.hasMoreElements(); )
      {
	String fld=(String)e.nextElement();
	Pointer ptr= (Pointer)ptrs.remove(fld);
	if(ptr==null || ptr==Pointer.Null)
	  dt.put(fld, ((DataHolder)others.get(fld)).insert());
	else
	  ((DataHolder)others.get(fld)).update(ptr);	
      }

    // rest of ptrs should be ptrOnes to delete
    if(ptrs!=null)
      for(Enumeration e= ptrs.elements(); e.hasMoreElements(); )
	d.delete1((Pointer)e.nextElement());

    Object[] params= new Object[dt.size()+1];
    params[0]=p;
    int n=1;
    String set="";
    String comma="";
    for(Enumeration upd= dt.keys(); upd.hasMoreElements(); )
      {
	  set+=comma;
	String s=(String)upd.nextElement();
	params[n++]=dt.get(s);
	set+="this."+s+"=$"+n;
	comma=",";
      }
    if(set.trim().length()>0)
      d.executeUpdate(t.getRecordInfo().getName()+" this", set, "this."+MakumbaSystem.getDataDefinition(p.getType()).getIndexPointerFieldName()+"=$1", params);

    for(Enumeration e= sets.keys(); e.hasMoreElements();)
      {
	String fld=(String)e.nextElement();
	d.updateSet(p, fld, sets.get(fld));
      }    
  }
}

