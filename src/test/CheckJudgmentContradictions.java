package test;

import java.io.File;


import utils.FileUtils;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import annotation.JudgmentsLoader;

public class CheckJudgmentContradictions {
	public static void main(String[] args) throws Exception{
		
		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
		ConfigurationParams params = conf.getModuleConfiguration("Experiment");
		
		String mainDir = params.get("main-dir");
		String annoDir = params.get("anno-dir");
		String wiktionaryFile = params.get("wiktionary-file");
		String databaseName = params.get("database-name");
		
		String oldIndex = params.get("old-index");
		
		
		String wikiFolder = mainDir+"/wiktionary/";
		
	
		JudgmentsLoader jLoader = new JudgmentsLoader(databaseName,wikiFolder+wiktionaryFile,oldIndex);
		File mainFolder = new File(annoDir);
		for(File f:mainFolder.listFiles()){
			if(f.getName().endsWith(".dataGroups"))
				jLoader.checkContradictions(f);
		}
	}

}
