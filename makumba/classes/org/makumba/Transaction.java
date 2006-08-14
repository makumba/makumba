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

/** This class models operations with a database.  To obtain such an object, use methods from {@link MakumbaSystem}. <p>
  Stricly speaking this class represents a database connection (later on, transaction). Obtaining more such objects for the same database configurations will result in opening more connections. Connections must be given back to the system using the {@link #close()} method. That will be done automatically by the object's finalizer. In makumba business logic, connections passed to the BL methods are automatically closed by the system after the BL operations (including eventual automatic DB acceses) were completed. To open a "sibling" of a connection <i>conn</i> of this type, use MakumbaSystem.getConnectionTo(<i>conn</i>.getName()). In most cases, you will have to close the sibling yourself.<p>
 * At the level of this API, data is represented as java.util.Dictionary, both for reading and writing. Most methods throw {@link DBError} if a fatal database error occurs. If the connection to the database is lost, an attempt is made to reconnect before throwing a {@link DBError}.<P>
 * All methods throw subclasses of either Error or RuntimeException, so nothing needs to be caught explicitely.
 * @see org.makumba.MakumbaSystem#getDefaultDatabaseName()
 * @see org.makumba.MakumbaSystem#getDefaultDatabaseName(java.lang.String)
 * @see org.makumba.MakumbaSystem#getConnectionTo(java.lang.String)
 * @since makumba-0.5
 */
public interface Transaction extends Database
{
    /** Read fields of a record.
     * Database querying is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.query.compilation", "db.query.execution", "db.query.performance"</code></b> loggers, with {@link java.util.logging.Level#INFO} logging level. "db.query.execution" also logs {@link java.util.logging.Level#SEVERE} fatal errors.
     * @param ptr the pointer to the record
     * @param fields the fields to read, or null to read all fields. Can be an String[] or a Vector
     * @return a Dictionary, containing a name-value pair for each non-null field, or null if the record doesn't exist
     * @exception ClassCastException if the fields argument is not String[] or Vector
     * @exception org.makumba.DBError if a fatal database error occurs
     * @exception IllegalStateException if the connection was already closed
     */
    public java.util.Dictionary read(Pointer ptr, Object fields);

    /** Get the name of the database in the form host[_port]_dbprotocol_dbname */
    public String getName();
    
    /** Execute a parametrized OQL query.   
     * Queries are pre-compiled and cached in the database, so they should be parametrized as much as possible.
     * Database querying is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.query.compilation", "db.query.execution", "db.query.performance"</code></b> loggers, with {@link java.util.logging.Level#INFO} logging level. "db.query.execution" also logs {@link java.util.logging.Level#SEVERE} fatal errors.<br>
     * @param OQL the OQL query to execute. Refers to parameters as $1, $2 ...
     * @param parameterValues the parameter values. Should be null if there are no parameters. If there is only one parameter, it can be indicated directly. If there are more parameters, they can be indicated in a Object[] or a java.util.Vector
     * @param limit the maximum number of records to return, -1 for all
     * @param offset the offset of the first record to return, 0 for first
     * @return a Dictionary, containing a name-value pair for each non-null SELECT column. If a certain SELECT column is not named using AS, it will be automatically named like col1, col2, etc. 
     * @exception org.makumba.DBError if a fatal database error occurs
     * @exception org.makumba.OQLParseError if the OQL is not correct
     * @exception InvalidValueException in case of makumba type conflict between a pointer value passed as parameter and the type expected in the respective OQL expression
     * @exception IllegalStateException if the connection was already closed
     */
    public java.util.Vector executeQuery(String OQL, Object parameterValues, int offset, int limit);
    
    /** Execute query without limiting the results. 
     * @see org.makumba.Transaction#executeQuery(java.lang.String,java.lang.Object,int,int) */
    public java.util.Vector executeQuery(String OQL, Object parameterValues);

