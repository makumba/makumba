package test;

import java.util.Vector;

import org.makumba.MakumbaSystem;
import org.makumba.OQLAnalyzer;
import org.makumba.ProgrammerError;
import org.makumba.db.hibernate.hql.HqlAnalyzer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class oqlanalyzer extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(oqlanalyzer.class);
    }

    public void run(TestResult r) {
        try {
            super.run(r);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void testDateParameterType() {
        String q1 = "SELECT p as id FROM test.Person p WHERE $1<p.TS_create";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q1);
        
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
//        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("dateCreate", oA.getParameterTypes().getFieldDefinition(0).getType());
//        assertEquals("param", oA.getProjectionType().getFieldDefinition(1).getDescription());   
    }
    
    public void testAnalysisComplexSet() {

        String q1 = "SELECT p AS pointer, i.surname as surname, a.description as addressdescription FROM test.Person p, p.indiv i, p.address a";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q1);

        assertEquals("pointer", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
//        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());

        assertEquals("surname", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(1).getType());
//        assertEquals("surname", oA.getProjectionType().getFieldDefinition(1).getDescription());

        assertEquals("addressdescription", oA.getProjectionType().getFieldDefinition(2).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(2).getType());
//        assertEquals("description", oA.getProjectionType().getFieldDefinition(2).getDescription());

    }

    public void testAnalysisSimpleFields() {

        String q2 = "SELECT p AS pointer, p AS key, p.birthdate AS birthdate, p.uniqInt, p.hobbies AS text FROM test.Person p";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q2);

        assertEquals("pointer", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
//        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());

        assertEquals("key", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(1).getType());
//        assertEquals("id", oA.getProjectionType().getFieldDefinition(1).getDescription());

        assertEquals("birthdate", oA.getProjectionType().getFieldDefinition(2).getName());
        assertEquals("date", oA.getProjectionType().getFieldDefinition(2).getType());
//        assertEquals("birthdate", oA.getProjectionType().getFieldDefinition(2).getDescription());

        assertEquals("col4", oA.getProjectionType().getFieldDefinition(3).getName());
        assertEquals("int", oA.getProjectionType().getFieldDefinition(3).getType());
//        assertEquals("uniqInt", oA.getProjectionType().getFieldDefinition(3).getDescription());

        assertEquals("text", oA.getProjectionType().getFieldDefinition(4).getName());
        assertEquals("text", oA.getProjectionType().getFieldDefinition(4).getType());
//      assertEquals("hobbies", oA.getProjectionType().getFieldDefinition(4).getDescription());
    }

    public void testAnalysisExtenalSetSimple() {

        String q3 = "SELECT l.name as n FROM test.Person p, p.speaks as l";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q3);

        assertEquals("n", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(0).getType());
//        assertEquals("name", oA.getProjectionType().getFieldDefinition(0).getDescription());

    }

    public void testAnalysisExtenalSetSelectSetPointer() {

        String q3 = "SELECT l FROM test.Person p, p.speaks l";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q3);

        assertEquals("col1", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
//        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());

    }

    public void testAnalysisSelectPointer() {

        String q3 = "SELECT p FROM test.Person p";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q3);

        assertEquals("col1", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
  //      assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());

    }
    
    public void testAnalysisSetIntEnum() {

        String q = "SELECT q.enum as intset FROM test.Person p, p.intSet q";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q);

        assertEquals("intset", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("intEnum", oA.getProjectionType().getFieldDefinition(0).getType());
//        assertEquals("enum", oA.getProjectionType().getFieldDefinition(0).getDescription());

    }
    
    public void testAnalysisInSet() {

        String q = "SELECT p as id, p.age as age FROM test.Person p WHERE p.age IN SET($1)";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q);

        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
  //      assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("age", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("int", oA.getProjectionType().getFieldDefinition(1).getType());
 //       assertEquals("age", oA.getProjectionType().getFieldDefinition(1).getDescription());

    }
        
    
    
    public void testAnalysisArithmeticOperationOk() {

        String q1 = "SELECT p as id, p.age+17 as agePlus17 FROM test.Person p";
        OQLAnalyzer oA1 = MakumbaSystem.getOQLAnalyzer(q1);

        assertEquals("id", oA1.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA1.getProjectionType().getFieldDefinition(0).getType());
   //     assertEquals("id", oA1.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("agePlus17", oA1.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("int", oA1.getProjectionType().getFieldDefinition(1).getType());
  //      assertEquals("agePlus17", oA1.getProjectionType().getFieldDefinition(1).getDescription());
        
        
        
        String q2 = "SELECT p as id, p.age+1.2 as agePlus1dot2 FROM test.Person p";
        OQLAnalyzer oA2 = MakumbaSystem.getOQLAnalyzer(q2);

        assertEquals("id", oA2.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA2.getProjectionType().getFieldDefinition(0).getType());
 //       assertEquals("id", oA2.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("agePlus1dot2", oA2.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("int", oA2.getProjectionType().getFieldDefinition(1).getType());
 //       assertEquals("agePlus1dot2", oA2.getProjectionType().getFieldDefinition(1).getDescription());      
       
        String q4 = "SELECT p as id, p.hobbies+p.comment as text FROM test.Person p";
        OQLAnalyzer oA4 = MakumbaSystem.getOQLAnalyzer(q4);

        assertEquals("id", oA4.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA4.getProjectionType().getFieldDefinition(0).getType());
 //       assertEquals("id", oA4.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("text", oA4.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("text", oA4.getProjectionType().getFieldDefinition(1).getType());
 //       assertEquals("text", oA4.getProjectionType().getFieldDefinition(1).getDescription());
    }
    
    
    public void testAnalysisArithmeticOperationParameter() {

        String q1 = "SELECT p as id, p.age+$1 as param FROM test.Person p";
        OQLAnalyzer oA = MakumbaSystem.getOQLAnalyzer(q1);
        
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
//        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("param", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("int", oA.getProjectionType().getFieldDefinition(1).getType());
//        assertEquals("param", oA.getProjectionType().getFieldDefinition(1).getDescription());
        
    }

    
}
