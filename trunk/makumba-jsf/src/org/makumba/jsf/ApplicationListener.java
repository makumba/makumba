package org.makumba.jsf;

import javax.faces.application.Application;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

import org.makumba.el.MakumbaELResolver;

/**
 * Application listener that registers the {@link MakumbaELResolver} on application startup
 * 
 * @author manu
 */
// @ListenerFor(systemEventClass = PostConstructApplicationEvent.class) -- would work with CDI
public class ApplicationListener implements SystemEventListener {

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof Application;
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        if (event.getClass().equals(PostConstructApplicationEvent.class)) {
            Application app = (Application) event.getSource();
            app.addELResolver(new MakumbaELResolver());
        }

    }

}
