package org.makumba.commons;

/**
 * Simple interface that makes it possible to release singletons (by e.g. nullifying their static reference) for classes that
 * use Singletons so that GC can occur.
 * 
 * @author Manuel Gay
 * @version $Id: SingletonHolder.java,v 1.1 Jan 5, 2009 5:12:56 PM manu Exp $
 */
public interface SingletonHolder {
    
    public void release();

}
