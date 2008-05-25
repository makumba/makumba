package org.makumba.devel.relations;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.FileUtils;
import org.makumba.commons.NamedResources;
import org.makumba.commons.ReadableFormatter;
import org.makumba.devel.relations.FileRelations.RelationOrigin;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.datadefinition.makumba.RecordInfo;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * This crawler looks for relations between Makumba files and stores them in a database table.<br>
 * TODO keep a list of things that could not be analyzed (may it be entire files, or query strings etc<br>
 * 
 * @author Manuel Gay
 * @version $Id: RelationsCrawler.java,v 1.1 Apr 13, 2008 4:16:16 PM manu Exp $
 */
public class RelationCrawler {

    protected static boolean subProcess = false;

    private String webappRoot;

    private String targetDatabase;

    private boolean forceDatabase;

    private int JSPCrawlCount = 0;

    private JSPRelationMiner JSPRelationMiner;

    private MDDRelationMiner MDDRelationMiner;

    private JavaRelationMiner JavaRelationMiner;

    private Hashtable<String, Throwable> JSPAnalysisErrors = new Hashtable<String, Throwable>();

    private Vector<String> JavaAnalysisErrors = new Vector<String>();

    private Map<String, Map<String, Vector<Dictionary<String, Object>>>> detectedRelations = new HashMap<String, Map<String, Vector<Dictionary<String, Object>>>>();

    private String URLprefix;

    private String URLroot;

    private TransactionProvider tp = TransactionProvider.getInstance();

    private static Map<String, RelationCrawler> relationCrawlers = new HashMap<String, RelationCrawler>();

    /**
     * Gets a RelationCrawler instance.
     * 
     * @param webappRoot
     *            the path to the root of the webapp that should be crawled
     * @param targetDatabase
     *            the makumba name of the database the relations should be written to
     * @param forcetarget
     *            indicates whether the target database should be forced: if set to true, even if relations were
     *            previously written to another database, this will force writing them to the indicated database
     * @param URLprefix
     *            the prefix of the relation URL, e.g. "file://"
     * @param URLroot
     *            the root of the relation, e.g. a webapp name
     * @return a {@link RelationCrawler} instance
     */
    public static RelationCrawler getRelationCrawler(String webappRoot, String targetDatabase, boolean forcetarget,
            String URLprefix, String URLroot) {
        RelationCrawler instance = relationCrawlers.get(webappRoot + targetDatabase + forcetarget + URLprefix + URLroot);
        if (instance == null) {
            instance = new RelationCrawler(webappRoot, targetDatabase, forcetarget, URLprefix, URLroot);
            relationCrawlers.put(webappRoot + targetDatabase + forcetarget + URLprefix + URLroot, instance);
        }
        return instance;
    }

    private RelationCrawler(String webappRoot, String targetDatabase, boolean forcetarget, String URLprefix,
            String URLroot) {
        this.webappRoot = webappRoot;
        this.targetDatabase = targetDatabase;
        this.forceDatabase = forcetarget;
        this.URLprefix = URLprefix;
        this.URLroot = URLroot;

        // initalise relation miners
        this.JSPRelationMiner = new JSPRelationMiner(this);
        this.MDDRelationMiner = new MDDRelationMiner(this);
        this.JavaRelationMiner = new JavaRelationMiner(this);

    }

    private Map<String, Map<String, Vector<Dictionary<String, Object>>>> getDetectedRelations() {
        return this.detectedRelations;
    }

    public String getRelationDatabase() {
        return targetDatabase;
    }

    protected String getWebappRoot() {
        return this.webappRoot;
    }

    public Hashtable<String, Throwable> getJSPAnalysisErrors() {
        return JSPAnalysisErrors;
    }

    public Vector<String> getJavaAnalysisErrors() {
        return JavaAnalysisErrors;
    }

    protected void addJSPAnalysisError(String s, Throwable t) {
        this.JSPAnalysisErrors.put(s, t);
    }

    protected void addJavaAnalysisError(String s) {
        this.JavaAnalysisErrors.add(s);
    }

    public static void writeJSPAnalysisError(String fileName, Hashtable<String, Throwable> analysisErrors,
            int JSPCrawlCount) {

        File f = new File(fileName);
        PrintWriter pw = null;
        try {
            f.createNewFile();
            pw = new PrintWriter(new FileOutputStream(f));
            pw.println("Date of crawling: " + new Date());
            pw.println("Total number of page crawled: " + JSPCrawlCount);
            pw.println("Total number of JSP page analysis errors: " + analysisErrors.size());
            pw.println("\nError summary\n");

            int n = 0;
            for (String file : analysisErrors.keySet()) {
                pw.println(n + ".\t" + file);
                pw.println("\t" + analysisErrors.get(file).getMessage() + "\n");
                n++;
            }

            pw.println("\nError detail\n");

            n = 0;
            for (String file : analysisErrors.keySet()) {
                pw.println(n + ".\t" + file + "\n\n");
                analysisErrors.get(file).printStackTrace(pw);
                pw.println("******************************************************************************\n");
                n++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pw.flush();
            pw.close();
        }
    }

    /**
     * Extracts relations from a set of files.
     * 
     * @param args
     *            the arguments needed to crawl: webappRoot destinationDb forceDatabase URLprefix URLroot [fileList]<br>
     *            where:
     *            <ul>
     *            <li>webappRoot is the absolute path to the webapp root on the disk</li>
     *            <li>destinationDb is the database (e.g. localhost_mysql_karamba) to which the relations table should
     *            be written</li>
     *            <li>forceDatabase indicates whether relations should be written to the indicated database, even if
     *            there's already an existing table for this webapp indicated somewhere. in order to be enabled, the
     *            value should be "forceDatabase", any other value disabling it. this should not be used for standalone
     *            makumba webapps, but may be used in an environment where all makumba webapps should have their
     *            relations saved in the same table of one database.</li>
     *            <li>URLprefix is the prefix given to the file URL, e.g. "file://"</li>
     *            <li>URLroot is the root of the URL, e.g. the name of the crawled webapp</li>
     *            <li>fileList is a list of files to be crawled</li>
     *            </ul>
     */
    public static void main(String[] args) {

        JSAP jsap = new JSAP();
        try {
            jsap.registerParameter(new FlaggedOption("webappRoot", JSAP.STRING_PARSER, ".", false, 'w', "root"));
            jsap.registerParameter(new FlaggedOption("targetDatabase", JSAP.STRING_PARSER, null, true, 'd', "database"));
            jsap.registerParameter(new Switch("forceDatabase", 'f', "forceDB"));
            jsap.registerParameter(new FlaggedOption("urlPrefix", JSAP.STRING_PARSER, null, false, 'p', "urlPrefix"));
            jsap.registerParameter(new FlaggedOption("urlRoot", JSAP.STRING_PARSER, null, false, 'u', "urlRoot"));
            jsap.registerParameter(new FlaggedOption("skipPaths", JSAP.STRING_PARSER, null, false, 's', "skipPaths"));
            jsap.registerParameter(new UnflaggedOption("path", JSAP.STRING_PARSER, null, false, true));
        } catch (JSAPException e) {
            e.printStackTrace();
        }
        JSAPResult result = jsap.parse(args);
        if (!result.success()) {
            // print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help. This is called "beating the user with a clue stick."
            System.err.println();
            for (Iterator<?> errs = result.getErrorMessageIterator(); errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }
            System.err.println();
            System.err.println("Usage: java " + RelationCrawler.class.getName());
            System.err.println("                " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(-1);
        }

        String webappRoot = result.getString("webappRoot");
        String targetDatabase = result.getString("targetDatabase");
        boolean forceDatabase = result.getBoolean("forceDatabase");
        String URLprefix = result.getString("urlPrefix");
        String URLroot = result.getString("urlRoot");
        String[] skipPaths = result.getString("skipPaths") != null ? result.getString("skipPaths").split(",")
                : new String[] {};
        String[] files = result.getString("path") != null ? result.getString("path").split(",") : new String[] {};

        System.out.println("Starting relation crawler, config:");
        System.out.println("\twebappRoot: " + webappRoot);
        System.out.println("\ttargetDatabase: " + targetDatabase);
        System.out.println("\tforceDatabase: " + forceDatabase);
        System.out.println("\tURLprefix: " + URLprefix);
        System.out.println("\tURLroot: " + URLroot);
        System.out.println("\tSkip: " + Arrays.toString(skipPaths));
        System.out.println("\tFiles: " + Arrays.toString(files));
        System.out.println("\t(from : " + Arrays.toString(args) + ")");
        Date beginDate = new Date();
        System.out.println("\nCrawling starts at " + beginDate + "\n");

        if (files == null || files.length == 0) {
            System.out.println("\nNo paths indicated, crawling all files in webapp\n");
            ArrayList<String> allFilesInDirectory = FileUtils.getAllFilesInDirectory(webappRoot, skipPaths,
                new MakumbaRelatedFileFilter());
            Collections.sort(allFilesInDirectory);
            files = (String[]) allFilesInDirectory.toArray(new String[allFilesInDirectory.size()]);
        }

        RelationCrawler rc = getRelationCrawler(webappRoot, targetDatabase, forceDatabase, URLprefix == null ? ""
                : URLprefix, URLroot == null ? "" : URLroot);

        // while we crawl, we adjust the MDD provider root to the webapp root
        RecordInfo.setWebappRoot(webappRoot);

        for (int i = 0; i < files.length; i++) {
            rc.crawl(files[i]);
        }

        // we set it back to null after the crawling and clean the cache
        RecordInfo.setWebappRoot(null);
        NamedResources.cleanStaticCache(RecordInfo.infos);

        System.out.println("\n\nCrawling finished, took: "
                + ReadableFormatter.readableAge(System.currentTimeMillis() - beginDate.getTime()));

        RelationCrawler.writeJSPAnalysisError("analysis-errors.txt", rc.JSPAnalysisErrors, rc.JSPCrawlCount);

        rc.writeRelationsToDb();

        System.out.println("\n\nWriting finished, total time: "
                + ReadableFormatter.readableAge(System.currentTimeMillis() - beginDate.getTime()));
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
            this.JSPCrawlCount++;

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
    protected void addRelation(String fromFile, String toFile, Dictionary<String, Object> relationData) {
        Map<String, Vector<Dictionary<String, Object>>> dic;
        Vector<Dictionary<String, Object>> v;
        if ((dic = detectedRelations.get(toFile)) != null) {
            v = dic.get(fromFile);
            if (v == null) {
                v = new Vector<Dictionary<String, Object>>();
            }
            v.add(relationData);
            dic.put(fromFile, v);
        } else {
            dic = new Hashtable<String, Vector<Dictionary<String, Object>>>();
            v = new Vector<Dictionary<String, Object>>();
            v.add(relationData);
            dic.put(fromFile, v);
            detectedRelations.put(toFile, dic);
        }

    }

    /**
     * Writes the relations to the database. This should be called after crawling is done.
     */
    public void writeRelationsToDb() {
        // here we save all the computed relations to the relations database

        Map<String, Map<String, Vector<Dictionary<String, Object>>>> relations = getDetectedRelations();

        Pointer webappPointer = determineRelationsDatabase(tp, forceDatabase);

        for (String toFile : relations.keySet()) {
            Map<String, Vector<Dictionary<String, Object>>> map = relations.get(toFile);
            for (String fromFile : map.keySet()) {
                Vector<Dictionary<String, Object>> origins = map.get(fromFile);
                Dictionary<String, Object> relationInfo = new Hashtable<String, Object>();
                relationInfo.put("type", "dependsOn");
                relationInfo.put("fromFile", fromFile);

                String fromURL = this.URLprefix + this.URLroot
                        + (this.URLroot.endsWith("/") || fromFile.startsWith("/") ? "" : "/") + fromFile;
                String toURL = this.URLprefix + this.URLroot
                        + (this.URLroot.endsWith("/") || toFile.startsWith("/") ? "" : "/") + toFile;

                System.out.println(fromURL + " -(" + "dependsOn" + ")-> " + toURL);

                // now we insert the records into the relations table, in the right database
                Transaction tr2 = null;

                try {
                    tr2 = tp.getConnectionTo(targetDatabase);

                    // we check if there's already such a relation in the database

                    String oqlQuery = "SELECT relation AS relation FROM org.makumba.devel.relations.Relation relation WHERE relation.toFile = $1 AND relation.fromFile = $2";
                    String hqlQuery = "SELECT relation.id AS relation FROM org.makumba.devel.relations.Relation relation WHERE relation.toFile = ? AND relation.fromFile = ?";
                    Object[] args = { toFile, fromFile };
                    Vector<Dictionary<String, Object>> previousRelation = tr2.executeQuery(
                        tp.getQueryLanguage().equals("oql") ? oqlQuery : hqlQuery, args);

                    if (previousRelation.size() > 0) {
                        // we delete the previous relation origin

                        Pointer previousRelationPtr = (Pointer) previousRelation.get(0).get("relation");

                        deleteRelation(tr2, previousRelationPtr);

                    }

                    relationInfo.put("toFile", toFile);
                    relationInfo.put("fromURL", fromURL);
                    relationInfo.put("toURL", toURL);
                    relationInfo.put("webapp", webappPointer);

                    Vector<Pointer> originSet = new Vector<Pointer>();

                    for (Dictionary<String, Object> origin : origins) {
                        originSet.add(tr2.insert("org.makumba.devel.relations.RelationOrigin", origin));
                    }

                    relationInfo.put("origin", originSet);

                    tr2.insert("org.makumba.devel.relations.Relation", relationInfo);

                } finally {
                    tr2.close();
                }
            }
        }
        relations.clear();
    }

    private void deleteRelation(Transaction tr2, Pointer previousRelationPtr) {
        String oqlQuery1 = "SELECT origin AS origin FROM org.makumba.devel.relations.Relation relation, relation.origin origin WHERE relation = $1";
        String hqlQuery1 = "SELECT origin.id AS origin FROM org.makumba.devel.relations.Relation relation JOIN relation.origin origin WHERE relation.id = ?";
        Vector<Dictionary<String, Object>> previousRelationOrigin = tr2.executeQuery(
            tp.getQueryLanguage().equals("oql") ? oqlQuery1 : hqlQuery1, new Object[] { previousRelationPtr });

        for (Dictionary<String, Object> dictionary : previousRelationOrigin) {
            tr2.delete((Pointer) dictionary.get("origin"));
        }

        // we now delete the relation itself
        tr2.delete(previousRelationPtr);
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
            Vector<Dictionary<String, Object>> databaseLocation = getWebappDatabasePointer(tp, tr);
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

    private Vector<Dictionary<String, Object>> getWebappDatabasePointer(TransactionProvider tp, Transaction tr) {
        String oqlQuery = "SELECT wdb AS webappPointer, wdb.relationDatabase AS relationDatabase from org.makumba.devel.relations.WebappDatabase wdb WHERE wdb.webappRoot = $1";
        String hqlQuery = "SELECT wdb.id AS webappPointer, wdb.relationDatabase AS relationDatabase from org.makumba.devel.relations.WebappDatabase wdb WHERE wdb.webappRoot = ?";
        Vector<Dictionary<String, Object>> databaseLocation = tr.executeQuery(
            tp.getQueryLanguage().equals("oql") ? oqlQuery : hqlQuery, new String[] { webappRoot });
        return databaseLocation;
    }

    private String relationDatabaseName = null;

    private String getRelationsDatabaseName(TransactionProvider tp) {
        if (relationDatabaseName != null) {
            return relationDatabaseName;
        }

        Transaction tr = null;
        try {
            tr = tp.getConnectionTo(tp.getDefaultDataSourceName());
            Vector<Dictionary<String, Object>> databaseLocation = getWebappDatabasePointer(tp, tr);
            if (databaseLocation.size() > 1) {
                // that's too much
                throw new RuntimeException("Too many possible locations for the relations database of webapp "
                        + webappRoot);
            } else if (databaseLocation.size() == 1) {
                relationDatabaseName = (String) databaseLocation.firstElement().get("relationDatabase");
                return (String) databaseLocation.firstElement().get("relationDatabase");
            } else if (databaseLocation.size() == 0) {
                return tp.getDefaultDataSourceName();
            }
        } finally {
            tr.close();
        }

        return null;

    }

    /**
     * Deletes the dependency relations of this file
     * 
     * @param relativePath
     *            the relative path to the file
     */
    public void deleteFileRelations(String relativePath) {
        String relationQueryOQL = "SELECT r AS rel FROM org.makumba.devel.relations.Relation r WHERE r.fromFile = $1";
        String relationQueryHQL = "SELECT r.id AS rel FROM org.makumba.devel.relations.Relation r WHERE r.fromFile = ?";

        Transaction t = tp.getConnectionTo(getRelationDatabase());

        Vector<Dictionary<String, Object>> res = t.executeQuery(tp.getQueryLanguage().equals("oql") ? relationQueryOQL
                : relationQueryHQL, new Object[] { relativePath });
        for (Dictionary<String, Object> dictionary : res) {
            deleteRelation(t, (Pointer) dictionary.get("rel"));
        }

        t.close();
    }

    /**
     * Gets the dependencies of a file, i.e. the JSP, Java and MDD files this file depends on
     * 
     * @param relativePath
     *            the relative path to the file, within the webapp root
     * @return a {@link FileRelations} object containing all the dependencies, as well as their origin detail
     */
    public FileRelations getFileDependencies(String relativePath) {

        String relationQueryOQL = "SELECT r.toFile AS file, r AS relation FROM org.makumba.devel.relations.Relation r WHERE r.fromFile = $1";
        String relationQueryHQL = "SELECT r.toFile AS file, r.id AS relation FROM org.makumba.devel.relations.Relation r WHERE r.fromFile = ?";

        return getFileRelations(relativePath, tp.getQueryLanguage().equals("oql") ? relationQueryOQL : relationQueryHQL);

    }

    /**
     * Gets the dependents of a file, i.e. the JSP, Java and MDD files that depend on this file
     * 
     * @param relativePath
     *            the relative path to the file, within the webapp root
     * @return a {@link FileRelations} object containing all the dependents, as well as their origin detail
     */
    public FileRelations getFileDependents(String relativePath) {

        String relationQueryOQL = "SELECT r.fromFile AS file, r AS relation FROM org.makumba.devel.relations.Relation r WHERE r.toFile = $1";
        String relationQueryHQL = "SELECT r.fromFile AS file, r.id AS relation FROM org.makumba.devel.relations.Relation r WHERE r.toFile = ?";

        return getFileRelations(relativePath, tp.getQueryLanguage().equals("oql") ? relationQueryOQL : relationQueryHQL);

    }

    /**
     * Gets the relations of a file, depending on the query to get the relations
     * 
     * @param relativePath
     *            the relative path to the file, within the webapp root
     * @param relationQuery
     *            the query used to get the relations
     * @return the {@link FileRelations} object containing all the dependents, as well as their origin detail
     * @throws MakumbaError
     *             if the file is not found
     */
    private FileRelations getFileRelations(String relativePath, String relationQuery) throws MakumbaError {
        FileRelations result = null;

        if (!new File(webappRoot + File.separator + relativePath).exists()) {
            throw new MakumbaError("File " + relativePath + " does not exist in webapp " + webappRoot);
        }

        // let's fetch the files this file depends on
        TransactionProvider tp = TransactionProvider.getInstance();

        Transaction t = null;
        try {
            String relationDatabase = getRelationsDatabaseName(tp);
            if (tp == null) {
                // TODO replace this with mechanism to launch crawling
                throw new MakumbaError("No relations table in database. Should crawl first.");
            }
            t = tp.getConnectionTo(relationDatabase);
            Vector<Dictionary<String, Object>> dependencies = t.executeQuery(relationQuery,
                new Object[] { relativePath });
            result = buildFileRelations(relativePath, dependencies, t);
        } finally {
            if (t != null) {
                t.close();
            }
        }

        return result;
    }

    /**
     * Builds a {@link FileRelations} object based on a number of relations
     * 
     * @param path
     *            the path of the file we want to know the relations of
     * @param relations
     *            a Vector containing the path of the pointed file, as well as the pointer to the Relation
     * @param t
     *            a Transaction needed to fetch the relation origin
     * @return the {@link FileRelations} object, filled with the detail
     */
    private FileRelations buildFileRelations(String path, Vector<Dictionary<String, Object>> relations, Transaction t) {
        FileRelations fr = new FileRelations(path);

        for (Dictionary<String, Object> dictionary : relations) {
            String file = (String) dictionary.get("file");
            Pointer relation = (Pointer) dictionary.get("relation");

            // fetch the origin of the relation
            String queryOQL = "SELECT ro.startcol AS startcol, ro.endcol AS endcol, ro.startline AS startline, ro.endline AS endline, ro.tagname AS tagname, ro.expr AS expr, ro.field AS field, ro.reason AS reason FROM org.makumba.devel.relations.Relation r, r.origin ro WHERE r = $1 order by ro.startline, ro.startcol";
            String queryHQL = "SELECT ro.startcol AS startcol, ro.endcol AS endcol, ro.startline AS startline, ro.endline AS endline, ro.tagname AS tagname, ro.expr AS expr, ro.field AS field, ro.reason AS reason FROM org.makumba.devel.relations.Relation r, r.origin ro WHERE r.id = ? order by ro.startline, ro.startcol";
            Vector<Dictionary<String, Object>> relationOrigin = t.executeQuery(
                tp.getQueryLanguage().equals("oql") ? queryOQL : queryHQL, new Object[] { relation });

            Vector<RelationOrigin> relationOriginVector = new Vector<RelationOrigin>();

            for (Dictionary<String, Object> dictionary2 : relationOrigin) {
                Object startcol = dictionary2.get("startcol");
                Object endcol = dictionary2.get("endcol");
                Object startline = dictionary2.get("startline");
                Object endline = dictionary2.get("endline");
                Object tagname = dictionary2.get("tagname");
                Object expr = dictionary2.get("expr");
                Object field = dictionary2.get("field");
                Object reason = dictionary2.get("reason");

                RelationOrigin ro = fr.new RelationOrigin(startcol == null ? -1 : (Integer) startcol,
                        endcol == null ? -1 : (Integer) endcol, startline == null ? -1 : (Integer) startline,
                        endline == null ? -1 : (Integer) endline, tagname == null ? null : (String) tagname,
                        expr == null ? null : (String) expr, field == null ? null : (String) field,
                        reason == null ? null : (String) reason);
                relationOriginVector.add(ro);
            }

            if (file.endsWith(".mdd")) {
                fr.getMddRelations().put(file, relationOriginVector);
            } else if (file.endsWith(".jsp")) {
                fr.getJspRelations().put(file, relationOriginVector);
            } else if (file.endsWith(".java")) {
                fr.getJavaRelations().put(file, relationOriginVector);
            }
        }

        return fr;
    }

    /** A filenameFilter that accepts .jsp, .mdd and .java files, or directories. */
    private static final class MakumbaRelatedFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            return pathname.getAbsolutePath().endsWith(".jsp") || pathname.getAbsolutePath().endsWith(".java")
                    || pathname.getAbsolutePath().endsWith(".mdd") || pathname.getAbsolutePath().endsWith(".idd")
                    || pathname.isDirectory();
        }
    }

}