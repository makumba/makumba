package org.makumba.commons.documentation;

import java.util.Map;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;

/**
 * A simple plugin that simply passes on the content unmodified, and thus enables pure HTML in JSPWiki.
 * 
 * @author rudi
 * @version $Id$
 */
public class HTML implements WikiPlugin {

    public String execute(WikiContext arg0, Map arg1) throws PluginException {
        // it would also be possible to use _body, that is the content of the element, without the first line.
        return String.valueOf(arg1.get("_cmdline"));
    }

}
