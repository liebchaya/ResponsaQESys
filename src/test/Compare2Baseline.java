package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import utils.FileUtils;

import ac.biu.nlp.nlp.general.configuration.ConfigurationException;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import annotation.JudgmentsLoader;
import annotation.SQLAccess;

public class Compare2Baseline {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, ConfigurationException {
				
			String databaseName = "judgmentssys";
			String baselineDatabaseName = "baselinesys";
			
			
			String outputFolder ="./compare2baseline/";
		
			File outputDir = new File(outputFolder);
			if (outputDir.exists())
				FileUtils.deleteRecursive(outputDir);
			outputDir.mkdir();

			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder+"combareGroups.txt"));
			SQLAccess sql = new SQLAccess(databaseName);
			SQLAccess sqlBL = new SQLAccess(baselineDatabaseName);
			
			writer.write("target_term\tbaseline\titer\tunion\tintersection\n");
			
			// get term list
			HashMap<Integer, String> terms = sql.getTargetTermList();
			for(int target_term_id:terms.keySet()){
				System.out.println("Target term: " + terms.get(target_term_id) );
				HashMap<Integer, String> iter = sql.getGroupsData(target_term_id);
				HashMap<Integer, String> baseline = sqlBL.getGroupsData(target_term_id);
				System.out.println("Target term: " + terms.get(target_term_id) + "iter: " + iter.size() + "baseline " + baseline.size()  );
				
				Set<Integer> intersection = new HashSet<Integer>(iter.keySet()); // use the copy constructor
				intersection.retainAll(baseline.keySet());
				
				Set<Integer> union = new HashSet<Integer>(iter.keySet()); // use the copy constructor
				union.addAll(baseline.keySet());
				
				writer.write(terms.get(target_term_id) + "\t" + baseline.size() + "\t" + iter.size() + "\t" + union.size() + "\t" + intersection.size() + "\n");	
			}
			
			writer.close();
			
		}

		


	}


