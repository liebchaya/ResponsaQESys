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
public class SQLAccess {
	
	/**
	 * Initializes the connection to the database
	 * @param databaseName
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public SQLAccess(String databaseName) throws ClassNotFoundException, SQLException{
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
	 * Inserts new target terms to the database
	 * @param origTermsList
	 * @return  a hashMap with the target terms ids from the database
	 * @throws SQLException
	 * @throws IOException
	 */
	public HashMap<Integer,String> insertAndExtractInputFile(List<String> origTermsList) throws SQLException{
		HashMap<Integer,String> termMap = new HashMap<Integer, String>();
		// get the first new id that will be inserted
		int prevId = getLastTargetTermId();
		
		// insert the target terms to the database
		StringBuffer mySql = new StringBuffer("insert into target_terms (target_term) values (?)");
		for (int i=0; i<origTermsList.size()-1; i++) {
		    mySql.append(", (?)");
		}
		int i=1;
		PreparedStatement preparedStatement = m_connection.prepareStatement(mySql.toString());
		for (String term:origTermsList) {
			preparedStatement.setString(i++, term);
		}
		preparedStatement.executeUpdate();
		 
		// get the assigned target terms ids from the database
		preparedStatement = m_connection
	      .prepareStatement("select * from target_terms where id > ? ; ");
	      preparedStatement.setInt(1, prevId);
	      ResultSet rs = preparedStatement.executeQuery();
	      while(rs.next()){
	    	  termMap.put(rs.getInt(1),rs.getString(2));
	      }
	    return termMap;
	}
	
	/**
	 * Inserts a new annotation to the database
	 * @param result
	 * @param lemma
	 * @param target_term_id
	 * @param generation
	 * @param position
	 * @param judgement
	 * @param period
	 * @param result_group
	 * @param expasion_id
	 * @param isNew
	 * @throws SQLException
	 */
	public void insertAnnotation(String result,String lemma, int target_term_id, int generation, int position, int judgement, int period, int result_group, int expasion_id, Boolean isNew, int isManual) throws SQLException{
	    PreparedStatement preparedStatement = m_connection
        .prepareStatement("insert into annotations(id,result,lemma,target_term_id,generation,position,judgement,period,result_group,expansion_id,new_anno,is_manual,created_at) values (null, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?, null)");
		
	      // Parameters start with 1
	      preparedStatement.setString(1, result);
	      preparedStatement.setString(2, lemma);
	      preparedStatement.setInt(3, target_term_id);
	      preparedStatement.setInt(4, generation);
	      preparedStatement.setInt(5, position);
	      preparedStatement.setInt(6, judgement);
	      preparedStatement.setInt(7, period);
	      preparedStatement.setInt(8, result_group);
	      preparedStatement.setInt(9, expasion_id);
	      preparedStatement.setBoolean(10, isNew);
	      preparedStatement.setInt(11, isManual);
	      System.out.println(preparedStatement.toString());
	      preparedStatement.executeUpdate();
	}
	
	/**
	 * Inserts an expansion to the database
	 * @param expansion
	 * @param target_term
	 * @param parent_exp_id
	 * @param generation
	 * @param period
	 */
	public void insertExpansion(String expansion, String target_term, int parent_exp_id, int generation, int period, int isManual) throws SQLException{
	      // PreparedStatements can use variables and are more efficient
	      PreparedStatement preparedStatement = m_connection
	          .prepareStatement("insert into expansions(id, expansion, target_term_id, parent_exp_id, generation, period, is_manual, created_at) values (null, ?, ?, ?, ?, ?, ?, null)");
	      int target_term_id = Integer.parseInt(target_term);//getTargetTermId(target_term);
	      // Parameters start with 1
	      preparedStatement.setString(1, expansion);
	      preparedStatement.setInt(2, target_term_id);
	      preparedStatement.setInt(3, parent_exp_id);
	      preparedStatement.setInt(4, generation);
	      preparedStatement.setInt(5, period);
	      preparedStatement.setInt(6, isManual);
	      preparedStatement.executeUpdate();
	}
	
//	/**
//	 * Gets previously annotated lemmas for a target term
//	 * @param target_term_id
//	 * @return a lemma set
//	 * @throws SQLException
//	 */
//	public HashSet<String> getAnnotatedLemmas(int target_term_id) throws SQLException{
//		// Select all annotated candidates for the target term
//		HashSet<String> lemmaSet = new HashSet<String>();
//	      PreparedStatement preparedStatement = m_connection
//	      .prepareStatement("select lemma from annotations where target_term_id= ? ; ");
//	      preparedStatement.setInt(1, target_term_id);
//	      ResultSet rs = preparedStatement.executeQuery();
//	      while (rs.next()) 
//	    	  lemmaSet.add(rs.getString("lemma"));
//	      rs.close();
//	      return lemmaSet;
//	}
	
