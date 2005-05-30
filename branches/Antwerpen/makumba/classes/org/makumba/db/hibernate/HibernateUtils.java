package org.makumba.db.hibernate;

public class HibernateUtils {

	protected String getBaseName(String name) {
		int dot;
		dot = name.lastIndexOf(".");
		if (dot != -1)
			return name.substring(dot+1);
		return name;
	}

	protected String getArrowBaseName(String name) {
		int dot;
		dot = name.lastIndexOf("->");
		if (dot != -1)
			return name.substring(0, dot);
		return name;
	}

	protected String dotToUnderscore(String name) {
		return name.replaceAll("\\.", "_");
	}

	protected String arrowToDot(String name) {
		return name.replaceAll("->", ".");
	}

	protected String arrowToDoubleDot(String name) {
		return name.replaceAll("->", "..");
	}
}
