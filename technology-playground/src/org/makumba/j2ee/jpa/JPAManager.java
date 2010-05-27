package org.makumba.j2ee.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.makumba.j2ee.model.Person;

public class JPAManager {

	private static EntityManagerFactory emf;
	private EntityManager em = null;

	public synchronized void init() {
		Map<String, String> emfProperties = new HashMap<String, String>() {
			{
				put("javax.persistence.provider",
						"org.hibernate.ejb.HibernatePersistence");
				put("hibernate.hbm2ddl.auto", "update");
				put("hibernate.show_sql", "false");
				put("hibernate.connection.driver_class",
						"org.hibernate.connection.C3P0ConnectionProvider");
				put("hibernate.connection.username", "root");
				put("hibernate.connection.password", "");
				put("hibernate.connection.url",
						"jdbc:mysql://localhost/makumba");
			}
		};

		emf = Persistence.createEntityManagerFactory("makumba", emfProperties);
	}

	public void shutdown() {
		if (emf != null)
			emf.close();
	}

	public void openConnection() {
		em = emf.createEntityManager();
	}

	public void closeConnection() {
		em.close();
	}

	public void commit() {
		em.getTransaction().commit();
	}

	public void rollback() {
		em.getTransaction().rollback();
	}

	public void create(Object entity) {

		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}

		System.out.println("Inserting " + entity.toString());

		try {
			em.persist(entity);
		} catch (Throwable t) {
			System.err.println("Panic, could not create entity");
			rollback();
		}

		em.getTransaction().commit();

	}

	public void update(Object entity) {
		if (!em.getTransaction().isActive())
			em.getTransaction().begin();
		try {
			System.out.println("Updating " + entity.toString());

			em.merge(entity);

			em.getTransaction().commit();
			System.out.println("Finished updating " + entity.toString());

		} catch (Throwable t) {
			System.err.println("Panic, could not update entity");
			rollback();
		}

	}

	public void delete(Object entity) {
		if (!em.getTransaction().isActive())
			em.getTransaction().begin();
		try {
			System.out.println("Deleting " + entity.toString());

			em.remove(entity);

			em.getTransaction().commit();
		} catch (Throwable t) {
			System.err.println("Panic, could not delete entity");
			rollback();
		}
	}

	public List<?> query(String queryString, Map<String, Object> parameters) {

		if (!em.getTransaction().isActive())
			em.getTransaction().begin();

		Query q = em.createQuery(queryString);

		for (String parameterName : parameters.keySet()) {
			q.setParameter(parameterName, parameters.get(parameterName));
		}

		List<?> result = q.getResultList();

		em.getTransaction().commit();

		return result;
	}

	public static void main(String... args) {

		JPAManager jpaManager = new JPAManager();

		jpaManager.init();

		System.out.println("=== Connecting to the database");
		jpaManager.openConnection();

		System.out.println("=== Inserting Chuck into the database");

		Person p = new Person();
		p.setName("Chuck");
		p.setSurname("Norris");
		p.setAge(70); // yes, he is that old

		jpaManager.create(p);

		System.out.println("=== Querying for Chuck");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", "Chuck");

		List<?> people = jpaManager.query("from Person p where p.name = :name",
				params);

		for (Object o : people) {
			System.out.println("Query result: " + o.toString());
		}

		System.out.println("=== Updating Chuck's age");

		p.setAge(30);

		jpaManager.update(p);

		System.out.println("=== Querying for Chuck, again");

		List<?> newPeople = jpaManager.query(
				"from Person p where p.name = :name", params);

		for (Object o : newPeople) {
			System.out.println("Query result: " + o.toString());
		}

		System.out.println("=== Deleting Chuck");
		jpaManager.delete(p);

		System.out.println("=== Closing database connection");
		jpaManager.closeConnection();

		System.out.println("=== Closing EntityManagerFactory");
		jpaManager.shutdown();

	}

}
