package org.makumba.devel.relations;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.TransactionProvider;

/**
 * This crawler looks for relations between Makumba files and stores them in a database table.<br>
 * It is ran with the following arguments: RelationCrawler webappRoot destinationDb [fileList]<br>
 * where destinationDb is the database (e.g. localhost_mysql_karamba) to which the relations table should be written.
 * FIXME currently this needs to have the webapp's dataDefinitions folder in the classpath in order to run
 *
 * @author Manuel Gay
 * @version $Id: RelationsCrawler.java,v 1.1 Apr 13, 2008 4:16:16 PM manu Exp $
 */
public class RelationCrawler {

    protected static boolean subProcess = false;

    private Logger logger = Logger.getLogger("org.makumba.relationCrawler");

    private String webappRoot;

    private String targetDatabase;

    private boolean forceDatabase;

    private JSPRelationMiner JSPRelationMiner;

    private MDDRelationMiner MDDRelationMiner;

    private JavaRelationMiner JavaRelationMiner;

    private Map<String, Dictionary<String, Object>> detectedRelations = new HashMap<String, Dictionary<String, Object>>();

    public RelationCrawler(String webappRoot, String targetDatabase, boolean forcetarget) {
        this.webappRoot = webappRoot;
        this.targetDatabase = targetDatabase;
        this.forceDatabase = forcetarget;

        // initalise relation miners
        this.JSPRelationMiner = new JSPRelationMiner(this);
        this.MDDRelationMiner = new MDDRelationMiner(this);
        this.JavaRelationMiner = new JavaRelationMiner(this);
    }

    public Map<String, Dictionary<String, Object>> getDetectedRelations() {
        return this.detectedRelations;
    }

    public String getRelationDatabase() {
        return targetDatabase;
    }

    public void setRelationDatabase(String relationDatabase) {
        this.targetDatabase = relationDatabase;
    }

    public String getWebappRoot() {
        return this.webappRoot;
    }

    public static void main(String[] args) {

        if (args.length == 0) {

            // composing example start arguments

            // some JSPs
            String webappPath = "/home/manu/workspace/karamba/public_html/";

            Vector<String> arguments = new Vector<String>();
            arguments.add(webappPath);
            arguments.add("localhost_mysql_makumba");
            arguments.add("forceTargetDb");
            File dir = new File(webappPath + "archive/");
            String[] files = dir.list();
            for (int i = 0; i < files.length; i++) {
                if (new File(webappPath + "archive/" + files[i]).isFile()) {
                    arguments.add("/archive/" + files[i]);
                }
            }

            // some MDDs
            File dir2 = new File(webappPath + "WEB-INF/classes/dataDefinitions/best");
            String[] files2 = dir2.list();
            for (int i = 0; i < files2.length; i++) {
                if (new File(webappPath + "WEB-INF/classes/dataDefinitions/best/" + files2[i]).isFile()) {
                    arguments.add("/WEB-INF/classes/dataDefinitions/best/" + files2[i]);
                }
            }

            // some Java-s
            File dir3 = new File(webappPath + "WEB-INF/classes/org/eu/best/privatearea");
            String[] files3 = dir3.list();
            for (int i = 0; i < files3.length; i++) {
                if (new File(webappPath + "WEB-INF/classes/org/eu/best/privatearea/" + files3[i]).isFile()) {
                    arguments.add("/WEB-INF/classes/org/eu/best/privatearea/" + files3[i]);
                }
            }

            //String[] args1 = { "/home/manu/workspace/karamba/public_html", "localhost_mysql_makumba", "forcetarget",
            //        "/WEB-INF/classes/org/eu/best/general/AccessControlLogic.java" };
            //args = args1;

            args = arguments.toArray(new String[arguments.size()]);
        }

        System.out.println("RelationsCrawler main");

        String webappRoot = args[0];
        String targetDatabase = args[1];
        String forceDatabase = args[2];

        String[] path = new String[args.length - 3];

        for (int i = 3; i < args.length; i++) {
            path[i - 3] = args[i];
        }

        // this if when we run the guy from the command line directly with the right CP
        boolean rightClassPath = RelationCrawler.class.getClassLoader().getResource("general/Person.mdd") != null;
        if (!rightClassPath && !subProcess) {
            subProcess = true;
            System.out.println("subprocess is " + subProcess);
            System.out.println("right class path " + rightClassPath);
            Runtime r = Runtime.getRuntime();
            Vector<String> cmd = new Vector<String>();
            cmd.add("java");
            cmd.add("-cp");
            cmd.add(webappRoot + "/WEB-INF/classes/dataDefinitions/:.");
            cmd.add("org.makumba.devel.relations.RelationsCrawler");
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    cmd.add(args[i]);
                }
            }
            String[] cmds = cmd.toArray(new String[cmd.size()]);

