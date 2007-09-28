package org.makumba.db.hibernate;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.commons.ClassResource;
import org.makumba.commons.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.datadefinition.makumba.RecordInfo;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.EnumMemberValue;

public class MddToClass extends HibernateUtils {
    //public static final String generatedClassPath="work/generated-hibernate-classes";
    public String generatedClassPath="";
    private List mddsDone = new ArrayList();
	private LinkedList mddsToDo = new LinkedList();
	private LinkedList appendToClass = new LinkedList();
    
    private Configuration c = new Configuration();
    
    private DataDefinitionProvider ddp;

    public MddToClass(Vector v, String generationPath)throws CannotCompileException, NotFoundException, IOException{
      this.ddp = new DataDefinitionProvider(c);
      this.generatedClassPath = generationPath;
      for(int i=0; i<v.size(); i++)
        generateClass(ddp.getDataDefinition((String)v.elementAt(i)));
      while (!mddsToDo.isEmpty()) 
            generateClass((DataDefinition)mddsToDo.removeFirst());
        while (!appendToClass.isEmpty()) {
            Object[] append = (Object[]) appendToClass.removeFirst();
            appendClass((String)append[0], (FieldDefinition)append[1]);
        }
    }
	public MddToClass(DataDefinition dd, String generationPath) throws CannotCompileException, NotFoundException, IOException {
        this.ddp = new DataDefinitionProvider(c);
        this.generatedClassPath = generationPath;
        generateClass(dd);
		while (!mddsToDo.isEmpty()) {
			generateClass((DataDefinition)mddsToDo.removeFirst());	
		}

		while (!appendToClass.isEmpty()) {
			Object[] append = (Object[]) appendToClass.removeFirst();
			appendClass((String)append[0], (FieldDefinition)append[1]);
		}
	}
	
	/**
	 * Creates a bytecode .class file for the given DataDefinition
	 * @param dd DataDefinition that needs to be mapped   
	 **/
	public void appendClass(String classname, FieldDefinition fd) throws NotFoundException, CannotCompileException, IOException {
		ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
		CtClass cc = cp.get(classname);
		cc.defrost();
		
		String type = null;
		String name = fd.getName();;
		switch (fd.getIntegerType()) {
			case FieldDefinition._ptr:
			case FieldDefinition._ptrOne:
				type = arrowToDoubleUnderscore(fd.getPointedType().getName());
				break;
			case FieldDefinition._set:
				type = "java.util.ArrayList";
				break;
		}
        name=checkReserved(name);
		cc.addField(CtField.make("private "+type+" "+name+";", cc));
		cc.addMethod(CtNewMethod.getter("get"+name, CtField.make("private "+type+" "+name+";", cc)));
		cc.addMethod(CtNewMethod.setter("set"+name, CtField.make("private "+type+" "+name+";", cc)));		

		cc.writeFile(generatedClassPath);
	}
	
