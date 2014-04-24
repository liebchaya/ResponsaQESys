package test;

import java.sql.SQLException;
import java.util.Set;

import annotation.SQLAccess;

public class Eval304Compare {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
//		int[] terms = {1,2,3,4,7,8,9,10,11,12,15,17,16,19,18,21,20,23,22,25,24,27,26,28,31,30,35,32,33,38};
		SQLAccess sql = new SQLAccess("judgmentssys");
		Set<Integer> terms = sql.getTargetTermList().keySet();
		for(int t:terms){
			int count = sql.getIterStepAnnoCount(t);
			if(count>0)
				System.out.println(t+"\t"+count);
		}
	}

}
