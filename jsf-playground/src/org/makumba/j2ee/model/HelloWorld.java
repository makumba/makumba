package org.makumba.j2ee.model;

import java.util.Vector;
import javax.inject.Named;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Named
public class HelloWorld {

    private String name;
    
    private String letters;
    
    private String numbers;
    
    private String email;
    
    public String getName() {
        return name;
    }
    
    @NotNull
    @NotEmpty
    @Pattern(regexp = "[A-Za-z]*", message = "must contain only letters")
    public String getLetters()
    {
       return letters;
    }

    public void setLetters(String letters)
    {
       this.letters = letters;
    }

    @NotNull
    @NotEmpty
    @Digits(fraction = 0, integer = 2)
    public String getNumbers()
    {
       return numbers;
    }

    public void setNumbers(String numbers)
    {
       this.numbers = numbers;
    }

    @NotNull
    @NotEmpty
    @Email
    public String getEmail()
    {
       return email;
    }

    public void setEmail(String email)
    {
       this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector<String> getList() {
        Vector<String> v = new Vector<String>();
        v.add("apple");
        v.add("banana");
        v.add("kiwi");
        return v;
    }

}
