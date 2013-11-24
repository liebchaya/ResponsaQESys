package test;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;

import ac.biu.nlp.nlp.general.configuration.ConfigurationException;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import period.NgramsDataGeneration;
import utils.TargetTerm2Id;

public class TestDataGeneration {

	/**
	 * @param args
	 * @throws ConfigurationException 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ConfigurationException, IOException, ParseException {
		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
		ConfigurationParams params = conf.getModuleConfiguration("DataGeneration");
		String morphTargetTermFile = params.get("morph-target-terms-file");
		String ngramsIndex = params.get("ngrams-index");
		String oldIndex = params.get("old-index");
		String modernIndex = params.get("modern-index");
		String outputDir = params.get("clusters-dir");
		String oldNgramsFileName = params.get("old-ngrams-file");
		String modernJewishNgramsFileName = params.get("modernJewish-ngrams-file");
		// index contains up tp 4-grams
		int maxN = 4;
		
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(morphTargetTermFile));
		NgramsDataGeneration ngramsGenerator = new NgramsDataGeneration(oldIndex,ngramsIndex,modernIndex);
//		ngramsGenerator.generateDataFiles(outputDir, morphTargetTermFile, oldNgramsFileName, modernJewishNgramsFileName, maxN);

	}

}
