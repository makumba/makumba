package org.makumba.commons.documentation;

import java.util.Map;

import org.apache.commons.lang.WordUtils;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;

/**
 * Box plugin that emulates the behaviour of Forrest boxes.<br>
 * Styles for the different boxes can be configured in jspwiki.properties through the box.note/warning/fixme.styleName properties.
 * 
 * @author Manuel Gay
 * @version $Id: Box.java,v 1.1 Nov 20, 2009 5:32:51 PM manu Exp $
 */
public class Box implements WikiPlugin {
    
    private final static String PARAM_LABEL = "label";
    
    private final static String PARAM_TYPE = "type";
    
    private final static String PARAM_AUTHOR = "author";
    
    private static final String BODY = "_body";

    public enum BoxType {NOTE, WARNING, FIXME};
    
    private BoxType type;
    
    private String label;
    
    private String body;
    
    public String execute(WikiContext context, Map parameters) throws PluginException {
        
        label = (String) parameters.get(PARAM_LABEL);
        
        String t = (String) parameters.get(PARAM_TYPE);
        if(t == null) {
            type = BoxType.NOTE;
        } else {
            type = BoxType.valueOf(t.toUpperCase());
        }
        
        if(label == null) {
            label = WordUtils.capitalizeFully(type.name());
        }
        
        body = (String) parameters.get(BODY);
        if(body == null) {
            body = "";
        }
        
        String style = (String) context.getEngine().getWikiProperties().get("box." + type.name().toLowerCase() + ".styleName");
        
        switch(type) {
            case NOTE:
                if(style == null) {
                    style = "note";
                }
                break;
                
            case WARNING:
                if(style == null) {
                    style = "warning";
                }
                break;
                
            case FIXME:
                if(style == null) {
                    style = "fixme";
                }
                
                String author = (String) parameters.get(PARAM_AUTHOR);
                
                if(author == null) {
                    throw new PluginException("You can't have a FIXME box without specifying an author!");
                }
                
                label += " (" + author + ")";
        }
        
        
        StringBuilder b = new StringBuilder();
        b.append("<div class=\"" + style + "\"><div class=\"label\">");
        b.append(label);
        b.append("</div><div class=\"content\">");
        b.append(context.getEngine().textToHTML(context, body));
        b.append("</div></div>");
        
        return b.toString();
    }
    
}