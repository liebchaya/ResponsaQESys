package period;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mwe.scorers.MutualExpectation;
import obj.Pair;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.Version;

import representation.NgramsTargetTermRepresentation;
import representation.TargetTermRepresentation;
import representation.TargetTermRepresentation.TargetTermType;
import search.QueryGenerator;
import search.QueryGenerator.InputType;

import utils.StringUtils;
import utils.TargetTerm2Id;

/**
 * Ngram data generation
 * @author HZ
 *
 */
public class NgramsDataGeneration {
	
	public NgramsDataGeneration(String oldIndex, String ngramsIndex, String modernIndex) throws CorruptIndexException, IOException {
		m_ngramData = new NgramData(oldIndex, ngramsIndex, modernIndex);
		m_ngramsIndex = ngramsIndex;
		m_qg = new QueryGenerator(new StandardAnalyzer(Version.LUCENE_31), "TERM_VECTOR");
		m_qg.setType(InputType.Query);
		
	}
	
	/**
	 * Generate clusters' data files
	 * @param outputDir - clusters directory
	 * @param expTargetTermFile
	 * @param oldNgramsFileName
	 * @param modernJewishNgramsFileName
	 * @param maxN
	 * @param isFirstRun
	 * @throws IOException
	 * @throws ParseException
	 */
	public void generateDataFiles(String outputDir, String expTargetTermFile, String oldNgramsFileName, String modernJewishNgramsFileName, int maxN, boolean isFirstRun) throws IOException, ParseException{
		// FO n-grams extraction - from ngrams index
		TargetTermType targetType = TargetTermType.Surface;
		TargetTermRepresentation targetRp = new NgramsTargetTermRepresentation(targetType, expTargetTermFile, m_ngramsIndex);
		HashMap<String, ArrayList<ScoreDoc>> targetDocs = targetRp.extractDocsByRepresentation();
		
		HashMap<String, HashSet<Integer>> targetDocsSet = new HashMap<String, HashSet<Integer>>();
		for(String targetTerm: targetDocs.keySet()) {
			HashSet<Integer> docsSet = new HashSet<Integer>();
			ArrayList<ScoreDoc> origDocs = targetDocs.get(targetTerm);
			for(ScoreDoc sd:origDocs)
				docsSet.add(sd.doc);
			targetDocsSet.put(targetTerm, docsSet);
		}
		
		// MWE treatment
		MutualExpectation me = new MutualExpectation(oldNgramsFileName,maxN);
		me.addNgramsData(modernJewishNgramsFileName,maxN);
	
		File dir = new File(outputDir);
		for (File f:dir.listFiles()) {
			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith(".clusters")) {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + f.getName().replace(".clusters", ".dataClusters.txt")));
				writer.write("Candidate\tLemma\tScore\tOld Period Count\tAdded Union\tAdded Intersection\tModern Intersection\tMWE\n");
				String line = reader.readLine();
//				int lineNum = 0;
				while (line!=null){
//					System.out.println(line);
//					lineNum++;
//					if (lineNum > 2500)
//						break;
					String[] tokens = line.split("\t");
					String candidate = tokens[0];
					String clusterString = formatQueryString(candidate);
					if (clusterString.isEmpty()){
						line = reader.readLine();
						continue;
					}
					Query clusterQuery = m_qg.generate(clusterString);
					System.out.println(clusterQuery);
					int oldPeriodCount = m_ngramData.countOldPeriod(clusterQuery);
					String origTargetTerm = TargetTerm2Id.getStrDesc(Integer.parseInt(f.getName().substring(0,f.getName().indexOf("_"))));
					Pair<Integer,Integer> contribPair = m_ngramData.NgramPossibleContribution(clusterQuery, origTargetTerm, targetDocsSet);
					System.out.println(formatQueryString(candidate).split("\t")[0]);
					double meScore = me.score(formatQueryString(candidate).split("\t"));
					String candTerm = candidate.replaceAll("\'\"","");
					int modernInter = 0;
					if (!candTerm.equals(candidate))
						modernInter = m_ngramData.countModernIntersaction(clusterQuery, m_qg.generate(formatQueryString(origTargetTerm)));
					if (!isFirstRun)
						writer.write(line + "\t" + oldPeriodCount + "\t" + contribPair.key() + "\t" + contribPair.value() + "\t" + modernInter + "\t" + meScore + "\n");
					else
						writer.write(line + "\t" + oldPeriodCount + "\t" + contribPair.key() + "\t" + contribPair.value() + "\t" + modernInter + "\t" + meScore + "\t-1\n");
					line = reader.readLine();
				}
				reader.close();
				writer.close();
			}
		}
		
	}

	/**
	 * Format the query in a suitable format for the QueryParser
	 * @param candidate
	 * @return a formated string (tab-delimited)
	 */
	private String formatQueryString(String candidate) {
		String formatedQuery = "";
		HashSet<String> candSet = StringUtils.convertStringToSet(candidate);
		for(String candTerm:candSet) {
			//Clean up punctuation and digits
			candTerm = candTerm.replaceAll("\\p{Punct}|\\d","").trim();
			formatedQuery = formatedQuery + candTerm + "\t";
		}
		return formatedQuery.trim();
	}
	
	private NgramData m_ngramData = null;
	private String m_ngramsIndex;
	private QueryGenerator m_qg;

	
}
