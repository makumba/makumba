package org.makumba.view.jsptaglib;
import java.util.*;
import org.makumba.*;

/** a bufffer where the info that is assigned to the tags is stored. reduces the memory allocations */

public class RootQueryBuffer
{
  // for query tags
  String[] bufferQueryProps= new String[4];
  String bufferSeparator="";

  String bufferCountVar=null;
  String bufferMaxCountVar=null;

  // for value tags
  String bufferExpr;
  String bufferVar=null;
  String bufferPrintVar=null;

  FieldDefinition bufferSet=null;

  Hashtable bufferParams= new Hashtable();
}
