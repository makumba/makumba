package org.makumba.db.hibernate;

import java.io.IOException;
import java.util.Vector;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import javax.xml.transform.TransformerConfigurationException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.xml.sax.SAXException;

public class HibernateTest  {
	public static void main (String[] args) {
        
        
        Vector dds= org.makumba.MakumbaSystem.mddsInDirectory("dataDefinitions");
        //dds= new Vector(); dds.add("general.survey.Response");
        Configuration cfg = new Configuration().configure("org/makumba/db/hibernate/localhost_mysql_karambasmall.cfg.xml");
		
        System.out.println(new java.util.Date());
		try {
			MddToClass jot = new MddToClass(dds);
		} catch (CannotCompileException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println(new java.util.Date());
        
		try {
			MddToMapping xot = new MddToMapping(dds, cfg);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}


		SessionFactory sf = cfg.buildSessionFactory();
		SchemaUpdate schemaUpdate = new SchemaUpdate(cfg);
		schemaUpdate.execute(true, true);
		
		Session session = sf.openSession();
		Transaction tx = session.beginTransaction();
		
//		List languages = new ArrayList();
//		languages.add(new Language("English", "en"));
//		languages.add(new Language("French", "fr"));
//		
//		Person brother = new Person();
//		brother.setWeight(new Double("50"));
//		
//		Individual indiv = new Individual();
//		indiv.setName("Bart");
//		indiv.setSurname("Van de Velde");
//		
//		Address address = new Address("fzpoegjfzpojgpz");
//		
//		Person person = new Person();
//		person.setWeight(new Double("73.6"));
//		person.setBrother(brother);
//		person.setIndiv(indiv);
//		person.setBirthdate(new Date());
//		person.setHobbies(new Text("Hobybybybybybyybybybybybyby."));
//		person.setSpeaks(languages);		
//		person.addAddress(address);
//		person.addCharSet(new CharSet("a"));
//		person.addCharSet(new CharSet("b"));
//		person.addIntSet(new IntSet(new Integer(0)));
//		person.addIntSet(new IntSet(new Integer(1)));

//		session.saveOrUpdate(person);		
//		tx.commit();
//		
		//Query q = session.createQuery("UPDATE test.Person SET indiv.name = :newName WHERE indiv.name = :oldName").setString("newName", "Johannes").setString("oldName", "Bart");

		//       SELECT p would select the whole test.Person!
        //Query q = session.createQuery("SELECT p.id FROM test.Person p");
        
        //       SELECT p.indiv would select the whole test.Individual!
        //Query q = session.createQuery("SELECT p.hibernate_indiv FROM test.Person p");
        
        //       FROM test.Person p, p.indiv i does not pass the HQL-SQL parser
		//Query q = session.createQuery("SELECT i.surname, p.weight FROM test.Person p, IN(p.indiv) i WHERE i.name = 'Bart'");
        
		//       FROM test.Person p, p.intSet s, p.speaks l does not pass the HQL-SQL parser
		//       SELECT s will select the whole enumerator rather than just the value!
        //Query q = session.createQuery("SELECT s.enum, s1.name FROM test.Person p JOIN p.intSet s JOIN p.speaks s1 WHERE p.indiv.name = 'Bart'");

		//      FROM test.Person p, p.address a does not pass the HQL-SQL parser
        //Query q = session.createQuery("SELECT a.streetno FROM test.Person p JOIN p.address a WHERE p.indiv.name = 'Bart'");

        //       a manual pointer join
        //Query q = session.createQuery("SELECT i.name, p.weight FROM test.Person p, test.Individual i WHERE p.indiv = i");
		
        //       a more automatic pointer join
        //Query q = session.createQuery("SELECT p.indiv.name, p.weight FROM test.Person p");
        
        //List list = q.list();
		//for (int i=0; i < list.size(); i++) {
		//	if (list.get(i) == null) continue;
//			test = (Person)list.get(i);
		//	System.out.println(list.get(i).getClass());
//			Double row = (Double)list.get(i);
//			System.out.println(row.toString() + "\n");
		//}
//		list = test.getSpeaks();
//		for (int i=0; i<list.size(); i++ ) {
//			System.out.println((Language)list.get(i));
//		}
		session.close();
	}
}


