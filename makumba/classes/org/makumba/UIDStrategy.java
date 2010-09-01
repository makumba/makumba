package org.makumba;

/**
 * Strategy class that makes it possible to to adapt UIDs for {@link Pointer}-s.
 * 
 * @author manu
 * @version $Id: UIDStrategy.java,v 1.1 Sep 1, 2010 10:56:19 AM manu Exp $
 */
public interface UIDStrategy {

    /**
     * Hook to modify the UID when it's being read from a source.
     * 
     * @return a long representing the unique identifier of an object
     */
    public abstract long readFrom(String type, long n);

    /**
     * Hook to modify the UID when it's being written out
     * 
     * @param n
     *            the UID to write
     * @return the converted UID value
     */
    public abstract long writeTo(String type, long n);

}
