package org.makumba;

/** Error thrown if a configuration file is not found or cannot be accessed */
public class ConfigFileError extends MakumbaError
{
  public ConfigFileError(Throwable reason, String filename) 
  { super(reason, "Configuration file not found: "+filename); }

  public ConfigFileError(String filename) 
  { super("Configuration file not found: "+filename); }
}
