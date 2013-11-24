package mwe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import utils.StringUtils;

import clustering.JungClustering;

import com.aliasi.util.Distance;

/**
 * Ngrams grouping
 * @author HZ
 *
 */
public class MWEGrouping {
	
	/**
	 * Loads statistics files with ngrams data
	 * @param fileName
	 * @param MWEthreshold
	 * @param filterModern
	 * @return set of ngrams
	 * @throws IOException
	 */
	private HashSet<String> loadDataFile(String fileName, double MWEthreshold, boolean filterModern) throws IOException{
		m_idMap = new HashMap<String, Integer>();
		m_dataMap = new HashMap<String, String>();
		HashSet<String> keys = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		// skip the headline
		reader.readLine();
		String line = reader.readLine();
		int lineNum = 1;
		while(line != null){
			if (Double.parseDouble(line.split("\t")[7]) < MWEthreshold){
				line = reader.readLine();
				continue;
			}
			if (filterModern && Integer.parseInt(line.split("\t")[6]) > 0){
				line = reader.readLine();
				continue;
			}
			m_idMap.put(line.split("\t")[0], lineNum);
			m_dataMap.put(line.split("\t")[0], line.substring(line.indexOf("\t")+1));
			keys.add(line.split("\t")[0]);
			lineNum++;
			line = reader.readLine();
		}
		reader.close();
		return keys;
	}
	
	/**
	 * Groups ngrams from the input file
	 * @param inputFile
	 * @param threshold
	 * @param filterModern
	 * @throws IOException
	 */
	public void groupMWEfile(String inputFile, double threshold, boolean filterModern ) throws IOException{
		HashSet<String> strings2group = loadDataFile(inputFile, threshold, filterModern);
		Set<Set<String>> groups = groupMWE(strings2group);
		printMWEgroups(inputFile.replace(".dataClusters.txt", ".dataGroups.txt"), groups);
	}
	
	/**
	 * Clusters the ngrams
	 * @param strings2group
	 * @return set of ngrams set
	 */
	private Set<Set<String>> groupMWE(Set<String> strings2group){
		Distance<CharSequence> dist = new MWEDistance();
		JungClustering jung = new JungClustering();
		jung.buildGraph(strings2group, dist);
		return jung.cluster();
	}
	
	/**
	 * Prints the results file after the grouping, first position of the group is the ngram group position
	 * @param groupsFileName results file name
	 * @param groups
	 * @throws IOException
	 */
	private void printMWEgroups(String groupsFileName, Set<Set<String>> groups) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(groupsFileName));
		HashMap<Integer,String> groupsMapData = new HashMap<Integer, String>();
		for (Set<String> group:groups){
			int minGroupId = Integer.MAX_VALUE;
			String selectedStr = null;
			HashSet<String> groupStrings = new HashSet<String>();
			HashSet<String> lemmaStrings = new HashSet<String>();
			int oldPeriodCount = 0;
			for(String str:group){
				groupStrings.addAll(StringUtils.convertStringToSet(str));
				lemmaStrings.addAll(StringUtils.convertStringToSet(m_dataMap.get(str).split("\t")[0]));
				oldPeriodCount += Integer.parseInt(m_dataMap.get(str).split("\t")[2]);
				if (m_idMap.get(str) < minGroupId){
					minGroupId = m_idMap.get(str);
					selectedStr = str;
				}
			}
			String tokens[] = m_dataMap.get(selectedStr).split("\t");
			groupsMapData.put(minGroupId, StringUtils.convertSetToString(groupStrings) + "\t"+ StringUtils.convertSetToString(lemmaStrings) + "\t" + tokens[1]  + "\t" + oldPeriodCount +  "\t" + tokens[3] + "\t" + tokens[4] + "\t" + tokens[5]+ "\t" + tokens[6] + "\t" + tokens[7]);
		}
		writer.write("Candidate\tLemma\tScore\tOld Period Count\tAdded Union\tAdded Intersection\tModern Intersection\tMWE\tExp Id\n");
		SortedSet<Integer> keys = new TreeSet<Integer>(groupsMapData.keySet());
		for (Integer key : keys) { 
		   String value = groupsMapData.get(key);
		   writer.write(value + "\n");
		}
		writer.close();
			
	}
	
	private HashMap<String, Integer> m_idMap = null;
	private HashMap<String, String> m_dataMap = null;
}
