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
import annotation.PrevSQLAccess;
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
				
			String databaseName = "iterativefull";
			String baselineDatabaseName = "baseline";
			
			
			String outputFolder ="C:/Documents and Settings/HZ/Desktop/AnalJournalIterative/compareGroups/prevScheme/";
		
			File outputDir = new File(outputFolder);
//			if (outputDir.exists())
//				FileUtils.deleteRecursive(outputDir);
//			outputDir.mkdir();

			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder+"combareGroupsStep0.txt"));
			PrevSQLAccess sql = new PrevSQLAccess(databaseName);
			PrevSQLAccess sqlBL = new PrevSQLAccess(baselineDatabaseName);
			
			writer.write("target_term\tbaseline\titer\tunion\tintersection\n");
			
			// get term list
//			HashMap<Integer, String> terms = sql.getTargetTermList();
			HashSet<String> terms = sqlBL.getTargetTermList();
			for(String target_term:terms){
				System.out.println("Target term: " + target_term );
				HashSet<Integer> iter = sql.getGroupsData(target_term);
//				HashMap<Integer, String> iter = sql.getGroupsData(target_term_id);
				HashSet<Integer> baseline = sqlBL.getGroupsData(target_term);
//				HashMap<Integer, String> baseline = sqlBL.getGroupsData(target_term_id);
				System.out.println("Target term: " + target_term + "iter: " + iter.size() + "baseline " + baseline.size()  );
				
				Set<Integer> intersection = new HashSet<Integer>(iter); // use the copy constructor
				intersection.retainAll(baseline);
				
				Set<Integer> union = new HashSet<Integer>(iter); // use the copy constructor
				union.addAll(baseline);
				
				writer.write(target_term + "\t" + baseline.size() + "\t" + iter.size() + "\t" + union.size() + "\t" + intersection.size() + "\n");	
			}
			
			writer.close();
			
		}

		


	}


