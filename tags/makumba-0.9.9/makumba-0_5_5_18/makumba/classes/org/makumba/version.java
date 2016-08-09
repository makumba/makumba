package org.makumba;

class version {
    public static void main(String[] args) {
        String version=("$Name$".substring(7,"$Name$".length()-2));
        if(version.indexOf('-')>0) version=version.substring(version.indexOf('-')+1);
        if(version.length()>2) version=version.replace('_','.');
        else version="development";

        System.out.println("name=Makumba"); 
        System.out.println("version="+version); 
        System.out.println("date="+new java.util.Date()); 
        try{
          System.out.println("buildhost="+(java.net.InetAddress.getLocalHost()).getHostName()+" ("+(java.net.InetAddress.getLocalHost()).getHostAddress()+")"); 
        } catch (Exception e) {
          System.out.println("buildhost=unknown.host"); 
        }
    }
}
