package lexicalResources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import obj.Pair;

/**
 * Extract related terms from Hebrew wiktionary
 * @author HZ
 *
 */
public class Wiktionary {
	
	/**
	 * Loads the data file
	 * @param wikiFile
	 * @throws IOException
	 */
	public Wiktionary(File wikiFile) throws IOException{
		m_wikiRel = new HashMap<String, HashMap<String,Integer>>();
		m_wikiRevRel = new HashMap<String, HashMap<String,Integer>>();
		BufferedReader reader = new BufferedReader(new FileReader(wikiFile));
		String line = reader.readLine();
		while (line != null){
			String [] tokens = line.split("\t");
			if (!m_wikiRel.containsKey(tokens[0])){
				HashMap<String,Integer> relsMap = new HashMap<String,Integer>();
				relsMap.put(tokens[1], Integer.parseInt(tokens[2]));
				m_wikiRel.put(tokens[0], relsMap);
			}
			else
				m_wikiRel.get(tokens[0]).put(tokens[1], Integer.parseInt(tokens[2]));
			
			if (!m_wikiRevRel.containsKey(tokens[1])){
				HashMap<String,Integer> relsMap = new HashMap<String,Integer>();
				relsMap.put(tokens[0], Integer.parseInt(tokens[2]));
				m_wikiRevRel.put(tokens[1], relsMap);
			}
			else
				m_wikiRevRel.get(tokens[1]).put(tokens[0], Integer.parseInt(tokens[2]));
			line = reader.readLine();
		}
	}
	
	/**
	 * Gets related term for a certain term
	 * @param targetTerm
	 * @param bIncludeRev - search in the opposite direction
	 * @param maxLevel 1=synonyms, 2=links (see also), 3=category
	 * @return list of expansion-relation pairs
	 */
	public LinkedList<Pair<String, Integer>> getRel(String targetTerm, boolean bIncludeRev, int maxLevel){
		LinkedList<Pair<String,Integer>> relList = new LinkedList<Pair<String,Integer>>();
		if(m_wikiRel.containsKey(targetTerm)){
			HashMap<String, Integer> rels = m_wikiRel.get(targetTerm);
			for(String rel:rels.keySet())
				if(rels.get(rel)<=maxLevel)
					relList.add(new Pair<String,Integer>(rel,rels.get(rel)));
		}
		if (bIncludeRev) {
			if(m_wikiRevRel.containsKey(targetTerm)){
				HashMap<String, Integer> rels = m_wikiRevRel.get(targetTerm);
				for(String rel:rels.keySet())
					if(rels.get(rel)<=maxLevel)
						relList.add(new Pair<String,Integer>(rel,rels.get(rel)));
			}
		}
		return relList;
	}
	
	/**
	 * Gets related term for a certain term
	 * @param targetTerm
	 * @param bIncludeRev - search in the opposite direction
	 * @param maxLevel 1=synonyms, 2=links (see also), 3=category
	 * @return set of terms (as String)
	 */
	public HashSet<String> getRelSet(String targetTerm, boolean bIncludeRev, int maxLevel){
		HashSet<String> relSet = new HashSet<String>();
		if(m_wikiRel.containsKey(targetTerm)){
			HashMap<String, Integer> rels = m_wikiRel.get(targetTerm);
			for(String rel:rels.keySet())
				if(rels.get(rel)<=maxLevel)
					relSet.add(rel);
		}
		if (bIncludeRev) {
			if(m_wikiRevRel.containsKey(targetTerm)){
				HashMap<String, Integer> rels = m_wikiRevRel.get(targetTerm);
				for(String rel:rels.keySet())
					if(rels.get(rel)<=maxLevel)
						relSet.add(rel);
			}
		}
		return relSet;
	}

		
	
	HashMap<String,HashMap<String,Integer>> m_wikiRel = null;
	HashMap<String,HashMap<String,Integer>> m_wikiRevRel = null;
}
