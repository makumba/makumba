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

    public void run(TestResult r) {
        try {
            super.run(r);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void testAnalysisComplexSet() {

        String q1 = "SELECT p.id AS pointer, i.surname as surname, p.address.description as addressdescription FROM test.Person p JOIN p.indiv as i";
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q1);

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
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q2);

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
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q3);

        assertEquals("n", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("char", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("name", oA.getProjectionType().getFieldDefinition(0).getDescription());

    }

    public void testAnalysisExtenalSetSelectSetPointer() {

        String q3 = "SELECT l.id FROM test.Person p JOIN p.speaks as l";
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q3);

        assertEquals("col1", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());

    }

    public void testAnalysisSelectPointer() {

        String q3 = "SELECT p.id FROM test.Person p";
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q3);

        assertEquals("col1", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());

    }
    
    public void testAnalysisSetIntEnum() {

        String q = "SELECT q.enum as intset FROM test.Person p JOIN p.intSet q";
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q);

        assertEquals("intset", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("intEnum", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("enum", oA.getProjectionType().getFieldDefinition(0).getDescription());

    }
    
    public void testAnalysisInSet() {

        String q = "SELECT p.id as id, p.age as age FROM test.Person p WHERE p.age IN (0, 1, 3, 2, 5)";
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q);

        /*
        String query = oA.getOQL();
        System.out.println("Query:\n" + query);
        Vector w = oA.getProjectionType().getFieldNames();
        System.out.println("getProjectionType(): +" + w.size() + "\n");

        for (int i = 0; i < w.size(); i++) {
            System.out.println(i + " Field Name: " + w.get(i)); // +oA.getProjectionType().getFieldDefinition(i).getDataType()
            System.out.println(i + " FieldDef Name: " + (oA.getProjectionType().getFieldDefinition(i).getName()));
            System.out.println(i + " FieldDef Type: " + (oA.getProjectionType().getFieldDefinition(i).getType()));
            System.out.println(i + " FieldDef Comment: "
                    + (oA.getProjectionType().getFieldDefinition(i).getDescription()));
        }
        */

        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("age", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("int", oA.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("age", oA.getProjectionType().getFieldDefinition(1).getDescription());

    }
    
    public void testAnalysisInSetOnSetEnum() {

        String q = "SELECT p.id as id, p.age as age FROM test.Person p WHERE p.age IN p.intSet";
        HqlAnalyzer oA = MakumbaSystem.getHqlAnalyzer(q);

        /*
        String query = oA.getOQL();
        System.out.println("Query:\n" + query);
        Vector w = oA.getProjectionType().getFieldNames();
        System.out.println("getProjectionType(): +" + w.size() + "\n");

        for (int i = 0; i < w.size(); i++) {
            System.out.println(i + " Field Name: " + w.get(i)); // +oA.getProjectionType().getFieldDefinition(i).getDataType()
            System.out.println(i + " FieldDef Name: " + (oA.getProjectionType().getFieldDefinition(i).getName()));
            System.out.println(i + " FieldDef Type: " + (oA.getProjectionType().getFieldDefinition(i).getType()));
            System.out.println(i + " FieldDef Comment: "
                    + (oA.getProjectionType().getFieldDefinition(i).getDescription()));
        }
        */

        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("age", oA.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("int", oA.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("age", oA.getProjectionType().getFieldDefinition(1).getDescription());

    }
    
    public void testAnalysisArithmeticOperationOk() {

        String q1 = "SELECT p.id as id, p.age+17 as agePlus17 FROM test.Person p";
        HqlAnalyzer oA1 = MakumbaSystem.getHqlAnalyzer(q1);

        assertEquals("id", oA1.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA1.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA1.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("agePlus17", oA1.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("int", oA1.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("agePlus17", oA1.getProjectionType().getFieldDefinition(1).getDescription());
        
        
        
        String q2 = "SELECT p.id as id, p.age+1.2 as agePlus1dot2 FROM test.Person p";
        HqlAnalyzer oA2 = MakumbaSystem.getHqlAnalyzer(q2);

        assertEquals("id", oA2.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA2.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA2.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("agePlus1dot2", oA2.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("real", oA2.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("agePlus1dot2", oA2.getProjectionType().getFieldDefinition(1).getDescription());
        

        String q3 = "SELECT p.id as id, true+false as boolean FROM test.Person p";
        HqlAnalyzer oA3 = MakumbaSystem.getHqlAnalyzer(q3);

        assertEquals("id", oA3.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA3.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA3.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("boolean", oA3.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("setcharEnum", oA3.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("boolean", oA3.getProjectionType().getFieldDefinition(1).getDescription());
        
        String q4 = "SELECT p.id as id, p.hobbies+p.comment as text FROM test.Person p";
        HqlAnalyzer oA4 = MakumbaSystem.getHqlAnalyzer(q4);

        assertEquals("id", oA4.getProjectionType().getFieldDefinition(0).getName());
        assertEquals("ptr", oA4.getProjectionType().getFieldDefinition(0).getType());
        assertEquals("id", oA4.getProjectionType().getFieldDefinition(0).getDescription());
        
        assertEquals("text", oA4.getProjectionType().getFieldDefinition(1).getName());
        assertEquals("text", oA4.getProjectionType().getFieldDefinition(1).getType());
        assertEquals("text", oA4.getProjectionType().getFieldDefinition(1).getDescription());
    }
    
   
    

}
