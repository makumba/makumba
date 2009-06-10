package test;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.UnauthenticatedException;
import org.makumba.UnauthorizedException;

public class LoginTestLogic {
    
    public void checkAttributes(Attributes a, Database db) throws LogicException {
        a.getAttribute("testAttribute");
        
    }
    
    public Object findTestAttribute(Attributes a, Database db) throws LogicException {
        try {
            a.getAttribute("username");
            a.getAttribute("password");
            
        } catch(AttributeNotFoundException anfe) {
            throw new UnauthorizedException();
        }
        
        return new Boolean(true);
    }

}
