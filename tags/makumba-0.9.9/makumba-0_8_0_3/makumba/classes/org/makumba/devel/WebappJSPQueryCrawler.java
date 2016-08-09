package org.makumba.devel;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import org.makumba.analyser.PageCache;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.FileUtils;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.ReadableFormatter;
import org.makumba.devel.relations.JspRelationsAnalyzer;
import org.makumba.devel.relations.RelationParseStatus;
import org.makumba.list.engine.ComposedQuery;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Analyses all JSP pages of a webapp and writes the queries found in the pages to a file. Inspired from
 * {@link WebappJSPAnalysisCrawler}.
 * 
 * @author Rudolf Mayer
 * @version $Id: WebappPageAnalyser.java,v 1.1 May 17, 2008 3:35:12 PM rudi Exp $
 */
public class WebappJSPQueryCrawler {
    /** A file filter that accepts JSP files and directories. */
    private static final class JSPFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".jsp") || pathname.isDirectory();
        }
    }

    public static Logger logger = Logger.getLogger("org.makumba.pageAnalyser");

    public static Hashtable<String, Throwable> JSPAnalysisErrors = new Hashtable<String, Throwable>();

    public static final FileFilter filter = new JSPFileFilter();

    public static void main(String[] args) throws FileNotFoundException {
        JSAPResult result = WebappJSPAnalysisCrawler.parseCrawlParams(args);

        String webappRoot = result.getString("webappRoot");
        String[] skipPaths = result.getString("skipPaths") != null ? result.getString("skipPaths").split(",")
                : new String[] {};
        String analysisOutputFile = "queries.txt";
        if (result.getString("queryOutputFile") != null) {
            analysisOutputFile = result.getString("queryOutputFile");
        }

        System.out.println("Starting query crawler, config:");
        System.out.println("\twebappRoot: " + webappRoot);
        System.out.println("\tqueryOutputFile: " + analysisOutputFile);
        System.out.println("\tSkip: " + Arrays.toString(skipPaths));
        System.out.println("\t(from : " + Arrays.toString(args) + ")");
        Date beginDate = new Date();
        System.out.println("\nCrawling starts at " + beginDate + "\n");

        File f = new File(analysisOutputFile);
        PrintWriter pw = new PrintWriter(new FileOutputStream(f));

        ArrayList<String> allFilesInDirectory = FileUtils.getAllFilesInDirectory(webappRoot, skipPaths, filter);
        Collections.sort(allFilesInDirectory);
        String[] files = (String[]) allFilesInDirectory.toArray(new String[allFilesInDirectory.size()]);

        for (int i = 0; i < files.length; i++) {
            JspParseData jpd = JspParseData.getParseData(webappRoot, files[i], JspRelationsAnalyzer.getInstance());
            try {
                Object analysisResult = jpd.getAnalysisResult(new RelationParseStatus());
                if (analysisResult != null && analysisResult instanceof PageCache) {
                    PageCache pageCache = (PageCache) analysisResult;
                    Map<Object, Object> cache = pageCache.retrieveCache(MakumbaJspAnalyzer.QUERY);
                    if (cache != null) {
                        for (Object key : cache.keySet()) {
                            ComposedQuery query = (ComposedQuery) cache.get(key);
                            pw.println(query.getTypeAnalyzerQuery());
                        }
                    }
                }
            } catch (Throwable t1) {
                t1.printStackTrace();
            }
        }

        System.out.println("\n\nCrawling finished, took: "
                + ReadableFormatter.readableAge(System.currentTimeMillis() - beginDate.getTime()));
        System.out.println("Wrote queries to file: " + f.getAbsolutePath());
    }

}
