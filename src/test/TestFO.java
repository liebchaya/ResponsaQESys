package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;

import representation.FeatureRepresentation;
import representation.NgramsFeatureRepresentation;
import representation.NgramsTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.FeatureRepresentation.FeatureType;
import representation.TargetTermRepresentation.TargetTermType;
import utils.TargetTerm2Id;
import ac.biu.nlp.nlp.general.configuration.ConfigurationException;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationFileDuplicateKeyException;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;
import fo.scorers.StatScorer;
import fo.similarity.FeatureVectorExtractor;

public class TestFO {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ConfigurationException 
	 * @throws ConfigurationFileDuplicateKeyException 
	 * @throws ParseException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws IOException, ConfigurationFileDuplicateKeyException, ConfigurationException, ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		ConfigurationFile conf = new ConfigurationFile(new File(args[0]));
		ConfigurationParams params = conf.getModuleConfiguration("Experiment");
		String targetTermFile = params.get("target-terms-file");
		String ngramsIndex = params.get("ngrams-index");
		String outputDir = params.get("output-dir");
		
		TargetTerm2Id.loadTargetTerm2IdMapping(new File(targetTermFile));
		// Expand target terms with morphology prefixes
		String expTargetTermFile = morphology.Morphology4TargetTermExp.generateMorphExpFile(targetTermFile, ngramsIndex);

		// FO n-grams extraction - from ngrams index
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new NgramsTargetTermRepresentation(targetType, expTargetTermFile, ngramsIndex);
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
		String scorerClass =params.get("stat-scorer");
		Class<?> cls = Class.forName(scorerClass);
		StatScorer scorer  = (StatScorer) cls.newInstance();
		
		FeatureType featureType = FeatureType.valueOf(params.get("feature-type"));
		FeatureRepresentation featureRp = new NgramsFeatureRepresentation(featureType, ngramsIndex);
		FeatureVectorExtractor vectorExtractor = new FeatureVectorExtractor(scorer,featureRp);
		vectorExtractor.extractTargetTermVectors(targetDocs, targetType, new File(outputDir));
	}

}
