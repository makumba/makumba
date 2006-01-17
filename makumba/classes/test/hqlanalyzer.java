package test;

import java.util.Vector;

import org.makumba.MakumbaSystem;
import org.makumba.db.hibernate.hql.HqlAnalyzer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class hqlanalyzer extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(hqlanalyzer.class);
    }
    
    public void run(TestResult r){
        try{
            super.run(r);

        } catch(Throwable t){ t.printStackTrace();  }
    }
    
 public void testAnalysisQ1() {
        
        String q1 = "SELECT p.id AS pointer, i.surname as surname, p.address.description as addressdescription FROM test.Person p JOIN p.indiv as i";
        HqlAnalyzer oA = new HqlAnalyzer(q1);
        String query = oA.getOQL();
        System.out.println("Query:\n" + query);

        System.out.println("getProjectionType():\n");
        Vector w = oA.getProjectionType().getFieldNames();
        
        for (int i = 0; i < w.size(); i++) {
            System.out.println(i + " Field Name: " + w.get(i)); // +oA.getProjectionType().getFieldDefinition(i).getDataType()
            System.out.println(i + " FieldDef Name: "
                    + (oA.getProjectionType().getFieldDefinition(i).getName()));
            System.out.println(i + " FieldDef Type: "
                    + (oA.getProjectionType().getFieldDefinition(i).getType()));
            System.out.println(i + " FieldDef Comment: "
                    + (oA.getProjectionType().getFieldDefinition(i).getDescription()));
        }
        
        //TODO - adapt the asserts
        /*
        assertEquals("pointer", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("key", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(1).getDescription());
        
        assertEquals("personName", oA.getProjectionType().getFieldDefinition(2).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(2).getType());
        assertEquals("name", oA.getProjectionType().getFieldDefinition(2).getDescription());
        
        assertEquals("col3", oA.getProjectionType().getFieldDefinition(3).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(3).getType());
        assertEquals("surname", oA.getProjectionType().getFieldDefinition(3).getDescription());
        
        assertEquals("specialShit", oA.getProjectionType().getFieldDefinition(4).getName());
        assertEquals("text", oA.getProjectionType().getFieldDefinition(4).getType());
        assertEquals("specialDiet", oA.getProjectionType().getFieldDefinition(4).getDescription());
        */
    }
 
 public void testAnalysisQ2() {
     
     String q2 = "SELECT p.id AS pointer, p.id AS key, p.name AS personName, p.surname, p.specialDiet AS specialShit FROM general.Person p";
     HqlAnalyzer oA = new HqlAnalyzer(q2);
     
     assertEquals("pointer", oA.getProjectionType().getFieldDefinition(0).getName());
     assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
     assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
     
     assertEquals("key", oA.getProjectionType().getFieldDefinition(1).getName());
     assertEquals("ptr", oA.getProjectionType().getFieldDefinition(1).getType());
     assertEquals("id", oA.getProjectionType().getFieldDefinition(1).getDescription());
     
     assertEquals("personName", oA.getProjectionType().getFieldDefinition(2).getName());
     assertEquals("char", oA.getProjectionType().getFieldDefinition(2).getType());
     assertEquals("name", oA.getProjectionType().getFieldDefinition(2).getDescription());
     
     assertEquals("col3", oA.getProjectionType().getFieldDefinition(3).getName());
     assertEquals("char", oA.getProjectionType().getFieldDefinition(3).getType());
     assertEquals("surname", oA.getProjectionType().getFieldDefinition(3).getDescription());
     
     assertEquals("specialShit", oA.getProjectionType().getFieldDefinition(4).getName());
     assertEquals("text", oA.getProjectionType().getFieldDefinition(4).getType());
     assertEquals("specialDiet", oA.getProjectionType().getFieldDefinition(4).getDescription());
 }
    
    public void testAnalysisQ3() {
        
        String q3 = "SELECT p.name, p.surname, c.name FROM general.Person p JOIN p.citizenship as c";
        HqlAnalyzer oA = new HqlAnalyzer(q3);
        String query = oA.getOQL();
        System.out.println("Query:\n" + query);

        System.out.println("getProjectionType():\n");
        Vector w = oA.getProjectionType().getFieldNames();
        
        for (int i = 0; i < w.size(); i++) {
            System.out.println(i + " Field Name: " + w.get(i)); // +oA.getProjectionType().getFieldDefinition(i).getDataType()
            System.out.println(i + " FieldDef Name: "
                    + (oA.getProjectionType().getFieldDefinition(i).getName()));
            System.out.println(i + " FieldDef Type: "
                    + (oA.getProjectionType().getFieldDefinition(i).getType()));
            System.out.println(i + " FieldDef Comment: "
                    + (oA.getProjectionType().getFieldDefinition(i).getDescription()));
        }
        //TODO - adapt the asserts
        /*
        
        assertEquals("pointer", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("key", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(1).getDescription());
        
        assertEquals("personName", oA.getProjectionType().getFieldDefinition(2).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(2).getType());
        assertEquals("name", oA.getProjectionType().getFieldDefinition(2).getDescription());
        
        assertEquals("col3", oA.getProjectionType().getFieldDefinition(3).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(3).getType());
        assertEquals("surname", oA.getProjectionType().getFieldDefinition(3).getDescription());
        
        assertEquals("specialShit", oA.getProjectionType().getFieldDefinition(4).getName());
        assertEquals("text", oA.getProjectionType().getFieldDefinition(4).getType());
        assertEquals("specialDiet", oA.getProjectionType().getFieldDefinition(4).getDescription());
        */
        
    }
}

