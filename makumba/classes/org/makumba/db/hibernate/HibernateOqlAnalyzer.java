package org.makumba.db.hibernate;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
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
            //System.out.println(i+" ==> "+paramTypes[i].getName());
            
            if(paramTypes[i].getName().equals("org.makumba.db.hibernate.customtypes.TextUserType")) {
                FieldDefinition fd = FieldInfo.getFieldInfo(getColumnName(aliases[i]),
                        "text", false);
                result.addField(fd);
                //FIXME this should return the real pointer
            } else if(paramTypes[i].getName().equals("org.makumba.db.hibernate.customtypes.PointerUserType")) {
                FieldDefinition fd = FieldInfo.getFieldInfo(getColumnName(aliases[i]),
                        "ptr toBeFixed", false);
                result.addField(fd);
            } else if(paramTypes[i].getName().equals("string")) {
                FieldDefinition fd = FieldInfo.getFieldInfo(getColumnName(aliases[i]),     
                        "char", false);
                result.addField(fd);
            } else if(paramTypes[i].getName().equals("integer")) {
                FieldDefinition fd = FieldInfo.getFieldInfo(getColumnName(aliases[i]),
                        "int", false);
                result.addField(fd);
            } else {
                FieldDefinition fd = FieldInfo.getFieldInfo(getColumnName(aliases[i]),
                        paramTypes[i].getName(), false);
                result.addField(fd);
            }
			
		}
        
		return result;

	}
    
    public String[] getProjectionLeftSides() {
        String queryProjection = query.substring(7, query.indexOf("FROM"));
        StringTokenizer separateProjections = new StringTokenizer(queryProjection, ",");
        String[] result = new String[separateProjections.countTokens()];
        int i = 0;
        while(separateProjections.hasMoreElements()) {
            String token = separateProjections.nextToken().toLowerCase();
            if(token.toLowerCase().indexOf("as") > 0) {
                result[i++] = token.substring(0, token.indexOf("as")).trim();
            } else {
                result[i++] = token;
            }
        }
        return result;
    }

    private String getColumnName(String colName) {
        try {
        	return "col" + Integer.parseInt(colName);            
        } catch(NumberFormatException e) {
            return colName;
        }        
    }

	public DataDefinition getLabelType(String labelName) {
		String[] aliases = qti.getReturnAliases();
		Type[] paramTypes = qti.getReturnTypes();

		for (int i = 0; i < aliases.length; i++) {
			if (labelName.equals(getColumnName(aliases[i]))) {
                return MakumbaSystem.getDataDefinition(paramTypes[i].getName());
            }
		}
		throw new OQLParseError("Could not determine type of label " + labelName);
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
        
        System.out.println("ProjectionLeftSide:\n");
        for(int i=0; i<oA.getProjectionLeftSides().length;i++) {
            System.out.print(oA.getProjectionLeftSides()[i]+" - ");
        }

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
