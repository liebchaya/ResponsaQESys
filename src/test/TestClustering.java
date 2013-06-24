package test;

import java.io.File;


import utils.TargetTerm2Id;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import clustering.ClusteringGeneration;

public class TestClustering {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String fileType = "_Dice.txt";
		ConfigurationFile conf = new ConfigurationFile(args[0]);
		ConfigurationParams clusterParam = conf.getModuleConfiguration("Clustering");
		String taggerDir = clusterParam.get("tagger-dir");
		int topNum = clusterParam.getInt("top-num");
		String termsDirName = clusterParam.get("terms-dir");
		String morphTargetTermsFile = clusterParam.get("morph-target-terms-file");
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(morphTargetTermsFile));
		
		ClusteringGeneration clsGen = new ClusteringGeneration(taggerDir,termsDirName,topNum);
		clsGen.clusterDir(fileType, topNum);
		
		
		
	

	}

}
