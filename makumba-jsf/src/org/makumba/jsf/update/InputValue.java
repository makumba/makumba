package org.makumba.jsf.update;

public class InputValue {

    private Object value;

    private String clientId;

    private boolean isPlaceholder;

    public InputValue(Object value, String clientId) {
        super();
        this.value = value;
        this.clientId = clientId;
    }

    public InputValue(ObjectInputValue v) {
        this.value = v;
        isPlaceholder = true;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public void setPlaceholder(boolean isPlaceholder) {
        this.isPlaceholder = isPlaceholder;
    }

}