            File f = new File("./classes");
            System.out.println(f.getAbsolutePath());
            System.out.println(Arrays.toString(cmds));

            try {
                Process p = r.exec(cmds, null, f);
                p.waitFor();
                System.out.println("Executed.");
                int c;
                while ((c = p.getInputStream().read()) != -1) {
                    System.out.write(c);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("We have the right classpath");

            RelationCrawler rc = new RelationCrawler(webappRoot, targetDatabase,
                    forceDatabase.equals("forceTargetDb"));

            for (int i = 0; i < path.length; i++) {
                rc.crawl(path[i]);
            }

            rc.writeRelationsToDb();
        }
    }

    /**
     * Crawls through a file using the relation miners
     *
     * @param path
     *            the path to the file
     */
    public void crawl(String path) {
        if (path.endsWith(".jsp")) {

            this.JSPRelationMiner.crawl(path);

        } else if (path.endsWith(".mdd")) {

            this.MDDRelationMiner.crawl(path);

        } else if (path.endsWith(".java")) {

            this.JavaRelationMiner.crawl(path);
        }
    }

    /**
     * Adds a relation which will later on be written to the database
     *
     * @param toFile
     *            the path to the file there is a relation with
     * @param relationData
     *            the relation data
     */
    public void addRelation(String toFile, Dictionary<String, Object> relationData) {

        Dictionary<String, Object> rel;
        if ((rel = detectedRelations.get(toFile)) != null) {

            // we just add the detail
            Vector<Dictionary<String, Object>> origin = (Vector<Dictionary<String, Object>>) rel.get("origin");
            origin.add((Dictionary<String, Object>) relationData.get("origin"));
            rel.put("origin", origin);

            detectedRelations.put(toFile, rel);

        } else {
            // we extract the relation detail, and add it to a detail Vector of Dictionaries
            Dictionary<String, Object> originData = (Dictionary<String, Object>) relationData.get("origin");
            Vector<Dictionary<String, Object>> origin = new Vector<Dictionary<String, Object>>();
            origin.add(originData);

            relationData.put("origin", origin);

            detectedRelations.put(toFile, relationData);
        }

    }