	public void generateClass(DataDefinition dd) throws CannotCompileException, NotFoundException, IOException {
		if (!mddsDone.contains(dd.getName())) {
			mddsDone.add(dd.getName());
            
            
			//checks if the class has to be generated
            File checkFile = new File(arrowToDoubleUnderscore(dd.getName()));
            if(checkFile.exists()) {
                
                if(dd.lastModified() < checkFile.lastModified())
                    return;
            }
            

			ClassPool cp = ClassPool.getDefault();
            cp.insertClassPath(new ClassClassPath(this.getClass()));
			CtClass cc = cp.makeClass(arrowToDoubleUnderscore(dd.getName()));

			String type = null;
			String name = null;
			
			for (int i = 0; i < dd.getFieldNames().size(); i++) {
				Object[] append = new Object[2];
				FieldDefinition fd = dd.getFieldDefinition(i);
				name = arrowToDoubleUnderscore(fd.getName());
				switch (fd.getIntegerType()) {
					case FieldDefinition._intEnum:
                        //type="enum";
                        //break;
					case FieldDefinition._int:
						type = "Integer";
						break;
					case FieldDefinition._real:
						type = "Double";
						break;
					case FieldDefinition._charEnum:
					case FieldDefinition._char:
						type = "String";
						break;
					case FieldDefinition._dateModify:
					case FieldDefinition._dateCreate:
					case FieldDefinition._date:
						type = "java.util.Date";
						break;
					case FieldDefinition._ptr:
					case FieldDefinition._ptrOne:
						mddsToDo.add(fd.getPointedType());
						append[0] = arrowToDoubleUnderscore(dd.getName());
						append[1] = fd;
						appendToClass.add(append);
						continue;
					case FieldDefinition._ptrRel:
						name = fd.getName();
						type = fd.getPointedType().getName();
						break;
					case FieldDefinition._ptrIndex:
						name = "primaryKey";
						type = "int";
						break;
					case FieldDefinition._text:
					case FieldDefinition._binary:
						type = "org.makumba.Text";
						break;
					case FieldDefinition._set:
						type = "java.util.ArrayList";
						mddsToDo.add(fd.getPointedType());
						break;
					case FieldDefinition._setComplex:
					case FieldDefinition._setCharEnum:
					case FieldDefinition._setIntEnum:
						type = "java.util.ArrayList";
						mddsToDo.add(fd.getSubtable());
						break;
					default:
						try {
							throw new Exception("Unmapped type: " + fd.getName() + "-" + fd.getType());
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
                //if(type.equals("enum")) {
                    //generateIntEnum(cc, name, dd.getFieldDefinition(name));
                //} else {
                    addFields(cc, type, name);
                //}
		
			}
            String nm= dd.getName();
            int lst= nm.lastIndexOf("->");
            if(lst!=-1)
                lst++;
            else
                lst=nm.lastIndexOf(".");
                
			cc.addConstructor(CtNewConstructor.make("public "+ nm.substring(lst+1)+"() {}", cc));
//			ClassFileWriter.print(cc.getClassFile());
			cc.writeFile(generatedClassPath);
		}
	}
	
    /**
     * Generates the code for a java enum from a Makumba intEnum.
     * TODO once javassist supports generation of enum bytecode, use the method
     * @param cc the CtClass to which the enum should be added
     * @param name the name of the enum
     * @param fd the field definition corresponding to the enum
     * @throws CannotCompileException
     */
    
	private void generateIntEnum(CtClass cc, String name, FieldDefinition fd) throws CannotCompileException {
        
        String enumName = name.substring(0,1).toUpperCase() + name.substring(1, name.length());
        
        String enumCode = "public enum "+enumName+" {";
        
        for(int i = 0; i < fd.getEnumeratorSize(); i++) {
            String currentIntEnumName = fd.getNameFor(i);
            Integer currentIntEnumValue = new Integer(fd.getIntAt(i));
            
            enumCode += "I"+i+ "(\""+currentIntEnumName+"\", "+currentIntEnumValue.intValue() + ")";
            if(i+1 != fd.getEnumeratorSize()) enumCode += ",";
        }
        enumCode +=";";
        
        enumCode +="private String endUserPresentation;";
        enumCode +="private int dbLevelValue;";
        enumCode +=enumName+"(String s, int n) {endUserPresentation=s; dbLevelValue=n;}";
        enumCode += "}";
        
        cc.addMethod(CtNewMethod.make(enumCode, cc));
       

    }
    private void addFields(CtClass cc, String type, String name) throws CannotCompileException {
        type= arrowToDoubleUnderscore(type);
        name= checkReserved(arrowToDoubleUnderscore(name));
		cc.addField(CtField.make("private "+type+" "+name+";", cc));
		cc.addMethod(CtNewMethod.getter("get"+name, CtField.make("private "+type+" "+name+";", cc)));
		cc.addMethod(CtNewMethod.setter("set"+name, CtField.make("private "+type+" "+name+";", cc)));		
	}    
}
