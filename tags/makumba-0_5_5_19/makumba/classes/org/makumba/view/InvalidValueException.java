package org.makumba.view;
import org.makumba.abstr.*;
import java.util.*;
import org.makumba.*;

public class InvalidValueException extends org.makumba.InvalidValueException
{
  public InvalidValueException(FieldFormatter f, String mes)
  {
    super("expression \'"+f.getExpr()+"\'", mes);
  }
  
}
