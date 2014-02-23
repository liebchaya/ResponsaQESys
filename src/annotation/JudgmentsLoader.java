package annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import obj.Pair;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;



import period.NgramData;

import lexicalResources.Wiktionary;
import mwe.MWEDistance;


import search.QueryGenerator;
import utils.FileUtils;
import utils.StringUtils;
import utils.TargetTerm2Id;

/**
 * Judgment and annotation treatment
 * @author HZ
 *
 */
public class JudgmentsLoader {
	
	
	/**
	 * Constructor - parameters are for the first load for adding wiktionary related terms.
	 * @param wiktionaryFile
	 * @param oldIndex
	 * @throws IOException
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public JudgmentsLoader(String databaseName, String wiktionaryFile, String oldIndex) throws IOException, ClassNotFoundException, SQLException {
		m_wiktionary = new Wiktionary(new File(wiktionaryFile));
		m_ngramData = new NgramData(oldIndex);
		m_qg = new QueryGenerator(new StandardAnalyzer(Version.LUCENE_31), "TERM_VECTOR");
		m_sql = new SQLAccess(databaseName);
	}
	
	/**
	 * Generates a new input file with the suitable ids from the database
	 * @param inputFile file with target terms
	 * @param outputFile file with id+target term
	 * @return first id that was added
	 * @throws IOException
	 * @throws SQLException
	 */
	public int generateInputFile(File inputFile, File outputFile) throws IOException, SQLException{
		List<String> origTermsList = FileUtils.loadFileToList(inputFile);
		HashMap<Integer, String> idTermsMap = m_sql.insertAndExtractInputFile(origTermsList);
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile,true));
		List<Integer> mapKeys = new ArrayList<Integer>(idTermsMap.keySet());
		Collections.sort(mapKeys);
		int firstId = mapKeys.get(0);
		for(int id:mapKeys)
			writer.write(id + "\t" + idTermsMap.get(id) + "\n");
		writer.close();
		return firstId;
	}

	/**
	 * Generates the first statistics extraction for judgment including wiktionary expansions
	 * Input file format: starts with ngram, lemma, score, ancient/modern
	 * Output file format: ngram, lemma, ancient/modern (1/0), judgment, group, expansion id, generation
	 * @param f statistics file (File name: target term id.dataClusters)
	 * @param outputFolder judgments' folder
	 */
	public void generateIntialJudgmentFile(File f, String outputFolder) throws IOException, ParseException{
		File judgmentsDir = new File(outputFolder);
		if (!judgmentsDir.exists())
			judgmentsDir.mkdir();
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String target_term = f.getName().substring(0,f.getName().indexOf("_")); 
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+target_term+".dataGroups"), "CP1255"));
		writer.write("Term\tLemma\tOldCount\tJudgment\tGroup\tParent\tGeneration\n");
		// add wiktionary synonyms at the beginning of the annotation file
		String target_term_desc = TargetTerm2Id.getStrDesc(Integer.parseInt(target_term));
		for(String relTerm:m_wiktionary.getRelSet(target_term_desc, true, 1)){
			int period = (m_ngramData.countOldPeriod(m_qg.generate(relTerm))>0?1:0);
			// get the period of the related term
			writer.write("["+relTerm+"]\t["+relTerm+"]\t"+ period +"\t-99\t-88\t-1\t0\n");
		}
		String line = reader.readLine();
		line = reader.readLine(); // skip the first line
		while(line != null) {
			writer.write(line.split("\t")[0]+"\t"+line.split("\t")[1]+"\t"+ line.split("\t")[3]+"\t-99\t-88\t-1\t0\n");
			line = reader.readLine();
		}
		reader.close();
		writer.close();
	}
	
	/**
	 * Generates the first statistics extraction for judgments, which include only wiktionary expansions
	 * Output file format: ngram, lemma, ancient/modern (1/0), judgment, group, expansion id
	 * @param outputFolder judgments' folder
	 */
	public void addWikiIntialJudgmentFiles(String outputFolder) throws IOException, ParseException{
		File judgmentsDir = new File(outputFolder);
		if (!judgmentsDir.exists())
			judgmentsDir.mkdir();
  
		// generate a set of already existing judgment files
		Set<Integer> idSet = new HashSet<Integer>();
		for(String s:judgmentsDir.list())
			idSet.add(Integer.parseInt(s.substring(0,s.indexOf("."))));
		
		for(int id:TargetTerm2Id.getIds()){
			if (!idSet.contains(id)){
				// add wiktionary synonyms at the beginning of the annotation file
				String target_term_desc = TargetTerm2Id.getStrDesc(id);
				HashSet<String> wikiList = m_wiktionary.getRelSet(target_term_desc, true, 1);
				if (!wikiList.isEmpty()){
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+id+".dataGroups"), "CP1255"));
					writer.write("Term\tLemma\tOldCount\tJudgment\tGroup\tParent\tGeneration\n");
					for(String relTerm:wikiList){
						int period = (m_ngramData.countOldPeriod(m_qg.generate(relTerm))>0?1:0);
						// get the period of the related term
						writer.write("["+relTerm+"]\t["+relTerm+"]\t"+ period +"\t-99\t-88\t-1\t0\n");
					}
					writer.close();
				}
			}
		}
		
	}
	
	/**
	 * Loads annotations to database, only new annotations were judged
	 * @param file (Input file format: ngram, lemma, ancient/modern (1/0), judgment, group, expansion id)
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void loadAnnotations(File file) throws ClassNotFoundException, SQLException, IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "CP1255"));
		String line = reader.readLine();
		line = reader.readLine(); // skip the first line
		int lineNum = 1;
		int target_term_id = Integer.parseInt(file.getName().substring(0,file.getName().indexOf(".")));
		System.out.println(file.getAbsolutePath());
		while(line != null) {
			m_sql.insertAnnotation(line.split("\t")[0], line.split("\t")[1], target_term_id, Integer.parseInt(line.split("\t")[6]), lineNum, Integer.parseInt(line.split("\t")[3]),Integer.parseInt(line.split("\t")[2]),Integer.parseInt(line.split("\t")[4]),Integer.parseInt(line.split("\t")[5]),true);
			line = reader.readLine();
			lineNum ++;
		}
		reader.close();
	}
	
	/**
	 * Generates a new expansions file
	 * @param inputFile an expansion file from the GUI
	 * @param outputFile id+expansion expansion file
	 * @return expansions map for further mapping
	 * @throws IOException
	 * @throws SQLException
	 */
	public HashMap<Integer, Pair<Integer,String>> generateExpansionsFile(File inputFile, File outputFile) throws IOException, SQLException{
		// insert the expansion to the database
		int prev_exp_Id = m_sql.getLastExpansionId();
		String encoding = utils.FileUtils.getFileEncoding(inputFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), encoding));
		HashMap<Integer,Integer> idGenerationMap = new HashMap<Integer, Integer>();
		String line = reader.readLine();
		while (line != null){
			String[] tokens = line.split("\t");
			int target_term_id = Integer.parseInt(tokens[0]);
			int generation = -1;
			if(idGenerationMap.containsKey(target_term_id))
				generation = idGenerationMap.get(target_term_id);
			else {
				generation = m_sql.getGeneration(target_term_id)+1;
				idGenerationMap.put(target_term_id, generation);
			}
			if (!tokens[1].trim().isEmpty())
				m_sql.insertExpansion(tokens[1], tokens[0], Integer.parseInt(tokens[2]), generation, Integer.parseInt(tokens[3]));
			line = reader.readLine();
		}
		reader.close();
		
		// generate the expansion file
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		m_dupExpMap = new HashMap<Integer, Integer>();
		HashMap<String,Integer> convertMap = new HashMap<String, Integer>();
		
		HashMap<Integer, Pair<Integer,String>> expMap = m_sql.getExpansions(prev_exp_Id+1);
		for(int id:expMap.keySet()){
			String terms = expMap.get(id).value();
			
			if(!convertMap.containsKey(terms)){
				convertMap.put(terms, id);
				writer.write(id + "\t" + terms + "\n");
			}
			else
				m_dupExpMap.put(id, convertMap.get(terms));
//			HashSet<String> termsSet = StringUtils.convertStringToSet(terms);
//			String query = "";
//			for(String t:termsSet)
//				query = query + t + "\t";
//			writer.write(id + "\t" + query.trim() + "\n");
			
		}
		System.out.println("dupMap: " + m_dupExpMap);
		writer.close();
		return expMap;
	}
	
	
	/**
	 * Exports the statistics extraction data for judgment (assign previous annotations)
	 * Output file format: ngram, lemma, ancient/modern (1/0), judgment, group, expansion id, generation
	 * @param file (Input file format: starts with ngram, lemma, score, ancient/modern, expansion id)
	 * @param outputFolder
	 * @throws IOException
	 * @throws SQLException
	 */
	public void exportRunData(File file, String outputFolder) throws IOException, SQLException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int target_term_id = Integer.parseInt(file.getName().substring(0,file.getName().indexOf("_")));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+ target_term_id + ".dataGroups"), "CP1255"));
		String line = reader.readLine();
		line = reader.readLine(); // skip the first line
		int lineNum = 1;
		HashMap<String, Integer> lemmaGroups = m_sql.getLemmas(target_term_id);
		HashMap<String, Integer> resultGroups = m_sql.getResults(target_term_id);
		HashSet<Integer> prevGroups = new HashSet<Integer>();
		int generation = m_sql.getGeneration(target_term_id);
		while(line != null) {
			Pair<Integer, Integer> annoPair = getAnnotation(resultGroups,lemmaGroups,line.split("\t")[0],line.split("\t")[1]);
			// positive judgment with group id
			if (annoPair.value()>-1){
				prevGroups.add(annoPair.value());
				m_sql.insertAnnotation(line.split("\t")[0], line.split("\t")[1], target_term_id, generation, lineNum,annoPair.key(), Integer.parseInt(line.split("\t")[3]),annoPair.value(),Integer.parseInt(line.split("\t")[8]),false);
			}
			// already seen negative judgment
			else if (annoPair.key()==0){
				m_sql.insertAnnotation(line.split("\t")[0], line.split("\t")[1], target_term_id, generation, lineNum,annoPair.key(), Integer.parseInt(line.split("\t")[3]),annoPair.value(),Integer.parseInt(line.split("\t")[8]),false);
			}
			// write only non-annotated values
			else
				writer.write(line.split("\t")[0]+"\t"+line.split("\t")[1] + "\t" + line.split("\t")[3] + "\t" + annoPair.key() + "\t" + annoPair.value() + "\t" + Integer.parseInt(line.split("\t")[8]) + "\t" + generation + "\n");
			line = reader.readLine();
			lineNum ++;
		}
		for(int group_id:prevGroups)
			m_sql.updateGroupAnnotations(target_term_id, group_id);
		reader.close();
		writer.close();
	}
	
	// @TODO implement other merging methods
	/**
	 * Merges files for annotation
	 * add expansion id information to the file
	 * @param targetTermId
	 * @param idList
	 * @param inputFolder
	 * @param outpuFolder
	 * @throws IOException
	 */
	public void mergeFiles(int targetTermId, ArrayList<Integer> idList, String inputFolder, String outpuFolder) throws IOException{
		HashSet<String> seenLemmas = new HashSet<String>();
		System.out.println("writing merged file: " + outpuFolder+ "/" + targetTermId +"_Dice.dataClusters.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(outpuFolder+ "/" + targetTermId +"_Dice.dataClusters.txt"));
		ArrayList<Pair<Integer,BufferedReader>> readersList = new ArrayList<Pair<Integer,BufferedReader>>();
		for (int id:idList) {
			int curId = id;
			if(m_dupExpMap.containsKey(id))
				curId = m_dupExpMap.get(id);
			File idFile = new File(inputFolder+ "/" + curId +"_Dice.dataClusters.txt");
			if(idFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFolder+ "/" + curId +"_Dice.dataClusters.txt"));
				readersList.add(new Pair<Integer,BufferedReader>(id,reader));
			}
		}
		// merge a line from each file, avoid duplicate lines
		int activeCounter = readersList.size();
		while (activeCounter > 0) {
			activeCounter = readersList.size();
			for(int i=0; i<readersList.size(); i++ ){
				String line = readersList.get(i).value().readLine();
				if(line == null)
					activeCounter --;
				else {
					String lemma = line.split("\t")[1];
					HashSet<String> lemmaSet = StringUtils.convertStringToSet(lemma);
					boolean bfound = false;
					for(String l:lemmaSet)
						if(seenLemmas.contains(l))
							bfound = true;
						else
							seenLemmas.add(l);
					if (!bfound)
						writer.write(line+"\t"+readersList.get(i).key()+"\n");
				}
			}
		}
	    for(Pair<Integer,BufferedReader> r:readersList)
	    	r.value().close();
	    writer.close();
	}
	
	/**
	 * Prints groups data to a file
	 * @param outputFolder
	 * @param fileName for the id of the target term
	 * @throws SQLException
	 * @throws IOException
	 */
	public void printUpdatedGroupsData(String outputFolder, String fileName) throws SQLException, IOException{
		int target_term_id = Integer.parseInt(fileName.substring(0, fileName.indexOf("_")));
		HashMap<Integer, String> groupsData = m_sql.getGroupsData(target_term_id);
		if(!groupsData.isEmpty()){
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+target_term_id+".groups"), "CP1255"));
			SortedSet<Integer> keys = new TreeSet<Integer>(groupsData.keySet());
			for(int i:keys)
				writer.write(i + "\t" + groupsData.get(i) + "\n");
			writer.close();
		}
		
	}
	
	/**
	 * Prints groups data to a file
	 * @param outputFolder
	 * @param fileName for the id of the target term
	 * @throws SQLException
	 * @throws IOException
	 */
	public void printAllGroupsData(String outputFolder) throws SQLException, IOException{
		HashMap<Integer, String> targetTerms = m_sql.getTargetTermList();
		for(int target_term_id:targetTerms.keySet()){
			HashMap<Integer, String> groupsData = m_sql.getGroupsData(target_term_id);
			if(!groupsData.isEmpty()){
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+"/"+target_term_id+".groups"), "CP1255"));
				SortedSet<Integer> keys = new TreeSet<Integer>(groupsData.keySet());
				for(int i:keys)
					writer.write(i + "\t" + groupsData.get(i) + "\n");
				writer.close();
			}
		}
		
	}
	
	/**
	 * Prints thesaurus files
	 * @param outputFolder
	 * @throws SQLException
	 * @throws IOException
	 */
	public void printThesaurus(String outputFolder) throws SQLException, IOException{
		HashMap<Integer, String> targetTermsMap = m_sql.getTargetTermList();
		File thesFolder = new File(outputFolder);
		if (!thesFolder.exists())
			thesFolder.mkdir();
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder+"/targetTerms.txt"));
		for(Integer i:targetTermsMap.keySet()){
			writer.write(i+"\t"+targetTermsMap.get(i) + "\n");
			List<Pair<String, String>> termData = m_sql.GetThesaurusData(i);
			if (!termData.isEmpty()){
				BufferedWriter dataWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFolder+i+".thes"), "CP1255"));
				for(Pair<String,String> data:termData)
					dataWriter.write(data.key().replaceAll("\\[|\\]", "") + "\t" + data.value().replaceAll("\\[|\\]", "") + "\n");
				dataWriter.close();
			}
			
		}
		writer.close();
	}
	
	//@TODO - re-implement if necessary
	/**
	 * Gets previous annotation
	 * Implements the matching between seen and unseen candidates
	 * @param resultGroups map of previously annotated results of a certain target term
	 * @param lemmaGroups map of previously annotated lemmas of a certain target term
	 * @param result
	 * @param lemma
	 * @return Pair: annotation or -99 (if it's a new annotation)
	 * 				 group or -1
	 * @throws SQLException 
	 */
	private Pair<Integer,Integer> getAnnotation(HashMap<String, Integer> resultGroups, HashMap<String,Integer> lemmaGroups, String result, String lemma) throws SQLException{
		int annotation = -99;
		int group = -1;
		HashSet<String> lemmaInput = StringUtils.convertStringToSet(lemma);
		for(String lem:lemmaGroups.keySet()){
			for(String l: lemmaInput)
				if (StringUtils.convertStringToSet(lem).contains(l)){
					annotation = (lemmaGroups.get(lem)>=0?1:0);
					return new Pair<Integer, Integer>(annotation,lemmaGroups.get(lem));
				}
		}
		MWEDistance dist = new MWEDistance();
		for(String res:resultGroups.keySet()){
			if (dist.distance(res, result) == 0){
				annotation = (resultGroups.get(res)>=0?1:0);
				return new Pair<Integer, Integer>(annotation,resultGroups.get(res));
			}
		}
		
		return new Pair<Integer, Integer>(annotation,group);
	}
	
