/*
 * Created on Jul 23, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

import com.sun.faces.facelets.component.UIRepeat;

public class UIRepeatListComponent extends UIRepeat {

    public UIRepeatListComponent() {
        // example forcing a value on the UIRepeat
        setValue(new Object[] { "a", "b" });
    }

    public void analyze() {

        System.out.println(this);

        // check whether we have not computed the queries of this mak:list group
        // before
        // if so, retrieve them from cache

        // look for all children mak:lists and start making their queries
        // look for all children mak:values and for all children that contain #{
        // mak:expr(QL) }, add the expressions as projection to the enclosing
        // mak:list query

        // look for all children that contain #{ label.field } where label is
        // defined in a mak:list's FROM, add label and label.field to the
        // projections of that mak:list

        // cache the queries of this mak:list group.

        // execute the queries and prepare the DataModel
        // use setValue() to give the DataModel to the UIRepeat
    }
}
