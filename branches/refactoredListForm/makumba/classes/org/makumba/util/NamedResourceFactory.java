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

package org.makumba.util;

/**
 * This models a factory for named resources. Normally a subclass should just implement
 * {@link #makeResource(java.lang.Object) makeResource(name)}, which is called whenever the
 * {@link util.NamedResources.html NamedResources} is requested an object that it doesn't hold. Still, there are some
 * special cases:
 * <ul>
 * <li> the {@link util.NamedResources.html NamedResources} keeps objects in a hashtable. If the key for that hashtable
 * corresponding to a certain name is not the name itself, the
 * {@link #getHashObject(java.lang.Object) getHashObject(name)} method should be redefined.
 * <li> if the process of constructing the resource needs the hash key, the
 * {@link #makeResource(java.lang.Object, java.lang.Object) makeResource(name, hashName)} method should be redefined,
 * instead of {@link #makeResource(java.lang.Object) makeResource(name)}
 * <li> if the process of constructing the resource needs other resources, that in turn might need the resource which is
 * just built, these resources should not be retreived in the makeResource() methods, since that would cause infinite
 * loops. makeResource() should just build the resource, and
 * {@link #configureResource(java.lang.Object, java.lang.Object, java.lang.Object) configureResource(name, hashName, resource)}
 * should be redefined to do further resource adjustments.
 * </ul>
 * 
 * @author Cristian Bogdan
 * @version $Id$
 * 
 */
public abstract class NamedResourceFactory implements java.io.Serializable {
    protected Object supplementary;

    /**
     * This method should make the resource with the given name.
     * 
     * All exceptions thrown here will be caught and thrown further as RuntimeWrappedExceptions. This method should not
     * lead to the request of the same resource from the NamedResources (by for example requesting another resource that needs
     * to refer this resource). Such actions should be performed in configureResource()
     * 
     * @param name the name of the resource to be made
     * @throws Throwable
     * @return the newly created resource
     */
    protected Object makeResource(Object name) throws Throwable {
        throw new RuntimeException("should be redefined");
    }

    /**
     * If the hash object for the resource is different from the name, this is the method used to build the named
     * resource. All exceptions throws here will be caught and thrown further as RuntimeWrappedExceptions. This method
     * should not lead to the request of the same resource from the NamedResources (by e.g. requesting another resource
     * that needs to refer this resource). Such actions should be performed in configureResource()
     * 
     * @param name name of the resource to be made
     * @param hashName name of the hash for the object
     * @throws Throwable
     * @return the newly created resource
     */
    protected Object makeResource(Object name, Object hashName) throws Throwable {
        return makeResource(name);
    }

    /**
     * This method builds the hash object from the name of the object. By defaault, it returns the name
     * @param name the name of the object
     * @throws Throwable
     * @return The hash object
     */
    protected Object getHashObject(Object name) throws Throwable {
        return name;
    }

    /**
     * This method is called immediately after the resource is built, but before making it accessible to other threads,
     * and before the resource being returned to the client that requested it.
     * @param name the name of the object
     * @param hashName the hash name of the object
     * @param resource the resource
     * @throws Throwable
     */
    protected void configureResource(Object name, Object hashName, Object resource) throws Throwable {
    }

}
