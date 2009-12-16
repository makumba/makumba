package org.makumba.db.sql;
import org.makumba.*;

import java.util.*;
import java.sql.*;
import org.makumba.util.*;

// wrong dependencies
import org.makumba.db.DBConnection;
import org.makumba.db.sql.oql.QueryAST;
import org.makumba.abstr.*;

/** SQL implementation of a OQL query */
public class Query implements org.makumba.db.Query
{
  DBConnection dbc;
  OQLAnalyzer tree;
  //  PreparedStatement ps;

  String query;
  RecordManager resultHandler;
  String command;
  ParameterAssigner assigner;

  public String getCommand(){ return command; }


  public Query(DBConnection dbc, String OQLQuery){ this(dbc, MakumbaSystem.getOQLAnalyzer(OQLQuery)); }
  public Query(DBConnection dbc, OQLAnalyzer t) 
  {
    tree=t;
    if(dbc instanceof org.makumba.db.DBConnectionWrapper)
	dbc=((org.makumba.db.DBConnectionWrapper)dbc).getWrapped();
    command= ((QueryAST)tree).writeInSQLQuery(dbc.getHostDatabase());
    this.dbc=dbc;

    resultHandler= (RecordManager)dbc.getHostDatabase().getTable((RecordInfo)getResultType());
    assigner= new ParameterAssigner(dbc, tree);
    //ps=((SQLDBConnection)dbc).getPreparedStatement(command);
  }

  public DataDefinition getResultType() { return tree.getProjectionType(); }

  /** Get the data type of the given label */
  public DataDefinition getLabelType(String label){ return tree.getLabelType(label); }


  public Vector execute(Object [] args)
  {
    PreparedStatement ps=((SQLDBConnection)dbc).getPreparedStatement(command);
    try{
      String s=assigner.assignParameters(ps, args);
      if(s!=null)
	throw new InvalidValueException("Errors while trying to assign arguments to query:\n"+command+"\n"+s);

      org.makumba.db.sql.Database db=(org.makumba.db.sql.Database)dbc.getHostDatabase();

      MakumbaSystem.getMakumbaLogger("db.query.execution").fine(""+ps);
      java.util.Date d= new java.util.Date();
      ResultSet rs= null; 
      try{
	rs= ps.executeQuery();
      }catch(SQLException se)
	{ 
	  org.makumba.db.sql.Database.logException(se, dbc);
	  throw new DBError(se, command);
      }
      long diff = new java.util.Date().getTime()-d.getTime();
      MakumbaSystem.getMakumbaLogger("db.query.performance").fine(""+ diff +" ms "+command);
      return goThru(rs, resultHandler);
    }
    catch(SQLException e){ throw new org.makumba.DBError(e); }  
  }

  Vector goThru(ResultSet rs, RecordManager rm) 
  {
    int size= rm.keyIndex.size();

    Vector ret=new Vector(100, 100);
    Object []dt;
    try{ 
      while(rs.next())
	{
	  rm.fillResult(rs, dt=new Object[size]);
	  ret.addElement(new org.makumba.util.ArrayMap(rm.keyIndex, dt));
	}
      rs.close();
    }catch(SQLException e){ throw new org.makumba.DBError(e, rm.getRecordInfo().getName()); }
    return ret;
  }
   


}
