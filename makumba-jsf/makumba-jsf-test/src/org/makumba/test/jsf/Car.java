package org.makumba.test.jsf;

import javax.faces.bean.ManagedBean;

@ManagedBean
public class Car {
	
	private String name;
	
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Car(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
	

}
