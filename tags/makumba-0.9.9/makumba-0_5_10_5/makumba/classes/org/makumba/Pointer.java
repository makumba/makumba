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

package org.makumba;

/** This class represents an abstract makumba pointer.
 * It is up to the concrete database to represent it. Pointer values are returned by the database  inserts and queries, they cannot be constructed explicitely.
 */
public class Pointer implements java.io.Serializable
{
  protected Pointer(){}

  static final protected int MASK_ORDER=24; 

  protected long n;
  protected String type;

  /** null constant for pointers */
  public static Object Null= new NullObject("null");

  /** null constant for integers */
  public static Object NullInteger= new NullObject("null integer");

  /** null constant for real numbers */
  public static Object NullReal= new NullObject("null real");

  /** null constant for characters */
  public static Object NullString= new NullObject("null char");

  /** null constant for texts */
  public static Object NullText= new NullObject("null text");

  /** null constant for dates */
  public static Object NullDate= new NullObject("null date");

  /** null constant for external sets */
  public static Object NullSet= new NullObject("null set");

  /** Get the database identifier of the database where the pointed record was created */
  public int getDbsv() { return (int)(n >>MASK_ORDER); }

  /** Get the unique index of the pointer within the parent database and the respective type */
  public int getUid() { return (int)(n & ((1l<<MASK_ORDER)-1)); }

  /** Get the makumba type of the pointed object */
  public String getType() {return type; } 

  /** generate a printable format */
  public String toString(){ return getType()+"["+getDbsv()+":"+getUid()+"]"; }

  static long crc(long v)
  {
    long r=0;
    for(int i=0; i<32; i++)
      {
	if((v&1) ==1)
	  r++;
	v=v>>1;
      }
    return r;
  }

  /** encode in external format */
  public String toExternalForm()
  {
    long hc= (long)type.hashCode() & 0xffffffffl;
    return Long.toString((crc(n)& 0xfl)<<32| n^hc,Character.MAX_RADIX);
  }

  public Pointer(String type, String externalForm)
  {
    this.type=type;
    long hc= (long)type.hashCode()& 0xffffffffl;
    long l= 0l;
    try{
      l=Long.parseLong(externalForm, Character.MAX_RADIX);
    }catch(NumberFormatException nfe)
      { throw new InvalidValueException("invalid pointer value: "+externalForm);}
    n= l&0xffffffffl;
    n= n^hc ;
    if(l>>32!=((long)crc(n)&0xfl))
      throw new InvalidValueException("invalid external pointer for type "+type+" : "+externalForm);
  }

  /** see if this Pointer is equal with the object provided */
  public boolean equals(Object o)
  {
    if(!(o instanceof Pointer))
      return false;
    Pointer p= (Pointer)o;
    if(!p.getType().equals(getType()))
      {
	System.err.println("warning: "+new InvalidValueException(this, p));
	return false;
      }
    return p.getDbsv()==getDbsv()&& p.getUid()==getUid();
  }
  
  /** A hash code for this pointer */
  public int hashCode(){ return (""+longValue()).hashCode()*getType().hashCode();}

  /** Utility method to combine DBSV and UID in one long value */
  public long longValue(){ return n; }

}

class NullObject
{
  String s;
  NullObject(String s){this.s= s; }
  public String toString() {return s; }
}

