package org.makumba.util;
import javax.servlet.ServletContextEvent;

public class NamedResourcesContextListener implements javax.servlet.ServletContextListener
{
  public NamedResourcesContextListener()
  {
    // this also ensures that the loadTime of MakumbaSystem is set
    org.makumba.MakumbaSystem.getLogger("system").info("loading context listener");
  }
  
  public void contextInitialized(ServletContextEvent sce)
  {}
  
  public void contextDestroyed(ServletContextEvent sce)
  {
    org.makumba.MakumbaSystem.getLogger("system").info("destroying makumba caches");
    NamedResources.cleanup();
  }
}
