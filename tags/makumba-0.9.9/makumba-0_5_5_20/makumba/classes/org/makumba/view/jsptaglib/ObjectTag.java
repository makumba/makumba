package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;

public class ObjectTag extends QueryTag
{
  public TagStrategy makeNonRootStrategy(Object key)
  { return new ObjectStrategy(); }
}
