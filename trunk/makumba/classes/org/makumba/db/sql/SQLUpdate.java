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
      throw new org.makumba.OQLParseError(e.getMessage()+"\r\nin "+debugString+"\n"+OQLQuery);
    }
    
    
    String fakeCommand;
    try{ fakeCommand= ((QueryAST)tree).writeInSQLQuery(dbc.getHostDatabase()); }
    catch(RuntimeException e){ throw new MakumbaError(debugString+"\n"+OQLQuery); }

    // we remove the label and label. seqences from the command
    StringBuffer replaceLabel=new StringBuffer();
    int n=0, lastN;
    while(true){
      lastN=n;
      n=fakeCommand.indexOf(label, n);
      if(n==-1)
	{
	  replaceLabel.append(fakeCommand.substring(lastN));
	  break;
	}
      replaceLabel.append(fakeCommand.substring(lastN, n));
      n+=label.length();
      if(fakeCommand.charAt(n)=='.')
	n++;
    }

    fakeCommand=replaceLabel.toString();

    // now we break the query SQL in pieces to form the update SQL
    StringBuffer command= new StringBuffer();
    command.append(set==null?"DELETE FROM":"UPDATE");
    command.append(fakeCommand.substring(fakeCommand.indexOf("FROM")+4, fakeCommand.indexOf("WHERE")));
    if(set!=null)
      {
	String setString=fakeCommand.substring(fakeCommand.indexOf("SELECT")+6, fakeCommand.indexOf("FROM"));
	n=0;
	while(true)
	  {
	    n=setString.indexOf("is null", n);
	    if(n==-1)
	      break;
	    setString=setString.substring(0, n)+" = null"+setString.substring(n+7);
	  }
	command.append("SET ").append(setString);
      }
    if(where!=null)
      command.append(fakeCommand.substring(fakeCommand.indexOf("WHERE")));
    
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
