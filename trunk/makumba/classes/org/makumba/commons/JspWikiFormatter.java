package org.makumba.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.makumba.commons.formatters.FieldFormatter;


import com.ecyrd.jspwiki.FileUtil;
import com.ecyrd.jspwiki.TestEngine;
import com.ecyrd.jspwiki.TranslatorReader;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiException;
import com.ecyrd.jspwiki.WikiPage;

/**
 * Wiki formatter from JSP wiki
 * 
 * @link http://www.jspwiki.org/
 * @author Rudolf Mayer
 * @version $Id$
 */
public class JspWikiFormatter implements WikiFormatter {

    protected static TestEngine testEngine;

    protected static Properties props = new Properties();

    protected static WikiContext context;

    private static final class SingletonHolder {
        
        static {
            props.put("jspwiki.workDir", ".");
            props.put("jspwiki.pageProvider", "com.ecyrd.jspwiki.providers.FileSystemProvider");
            props.put("jspwiki.fileSystemProvider.pageDir", "@tests.pagedir@");

            try {
                testEngine = new TestEngine(props);
            } catch (WikiException e) {
                e.printStackTrace();
            }
            context = new WikiContext(testEngine, new WikiPage("TestPage"));
            singleton = new JspWikiFormatter();
        }
        
        static JspWikiFormatter singleton;
    }
    
    /**
     * Returns the singleton instance of this formatter, or creates it if it did not exist yet.
     * 
     * @return
     */
    public static JspWikiFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    /**
     * @see org.makumba.commons.WikiFormatter#wiki2html(java.lang.String)
     */
    public String wiki2html(String s) {
        if (context == null) {
            getInstance();
        }
        TranslatorReader r = new TranslatorReader(context, new BufferedReader(new StringReader(s)));
        try {
            String result = FileUtil.readContents(r);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return s;
        }
    }

}
