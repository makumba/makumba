package org.makumba.db.hibernate;

import java.util.HashSet;
import java.util.Set;

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
    private static Set javaReservedKeywords;
    private static String[] javaReserved={
        "abstract",   "continue", "for", "new", "switch",  "assert", "default", 
        "goto", "package", "synchronized", "boolean", "do", "if", "private", "this",
        "break", "double", "implements", "protected", "throw", "byte", "else", "import",
        "public", "throws", "case", "enum", "instanceof", "return", "transient",
        "catch", "extends", "int", "short", "try", "char", "final", "interface", "static",
        "void", "class", "finally", "long", "strictfp", "volatile", "const", "float",    
        "native", "super", "while" };
    
    static{
        javaReservedKeywords= new HashSet();
        for(int i= 0; i<javaReserved.length; i++){
         javaReservedKeywords.add(javaReserved[i]);   
        }
    }
    
    protected String checkReserved(String name){
        // if the name is "id" we need to rename it, or it will annoy Hibernate's internal "id"
        if(name.equals("id"))
            return arrowToDoubleUnderscore("idField");
        
        // check if this is a java reserved keyword, not to annoy the class generator
        if(javaReservedKeywords.contains(name))
            return arrowToDoubleUnderscore(name+"_");
        return arrowToDoubleUnderscore(name);
    }
   
}
