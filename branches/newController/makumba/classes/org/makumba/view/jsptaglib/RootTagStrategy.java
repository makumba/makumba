package org.makumba.view.jsptaglib;
import org.makumba.*;

public interface RootTagStrategy extends TagStrategy
{
  public void onInit(TagStrategy ts) throws LogicException;
}
