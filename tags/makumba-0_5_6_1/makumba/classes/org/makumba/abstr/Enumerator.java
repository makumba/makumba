package org.makumba.abstr;
import java.util.Enumeration;

interface Enumerator
{
    Enumeration getValues();
    Enumeration getNames();
    int getEnumeratorSize();
    String getStringAt(int i);
    String getNameAt(int i);
    //Needed for intEnumHandler...others ??
    String getNameFor(int i);
}
