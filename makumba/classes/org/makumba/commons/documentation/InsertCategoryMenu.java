package org.makumba.commons.documentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.ecyrd.jspwiki.ReferenceManager;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiPage;
import com.ecyrd.jspwiki.plugin.AbstractReferralPlugin;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;

/**
 * Plugin that will insert the appropriate menu for the page according to its category, or just display the category<br>
 * 
 * @author Manuel Gay
 * @version $Id: InsertCategoryMenu.java,v 1.1 Nov 27, 2009 9:07:54 AM manu Exp $
 */
public class InsertCategoryMenu extends AbstractReferralPlugin implements WikiPlugin {

    private final static String PARAM_PAGE = "page";

    private final static String PARAM_CURRENTCATEGORY = "showCurrentCategory";
    
    private final static String[] DEFAULT_CATEGORIES = new String[] { "CategoryMain", "CategoryQuickStart", "CategoryConfiguration", "CategoryUsage",
            "CategoryDocumentation", "CategoryShowcase", "CategoryDownload" };

    public String execute(WikiContext context, Map params) throws PluginException {

        // fetch categories from jspwiki config
        String[] categories = null;
        String categoriesConf = (String) (String) context.getEngine().getWikiProperties().get("navigation.categories");
        if (categoriesConf == null) {
            categories = DEFAULT_CATEGORIES;
        } else {
            categories = categoriesConf.split(",");
        }

        ReferenceManager refmgr = context.getEngine().getReferenceManager();
        String pageName = (String) params.get(PARAM_PAGE);

        if (pageName == null) {
            pageName = context.getPage().getName();
        }

        WikiPage page = context.getEngine().getPage(pageName);
        if(page == null) {
            return "Page " + pageName + " does not exist yet so no menu for it can be computed.";
        }
        
        String toBeInserted = null;
        
        // if we are a category ourselves, just display the menu
        if(Arrays.asList(categories).contains(pageName)) {
            toBeInserted = page.getName();
        } else {
            
            // we lookup in which category this page is
            for (String cat : categories) {

                // all the pages that refer to the category
                Collection<String> links = refmgr.findReferrers(cat);
                if(links == null) {
                    continue;
                } else {
                    if (links.contains(pageName)) {
                        toBeInserted = cat;
                        break;
                    }
                }
            }
        }
        
        if(toBeInserted == null) {
            return "Page " + page.getName() + " is in none of the categories " + Arrays.toString(categories) + " hence its menu cannot be found.";
        }
        
        if(params.get(PARAM_CURRENTCATEGORY) != null && ((String)params.get(PARAM_CURRENTCATEGORY)).equals("true")) {
            return toBeInserted;
        } else {
            String wikiMarkup = "[{MenuTreePlugin menuPage='" + toBeInserted + "Menu" + "'}]";
            String html = context.getEngine().textToHTML(context, wikiMarkup);
            
            // FIXME make this either a param or a jspwiki.properties param
            if(toBeInserted.equals("CategoryDocumentation")) {
                // generate a file that can be used by the API docs
                // TODO make this configurable
                File f = new File(new File(".").getAbsolutePath() + "/doc-jspwiki/JSPWiki/api/leftMenu.html");
                if(!f.exists() || (f.exists() && (new Date(f.lastModified()).before(context.getEngine().getPage(toBeInserted + "Menu").getLastModified())))) {
                    try {
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                        FileWriter fw = new FileWriter(f);
                        fw.write(html);
                        fw.flush();
                        fw.close();
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
            
            return html;
        }

    }
}
