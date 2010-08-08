package org.makumba.jsf;

import java.util.Iterator;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseListener;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

import org.makumba.el.MakumbaCreateELResolver;
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
            app.addELResolver(new MakumbaCreateELResolver());
            app.addELResolver(new MakumbaELResolver());

            app.addComponent("makumbaList", "org.makumba.jsf.UIRepeatListComponent");
            app.addComponent("makumbaObject", "org.makumba.jsf.UIRepeatListComponent");
            app.addComponent("makumbaCreateObject", "org.makumba.jsf.CreateObjectComponent");
            app.addComponent("makumbaValue", "org.makumba.jsf.ValueComponent");

            LifecycleFactory f = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            for (Iterator<String> i = f.getLifecycleIds(); i.hasNext();) {
                Lifecycle l = f.getLifecycle(i.next());

                PhaseListener listListener = new LogicPhaseListener();
                l.removePhaseListener(listListener);
                l.addPhaseListener(listListener);

                PhaseListener valueSavingListener = new ValueSavingPhaseListener();
                l.removePhaseListener(valueSavingListener);
                l.addPhaseListener(valueSavingListener);
            }
        }

    }

}
