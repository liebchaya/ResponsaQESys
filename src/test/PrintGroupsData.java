package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;


import utils.FileUtils;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import annotation.JudgmentsLoader;
import annotation.SQLAccess;

public class PrintGroupsData {
	public static void main(String[] args) throws Exception{
		
//		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
//		ConfigurationParams params = conf.getModuleConfiguration("Experiment");
		
//		String mainDir = params.get("main-dir");
//		String wiktionaryFile = params.get("wiktionary-file");
//		String databaseName = params.get("database-name");
//		
//		String oldIndex = params.get("old-index");
		
		
//		String wikiFolder = mainDir+"/wiktionary/";
//		String outputFolder ="./groups/";
	
		
//		File outputDir = new File(outputFolder);
//		if (outputDir.exists())
//			FileUtils.deleteRecursive(outputDir);
//		outputDir.mkdir();
	
//		JudgmentsLoader jLoader = new JudgmentsLoader(databaseName,wikiFolder+wiktionaryFile,oldIndex);
//		jLoader.printAllGroupsData(outputDir.getAbsolutePath());
		
		String db = "iterativefull";
		String outputFile = "C:\\prevThes.txt";
		SQLAccess sql = new SQLAccess(db);
//		SQLAccess sql = new SQLAccess(args[0]);
//		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "CP1255"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "CP1255"));
		writer.write("id\tname\tgroup_id\tperiod\tgeneration\tdesc\n");
		HashMap<Integer, String> terms = sql.getTargetTermList();
		for(int id:terms.keySet()){
			String termData = sql.getGroupsDataLine(id);
			if(!termData.trim().isEmpty())
				writer.write(termData);
		}
		writer.close();
	}

}
