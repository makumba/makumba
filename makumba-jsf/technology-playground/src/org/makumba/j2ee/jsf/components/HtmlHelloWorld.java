package org.makumba.j2ee.jsf.components;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

/**
 * Hello world for a simple component that can display stuff passed in
 * "message". Additionally subscribes to an event (simple test to see if it can
 * be used as pre-render hook, as an entry point to list analysis)
 * 
 * @author manu
 */
@FacesComponent(value = "HtmlHelloWorld")
public class HtmlHelloWorld extends UIComponentBase implements SystemEventListener {

    @Override
    public String getFamily() {
        return null;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", this);
        writer.writeAttribute("style", "color:red", null);
        String message = (String) this.getAttributes().get("message");
        if (message == null)
            message = "nihilism";
        writer.writeText("Hello, world! Today we are believing in " + message, null);
        writer.endElement("div");

        writer.startElement("ul", this);
        for (UIComponent child : getChildren()) {
            writer.startElement("li", this);
            writer.writeText("Child with id " + child.getId() + " of class " + child.getClass(), null);
            writer.endElement("li");
        }
        writer.endElement("ul");
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        super.encodeChildren(context);
    }

    public HtmlHelloWorld() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // subscribe to the view event "PostAddToViewEvent" (see JSF reference).
        // this event should be fired before tree rendering time
        ctx.getViewRoot().subscribeToViewEvent(PostAddToViewEvent.class, this);
    }

    public void processEvent(SystemEvent event) throws AbortProcessingException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        // UIComponent dynamicallyGenerated =
        // ctx.getApplication().createComponent( "javax.faces.HtmlInputText" );
        // getChildren().add( dynamicallyGenerated );
        System.out.println("HtmlHelloWorld.processEvent(): event " + event.getClass() + " happened, source is "
                + event.getSource());

    }

    public boolean isListenerForSource(Object source) {
        return source instanceof HtmlHelloWorld;
    }

}
