package org.makumba.db.hibernate.propertyaccess;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.BasicPropertyAccessor;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;

public class HibernatePrimaryKey extends BasicPropertyAccessor {

	public Getter getGetter(Class klass, String propertyName) throws PropertyNotFoundException {
		int hib = propertyName.indexOf("hibernate_");
		if (hib == -1)
			return super.getGetter(klass, propertyName);
		return super.getGetter(klass, propertyName.substring(10));
	}

	public Setter getSetter(Class klass, String propertyName) throws PropertyNotFoundException {
		int hib = propertyName.indexOf("hibernate_");
		if (hib == -1)
			return super.getSetter(klass, propertyName);
		return super.getSetter(klass, propertyName.substring(10));
		
	}

}
