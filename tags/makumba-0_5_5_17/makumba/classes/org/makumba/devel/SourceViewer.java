package org.makumba.devel;
import org.makumba.abstr.*;
import java.io.*;

/** 
 * shows a file JSP or MDD file that refers to MDDs, and shows the 
 * references to other MDDs and Java files as links
 * the file is already known and open when parseText is called
 */
public interface SourceViewer
{
  /** parse the text and write the output */
  void parseText(PrintWriter w) throws IOException;

  /** if this resource is actually a directory, return it, otherwise return null */
  public File getDirectory();
}
