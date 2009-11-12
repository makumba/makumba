package test;

import java.util.Dictionary;

import org.makumba.Attributes;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.Transaction;

public class ValidationLogic {
    
    public void checkAttributes(Attributes a, Transaction t) throws LogicException {        
        System.out.println("Running checkAttributes of incorrect BL class" );
    }
    
    public void on_newTestPerson(Pointer p, Dictionary<String, Object> d, Attributes a, Transaction t) throws LogicException { 
        System.out.println("Running on_newTestPerson of incorrect BL class" );
    }



}
