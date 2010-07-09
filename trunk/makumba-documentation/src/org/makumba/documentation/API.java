package org.makumba.documentation;

import java.util.Map;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;

public class API implements WikiPlugin {
    
    public String execute(WikiContext context, Map parameters) throws PluginException {
        
        String clazz = (String) parameters.get("class");
        
        if(clazz == null) {
            throw new PluginException("Must provide a value for the 'class' parameter!");
        }
        
        String url = clazz.replace(".", "/") + ".html";
        
        String apiPath = (String) context.getEngine().getWikiProperties().get("api." + getApiType() + ".path");
        if(apiPath == null) {
            throw new PluginException("API type " + getApiType() + " not configured in jspwiki.properties!");
        }
        
        if(apiPath.startsWith("http")) {
             url = apiPath + url;
        } else {
            url = context.getEngine().getBaseURL() + apiPath + url;
            
        }
        
        return "<a href=\"" + url + "\">" + clazz + "</a>";
    }
    
    protected String getApiType() {
        return "simple";
    }

}
