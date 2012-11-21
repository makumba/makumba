package org.makumba.providers.query.hql;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * Generateds file org/makumba/db/hibernate/hql/HqlTokenTypes.txt<br/>
 * The file is needed for HQL analysis
 * 
 * @author Cristian Bogdan
 */
public class DetectHQLTokenTypes {

    public static void main(String[] args) {
        new DetectHQLTokenTypes().generate();
    }

    public void generate() {
        Class<?> c = org.hibernate.hql.antlr.HqlTokenTypes.class;

        PrintStream out = null;
        String HQLTokenPath = "";
        try {
            HQLTokenPath = getClass().getName().substring(0, getClass().getName().lastIndexOf(".")).replace('.', '/')
                    + "/HqlTokenTypes.txt";
            out = new PrintStream(new FileOutputStream("classes/" + HQLTokenPath));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        out.println("Hql");
        Field flds[] = c.getFields();
        for (Field fld : flds) {
            try {
                out.println(fld.getName() + "=" + fld.getInt(null));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        out.close();
        System.out.println("Detected " + flds.length
                + " token types in the HQL supported by the hibernate.jar included in this version.\n"
                + "Generated file " + HQLTokenPath + "\n" + "The file is needed for HQL analysis");
    }

}