//	/**
//	 * Checks whether the annotation already exist in the database
//	 * @param target_term_id
//	 * @param lemma
//	 * @return boolean
//	 * @throws SQLException
//	 */
//	private boolean isNewAnnoatation(int target_term_id, String lemma) throws SQLException{
//		HashSet<String> allLemmas = new HashSet<String>();
//		HashSet<String> lemmaSet = m_sql.getAnnotatedLemmas(target_term_id);
//		for(String lem:lemmaSet){
//			allLemmas.addAll(StringUtils.convertStringToSet(lem));
//		}
//		HashSet<String> lemmaInput = StringUtils.convertStringToSet(lemma);
//		for(String lem: lemmaInput)
//			if (allLemmas.contains(lem))
//				return false;
//		return true;
//		
//	}
	
	/**
	 * Gets duplicated expansions' mapping to avoid re-querying duplicated expansions
	 * @return duplicates' mapping
	 */
	public HashMap<Integer,Integer> getDuplicatedExpansionsMap(){
		return m_dupExpMap;
	}
	
	/**
	 * 
	 * @param file
	 * @throws IOException
	 * @throws SQLException
	 */
	public void checkContradictions(File file) throws IOException, SQLException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int target_term_id = Integer.parseInt(file.getName().substring(0,file.getName().indexOf(".")));
		String line = reader.readLine();
		line = reader.readLine(); // skip the first line
		int lineNum = 1;
		HashMap<String, Integer> lemmaGroups = m_sql.getLemmas(target_term_id);
		HashMap<String, Integer> resultGroups = m_sql.getResults(target_term_id);
		while(line != null) {
			Pair<Integer, Integer> annoPair = getAnnotation(resultGroups,lemmaGroups,line.split("\t")[0],line.split("\t")[1]);
			int curAnno = Integer.parseInt(line.split("\t")[3]);
			// positive judgment with group id
			if (annoPair.value()>-1 && curAnno==0 ){
				System.out.println(target_term_id+"\t"+line.split("\t")[0] + "\t" + lineNum + "\n");
			}
			// negative judgment
			else if (annoPair.key()==0 && curAnno==1 ){
				System.out.println(target_term_id+"\t"+line.split("\t")[0] + "\t" + lineNum + "\n");
			}
			line = reader.readLine();
		}
		reader.close();
	}
	
	
	private Wiktionary m_wiktionary;
	private NgramData m_ngramData; 
	private QueryGenerator m_qg;
	private HashMap<Integer,Integer> m_dupExpMap = null;
	private SQLAccess m_sql;
}
