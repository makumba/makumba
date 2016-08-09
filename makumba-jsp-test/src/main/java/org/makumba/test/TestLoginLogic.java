package org.makumba.test;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.LogicException;
import org.makumba.Transaction;
import org.makumba.UnauthenticatedException;

public class TestLoginLogic {

    public void checkAttributes(Attributes a, Transaction db) throws LogicException {
        a.getAttribute("testAttribute");

    }

    public Object findTestAttribute(Attributes a, Transaction db) throws LogicException {
        try {
            a.getAttribute("username");
            a.getAttribute("password");

        } catch (AttributeNotFoundException anfe) {
            throw new UnauthenticatedException();
        }

        return new Boolean(true);
    }

}
