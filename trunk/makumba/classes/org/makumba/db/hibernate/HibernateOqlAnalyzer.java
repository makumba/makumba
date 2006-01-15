package org.makumba.db.hibernate;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import org.hibernate.QueryException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.hql.ast.QueryTranslatorImpl;
import org.hibernate.type.Type;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.OQLAnalyzer;
import org.makumba.OQLParseError;
import org.makumba.abstr.FieldInfo;

public class HibernateOqlAnalyzer implements OQLAnalyzer {

	private SessionFactory sf = null;

	private String query = "";

	private QueryTranslatorImpl qti = null;

	private HibernateOqlAnalyzer(String query, SessionFactory sf) {
		this.sf = sf;
		this.query = query;
		this.qti = new QueryTranslatorImpl(this.query, new HashMap(),
				(SessionFactoryImplementor) sf);
		qti.compile(new HashMap(), false);
	}

	public static HibernateOqlAnalyzer getOqlAnalyzer(String query,
			SessionFactory sf) {
		HibernateOqlAnalyzer hoa = null;
		try {
			hoa = new HibernateOqlAnalyzer(query, sf);
		} catch (Error e) {
			throw new OQLParseError(e);
		} catch (QueryException ex) {
			throw new OQLParseError(ex);
		} catch (Exception oe) {
			throw new OQLParseError("Unknown error in OQL/HQL query: "
					+ oe.getMessage(), oe);
		}
		return hoa;
	}

	public String getOQL() {
		return this.query;
	}

	public DataDefinition getProjectionType() {

		String[] aliases = qti.getReturnAliases();
		Type[] paramTypes = qti.getReturnTypes();

		DataDefinition result = MakumbaSystem
				.getTemporaryDataDefinition("Projections for " + query);

		for (int i = 0; i < paramTypes.length; i++) {
			
			String colName = aliases[i];
			int colNum = -1;
			
			try {
				colNum = Integer.parseInt(colName);
			} catch(NumberFormatException e) {
				colNum = -1;
			}
			
			if(!(colNum == -1)) {
				colName="col"+(colNum+1);
			}
			
			FieldDefinition fd = FieldInfo.getFieldInfo(colName,
					paramTypes[i].getName(), false);
			result.addField(fd);
		}

		return result;

	}

	public DataDefinition getLabelType(String labelName) {
		DataDefinition result = MakumbaSystem
				.getTemporaryDataDefinition("ProjectionType for " + query);

		String[] aliases = qti.getReturnAliases();
		Type[] paramTypes = qti.getReturnTypes();

		int pos = -1;
		for (int i = 0; i < aliases.length; i++) {
			if (labelName.equals(aliases[i]))
				pos = i;

		}
		if (!(pos == -1)) {
			FieldDefinition fd = FieldInfo.getFieldInfo(aliases[pos],
					paramTypes[pos].getName(), false);
			result.addField(fd);
			return result;
		} else {
			throw new OQLParseError("Could not determine type of label "
					+ labelName);
		}

	}

	// TODO this doesn't work
	
	public DataDefinition getParameterTypes() {
		/*
		DataDefinition result = MakumbaSystem
				.getTemporaryDataDefinition("Parameters for " + query);

		Set paramNames = qti.getParameterTranslations()
				.getNamedParameterNames();
		for (Iterator i = paramNames.iterator(); i.hasNext();) {
			String paramName = (String) i.next();
			FieldDefinition fd = FieldInfo.getFieldInfo(paramName, qti
					.getParameterTranslations().getNamedParameterExpectedType(
							paramName), false);
			result.addField(fd);
		}

		return result;
		*/
		return null;
		
	}

	// TODO this doesn't work
	public int parameterNumber() {
		/*
		return qti.getParameterTranslations().getOrdinalParameterCount();
		*/
		return -1;
	}

	// not needed
	public int parameterAt(int index) {
		return -1;
	}
	
	
	
	
	public static void main(String argv[]) {

		String generatedMappingPath = "work/generated-hibernate-mappings";

		Configuration cfg = new Configuration()
				.configure("org/makumba/db/hibernate/localhost_mysql_karambasmall.cfg.xml");
		cfg.addDirectory(new File(generatedMappingPath));

		String q1 = argv[0];

		SessionFactory sf = cfg.buildSessionFactory();

		HibernateOqlAnalyzer oA = HibernateOqlAnalyzer.getOqlAnalyzer(q1, sf);

		String query = oA.getOQL();
		System.out.println("Query:\n" + query);

		System.out.println("Number of ordinal parameters: "
				+ oA.parameterNumber());
		System.out
				.println("Type for 'personName': "
						+ oA.getLabelType("personName").getFieldDefinition(0)
								.getType());

		System.out.println("getProjectionType():\n");
		Vector w = oA.getProjectionType().getFieldNames();
		System.out.println(w.size());
		for (int i = 0; i < w.size(); i++) {
			System.out.println(i + " Field Name: " + w.get(i)); // +oA.getProjectionType().getFieldDefinition(i).getDataType()
			System.out.println(i + " FieldDef Name: "
					+ (oA.getProjectionType().getFieldDefinition(i).getName()));
			System.out.println(i + " FieldDef Type: "
					+ (oA.getProjectionType().getFieldDefinition(i).getType()));
		}
		
		/*
		System.out.println("getParameterTypes():\n");
		Vector v = oA.getParameterTypes().getFieldNames();
		System.out.println(v.size());
		for (int i = 0; i < v.size(); i++) {
			System.out.println(i + " Field Name: " + v.get(i)); // +oA.getProjectionType().getFieldDefinition(i).getDataType()
			System.out.println(i + " FieldDef Name: "
					+ (oA.getParameterTypes().getFieldDefinition(i).getName()));
			System.out.println(i + " FieldDef Type: "
					+ (oA.getParameterTypes().getFieldDefinition(i).getType()));
		}
		*/

	}
}
