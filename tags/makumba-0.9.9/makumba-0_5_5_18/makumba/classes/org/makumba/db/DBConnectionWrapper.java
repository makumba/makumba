package org.makumba.db;
import org.makumba.*;

/** a wrapper for dbconnections, used to provide a temporary that holds a reference to a permanent DBConnection */
public class DBConnectionWrapper extends DBConnection
{
    DBConnection wrapped;
    public DBConnection getWrapped(){ return wrapped; }

    DBConnectionWrapper(){}
    DBConnectionWrapper(DBConnection wrapped){this.wrapped=wrapped; }

    public String getName(){ return getWrapped().getName(); }

    public Database getHostDatabase(){ return getWrapped().getHostDatabase(); }

    public java.util.Dictionary read(Pointer ptr, Object fields)
    { return getWrapped().read(ptr, fields); }
    
    public java.util.Vector executeQuery(String OQL, Object parameterValues)
    { return getWrapped().executeQuery(OQL, parameterValues); }
    
    public Pointer insert(String type, java.util.Dictionary data)
    { return getWrapped().insert(type, data); }
    
    public Pointer insert(Pointer host, String subsetField, java.util.Dictionary data)
    {return getWrapped().insert(host, subsetField, data); }

    public void update(Pointer ptr, java.util.Dictionary fieldsToChange)
    { getWrapped().update(ptr, fieldsToChange); }
    
    public int update(String from, String set, String where, Object parameters)
    { return getWrapped().update(from, set, where, parameters); }
    
    public void delete(Pointer ptr)
    { getWrapped().delete(ptr); }
    
    public int delete(String from, String where, Object parameters)
    { return getWrapped().delete(from, where, parameters); }
    
    public synchronized void close(){ 
	getHostDatabase().connections.put(getWrapped()); 
	wrapped=ClosedDBConnection.singleton;
    }	
    protected synchronized void finalize(){ 
	if(wrapped!=ClosedDBConnection.singleton)
	    close();
    }
}

class ClosedDBConnection extends DBConnectionWrapper
{
    static DBConnection singleton= new ClosedDBConnection();
    public DBConnection getWrapped() { throw new IllegalStateException("connection already closed"); }
}

