package org.makumba.devel.relations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.commons.FileUtils;
import org.makumba.commons.NamedResources;
import org.makumba.commons.ReadableFormatter;
import org.makumba.devel.relations.RelationCrawler.MakumbaRelatedFileFilter;
import org.makumba.providers.DataDefinitionProvider;

/**
 * Tool that triggers the crawling of a context, if it wasn't crawled previously.<br>
 * To be used by the developer tools. <br>
 * TODO: implement a way to flush all the relations in order to re-crawl, using e.g. a param. needs to have a new method
 * in RelationCrawler that runs a "delete from..."
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: RelationCrawlerTool.java,v 1.1 Oct 19, 2008 5:18:46 PM manu Exp $
 */
public class RelationCrawlerTool extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // fetch parameters
        String webappRoot = req.getSession().getServletContext().getRealPath("/");
        if (webappRoot.endsWith("/")) {
            webappRoot = webappRoot.substring(0, webappRoot.length() - 1);
        }

        // initialise crawler
        RelationCrawler rc = RelationCrawler.getRelationCrawler(webappRoot, RelationCrawler.getDefaultTargetDatabase(),
            false, "file://", req.getContextPath().startsWith("/") ? req.getContextPath() : "root", false);

        // crawl
        Date beginDate = new Date();
        Logger.getLogger("org.makumba.devel.relations").info("\nCrawling starts at " + beginDate + "\n");

        ArrayList<String> allFilesInDirectory = FileUtils.getAllFilesInDirectory(webappRoot, new String[] {},
            new MakumbaRelatedFileFilter());
        Collections.sort(allFilesInDirectory);
        String[] files = allFilesInDirectory.toArray(new String[allFilesInDirectory.size()]);

        // while we crawl, we adjust the MDD provider root to the webapp root
        DataDefinitionProvider.setWebappRoot(webappRoot);

        for (String file : files) {
            rc.crawl(file);
        }

        // we set it back to null after the crawling and clean the cache
        DataDefinitionProvider.setWebappRoot(null);
        NamedResources.cleanStaticCache(DataDefinitionProvider.infos);

        rc.writeRelationsToDb(false);

        Logger.getLogger("org.makumba.devel.relations").info(
            "\n\nCrawling finished, took: "
                    + ReadableFormatter.readableAge(System.currentTimeMillis() - beginDate.getTime()));

        // RelationCrawler.writeJSPAnalysisError(webappRoot + File.separator + "analysis-errors.txt",
        // rc.getJSPAnalysisErrors(), rc.getJSPCrawlCount());

        Logger.getLogger("org.makumba.devel.relations").info(
            "\n\nWriting to database finished, total time: "
                    + ReadableFormatter.readableAge(System.currentTimeMillis() - beginDate.getTime()));

        // go back to the page that called us
        resp.sendRedirect(req.getHeader("referer"));
    }

}
