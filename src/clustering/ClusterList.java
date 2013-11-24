package clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import obj.Term;
import obj.WeightedTerm;
import utils.StringUtils;

/**
 * Conversions and operations over a list of clusters
 * @author HZ
 *
 */
public class ClusterList {
	
	/**
	 * Converts a map to a set of clusters
	 * @param map
	 * @return a set of clusters
	 */
	public static HashSet<Cluster> mapToClusters(HashMap<String,HashSet<String>> map) {
		HashSet<Cluster> clusters = new HashSet<Cluster>();
		for(String key:map.keySet()) {
			HashSet<String> set = new HashSet<String>();
			set.add(key);
			clusters.add(new Cluster(set,map.get(key)));
		}
		return clusters;
	}
	
	/**
	 * Converts a map to a set of clusters with a default annotation
	 * @param map
	 * @param bAnno
	 * @return a set of clusters
	 */
	public static HashSet<Cluster> mapToClusters(HashMap<String,HashMap<String,Integer>> map, boolean bAnno) {
		HashSet<Cluster> clusters = new HashSet<Cluster>();
		for(String key:map.keySet()) {
			HashSet<String> set = new HashSet<String>();
			Integer anno = 0;
			for(String t:map.get(key).keySet()) {
				Integer cur_anno = map.get(key).get(t);
				if(cur_anno > anno )
					anno = cur_anno;
				set.add(key);
			}
			clusters.add(new Cluster(set,map.get(key).keySet(),bAnno,anno));
		}
		return clusters;
	}
	
	 /**
	  * Converts a map to a list of clusters with a default annotation
	  * @param map
	  * @param bAnno
	  * @return a list of clusters
	  */
	public static LinkedList<Cluster> mapToClustersList(HashMap<String,HashMap<String,Integer>> map, boolean bAnno) {
		LinkedList<Cluster> clusters = new LinkedList<Cluster>();
		for(String key:map.keySet()) {
			HashSet<String> set = new HashSet<String>();
			Integer anno = 0;
			for(String t:map.get(key).keySet()) {
				Integer cur_anno = map.get(key).get(t);
				if(cur_anno > anno )
					anno = cur_anno;
				set.add(key);
			}
			clusters.add(new Cluster(set,map.get(key).keySet(),bAnno,anno));
		}
		return clusters;
	}
	
	/**
	 * Converts weighted terms list to a list of clusters
	 * @param responsaScores - weighted terms list
	 * @return a list of clusters
	 */
	public static LinkedList<Cluster> wtListToClusters(List<WeightedTerm> responsaScores) {
		LinkedList<Cluster> clusters = new LinkedList<Cluster>();
		for(WeightedTerm wt:responsaScores) {
			clusters.add(new Cluster(StringUtils.convertStringToSet(wt.getLemma()),StringUtils.convertStringToSet(wt.getValue()),wt.weight()));
		}
		return clusters;
	}
	
	/**
	 * Converts a list of clusters to a list of weighted terms
	 * @param responsaClusters
	 * @return a list of weighted terms
	 */
	public static LinkedList<WeightedTerm> clustersToWt(List<Cluster> responsaClusters) {
		LinkedList<WeightedTerm> responsaScores = new LinkedList<WeightedTerm>();
		for(Cluster cls:responsaClusters) {
			responsaScores.add(new WeightedTerm(new Term(StringUtils.convertSetToString(cls.getTerms()),StringUtils.convertSetToString(cls.getLemmas())),cls.getScore()));
		}
		return responsaScores;
	}
	
	/**
	 * Assigns clusters' judgment by a list of positive terms 
	 * @param clusters
	 * @param posTerms
	 */
	public static void assignClustersJudgments(LinkedList<Cluster> clusters, HashSet<String> posTerms){
		for(Cluster cls:clusters){
			int posCount = 0, negCount =0;
			boolean bPos = false;
			for(String t:cls.getTerms())
				if(posTerms.contains(t)){
					bPos = true;
					posCount++;
				} else
					negCount++;
			cls.setbAnno(bPos);
			if(posCount != cls.getTerms().size() && negCount != cls.getTerms().size())
				cls.setIsConflict(true);
		}
	}
	
	/**
	 * Gets the number of clusters with conflicts from a cluster list
	 * @param clusters
	 * @return number of conflicts
	 */
	public static int getClustersConflictsNum(LinkedList<Cluster> clusters){
		int num = 0;
		for(Cluster cls:clusters){
			if(cls.isConflict()){
				num +=1;
				System.out.println("conflict: "+ cls);
			}
		}
		return num;
	}
	
	/**
	 * Initiates a list of clusters with the same judgment
	 * @param clusters
	 * @param anno
	 */
	public static void initClustersJudgments(LinkedList<Cluster> clusters, boolean anno){
		for(Cluster cls:clusters){
			cls.setbAnno(anno);
		}
	}
	
	/**
	 * Gets the number of missing clusters (clusters which appears in relevant clusters list, but not in the judged list
	 * @param judgedClusters
	 * @param relevantClusterJudges
	 * @return number of missing clusters 
	 */
	public static int getClustersMissingNum(LinkedList<Cluster> judgedClusters,HashSet<Cluster> relevantClusterJudges){
		int num = 0;
		for(Cluster cls:judgedClusters){
			if(cls.getbAnno()&& !relevantClusterJudges.contains(cls)){
				num +=1;
				System.out.println("missing recall point: "+ cls);
			}
		}
		return num;
	}
	
	/**
	 * Loads cluster list from a file
	 * @param clustersFile
	 * @return a list
	 * @throws IOException
	 */
	public static LinkedList<Cluster> loadClusterFromFile(File clustersFile) throws IOException{
		LinkedList<Cluster> clusters = new LinkedList<Cluster>();
		BufferedReader reader = new BufferedReader(new FileReader(clustersFile));
		String line = reader.readLine();
		while (line != null) {
			String [] tokens = line.split("\t");
			clusters.add(new Cluster(StringUtils.convertStringToSet(tokens[1]), StringUtils.convertStringToSet(tokens[0]), Double.parseDouble(tokens[2])));
			line = reader.readLine();
		}
		reader.close();
		return clusters;
	}
	
	/**
	 * Converts cluster list to a string list
	 * @param responsaClusters
	 * @return a list of terms
	 */
	public static LinkedList<String> convertClusters2StringList(List<Cluster> responsaClusters) {
		LinkedList<String> stringSet = new LinkedList<String>();
		for(Cluster cls:responsaClusters) {
			for (String term:cls.getTerms())
				stringSet.add(term);
		}
		return stringSet;
	}
	
}
