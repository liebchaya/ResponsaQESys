package test;

import java.io.File;


import utils.FileUtils;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import annotation.JudgmentsLoader;

public class PrintGroupsData {
	public static void main(String[] args) throws Exception{
		
		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
		ConfigurationParams params = conf.getModuleConfiguration("Experiment");
		
		String mainDir = params.get("main-dir");
		String wiktionaryFile = params.get("wiktionary-file");
		String databaseName = params.get("database-name");
		
		String oldIndex = params.get("old-index");
		
		
		String wikiFolder = mainDir+"/wiktionary/";
		String outputFolder ="./groups/";
	
		
		File outputDir = new File(outputFolder);
		if (outputDir.exists())
			FileUtils.deleteRecursive(outputDir);
		outputDir.mkdir();
	
		JudgmentsLoader jLoader = new JudgmentsLoader(databaseName,wikiFolder+wiktionaryFile,oldIndex);
		jLoader.printAllGroupsData(outputDir.getAbsolutePath());
	}

}
