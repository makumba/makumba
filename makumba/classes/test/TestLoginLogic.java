package test;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.UnauthenticatedException;

public class TestLoginLogic {
    
    public void checkAttributes(Attributes a, Database db) throws LogicException {
        a.getAttribute("testAttribute");
        
    }
    
    public Object findTestAttribute(Attributes a, Database db) throws LogicException {
        try {
            a.getAttribute("username");
            a.getAttribute("password");
            
        } catch(AttributeNotFoundException anfe) {
            throw new UnauthenticatedException();
        }
        
        return new Boolean(true);
    }

}
