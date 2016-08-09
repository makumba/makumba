package org.makumba.j2ee.jsf.components;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.FacesComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * Hello world for a custom input. This component generates an input field, a
 * submit button, and an output field.
 * 
 * @author manu
 */
@FacesComponent(value = "HtmlHelloInput")
public class HtmlHelloInput extends UIInput implements NamingContainer {
	
    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        String clientId = getClientId(context);
        System.out.println("HtmlHelloWorld.encodeEnd() clientId: " + clientId);
        char sep = UINamingContainer.getSeparatorChar(context);
        encodeInputField(context, clientId + sep + "inputField");
        encodeSubmitButton(context, clientId + sep + "submit");
        encodeOutputField(context);
    }

    private void encodeInputField(FacesContext context, String clientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("input", this);
        writer.writeAttribute("type", "text", null);
        writer.writeAttribute("name", clientId, "clientId");
        Object value = getValue();
        if (value != null) {
            writer.writeAttribute("value", value.toString(), "value");
        }
        writer.writeAttribute("size", "6", null);
        writer.endElement("input");

    }

    private void encodeSubmitButton(FacesContext context, String clientId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("input", this);
        writer.writeAttribute("type", "submit", null);
        writer.writeAttribute("name", clientId, "clientId");
        writer.writeAttribute("value", "Click me!", null);
        writer.endElement("input");
    }

    private void encodeOutputField(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String hellomsg = (String) getAttributes().get("value");
        if(hellomsg == null) {
            hellomsg = "nothing at all";
        }
        writer.startElement("p", this);
        writer.writeText("You entered: " + hellomsg, null);
        writer.endElement("p");

    }

    @Override
    public void decode(FacesContext context) {
        Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
        String clientId = getClientId(context);
        char sep = UINamingContainer.getSeparatorChar(context);
        String submitted_hello_msg = ((String) requestMap.get(clientId + sep + "inputField"));
        setSubmittedValue(submitted_hello_msg);
    }

}
