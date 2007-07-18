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

package org.makumba.view;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A subquery of a composed query
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ComposedSubquery extends ComposedQuery {

    /** The enclosing query */
    ComposedQuery superQuery;

    /**
     * Make a subquery of the indicated query, from the given sections.
     * 
     * @param usesHQL
     *            <code>true</code> if we use Hibernate to execute the query
     */
    public ComposedSubquery(String[] subsections, ComposedQuery cq, boolean usesHQL) {
        super(subsections, usesHQL);
        superQuery = cq;
        superQuery.addSubquery(this);
        derivedSections = new String[5];
        deriveFrom(FROM);
        deriveFrom(VARFROM);

        concat(derivedSections, superQuery.derivedSections, sections, WHERE, " AND ", true);
        // concat(derivedSections, superQuery.derivedSections, sections, GROUPBY, ",", false);
        String gpb = sections[GROUPBY];
        if (gpb != null)
            derivedSections[GROUPBY] = gpb;

        // concat(derivedSections, superQuery.derivedSections, sections, "ORDERBY", ",");
        String order = sections[ORDERBY];
        if (order != null)
            derivedSections[ORDERBY] = order;
    }

    /**
     * Derives the FROM section using the sections of the superQuery
     * 
     * @param n
     *            the index of the section
     */
    void deriveFrom(int n) {
        derivedSections[n] = superQuery.derivedSections[n];
        if (sections[n] != null)
            if (derivedSections[n] != null)
                if (sections[n].trim().toLowerCase().startsWith("join"))
                    // FIXME: this seems to be a dirty fix...maybe the separator should be join in all cases?
                    derivedSections[n] += " " + sections[n];
                else
                    derivedSections[n] += "," + sections[n];
            else
                derivedSections[n] = sections[n];
    }

    /**
     * Concatenates sections on the given index, with the given separator
     * 
     * @param result
     *            the String array containing the result
     * @param h1
     *            first array of sections
     * @param h2
     *            second array of sections
     * @param what
     *            the index at which we should concatenate
     * @pram sep the separator used for concatenation
     * @param paran
     *            do we want parenthesis
     */
    static void concat(String[] result, String[] h1, String[] h2, int what, String sep, boolean paran) {
        String lp = "";
        String rp = "";
        if (paran) {
            lp = "(";
            rp = ")";
        }
        String s1 = h1[what];
        String s2 = h2[what];
        if (s1 != null && s1.trim().length() == 0)
            s1 = null;

        if (s2 != null && s2.trim().length() == 0)
            s2 = null;

        if (s1 == null && s2 != null) {
            result[what] = s2;
            return;
        }
        if (s2 == null && s1 != null) {
            result[what] = s1;
            return;
        }
        if (s2 != null && s1 != null)
            result[what] = lp + s1 + rp + sep + lp + s2 + rp;
    }

    /**
     * Initializes the keysets by adding the superquery's previousKeyset to its keyset.
     */
    protected void initKeysets() {
        previousKeyset = (Vector) superQuery.previousKeyset.clone();
        keyset = (Vector) superQuery.keyset.clone();
        keysetLabels = (Vector) superQuery.keysetLabels.clone();

        for (Enumeration e = keysetLabels.elements(); e.hasMoreElements();)
            addProjection((String) e.nextElement());
        previousKeyset.addElement(superQuery.keyset);
    }

}
