package org.makumba.test.jsf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.bean.ManagedBean;

@ManagedBean
public class SelectBean {
	
	private Car selectedCar = new Car("Ferrari", "Italian car");
	
	private List<Car> possibleCars = new ArrayList<Car>();

	private List<String> selectedCars = new ArrayList<String>();

	public SelectBean() {
		Car c1 = new Car("Ferrari", "Italian Sportscar");
		Car c2 = new Car("Lada", "Romanian Sportscar");
		Car c3 = new Car("Twingo", "French Sportscar");
		
		possibleCars.add(c1);
		possibleCars.add(c2);
		possibleCars.add(c3);
		
		// name is the key of the selection. toString() of Car evaluates to the name. that's how the selection is matched
		selectedCars.add(c1.getName());
	}
	
	public List<Car> getPossibleCars() {
		return possibleCars;
	}

	public void setPossibleCars(List<Car> possibleCars) {
		this.possibleCars = possibleCars;
	}

	public List<String> getSelectedCars() {
		return selectedCars;
	}

	public void setSelectedCars(List<String> selectedCars) {
		System.out.println(Arrays.toString(selectedCars.toArray()));
		this.selectedCars = selectedCars;
	}

	public Car getSelectedCar() {
		return selectedCar;
	}

	public void setSelectedCar(Car selectedCar) {
		this.selectedCar = selectedCar;
	}
	

}