    /**
     * Writes the relations to the database TODO make sure the previous data is removed, before writing the new
     * relations
     */
    public void writeRelationsToDb() {
        // here we save all the computed relations to the relations database

        Map<String, Dictionary<String, Object>> relations = getDetectedRelations();

        TransactionProvider tp = TransactionProvider.getInstance();

        Pointer webappPointer = determineRelationsDatabase(tp, forceDatabase);

        Iterator<String> it = relations.keySet().iterator();
        while (it.hasNext()) {
            String toFile = it.next();
            Dictionary<String, Object> relationInfo = relations.get(toFile);
            String relationType = (String) relationInfo.get("type");
            String fromFile = (String) relationInfo.get("fromFile");

            System.out.println(fromFile + " -(" + relationType + ")-> " + toFile);

            // now we insert the records into the relations table, in the right database
            Transaction tr2 = null;

            try {
                tr2 = tp.getConnectionTo(targetDatabase);

                // we check if there's already such a relation in the database

                Object[] args = { toFile, fromFile };
                Vector<Dictionary<String, Object>> previousRelation = tr2.executeQuery(
                    "SELECT relation AS relation FROM org.makumba.devel.relations.Relation relation WHERE relation.toFile = $1 AND relation.fromFile = $2",
                    args);

                if (previousRelation.size() > 0) {
                    // we delete the previous relation origin

                    Pointer previousRelationPtr = (Pointer) previousRelation.get(0).get("relation");

                    Vector<Dictionary<String, Object>> previousRelationOrigin = tr2.executeQuery(
                        "SELECT origin AS origin FROM org.makumba.devel.relations.Relation relation, relation.origin origin WHERE relation = $1",
                        new Object[] { previousRelationPtr });

                    for (Iterator iterator = previousRelationOrigin.iterator(); iterator.hasNext();) {
                        Dictionary<String, Object> dictionary = (Dictionary<String, Object>) iterator.next();
                        tr2.delete((Pointer) dictionary.get("origin"));
                    }

                    // we now delete the relation itself
                    tr2.delete(previousRelationPtr);

                }

                relationInfo.put("toFile", toFile);
                relationInfo.put("webapp", webappPointer);

                Vector<Pointer> originSet = new Vector<Pointer>();

                Vector<Dictionary<String, Object>> relationOrigins = (Vector<Dictionary<String, Object>>) relationInfo.get("origin");

                for (Iterator iterator = relationOrigins.iterator(); iterator.hasNext();) {
                    Dictionary<String, Object> dictionary = (Dictionary<String, Object>) iterator.next();
                    originSet.add(tr2.insert("org.makumba.devel.relations.RelationOrigin", dictionary));
                }

                relationInfo.put("origin", originSet);

                tr2.insert("org.makumba.devel.relations.Relation", relationInfo);

            } finally {
                tr2.close();
            }
        }

    }

    /**
     * Figures out the relations database, i.e. to which database relations should be written to, and if there's none,
     * creates an entry in the default database (per webappRoot).
     *
     * @param tp
     *            the TransactionProvider that makes it possible to run the determination query
     * @param forceDestination
     *            whether or not to force the database to write to. If enabled, will also update the relations database
     *            record in the default database.
     * @return a Pointer to the record in the default database that points to the relations database
     */
    private Pointer determineRelationsDatabase(TransactionProvider tp, boolean forceDestination) {

        Pointer webappPointer = null;

        // first we are going to check in which db the relations are, if there are any
        Transaction tr = null;
        try {
            tr = tp.getConnectionTo(tp.getDefaultDataSourceName());
            Vector<Dictionary<String, Object>> databaseLocation = tr.executeQuery(
                "SELECT wdb AS webappPointer, wdb.relationDatabase AS relationDatabase from org.makumba.devel.relations.WebappDatabase wdb WHERE wdb.webappRoot = $1",
                new String[] { webappRoot });
            if (databaseLocation.size() > 1) {
                // that's too much
                throw new RuntimeException("Too many possible locations for the relations database of webapp "
                        + webappRoot);
            } else if (databaseLocation.size() == 1) {

                if (forceDestination) {
                    // we set the location to the one provided at execution
                    Dictionary<String, String> data = new Hashtable<String, String>();
                    data.put("relationDatabase", targetDatabase);
                    Pointer oldPointer = webappPointer = (Pointer) databaseLocation.get(0).get("webappPointer");
                    tr.update(oldPointer, data);
                } else {
                    // we re-use the previous location
                    targetDatabase = (String) databaseLocation.get(0).get("relationDatabase");
                    webappPointer = (Pointer) databaseLocation.get(0).get("webappPointer");
                }
            } else if (databaseLocation.size() == 0) {
                // we set the location to the one provided at execution
                Dictionary<String, String> data = new Hashtable<String, String>();
                data.put("webappRoot", webappRoot);
                data.put("relationDatabase", targetDatabase);
                webappPointer = tr.insert("org.makumba.devel.relations.WebappDatabase", data);
            }
        } finally {
            tr.close();
        }

        return webappPointer;

    }
}