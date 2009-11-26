package org.makumba.devel;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.FileUtils;
import org.makumba.commons.ReadableFormatter;
import org.makumba.devel.relations.JspRelationsAnalyzer;
import org.makumba.devel.relations.RelationCrawler;
import org.makumba.devel.relations.RelationParseStatus;

/**
 * Analyses all JSP pages of a webapp and writes the found erorrs to a file. Inspired from {@link RelationCrawler}, but
 * focusing on the analysis part only, for performance reasons.
 * 
 * @author Rudolf Mayer
 * @version $Id: WebappPageAnalyser.java,v 1.1 May 17, 2008 3:35:12 PM rudi Exp $
 */
public class WebappJSPAnalysisCrawler {
    /** A file filter that accepts JSP files and directories. */
    public static final class JSPFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".jsp") || pathname.isDirectory();
        }
    }

    public static Logger logger = Logger.getLogger("org.makumba.pageAnalyser");

    public static Hashtable<String, Throwable> JSPAnalysisErrors = new Hashtable<String, Throwable>();

    public static final FileFilter filter = new JSPFileFilter();

    public static void main(String[] args) {
        
        CommandLine line = parseCrawlParams(args, WebappJSPAnalysisCrawler.class.getName());

        String webappRoot = line.getOptionValue("w");
        String[] skipPaths = line.getOptionValues("s");
        
        // this seems to be a bug in commons CLI
        if(skipPaths == null) {
            skipPaths = new String[] {};
        }
        String analysisOutputFile = line.getOptionValue("o", "analysis-errors.txt");

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
            JspParseData jpd = JspParseData.getParseData(webappRoot, files[i], JspRelationsAnalyzer.getInstance());
            try {
                jpd.getAnalysisResult(new RelationParseStatus());
            } catch (Throwable t) {
                // page analysis failed
                logger.warning("Page analysis for page " + files[i] + " failed due to error: " + t.getMessage());
                JSPAnalysisErrors.put(files[i], t);
            }
        }
        RelationCrawler.writeJSPAnalysisError(analysisOutputFile, JSPAnalysisErrors, files.length);
        System.out.println("\n\nCrawling finished, took: "
                + ReadableFormatter.readableAge(System.currentTimeMillis() - beginDate.getTime()));
    }
    
    public static CommandLine parseCrawlParams(String[] args, String name) {
        Options options = new Options();
        
        Option webappRootOption = new Option("w", "root", true, "the root of the makumba webapp to crawl");
        Option skipPathsOption = new Option("s", "skipPaths", true, "a list of paths to be skipped during the crawling, separated by a comma");
        skipPathsOption.setValueSeparator(',');
        Option analysisOutput = new Option("o", "output", true, "the file in which the output of the analysis should be written");
        Option queryOutput = new Option("q", "queryOutputFile", true, "the file in which the crawled queries should be written");
        
        options.addOption(webappRootOption);
        options.addOption(skipPathsOption);
        options.addOption(analysisOutput);
        options.addOption(queryOutput);
        
        HelpFormatter formatter = new HelpFormatter();
        
        CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        
        try {
            line = parser.parse(options, args);
        } catch(ParseException p) {
            System.out.println("Error while executing the crawler: " + p.getMessage());
            System.out.println();
            formatter.printHelp("java " + name + "[OPTION]... [FILE]...", options);
            System.exit(-1);
        }
        
        return line;
        
    }

}