    /** Insert a record of the given type. <BR>
     * Database update is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.update.execution", "db.update.performance"</code></b> loggers, with {@link java.util.logging.Level#INFO} logging level. "db.update.execution" also logs {@link java.util.logging.Level#SEVERE} fatal errors.
     <p>Special makumba fields are treated as follows:<ul>
     <li> External sets (sets of records in other tables), as well as sets of int and char can be inserted as Vectors.<BR>
     <li> Base records and subrecords can be inserted as well. Base records are indicated by fixed, notnull pointers and the record constitutes logical extensions of its base records (this is called subtyping in database theory). 
     <li>Subrecords are indicated by 1-1 pointers, and are a logical extension of the record. 
     <li>Both base record fields and subrecord fields can be referred to via their pointer, like P.field, where P is a pointer to the base record or a 1-1 pointer. By extension P1.P2.field notations can exist.
     </ul>
     * @param type the makumba type to create a new record for
     * @param data the field-value mapping for the new record. <br>
     The ommitted fields will be set to null.<br>
     To insert an set of integets (set int {...}) pass a Vector of Integers. (or null, or an empty vector).<br>
     To insert an set of strings (set char {...}) pass a Vector of String. (or null, or an empty vector). <br>
     To refer to a field of base record or subrecord, indicate the pointer that leads to the record, and the respective field, like ptr1.ptr2.field. Every mentioned base record and subrecord will be inserted. 
     * @return a Pointer to the inserted record 
     * @exception DBError if a fatal database error occurs
     * @exception DataDefinitionNotFoundError if the indicated type is not found
     * @exception InvalidValueException if a certain field does not accept the given value
     * @exception InvalidValueException in case of makumba type conflict between a pointer value passed and the definition of the respective field
     * @exception java.lang.ClassCastException in case of java type conflict between a value passed and the definition of the respective field
     * @exception IllegalStateException if the connection was already closed
     */ 
    public Pointer insert(String type, java.util.Dictionary data);

    /** Insert a record in a subset (1-N set) of the given record. <br>
     * Database update is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.update.execution", "db.update.performance"</code></b> loggers, with {@link java.util.logging.Level#INFO} logging level. "db.update.execution" also logs {@link java.util.logging.Level#SEVERE} fatal errors.<br
     <p>Special makumba fields are treated as follows:<ul>
      <li>The new member's external sets (sets of records in other tables) as well as sets of int and char can be inserted as Vectors.<BR>
      <li>The new member can have subrecords, but cannot have base records.
      </ul>
     * @return a Pointer to the inserted record 
     * @param host a pointer to the host record, to which a subrecord will be added
     * @param subsetField the name of the subrecord field.
     * @param data the field-value mapping for the new subrecord. <br> 
     The ommitted fields will be set to null. <br>
     To insert an external set, pass a Vector of Pointers (or null, or an empty vector). <br>
     To insert an set of integets (set int {...}) pass a Vector of Integers. (or null, or an empty vector).<br>
     To insert an set of strings (set char {...}) pass a Vector of String. (or null, or an empty vector). <br>
     To refer to a field of a subrecord (subset members cannot have base records), indicate the pointer that leads to the record, and the respective field, like ptr1.ptr2.field. 
     * @exception DBError if a fatal database error occurs
     * @exception InvalidValueException if a certain value is not valid for a field
     * @exception InvalidValueException in case of makumba type conflict between a pointer value passed and the definition of the respective field
     * @exception java.lang.ClassCastException in case of java type conflict between a value passed and the definition of the respective field
     * @exception IllegalStateException if the connection was already closed
     */ 
    public Pointer insert(Pointer host, String subsetField, java.util.Dictionary data);

    /** Insert the results of the query in the given type. Generates an INSERT...SELECT
     * @param type the type where to insert
     * @param OQL the OQL query to execute. Refers to parameters as $1, $2 ...
     * @param parameterValues the parameter values. Should be null if there are no parameters. If there is only one parameter, it can be indicated directly. If there are more parameters, they can be indicated in a Object[] or a java.util.Vector
     * @return the number of records inserted */
    public int insertFromQuery(String type, String OQL, Object parameterValues);

    /** Change the record pointed by the given pointer. Only fields indicated as keys in fieldsToChange are changed to the respective values. <BR>
     * Database update is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.update.execution", "db.update.performance"</code></b> loggers, with {@link java.util.logging.Level#INFO} logging level. "db.update.execution" also logs {@link java.util.logging.Level#SEVERE} fatal errors.<br>
     <p>Special makumba fields are treated as follows:<ul>
     <li>External sets (sets of records in other tables), as well as sets of intger or char, can be indicated for change as Vectors.<BR>
      <li>Base records and subrecords can be changed as well. Base records are indicated by fixed, notnull pointers and the record constitutes logical extensions of its base records (this is called subtyping in database theory). 
      <li>Subrecords are indicated by 1-1 pointers, and are a logical extension of the record. Both base record fields and subrecord fields can be referred to via their pointer, like P.field, where P is a pointer to the base record or a 1-1 pointer. By extension P1.P2.field notations can exist.
      </ul>
     * @param ptr pointer to the record to update
     * @param fieldsToChange key-value pairs for the fields to modify. <br>
     To nullify a field, pass the respective Null value from the Pointer class. <br>
     To change an external set, pass a Vector of Pointers (an empty vector will empty the set). <br>
     To change a set of integrers (set int{...}), pass a Vector of Integers (an empty vector will empty the set). <br>
     To change a set of integrers (set char{...}), pass a Vector of Strings (an empty vector will empty the set). <br>
     To refer to a field of a base record or subrecord, indicate the pointer that leads to the record, and the respective field, like ptr1.ptr2.field. 
     * @exception DBError if a fatal database error occurs
     * @exception InvalidValueException if a certain value is not valid for a field
     * @exception InvalidValueException in case of makumba type conflict between a pointer value passed and the definition of the respective field
     * @exception java.lang.ClassCastException in case of java type conflict between a value passed and the definition of the respective field
     * @exception IllegalStateException if the connection was already closed
     * @see Pointer#Null
     * @see Pointer#NullInteger
     * @see Pointer#NullString
     * @see Pointer#NullText
     * @see Pointer#NullDate
     * @see Pointer#NullSet
     */
    public void update(Pointer ptr, java.util.Dictionary fieldsToChange);

