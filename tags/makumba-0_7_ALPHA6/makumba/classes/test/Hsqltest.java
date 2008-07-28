package test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Hsqltest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
	    try {
	        Class.forName("org.hsqldb.jdbcDriver" );
			//Class.forName("org.gjt.mm.mysql.Driver" );
	    } catch (Exception e) {
	        System.out.println("ERROR: failed to load HSQLDB JDBC driver.");
	        e.printStackTrace();
	        return;
	    }

		Connection c = DriverManager.getConnection("jdbc:hsqldb:hsqldata/makumba", "sa", "");
		//Connection c = DriverManager.getConnection("jdbc:mysql://localhost/makumba", "root", "");
		c.setAutoCommit(true);
		c.setTransactionIsolation(java.sql.Connection.TRANSACTION_REPEATABLE_READ);

		Statement st = c.createStatement();
		try {
		   
		   //st.executeUpdate("create table adler2(a int, b char(220))");
		   //st.executeUpdate("INSERT INTO adler2 values (1,2)");
		   //st.executeUpdate("CREATE TABLE org_makumba_db_Catalog_(Catalog_ INTEGER,TS_modify_ TIMESTAMP,TS_create_ TIMESTAMP,name_ VARCHAR(255))");
		   //System.out.println(st.executeUpdate("ALTER TABLE TEST_VALIDMDDS_DIVERSITY_ ALTER COLUMN this_ VARBINARY"));
		   //System.out.println(st.executeQuery ("SELECT * FROM org_makumba_db_Catalog_ c LIMIT 1000"));
			
			//ResultSet rs = c.getMetaData().getIndexInfo(null,null, "TEST_VALIDMDDS_CHARWITHLENGTH_", false, false);
			
			// --- TEST: inser a null value twice
			//st.executeUpdate("INSERT INTO test_validMdds_Diversity_ (newField_) VALUES ('null')");
/*
			st.executeUpdate("INSERT INTO test_validMdds_Diversity_" +
					" (Diversity_, ptrFNU_, ptrFN__, ptr_NU_, ptr_N__, PTRF_U_, PTR__U_) VALUES (3, 4, 2, 43, 3, 4, 3)");
*/
			
/*			ResultSet rs1 = st.executeQuery("SELECT * FROM test_validMdds_Diversity_");
			while (rs1.next()) {
				System.out.println("["+rs1.getString("cHar_")+
						"|"+rs1.getString("Diversity_")+"|"+
						rs1.getString("ptrFNU_")+"]\n");
			}
*/
			
/*			ResultSet rs = c.getMetaData().getIndexInfo(null,null, "test_validMdds_Diversity_".toUpperCase(),false, false);
			
			while (rs.next()) {
				String iname = rs.getString("INDEX_NAME");
				System.out.println("  ---> ["+iname+"] ");
				boolean non_unique = rs.getBoolean("NON_UNIQUE");

			}
*/

			ResultSet rs2;
            //SELECT avg(r.r_) as av, sum(r.r_) as su FROM test.validMdds.Real_ r");
			//rs2 = st.executeQuery("SELECT avg(r.r_) AS av, sum(r.r_) AS su FROM TEST_VALIDMDDS_REAL_ r");
			//st.executeUpdate("INSERT INTO TEST_VALIDMDDS_REAL_ (real_, r_) VALUES (3, 3)");
			//rs2 = st.executeQuery("SELECT * FROM TEST_VALIDMDDS_REAL_ r");

//--- test with REAL ---
//rs2 = st.executeQuery("SELECT avg(r.r_) as av, sum(r.r_) as su FROM TEST_VALIDMDDS_REAL_ r");
//--- test with INT ---
//rs2 = st.executeQuery("SELECT * FROM TEST_VALIDMDDS_INT_ i");

//for (int vari=0;vari < 6; vari++) {
//	st.executeUpdate("INSERT INTO TEST_VALIDMDDS_INT_ (int_, i_) VALUES ("+vari+", "+vari+")");
//}
rs2 = st.executeQuery("SELECT avg(0.0+i.i_) as av, sum(i.i_) as su FROM TEST_VALIDMDDS_INT_ i");

while (rs2.next()) {
	//String stuff = rs2.getString("i_");
	//System.out.println("  ---> ["+stuff+"]");
	
	String sum = rs2.getString("su");
	String avg = rs2.getString("av");
	System.out.println("  ---> sum["+sum+"] avg["+avg+"] ");
}

System.out.println(".end.");

//r=real	;The real number (Double precision)

		}
		finally {
			st.executeUpdate("SHUTDOWN"); //comment if you use mySQL, needed with Hsqldb
			c.close();
		}
	}

}
