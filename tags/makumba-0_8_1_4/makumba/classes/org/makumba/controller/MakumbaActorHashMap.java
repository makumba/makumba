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
//  $Id: Logic.java 2918 2008-07-25 14:10:23Z rosso_nero $
//  $Name$
/////////////////////////////////////
package org.makumba.controller;

import java.util.HashMap;

/**
 * A subclass of a {@link HashMap}, to return a unique type of map in
 * {@link Logic#computeActor(String, org.makumba.Attributes, String, org.makumba.commons.DbConnectionProvider)}.
 * 
 * @author Rudolf Mayer
 * @version $Id: MakumbaActorHashMap.java,v 1.1 Oct 5, 2008 3:45:01 PM rudi Exp $
 */
public class MakumbaActorHashMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
}