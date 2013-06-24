package clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import morphology.Tagger;

import obj.SortByWeightName;
import obj.Term;
import obj.WeightedTerm;
import utils.TargetTerm2Id;


import clustering.ClusterScorer.ScorerType;

import com.aliasi.spell.EditDistance;
import com.aliasi.util.Distance;

import ac.biu.nlp.nlp.general.configuration.ConfigurationFile;
import ac.biu.nlp.nlp.general.configuration.ConfigurationParams;

public class ClusteringGeneration {
	
	
	public ClusteringGeneration(String taggerDir, String termsDirName, int topNum) throws Exception{
		
		m_confName = "Surface_Surface";
		m_scoreType = "SUMSCORE";
		m_morphType = 2;
		m_termsDirName = termsDirName;
		
		// initialize the tagger for clustering
		Tagger.init(taggerDir);
		
		// Initialize fixed clustering parameters
		int termIndex = 0;
		int scoreIndex = 1;
		String distMeasure = "Morph";
		String clsQueryField = "TERM_VECTOR";
		Double scoreThreshold = 0.0;
		File indexDir = null;
		String termsDir = termsDirName + "/" + m_confName;
		
		InitClusteringGeneration(m_confName,termsDir,indexDir,clsQueryField,m_morphType, 
				termIndex,scoreIndex,topNum, scoreThreshold,distMeasure);
	}
	
	/**
	 * Init clustering fixed parameters
	 */
	private void InitClusteringGeneration(String confName, String termsDir, File indexDir, String queryField, int morphType,
			int termIndex, int scoreIndex, int topNum, double scoreThresholdString, String distMeasure) {
		m_morphPre = new MorphDistancePreprocessing(termsDir,indexDir,queryField,morphType,termIndex, scoreIndex, topNum, scoreThresholdString, false);
		if(distMeasure.equals("Morph"))
			m_distance = new MorphDistance(m_morphPre);
		else // if(distMeasure.equals("Edit"))
			m_distance = new EditDistance(false);
		m_jung = new JungClustering();
		
	}
	

	/**
	 * Load target term statistics for morphology pre-processing
	 * @param statFile
	 * @throws MorphDistancePrePException
	 */
	private void loadTargetTermData(File statFile) throws MorphDistancePrePException {
		 m_inputTerms = m_morphPre.loadDataFile(statFile.getName(), m_confName);
	}
	
	/**
	 * Print function (utility)
	 * @param writer
	 * @throws IOException
	 */
	public void printTargetTermData(BufferedWriter writer) throws IOException{
		if (m_inputTerms != null) {
			for (String term:m_inputTerms.keySet())
				writer.write(term + "\t" + m_inputTerms.get(term) + "\n");
		}
		else
			System.out.println("No data for current target term");
		
	}
	
	/**
	 * Generate cluster for the term that was loaded by {@link #loadTargetTermData(File) loadTargetTermData}
	 * @param scoreType
	 * @return
	 * @throws MorphDistancePrePException
	 */
	private LinkedList<Cluster> generateCluster() throws MorphDistancePrePException {
       
	    ClusterScorer clsScorer = new ClusterScorer(m_inputTerms,ScorerType.valueOf(m_scoreType));
	    List<WeightedTerm> responsaScores =  new LinkedList<WeightedTerm>();
	    
	    m_jung.buildGraph(m_inputTerms.keySet(),m_distance);
	    Set<Set<String>> responsePartition = m_jung.cluster();
	    
	    for(Set<String> cluster:responsePartition) {
	    	HashSet<String> lemmas = new HashSet<String>();
	    	for(String str:cluster)
	    		lemmas.addAll(m_morphPre.getLemmasList(str));
	    	WeightedTerm wt = new WeightedTerm(new Term(cluster.toString(),lemmas.toString()),clsScorer.getClusterScore(cluster));
	    	responsaScores.add(wt);
	    }
	    Collections.sort(responsaScores, new SortByWeightName());
	    LinkedList<Cluster> responsaClusters = ClusterList.wtListToClusters(responsaScores);
	    return responsaClusters;
	}
	
	/**
	 * Generate clusters for all the directory files
	 * @param fileType
	 * @param topNum
	 * @throws IOException
	 * @throws MorphDistancePrePException
	 */
	public void clusterDir(String fileType, int topNum) throws IOException, MorphDistancePrePException{
		String clustersDirName = m_termsDirName + "/" + m_confName +  "/clusters" + topNum + "_" + m_scoreType + "_" + m_morphType;
		//create clusters' dir
		File clustersDir = new File(clustersDirName);
		if (!clustersDir.exists())
			clustersDir.mkdir();
		File termsDir = new File(m_termsDirName + "/" + m_confName);
		// cluster each file
		for (File f:termsDir.listFiles()) {
			if (f.isFile() && f.getAbsolutePath().endsWith(fileType)) {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(clustersDir +"/" + f.getName().substring(0,f.getName().indexOf(".")) + ".clusters"), "UTF8"));
				loadTargetTermData(f);
				LinkedList<Cluster> clusters = generateCluster();
				for (Cluster cls:clusters)
					writer.write(cls.getTerms()+ "\t" + cls.getLemmas() + "\t" + cls.getScore() +"\n");
				writer.close();
			}
		}
	}
		
	
	
	private MorphDistancePreprocessing m_morphPre = null;
	private Distance<CharSequence> m_distance = null;
	private JungClustering m_jung = null;
	private Map<String,Double> m_inputTerms = null;
	private String m_confName;
	private String m_scoreType;
	private int m_morphType;
	private String m_termsDirName;
}
