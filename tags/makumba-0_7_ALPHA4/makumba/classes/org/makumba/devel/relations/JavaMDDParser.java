package org.makumba.devel.relations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple parser that detects relations to MDDs in Java files (queries and other)<br>
 * FIXME the regexp needs to be improved so as to exclude queries that are composed by additions of strings. now we try
 * to detect this by the usage of "+" but this can lead to issues with aggregate functions.<br>
 * TODO detect db.insert() etc.
 * 
 * @author Manuel Gay
 * @version $Id: JavaMDDParser.java,v 1.1 Apr 20, 2008 11:49:57 AM manu Exp $
 */
public class JavaMDDParser {

    private File file;

    private String content;

    private Pattern MakumbaQuery;

    private Vector<String> queries = new Vector<String>();

    public static void main(String[] args) {

        JavaMDDParser jqp = new JavaMDDParser(
                "/home/manu/workspace/karamba/public_html/WEB-INF/classes/org/eu/best/general/AccessControlLogic.java");

    }

    public JavaMDDParser(String path) {
        this.file = new File(path);
        content = readFile(file);

        String spaces = "[\\s]*";
        String minOneSpaces = "\\s" + spaces;

        MakumbaQuery = Pattern.compile("\"SELECT" + minOneSpaces + ".*(\"" + spaces + "," + spaces + ")");

        parse();

    }

    public Vector<String> getQueries() {
        return this.queries;
    }

    private void parse() {

        handleSelectQuery();
    }

    private void handleSelectQuery() {
        Matcher m = MakumbaQuery.matcher(content);
        while (m.find()) {
            String match = content.substring(m.start(), m.end());
            // FIXME this should get a better regexp instead of trying to remove heuristically queries built from
            // several strings
            // as well it will make fail queries using advanced aggregate functions
            if (match.indexOf("+") == -1) {
                // "SELECT trainer as trainer FROM best.internal.training.Trainer trainer WHERE trainer.person = $1",
                match = match.substring(1, match.lastIndexOf('"'));
                queries.add(match);
            }

        }

    }

    private String readFile(File file) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader rd = new BufferedReader(new FileReader(file));
            char[] buffer = new char[2048];
            int n;
            while ((n = rd.read(buffer)) != -1)
                sb.append(buffer, 0, n);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
