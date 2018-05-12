package org.makumba.test.jsf;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.faces.bean.ManagedBean;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@ManagedBean
public class HelloWorld {

	private String letters;

	private String numbers;

	private String email;

	public String getName() {
		return "John";
	}

	@NotNull
	@NotEmpty
	@Pattern(regexp = "[A-Za-z]*", message = "must contain only letters")
	public String getLetters() {
		return letters;
	}

	public void setLetters(String letters) {
		this.letters = letters;
	}

	@NotNull
	@NotEmpty
	@Digits(fraction = 0, integer = 2)
	public String getNumbers() {
		return numbers;
	}

	public void setNumbers(String numbers) {
		this.numbers = numbers;
	}

	@NotNull
	@NotEmpty
	@Email
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Vector<String> getList() {
		Vector<String> v = new Vector<String>();
		v.add("apple");
		v.add("banana");
		v.add("kiwi");
		return v;
	}

	public Map<String, Object> getMaps() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("key1", "level1val1");
		m.put("key2", "level1val2");
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("key11", "level2val1");
		m1.put("key22", "level2val2");
		m.put("key3", m1);
		return m;
	}

}
