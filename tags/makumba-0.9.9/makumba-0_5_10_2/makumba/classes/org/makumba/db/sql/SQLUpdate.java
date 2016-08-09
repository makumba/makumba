///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.db.sql;
import org.makumba.*;
import java.util.*;
import java.sql.*;

// wrong dependence
import org.makumba.db.*;
import org.makumba.db.sql.oql.QueryAST;

public class SQLUpdate implements Update
{
  // PreparedStatement ps;
  ParameterAssigner assigner;
  String debugString;
  DBConnection dbc;
  String updateCommand;

  SQLUpdate(SQLDBConnection dbc, String from, String set, String where)
  {
    debugString= (set==null?"delete":"update")+" on type: <"+from+">"+
      (set==null?" ":" setting: <"+set+">")+" where: <"+where+">";
    this.dbc=dbc;

    if(set!=null && set.trim().length()==0)
      throw new org.makumba.OQLParseError("Invalid empty update 'set' section in "+debugString); 

    if(where!=null && where.trim().length()==0)
      where=null;

    //a primitive check, better one needs to be done after OQLAnalyzer's job
    if(from!=null && from.indexOf(',')>=0)
      throw new org.makumba.OQLParseError("Only 1 table can be involved in "+debugString); 

    //make sure whitespace only consists of spaces
    from=from.replace('\t',' ');
    

    // we determine the dummy label used in the arguments 
    String label;
    try{
      label=from.substring(from.trim().indexOf(' ')+1).trim();
    }catch(StringIndexOutOfBoundsException e) 
      { throw new org.makumba.OQLParseError("Invalid delete/update 'type' section: "+from); }
    
    // to get the right SQL, we compil an imaginary OQL command made as follows:
    String OQLQuery="SELECT "+(set==null?label:set)+" FROM "+from;
    if(where!=null)
      OQLQuery+=" WHERE "+where;

    OQLAnalyzer tree;
    try{
      // FIXME: we should make sure here that the tree contains one single type!
      assigner= new ParameterAssigner(dbc, tree=MakumbaSystem.getOQLAnalyzer(OQLQuery));
    }catch(OQLParseError e){
      throw new org.makumba.OQLParseError(e.getMessage()+"\r\nin "+debugString+"\n"+OQLQuery, e);
    }
    
    
    String fakeCommand;
    try{ fakeCommand= ((QueryAST)tree).writeInSQLQuery(dbc.getHostDatabase()); }
    catch(RuntimeException e){ throw new MakumbaError(debugString+"\n"+OQLQuery); }

    StringBuffer replaceLabel=new StringBuffer();

    // we remove all "label." sequences from the SELECT part of the command
    int n=0;
    int lastN;
    int maxN=fakeCommand.indexOf(" FROM ");
    while(true){
      lastN=n;
      n=fakeCommand.indexOf(label+".", lastN);
      if(n==-1 || n>maxN)
	{
	  replaceLabel.append(fakeCommand.substring(lastN,maxN));
	  break;
	}
      replaceLabel.append(fakeCommand.substring(lastN, n));
      n+=label.length()+1;
    }

    // we remove the last instance of " label" from the FROM part of command
    lastN=fakeCommand.indexOf(" WHERE ");
    if(lastN<0)  //no where part, search to end
      lastN=fakeCommand.length();
    n=fakeCommand.lastIndexOf(" "+label, lastN);
    replaceLabel.append(fakeCommand.substring(maxN, n));

    // we remove all "label." sequences from the WHERE part of the command
    n=lastN; //start where we left off above
    while(true){
      lastN=n;
      n=fakeCommand.indexOf(label+".", lastN);
      if(n==-1)
	{
	  replaceLabel.append(fakeCommand.substring(lastN));
	  break;
	}
      replaceLabel.append(fakeCommand.substring(lastN, n));
      n+=label.length()+1;
    }

    fakeCommand=replaceLabel.toString();

    // now we break the query SQL in pieces to form the update SQL
    StringBuffer command= new StringBuffer();
    command.append(set==null?"DELETE FROM":"UPDATE");
    command.append(fakeCommand.substring(fakeCommand.indexOf(" FROM ")+5, fakeCommand.indexOf(" WHERE ")));
    if(set!=null)
      {
	String setString=fakeCommand.substring(fakeCommand.indexOf("SELECT ")+7, fakeCommand.indexOf(" FROM "));
	n=0;
	while(true)
	  {
	    n=setString.indexOf("is null", n);
	    if(n==-1)
	      break;
	    setString=setString.substring(0, n)+" = null"+setString.substring(n+7);
	  }
	command.append(" SET ").append(setString);
      }
    if(where!=null)
      command.append(fakeCommand.substring(fakeCommand.indexOf(" WHERE ")));
    
    debugString+="\n generated SQL: "+command;
    updateCommand=command.toString();
    // finally we can prepare a statement
    //    ps=dbc.getPreparedStatement(command.toString());
  }

  public int execute(Object[] args) 
  { 
    PreparedStatement ps=((SQLDBConnection)dbc).getPreparedStatement(updateCommand);
    try{
      String s=assigner.assignParameters(ps, args);
      if(s!=null)
	throw new InvalidValueException("Errors while trying to assign arguments to update:\n"+debugString+"\n"+s);
      
      org.makumba.db.sql.Database db=(org.makumba.db.sql.Database)dbc.getHostDatabase();

      MakumbaSystem.getMakumbaLogger("db.update.execution").fine(""+ps);
      java.util.Date d= new java.util.Date();
      int rez;
      try{
	 rez=ps.executeUpdate();
      }catch(SQLException se)
	{ 
	  if(((org.makumba.db.sql.Database)dbc.getHostDatabase())
	     .isDuplicateException(se))
	    // FIXME: need to determine the field that produced the error
	    throw new org.makumba.NotUniqueError(se);
	  
	  org.makumba.db.sql.Database.logException(se);
	  throw new DBError(se, debugString);
	}
      long diff = new java.util.Date().getTime()-d.getTime();
      MakumbaSystem.getMakumbaLogger("db.update.performance").fine(""+diff +" ms "+debugString);
      return rez;
    }
    catch(SQLException e){ throw new org.makumba.DBError(e); }  
  }
}
