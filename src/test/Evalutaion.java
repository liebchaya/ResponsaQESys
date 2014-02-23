package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import annotation.SQLAccess;

/**
 * Construct data for Recall evaluation
 * @author HZ
 *
 */
public class Evalutaion {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
	
		HashMap<Integer, HashSet<Integer>> iter = getPositiveIterGroups();
		HashMap<Integer, HashSet<Integer>> baseline = getPositiveGroups(10,42);
		HashMap<Integer, HashSet<Integer>> step0 = getPositiveGroups(8,15);
		System.out.println(iter.get(9));
		// union of iterative results (step0 + iter)
		Set<Integer> keys = iter.keySet();
		for(int key:keys){
			if(step0.containsKey(key))
				iter.get(key).addAll(step0.get(key));
		}
		System.out.println(iter.get(9));
		// now iter contains all the iterative process
		// union recall from iter and baseline
		HashMap<Integer, HashSet<Integer>> all = new HashMap<Integer, HashSet<Integer>>();
		for(int key:iter.keySet()){
			HashSet<Integer> groups = new HashSet<Integer>(); 
			groups.addAll(iter.get(key));
			if(baseline.containsKey(key))
				groups.addAll(baseline.get(key));
			all.put(key, groups);
		}
		System.out.println(iter.get(9));
		System.out.println("target_term_id\tbaseline\titer\tall\tstep0");
		for(int key:keys){
			int countAll = 0, countBaseline = 0, countIter = 0 , countStep0 = 0;
			countAll = all.get(key).size();
			countIter = iter.get(key).size();
			if(baseline.containsKey(key))
				countBaseline = baseline.get(key).size();
			if(step0.containsKey(key))
				countStep0 = step0.get(key).size();
			System.out.println(key + "\t" + countBaseline + "\t" + countIter + "\t" + countAll + "\t" + countStep0);
			
		}

	}

	public static void getStep0Groups() throws IOException{
		HashMap<Integer, HashSet<Integer>> baseline = getPositiveGroups(10,40);
		HashMap<Integer, HashSet<Integer>> step0 = getPositiveGroups(8,15);
		for(int key:baseline.keySet()){
			int step0Count = 0;
			if(step0.containsKey(key))
				step0Count = step0.get(key).size();
			System.out.println(key + "\t" + baseline.get(key).size() + "\t" + step0Count);
		}
		
	}
	
	private static HashMap<Integer,HashSet<Integer>> getPositiveGroups(int ancientK, int modernK) throws IOException {
		HashMap<Integer,HashSet<Integer>> groupsMap = new HashMap<Integer, HashSet<Integer>>();
//		String annotationDir = "C:\\Documents and Settings\\HZ\\Desktop\\QEsys_baseline\\QEsys\\annotated";
		String annotationDir = "/home/ir/liebesc/QEsys/annotated";
		File annDir = new File(annotationDir);
		for(File f:annDir.listFiles()){
			int countAncient = 0, countModern = 0;
			boolean bCountAncient = false, bCountModern = false;
			if(f.getName().endsWith(".dataGroups")){
				int target_term_id = Integer.parseInt(f.getName().substring(0,f.getName().indexOf(".")));
				if(target_term_id==2)
					System.out.println("2");
				BufferedReader reader = new BufferedReader(new FileReader(f));
				reader.readLine(); // skip the first line
				String line = reader.readLine();
				while (line != null){
					String [] tokens = line.split("\t");
					// ancient term
					if (Integer.parseInt(tokens[2])>0){
						// negative term
						if(Integer.parseInt(tokens[3])==0){
							countAncient ++;
							if (countAncient>=ancientK){
								line = reader.readLine();
								continue;
							}
						} else if (Integer.parseInt(tokens[3])>0 && countAncient<ancientK){
							if (groupsMap.containsKey(target_term_id))
									groupsMap.get(target_term_id).add(Integer.parseInt(tokens[4]));
							else {
								HashSet<Integer> groups = new HashSet<Integer>();
								groups.add(Integer.parseInt(tokens[4]));
								groupsMap.put(target_term_id,groups);
							}
							countAncient = 0;
						}
					}
					else if (Integer.parseInt(tokens[2])==0){ 
						// negative term
						if(Integer.parseInt(tokens[3])==0){
							countModern ++;
							if (countModern>=modernK){
								line = reader.readLine();
								continue;
							}
						} else if (Integer.parseInt(tokens[3])>0 && countModern<modernK){
							if (groupsMap.containsKey(target_term_id))
								groupsMap.get(target_term_id).add(Integer.parseInt(tokens[4]));
							else {
								HashSet<Integer> groups = new HashSet<Integer>();
								groups.add(Integer.parseInt(tokens[4]));
								groupsMap.put(target_term_id,groups);
							}
							countModern = 0;
						}
					}
					line = reader.readLine();
				}
			}
		}
		return groupsMap;
	}
	
	private static HashMap<Integer, HashSet<Integer>> getPositiveIterGroups() throws ClassNotFoundException, SQLException{
		HashMap<Integer, HashSet<Integer>> groupsMap = new HashMap<Integer, HashSet<Integer>>();
		SQLAccess sql = new SQLAccess("judgmentssys");
		for(int target_term_id:sql.getTargetTermList().keySet())
			groupsMap.put(target_term_id, sql.getIterGroups4TargetTerm(target_term_id));
		return groupsMap;
	}

}
