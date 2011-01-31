package test.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

import com.opower.util.powerpool.provider.GenericSqlConnectionProvider;
import com.opower.util.powerpool.provider.NewConnectionProvider;

public class HsqlDatabaseUtil {

	public final static String databaseUrl = "jdbc:hsqldb:mem:power-test";
	public final static String driver = "org.hsqldb.jdbc.JDBCDriver";
	public final static String user = "sa";
	public final static String password = "";
	
	public static NewConnectionProvider createHsqlProvider() throws ClassNotFoundException {
		
		return new  GenericSqlConnectionProvider(driver, databaseUrl, user, password);
	 	
	}
	
	public static void selectARow(Connection connection) throws SQLException  {
		
		Statement st = connection.createStatement();
		  try{ 
				
				st.execute("select top 1 *  FROM INFORMATION_SCHEMA.SYSTEM_TABLES");
		  } finally {
				st.close();
		  }
		

	}
		
	public static void dropAllTestSchemaObjects(Connection connection) throws SQLException  {
		
		Statement st = connection.createStatement();
		  try{ 
				
				st.execute("DROP SCHEMA PUBLIC CASCADE");
		  } finally {
				st.close();
		  }
		

		
	}
 
	public static void dropAllTestSchemaObjects()  {
		
		try {
			Connection connection  = createHsqlProvider().newConnection();
			try {
				Statement st = connection.createStatement();
				st.execute("DROP SCHEMA PUBLIC CASCADE");
	 		} finally {
	 			connection.close();
	 		}
		} catch (Exception e) { 
			
		}
		

		
	}
	 
}