	/**
	 * Gets full groups list from the database
	 * Each group has one presenting string, the longest character sequence 
	 * @param target_term_id
	 * @return id-group map
	 * @throws SQLException
	 */
	@Deprecated
	public HashMap<Integer, String> getGroupsDataMax(int target_term_id) throws SQLException
	{
		String sql = "select MAX(mlen), result, result_group, period, target_term_id from ( " +
			"select result, result_group, period, target_term_id, LENGTH(result) as mlen " +
			"from annotations order by mlen desc) as tbl " +
			"where result_group > -1 and target_term_id = ? group by result_group;";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		HashMap<Integer, String> groupsMap = saveGroupsData(rs);
		rs.close();
	    return groupsMap;
	}
	
	/**
	 * Gets full groups list from the database
	 * Each group has all her n-gram presentations 
	 * @param target_term_id
	 * @return id-group map
	 * @throws SQLException
	 */
	public HashMap<Integer, String> getGroupsData(int target_term_id) throws SQLException
	{
		String sql = "select result, result_group, MAX(period) as m_period, target_term_id, REPLACE(GROUP_CONCAT(result SEPARATOR ' '),'] [',',')" + 
					 " as mlen from annotations where result_group > -1 and target_term_id = ?" + 
                     " group by result_group;";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		HashMap<Integer, String> groupsMap = saveGroupsData(rs);
		rs.close();
	    return groupsMap;
	}
	
	/**
	 * Gets the data for thesaurus representation in result, parent format
	 * @param target_term_id
	 * @return list of pairs 
	 * @throws SQLException
	 */
	@Deprecated
	public List<Pair<String, String>> getThesaurusDataMax(int target_term_id) throws SQLException{
		String sql = "SELECT MAX( mlen ) , result as newRes, result_group, target_term_id, COALESCE( expansion, target_term ) as te "+
					 "FROM (SELECT annotations.result, annotations.result_group, annotations.target_term_id, annotations.expansion_id, expansions.expansion, expansions.id, target_terms.target_term, target_terms.id AS tid, LENGTH( annotations.result ) AS mlen " +
					 "FROM annotations LEFT JOIN expansions ON annotations.expansion_id = expansions.id "+
					 "INNER JOIN target_terms ON annotations.target_term_id = target_terms.id "+
					 "ORDER BY mlen DESC) AS tbl WHERE result_group > -1 AND target_term_id =? GROUP BY result_group";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		List<Pair<String, String>> thesData = saveThesaurusData(rs);
		rs.close();
	    return thesData;

	}
	
