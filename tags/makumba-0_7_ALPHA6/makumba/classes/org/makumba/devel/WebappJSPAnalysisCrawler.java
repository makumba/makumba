package org.makumba.devel;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Logger;

import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.FileUtils;
import org.makumba.commons.ReadableFormatter;
import org.makumba.devel.relations.JspRelationsAnalyzer;
import org.makumba.devel.relations.RelationCrawler;
import org.makumba.devel.relations.RelationParseStatus;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * Analyses all JSP pages of a webapp and writes the found erorrs to a file. Inspired from {@link RelationCrawler}, but
 * focusing on the analysis part only, for performance reasons.
 * 
 * @author Rudolf Mayer
 * @version $Id: WebappPageAnalyser.java,v 1.1 May 17, 2008 3:35:12 PM rudi Exp $
 */
public class WebappJSPAnalysisCrawler {
    /** A file filter that accepts JSP files and directories. */
    private static final class JSPFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".jsp") || pathname.isDirectory();
        }
    }

    public static Logger logger = Logger.getLogger("org.makumba.pageAnalyser");

    public static Hashtable<String, Throwable> JSPAnalysisErrors = new Hashtable<String, Throwable>();

    public static final FileFilter filter = new JSPFileFilter();

    public static void main(String[] args) {
        JSAP jsap = new JSAP();
        try {
            jsap.registerParameter(new FlaggedOption("webappRoot", JSAP.STRING_PARSER, ".", false, 'w', "root"));
            jsap.registerParameter(new FlaggedOption("skipPaths", JSAP.STRING_PARSER, null, false, 's', "skipPaths"));
            jsap.registerParameter(new FlaggedOption("analysisOutputFile", JSAP.STRING_PARSER, null, false, 'o',
                    "output"));
            jsap.registerParameter(new UnflaggedOption("path", JSAP.STRING_PARSER, null, false, true));
        } catch (JSAPException e) {
            e.printStackTrace();
        }
        JSAPResult result = jsap.parse(args);
        if (!result.success()) {
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
        String[] skipPaths = result.getString("skipPaths") != null ? result.getString("skipPaths").split(",")
                : new String[] {};
        String analysisOutputFile = "analysis-errors.txt";
        if (result.getString("analysisOutputFile") != null) {
            analysisOutputFile = result.getString("analysisOutputFile");
        }

        System.out.println("Starting relation crawler, config:");
        System.out.println("\twebappRoot: " + webappRoot);
        System.out.println("\tanalysisOutputFile: " + analysisOutputFile);
        System.out.println("\tSkip: " + Arrays.toString(skipPaths));
        System.out.println("\t(from : " + Arrays.toString(args) + ")");
        Date beginDate = new Date();
        System.out.println("\nCrawling starts at " + beginDate + "\n");

        ArrayList<String> allFilesInDirectory = FileUtils.getAllFilesInDirectory(webappRoot, skipPaths, filter);
        Collections.sort(allFilesInDirectory);
        String[] files = (String[]) allFilesInDirectory.toArray(new String[allFilesInDirectory.size()]);

        for (int i = 0; i < files.length; i++) {
            Throwable t = crawl(files[i], webappRoot);
            if (t != null) {
                JSPAnalysisErrors.put(files[i], t);
            }
        }
        RelationCrawler.writeJSPAnalysisError(analysisOutputFile, JSPAnalysisErrors, files.length);
        System.out.println("\n\nCrawling finished, took: "
                + ReadableFormatter.readableAge(System.currentTimeMillis() - beginDate.getTime()));
    }

    public static Throwable crawl(String path, String root) {
        JspParseData jpd = JspParseData.getParseData(root, path, JspRelationsAnalyzer.getInstance());
        try {
            jpd.getAnalysisResult(new RelationParseStatus());
        } catch (Throwable t) {
            // page analysis failed
            logger.warning("Page analysis for page " + path + " failed due to error: " + t.getMessage());
            return t;
        }
        return null;
    }

}
