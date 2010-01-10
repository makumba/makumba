package org.makumba.providers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Map;

import org.makumba.providers.Configuration.DataSourceType;

/**
 * Represents a Makumba DataSource
 */
class ConfiguredDataSource {

    private DataSourceType type;

    private String name;

    private String host;

    private String path;
    
    private String hostAddress;
    
    public String getHostAddress() {
        return hostAddress;
    }

    private Map<String, String> properties = new Hashtable<String, String>();

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public DataSourceType getType() {
        return type;
    }

    public void setType(DataSourceType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ConfiguredDataSource(String host, String name, String path, DataSourceType type) {
        super();
        this.host = host;
        try {
            this.hostAddress = InetAddress.getByName(host).toString();
        } catch (UnknownHostException e) {
            this.hostAddress = null;
        }
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public String toString() {
        return "dataSource:" + name + (host == null ? "": " host:" + hostAddress) + (path == null ? "" : " path:" + path);
    }

}