package org.makumba.db.hibernate;

import java.util.HashSet;

import org.makumba.commons.ReservedKeywords;

public class HibernateUtils {

	protected String dotToUnderscore(String name) {
		return name.replaceAll("\\.", "_");
	}

	protected String arrowToDot(String name) {
		return name.replaceAll("->", ".");
	}

	protected String arrowToDoubleDot(String name) {
		return name.replaceAll("->", "..");
	}
    protected String arrowToDoubleUnderscore(String name){
        return name.replaceAll("->", "__");        
    }
    
    protected String checkReserved(String name){
        // check if this is a java reserved keyword, not to annoy the class generator
        if(ReservedKeywords.getReservedKeywords().contains(name))
            return arrowToDoubleUnderscore(name+"_");
        return arrowToDoubleUnderscore(name);
    }
   
}