	/**
	 * Gets full lemmas list from the database 
	 * @param target_term_id
	 * @return lemma-group_id map
	 * @throws SQLException
	 */
	public HashMap<String, Integer> getLemmas(int target_term_id) throws SQLException
	{
		HashMap<String,Integer> lemmaMap = new HashMap<String, Integer>();
		String sql = "select result_group, lemma from annotations where target_term_id = ?";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next())
			lemmaMap.put(rs.getString("lemma"),rs.getInt("result_group"));
		rs.close();
	    return lemmaMap;
	}
	
	/**
	 * Gets full results list from the database 
	 * @param target_term_id
	 * @return HashMap<Integer, String>
	 * @throws SQLException
	 */
	public HashMap<String, Integer> getResults(int target_term_id) throws SQLException
	{
		HashMap<String,Integer> resultsMap = new HashMap<String, Integer>();
		String sql = "select result_group, result from annotations where target_term_id = ?";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next())
			resultsMap.put(rs.getString("result"),rs.getInt("result_group"));
		rs.close();
	    return resultsMap;
	}
	
	/**
	 * Gets the data for thesaurus representation in result, parent format
	 * @param target_term_id
	 * @return list of pairs
	 * @throws SQLException
	 */
	public List<Pair<String, String>> GetThesaurusData(int target_term_id) throws SQLException{
		String sql = "SELECT REPLACE(GROUP_CONCAT(result SEPARATOR ' '),'] [',',')  as newRes, result, result_group, target_term_id, COALESCE( expansion, target_term ) as te "+ 
					 "FROM (SELECT annotations.result, annotations.result_group, annotations.target_term_id, annotations.expansion_id, expansions.expansion, expansions.id, target_terms.target_term, target_terms.id AS tid "+
					 "FROM annotations LEFT JOIN expansions ON annotations.expansion_id = expansions.id " + 
					 "INNER JOIN target_terms ON annotations.target_term_id = target_terms.id) AS tbl "+
					 "WHERE result_group > -1 AND target_term_id =? GROUP BY result_group;";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		List<Pair<String, String>> thesData = saveThesaurusData(rs);
		rs.close();
	    return thesData;

	}
	
	/**
	 * Gets expansions list starting from a certain id
	 * @param startFrom_id
	 * @return map of  id-expansion pairs
	 * @throws SQLException
	 */
	public HashMap<Integer,Pair<Integer, String>> getExpansions(int startFrom_id) throws SQLException{
		// Select
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select * from expansions where id >= ? order by id; ");
	      preparedStatement.setInt(1, startFrom_id);
	      ResultSet rs = preparedStatement.executeQuery();
	      HashMap<Integer,Pair<Integer, String>> pairMap = saveExpansions(rs);
	      rs.close();
	      return pairMap;
	}
	
	/**
	 * Updates the group's period to be max period (converts modern to ancient if necessary)
	 * @param target_term_id
	 * @param result_group
	 * @throws SQLException
	 */
	public void updateGroupAnnotations(int target_term_id, int result_group) throws SQLException{
		 PreparedStatement preparedStatement = m_connection
	      .prepareStatement("UPDATE annotations SET period = (SELECT selected_value FROM (SELECT MAX(period) AS selected_value FROM annotations WHERE result_group = ? and target_term_id = ?) AS sub_selected_value) WHERE target_term_id = ? and result_group = ?");
	      preparedStatement.setInt(1, result_group);
	      preparedStatement.setInt(2, target_term_id);
	      preparedStatement.setInt(3, target_term_id);
	      preparedStatement.setInt(4, result_group);
	      preparedStatement.executeUpdate();
	}
	
	
	/**
	 * Saves the groups data
	 * @param resultSet
	 * @return group_id-result_period map
	 * @throws SQLException
	 */
	private HashMap<Integer, String> saveGroupsData(ResultSet resultSet) throws SQLException {
		HashMap<Integer, String> groupMap = new HashMap<Integer, String>();
	    // ResultSet is initially before the first data set
	    while (resultSet.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
	      String result = resultSet.getString("mlen");
	      int group = resultSet.getInt("result_group");
	      int period = resultSet.getInt("m_period");
	      if (!groupMap.containsKey(group))
	    	  groupMap.put(group, result+"\t"+period);
	      else
	    	  System.out.println("duplicated groups - ERROR");
	    }
	    return groupMap;
	  }
	
	/**
	 * Saves the theasurus presentation data
	 * @param resultSet
	 * @return list of pairs
	 * @throws SQLException
	 */
	private List<Pair<String, String>> saveThesaurusData(ResultSet resultSet) throws SQLException {
		List<Pair<String, String>> thesList = new LinkedList<Pair<String, String>>();
	    // ResultSet is initially before the first data set
	    while (resultSet.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
	      String result = resultSet.getString("newRes");
	      String te = resultSet.getString("te");
	      thesList.add(new Pair<String, String>(result,te));
	    }
	    return thesList;
	  }
	
	/**
	 * Saves the expansions data
	 * @param resultSet
	 * @return map of id-expansion pairs
	 * @throws SQLException
	 */
	private HashMap<Integer,Pair<Integer,String>> saveExpansions(ResultSet resultSet) throws SQLException {
		HashMap<Integer,Pair<Integer,String>> pairMap = new HashMap<Integer,Pair<Integer,String>>();
	    // ResultSet is initially before the first data set
	    while (resultSet.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
	      String result = resultSet.getString("expansion");
	      int target_term_id = resultSet.getInt("target_term_id");
	      int id = resultSet.getInt("id");
	      pairMap.put(id,new Pair<Integer, String>(target_term_id,result));
	    }
	    return pairMap;
	  }
	
	
	/**
	 * Gets the current generation based on the previous generations
	 * @param target_term_id
	 * @return last generation that was active
	 * @throws SQLException 
	 */
	public int getGeneration(int target_term_id) throws SQLException{
		// Select
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select MAX(generation) from expansions where target_term_id= ? ; ");
	      preparedStatement.setInt(1, target_term_id);
	      ResultSet rs = preparedStatement.executeQuery();
	      int generation = -1;
	      if (rs.next())
	      	generation = rs.getInt(1);
	      rs.close();
	      return generation;  
	}
	
	/**
	 * Gets the target term id from the database
	 * @param target_term
	 * @return target term id
	 * @throws SQLException 
	 */
	public int getTargetTermId(String target_term) throws SQLException{
		// Select
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select id from target_terms where target_term= ? ; ");
	      preparedStatement.setString(1, target_term);
	      ResultSet rs = preparedStatement.executeQuery();
	      int id = 0;
	      if (rs.next())
	      	id = rs.getInt(1);
	      rs.close();
	      return id;  
	}
	
	/**
	 * Gets the target term list from the database
	 * @return target term list
	 * @throws SQLException 
	 */
	public HashMap<Integer,String> getTargetTermList() throws SQLException{
		// Select
		HashMap<Integer,String> termsMap = new HashMap<Integer, String>();
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select * from target_terms ; ");
	      ResultSet rs = preparedStatement.executeQuery();
	      while (rs.next()){
	      	int id = rs.getInt("id");
	      	String term = rs.getString("target_term");
	      	termsMap.put(id, term);
	      }
	      rs.close();
	      return termsMap;  
	}
	
	/**
	 * Gets last target term id
	 * @return last term id available in the database
	 * @throws SQLException
	 */
	private int getLastTargetTermId() throws SQLException{
		// Select
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select MAX(id) from target_terms");
	      ResultSet rs = preparedStatement.executeQuery();
	      int id = 0;
	      if (rs.next())
	      	id = rs.getInt(1);
	      rs.close();
	      return id;
	      
	}
	
	/**
	 * Gets last expansion id
	 * @return last expansion id available in the database
	 * @throws SQLException
	 */
	public int getLastExpansionId() throws SQLException{
		// Select
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select MAX(id) from expansions");
	      ResultSet rs = preparedStatement.executeQuery();
	      int id = 0;
	      if (rs.next())
	      	id = rs.getInt(1);
	      rs.close();
	      return id;
	      
	}
	
	/**
	 * Gets groups list for iteration > 0
	 * @param target_term_id
	 * @return groups set
	 * @throws SQLException
	 */
	public HashSet<Integer> getIterGroups4TargetTerm(int target_term_id) throws SQLException
	{
		HashSet<Integer> groupsMap = new HashSet<Integer>();
		String sql = "select result_group from annotations where generation > 0 and result_group > -1 and target_term_id = ?" + 
                     " group by result_group;";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
		     int result = rs.getInt("result_group");
		     groupsMap.add(result);
		}
		rs.close();
	    return groupsMap;
	}
	
	/**
	 * Gets groups data from the database for analysis
	 * Each group has all her n-gram presentations 
	 * @param target_term_id
	 * @return data line for printing
	 * @throws SQLException
	 */
	public String getGroupsDataLine(int target_term_id) throws SQLException
	{
		String sql = "select target_terms.target_term, annotations.result, annotations.result_group, MAX(annotations.period) as m_period, MIN(annotations.generation) as m_generation, annotations.target_term_id, REPLACE(GROUP_CONCAT(annotations.result SEPARATOR ' '),'] [',',')" + 
					 " as mlen from annotations inner join target_terms on annotations.target_term_id = target_terms.id where result_group > -1 and target_term_id = ?" + 
                     " group by result_group;";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		String dataLine = "";
		while(rs.next()){
			int id = rs.getInt("target_term_id");
			String name = rs.getString("target_term");
			int group_id = rs.getInt("result_group");
			int period = rs.getInt("m_period");
			int generation = rs.getInt("m_generation");
			String desc = rs.getString("mlen");
			dataLine += id + "\t" + name + "\t" + group_id + "\t" + period + "\t"  + generation + "\t" + desc + "\n";
		}
		rs.close();
		return dataLine;
	}
	
	/**
	 * Gets annotations' sum, above step 0, for evaluation
	 * @param target_term_id
	 * @return
	 * @throws SQLException
	 */
	public int getIterStepAnnoCount(int target_term_id) throws SQLException
	{
		String sql = "select count(*) as count from annotations where generation>0 and new_anno=1 and target_term_id =?;";
		PreparedStatement preparedStatement = m_connection.prepareStatement(sql);
		preparedStatement.setInt(1, target_term_id);
		ResultSet rs = preparedStatement.executeQuery();
		int count = 0;
		while(rs.next()){
			count = rs.getInt("count");
		}
		rs.close();
		return count;
	}
	 

	 private Connection m_connection = null;
	 private String m_databaseName;
}
