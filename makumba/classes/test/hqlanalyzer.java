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
    
 public void testAnalysisComplexSet() {
        
        String q1 = "SELECT p.id AS pointer, i.surname as surname, p.address.description as addressdescription FROM test.Person p JOIN p.indiv as i";
        HqlAnalyzer oA = new HqlAnalyzer(q1);
        
        assertEquals("pointer", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("surname", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("surname", oA.getProjectionType().getFieldDefinition(1).getDescription());
        
        assertEquals("addressdescription", oA.getProjectionType().getFieldDefinition(2).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(2).getType());
        assertEquals("description", oA.getProjectionType().getFieldDefinition(2).getDescription());
        
    }
 
 public void testAnalysisSimpleFields() {
     
     String q2 = "SELECT p.id AS pointer, p.id AS key, p.birthdate AS birthdate, p.uniqInt, p.hobbies AS text FROM test.Person p";
     HqlAnalyzer oA = new HqlAnalyzer(q2);
     
     assertEquals("pointer", oA.getProjectionType().getFieldDefinition(0).getName());
     assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
     assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
     
     assertEquals("key", oA.getProjectionType().getFieldDefinition(1).getName());
     assertEquals("ptr", oA.getProjectionType().getFieldDefinition(1).getType());
     assertEquals("id", oA.getProjectionType().getFieldDefinition(1).getDescription());
     
     assertEquals("birthdate", oA.getProjectionType().getFieldDefinition(2).getName());
     assertEquals("date", oA.getProjectionType().getFieldDefinition(2).getType());
     assertEquals("birthdate", oA.getProjectionType().getFieldDefinition(2).getDescription());
     
     assertEquals("col4", oA.getProjectionType().getFieldDefinition(3).getName());
     assertEquals("int", oA.getProjectionType().getFieldDefinition(3).getType());
     assertEquals("uniqInt", oA.getProjectionType().getFieldDefinition(3).getDescription());
     
     assertEquals("text", oA.getProjectionType().getFieldDefinition(4).getName());
     assertEquals("text", oA.getProjectionType().getFieldDefinition(4).getType());
     assertEquals("hobbies", oA.getProjectionType().getFieldDefinition(4).getDescription());
 }
    
    public void testAnalysisExtenalSetSimple() {
        
        String q3 = "SELECT l.name as n FROM test.Person p JOIN p.speaks as l";
        HqlAnalyzer oA = new HqlAnalyzer(q3);
        
        assertEquals("n", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("name", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
    }
    
public void testAnalysisExtenalSetSelectSetPointer() {
        
        String q3 = "SELECT l.id FROM test.Person p JOIN p.speaks as l";
        HqlAnalyzer oA = new HqlAnalyzer(q3);
        
        assertEquals("col1", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
    }

public void testAnalysisSelectPointer() {
    
    String q3 = "SELECT p.id FROM test.Person p";
    HqlAnalyzer oA = new HqlAnalyzer(q3);
    /*
    String query = oA.getOQL();
    System.out.println("Query:\n" + query);
    Vector w = oA.getProjectionType().getFieldNames();
    System.out.println("getProjectionType(): +"+w.size()+"\n");
    
    
    for (int i = 0; i < w.size(); i++) {
       System.out.println(i + " Field Name: " + w.get(i)); // +oA.getProjectionType().getFieldDefinition(i).getDataType()
        System.out.println(i + " FieldDef Name: "
                + (oA.getProjectionType().getFieldDefinition(i).getName()));
        System.out.println(i + " FieldDef Type: "
                + (oA.getProjectionType().getFieldDefinition(i).getType()));
        System.out.println(i + " FieldDef Comment: "
                + (oA.getProjectionType().getFieldDefinition(i).getDescription()));
    }
    */
    
    
    assertEquals("col1", oA.getProjectionType().getFieldDefinition(0).getName());
    assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
    assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
    
}


}

