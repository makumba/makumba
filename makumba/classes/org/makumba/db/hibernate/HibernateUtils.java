package org.makumba.db.hibernate;

import java.util.HashSet;
import java.util.Set;

public class HibernateUtils {

	protected String getBaseName(String name) {
		int dot;
		dot = name.lastIndexOf(".");
		if (dot != -1)
			return name.substring(dot+1);
		return name;
	}

	protected String getArrowBaseName(String name) {
		int dot;
		dot = name.lastIndexOf("->");
		if (dot != -1)
			return name.substring(0, dot);
		return name;
	}

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
    
    protected String checkJavaReserved(String name){
        if(javaReservedKeywords.contains(name))
            return arrowToDoubleUnderscore(name+"_");
        return arrowToDoubleUnderscore(name);
    }
   
}
