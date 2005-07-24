package org.makumba.db.hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.CtNewConstructor;
import javassist.NotFoundException;

public class MddToClass extends HibernateUtils {
    public static final String generatedClassPath="work/generated-hibernate-classes";
    private List mddsDone = new ArrayList();
	private LinkedList mddsToDo = new LinkedList();
	private LinkedList appendToClass = new LinkedList();

	public MddToClass(DataDefinition dd) throws CannotCompileException, NotFoundException, IOException {
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
		CtClass cc = cp.get(classname);
		cc.defrost();
		
		String type = null;
		String name = fd.getName();;
		switch (fd.getIntegerType()) {
			case FieldDefinition._ptr:
			case FieldDefinition._ptrOne:
				type = arrowToDot(fd.getPointedType().getName());
				break;
			case FieldDefinition._set:
				type = "java.util.ArrayList";
				break;
		}
		cc.addField(CtField.make("private "+type+" "+name+";", cc));
		cc.addMethod(CtNewMethod.getter("get"+name, CtField.make("private "+type+" "+name+";", cc)));
		cc.addMethod(CtNewMethod.setter("set"+name, CtField.make("private "+type+" "+name+";", cc)));		

		cc.writeFile(generatedClassPath);
	}
	
	public void generateClass(DataDefinition dd) throws CannotCompileException, NotFoundException, IOException {
		if (!mddsDone.contains(dd.getName())) {
			mddsDone.add(dd.getName());

			ClassPool cp = ClassPool.getDefault();
			CtClass cc = cp.makeClass(arrowToDot(dd.getName()));

			String type = null;
			String name = null;
			
			for (int i = 0; i < dd.getFieldNames().size(); i++) {
				Object[] append = new Object[2];
				FieldDefinition fd = dd.getFieldDefinition(i);
				name = fd.getName();
				switch (fd.getIntegerType()) {
					case FieldDefinition._intEnum:
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
						append[0] = arrowToDot(dd.getName());
						append[1] = fd;
						appendToClass.add(append);
						continue;
					case FieldDefinition._ptrRel:
						name = getBaseName(fd.getPointedType().getName());
						type = fd.getPointedType().getName();
						break;
					case FieldDefinition._ptrIndex:
						name = "id";
						type = "int";
						break;
					case FieldDefinition._text:
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
				addFields(cc, type, name);
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
	
	private void addFields(CtClass cc, String type, String name) throws CannotCompileException {
		cc.addField(CtField.make("private "+type+" "+name+";", cc));
		cc.addMethod(CtNewMethod.getter("get"+name, CtField.make("private "+type+" "+name+";", cc)));
		cc.addMethod(CtNewMethod.setter("set"+name, CtField.make("private "+type+" "+name+";", cc)));		
	}
}
