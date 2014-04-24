package annotation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import obj.Pair;

/**
 * Database operations for judgment and annotation
 * @author HZ
 *
 */
public class PrevSQLAccess {
	
	/**
	 * Initializes the connection to the database
	 * @param databaseName
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public PrevSQLAccess(String databaseName) throws ClassNotFoundException, SQLException{
		// This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.jdbc.Driver");
	      m_databaseName = databaseName;
	      // Setup the connection with the DB
//	      m_connection = DriverManager
//	          .getConnection("jdbc:mysql://localhost/"+m_databaseName+"?characterEncoding=UTF-8&"
//	              + "user=root&password=Miescuel2");
	      m_connection = DriverManager
          .getConnection("jdbc:mysql://localhost/"+m_databaseName+"?characterEncoding=UTF-8&"
              + "user=root&password=");

	}
	
	/**
	 * Gets the target term list from the database
	 * @return target term list
	 * @throws SQLException 
	 */
	public HashSet<String> getTargetTermList() throws SQLException{
		// Select
		HashSet<String> termsMap = new HashSet<String>();
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select query from annotations group by query;");
	      ResultSet rs = preparedStatement.executeQuery();
	      while (rs.next()){
	      	String term = rs.getString("query");
	      	termsMap.add(term);
	      }
	      rs.close();
	      return termsMap;  
	}
	
	/**
	 * Gets full groups list from the database
	 * Each group has all her n-gram presentations 
	 * @param target_term_id
	 * @return id-group map
	 * @throws SQLException
	 */
	public HashSet<Integer> getGroupsData(String target_term) throws SQLException
	{
		String sql = "SELECT * FROM annotations WHERE query = ? AND judgement > -1 AND generation=0 GROUP BY judgement;";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setString(1, target_term);
		ResultSet rs = preparedStatement.executeQuery();
		HashSet<Integer> groupMap = new HashSet<Integer>();
	    // ResultSet is initially before the first data set
	    while (rs.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
	      int group = rs.getInt("judgement");
	      groupMap.add(group);
	    }
		rs.close();
	    return groupMap;
	}
	
	
	 private Connection m_connection = null;
	 private String m_databaseName;
}
