package org.makumba.providers;

import java.util.Dictionary;

import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;

/**
 * This interface describes the operations that a simple CRUD provider has to implement.
 * 
 * @author Manuel Gay
 * @version $Id: CRUDOperationProvider.java,v 1.1 01.11.2007 15:30:29 Manuel Exp $
 */
public interface CRUDOperationProvider {
    
    /**
     * Inserts data of a given type
     * @param t the Transaction handling the insertion
     * @param type the type of the object to be inserted
     * @param data the data to be inserted
     * @return a Pointer corresponding to the place where the record has been inserted
     */
    public Pointer insert(Transaction t, String type, Dictionary data);
    
    /**
     * Reads a record 
     * @param t the Transaction used to read the record
     * @param ptr the Pointer corresponding to the place at which we should read
     * @param fields the fields to read
     * @return a Dictionary holding the values of the fields
     */
    public Dictionary read(Transaction t, Pointer ptr, Object fields);
    
    /**
     * Updates a record with the given data
     * @param t the Transaction handling the update
     * @param ptr the Pointer corresponding to the place at which we should update
     * @param data a Dictionary holding the the fields to be updated
     */
    public void update(Transaction t, Pointer ptr, Dictionary data);
    
    /**
     * Deletes a record
     * @param t the Transaction handling the deletion
     * @param ptr the Pointer to the record to be deleted
     */
    public void delete(Transaction t, Pointer ptr);
    
    /**
     * Updates a set of values
     * @param t the Transaction handling the update
     * @param base the Pointer to the base object which has the set
     * @param fi the FieldDefinition corresponding to the set
     * @param val the data to be updated, hold in a Vector
     */
    public void updateSet(Transaction t, Pointer base, FieldDefinition fi, Object val);
    
    /**
     * Deletes a set
     * @param t the Transaction handling the deletion
     * @param base the base pointer to the object to be deleted
     * @param fi the FieldDefinition of the field containing the set
     */
    public void deleteSet(Transaction t, Pointer base, FieldDefinition fi);
    
    /**
     * Checks if a set of values can be updated in the database
     * @param t
     * @param type the type of the base object to insert to
     * @param fieldsToCheck the values to be checked
     * @param fieldsToIgnore the values of toCheck not to be checked
     * @param allFields the entire data to be inserted
     */
    public void checkInsert(Transaction t, String type, Dictionary fieldsToCheck, Dictionary fieldsToIgnore, Dictionary allFields);
    
    /**
     * Checks if a set of values can be updated in the database
     * @param t
     * @param type the type of the base object to insert to
     * @param pointer the pointer to the record to be updated
     * @param fieldsToCheck the values to be checked
     * @param fieldsToIgnore the values of toCheck not to be checked
     * @param allFields the entire data to be inserted
     */
    public void checkUpdate(Transaction t, String type, Pointer pointer, Dictionary fieldsToCheck, Dictionary fieldsToIgnore, Dictionary allFields);
    
}
