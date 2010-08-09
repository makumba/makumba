package org.makumba.jsf;

import java.util.Iterator;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

import org.makumba.el.MakumbaCreateELResolver;
import org.makumba.el.MakumbaELResolver;

/**
 * Application listener that registers a number of makumba-specific object programmatically so that we can define
 * dependencies between them
 * 
 * @author manu
 */
// @ListenerFor(systemEventClass = PostConstructApplicationEvent.class) -- this would work in a managed environment
public class ApplicationListener implements SystemEventListener {

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof Application;
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        if (event.getClass().equals(PostConstructApplicationEvent.class)) {

            ValueSavingPhaseListener valueSavingListener = new ValueSavingPhaseListener();
            LogicPhaseListener logicPhaseListener = new LogicPhaseListener();

            // replace the application with ours, which makes it possible to customize the way objects are created
            // JSF does not easily let you add custom ways of building the JSF object graph otherwise
            Application app = (Application) event.getSource();
            ApplicationFactory factory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            factory.setApplication(new MakumbaApplication(app, valueSavingListener));

            // register phase listeners
            LifecycleFactory f = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            for (Iterator<String> i = f.getLifecycleIds(); i.hasNext();) {
                Lifecycle l = f.getLifecycle(i.next());
                l.removePhaseListener(valueSavingListener);
                l.addPhaseListener(valueSavingListener);

                l.removePhaseListener(logicPhaseListener);
                l.addPhaseListener(valueSavingListener);
            }

            // conversion
            app.addConverter(org.makumba.Pointer.class, "org.makumba.jsf.PointerConverter");

            // validation
            app.addDefaultValidatorId("org.makumba.jsf.MakumbaValidator");
            app.addValidator("org.makumba.jsf.MakumbaValidator", "org.makumba.jsf.MakumbaValidator");

            // EL resolution - the order is important here, don't change it
            app.addELResolver(new MakumbaELResolver(valueSavingListener));
            app.addELResolver(new MakumbaCreateELResolver(valueSavingListener));

            // components
            app.addComponent("makumbaList", "org.makumba.jsf.component.UIRepeatListComponent");
            app.addComponent("makumbaObject", "org.makumba.jsf.component.UIRepeatListComponent");
            app.addComponent("makumbaCreateObject", "org.makumba.jsf.component.CreateObjectComponent");
            app.addComponent("makumbaValue", "org.makumba.jsf.component.ValueComponent");
        }

    }

}