    /** Update in the form <code>update("general.Person p", "p.birthdate=$1", "p=$2", params)</code> . <br>
     * Database update is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.update.execution", "db.update.performance"</code></b> loggers, with {@link java.util.logging.Level#INFO} logging level. "db.update.execution" also logs {@link java.util.logging.Level#SEVERE} fatal errors.<br>
     * NOTE that this method does not delete subrecords if their pointers are nullified
     * @exception IllegalStateException if the connection was already closed
     * @exception DBError if a fatal database error occurs
     * @param from a makumba type in which update will take place, for example "general.Person p"
     * @param set the assignments made by the update, as OQL expression e.g. "p.birthdate=$1". Use "nil" for null assignments.
     * @param where the OQL conditions selecting the objects on which the update will be made, e.g. "p=$2"
     * @param parameterValues the parameter values. Should be null if there are no parameters. If there is only one parameter, it can be indicated directly. If there are more parameters, they can be indicated in a Object[] or a java.util.Vector
     * @return the number of records affected
     * @since makumba-0.5.5
     */
    public int update(String from, String set, String where, Object parameterValues);

    /** Delete the record pointed by the given pointer. If the pointer is a 1-1, the pointer in the base record is set to null. All the subrecords and subsets are automatically deleted. <br>
     * Database update is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.update.execution", "db.update.performance"</code></b> loggers, with {@link java.util.logging.Level#INFO} logging level. "db.update.execution" also logs {@link java.util.logging.Level#SEVERE} fatal errors.<br>
     * @exception DBError if a fatal database error occurs
     * @exception IllegalStateException if the connection was already closed
     * @param ptr the pointer to the record to be deleted
     */
    public void delete(Pointer ptr);

    /** Delete in the form <code>delete("general.Person p", "p=$1", params)</code> . <br>
     * Database update is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.update.execution", "db.update.performance"</code></b> loggers, with {@link java.util.logging.Level#INFO} logging level. "db.update.execution" also logs {@link java.util.logging.Level#SEVERE} fatal errors.<br>
     * NOTE that this method does not delete subsets and subrecords
     * @return the number of records affected
     * @param from a makumba type in which delete will take place, for example "general.Person p"
     * @param where the OQL conditions selecting the objects to be deleted, e.g. "p=$1"
     * @param parameterValues the parameter values. Should be null if there are no parameters. If there is only one parameter, it can be indicated directly. If there are more parameters, they can be indicated in a Object[] or a java.util.Vector
     * @return the number of records affected
     * @exception DBError if a fatal database error occurs
     * @exception IllegalStateException if the connection was already closed
     * @since makumba-0.5.5
     */
    public int delete(String from, String where, Object parameterValues);

    /** Commit the transaction associated with this connection. Normally, simply closing the connection will do, but more often committs may be needed. The business logic manager will close (and therefore commit) all transaction that it provides for usage.*/
   public void commit();

    /** Give this connection back to the system. This will automatically commit the transaction if it was not committed. A connection cannot be used after closing.
     * @exception DBError if a fatal database error occurs
     * @exception IllegalStateException if the connection was already closed
     */  
    public void close();

    /** Rollback the transaction associated with this connection. Typically rollback should be doneif an error occurs in a business logic operation. The business logic manager will rollback a transaction that it provided for usage if it catches any exception during the business logic execution.*/
    public void rollback();
    
    /** Acquire a lock associated to the given application-specific symbol. This method will block as long as the lock is already taken on another Database object. The commit() and rollback() methods unlcok all locks acquired on this connection */
    public void lock(String symbol);
    
    /** Free the lock on the given symbol, if any exists. This will allow the continuation of  a thread that needs a lock on the same symbol and uses another Database object*/
    public void unlock(String symbol);
}



