package org.makumba.db.hibernate;

import org.makumba.Pointer;

public class HibernatePointer extends Pointer {

    public HibernatePointer(String type, long n) {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
        this.n = n;
    }

}
