package org.makumba.controller.http;

/** An exception allowed/left unreported by the controller */
public class AllowedException extends RuntimeException
{
    public AllowedException(String s){ super(s); }
}
