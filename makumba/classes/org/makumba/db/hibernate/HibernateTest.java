package org.makumba.db.hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import javax.xml.transform.TransformerConfigurationException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.makumba.DataDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.Text;
import org.xml.sax.SAXException;

public class HibernateTest extends HibernateUtils {
	public static List allmdds;
	public static final String packageprefix = "generatedfiles";
	
	public static void main (String[] args) {
		allmdds = new ArrayList();
		DataDefinition dd = MakumbaSystem.getDataDefinition("test/Person");
		
		try {
			MddToClass jot = new MddToClass(dd);
		} catch (CannotCompileException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			MddToMapping xot = new MddToMapping(dd);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		Configuration cfg = new Configuration().configure("org/makumba/db/hibernate/hibernate.cfg.xml");
		for (int i = 0; i < allmdds.size(); i++ ) {
			cfg.addResource("generatedfiles/"+(String)allmdds.get(i));
			System.out.println(allmdds.get(i));
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
		//Query q = session.createQuery("UPDATE test.hibernate.Person SET indiv.name = :newName WHERE indiv.name = :oldName").setString("newName", "Johannes").setString("oldName", "Bart");

		Query q = session.createQuery("SELECT p.hibernate_indiv FROM generatedfiles.test.Person p");
		
		//Query q = session.createQuery("SELECT i.surname, p.weight FROM test.hibernate.Person p, IN(p.individual) i WHERE i.name = 'Bart'");
		//Query q = session.createQuery("SELECT s FROM test.hibernate.Person p join p.speaks s WHERE p.individual.name = 'Bart'");
		//Query q = session.createQuery("SELECT i.name, p.weight FROM test.hibernate.Person p, test.hibernate.Individual i WHERE p.individual = i");
		List list = q.list();
		for (int i=0; i < list.size(); i++) {
			if (list.get(i) == null) continue;
//			test = (Person)list.get(i);
			System.out.println(list.get(i).getClass());
//			Double row = (Double)list.get(i);
//			System.out.println(row.toString() + "\n");
		}
//		list = test.getSpeaks();
//		for (int i=0; i<list.size(); i++ ) {
//			System.out.println((Language)list.get(i));
//		}
		session.close();
	}
}
