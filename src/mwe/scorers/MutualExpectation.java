package mwe.scorers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


public class MutualExpectation implements MWEScorer {

	public static void main(String[] args) throws IOException{
		MutualExpectation me = new MutualExpectation("C:\\responsa",4);
		System.out.println("בית הצואר " + me.score("בית הצואר"));
		System.out.println("בית המקדש " + me.score("בית המקדש"));
		System.out.println("בית ספר " + me.score("בית ספר"));
		System.out.println("בית הוא " + me.score("בית הוא"));
	}

	/*
	 * (non-Javadoc)
	 * @see mwe.scorers.MWEScorer#getName()
	 */
	public String getName() {
		return m_name;
	}

	/*
	 * (non-Javadoc)
	 * @see mwe.scorers.MWEScorer#score(java.lang.String)
	 */
	public double score(String ngram) {
		if (!m_ngramsData.containsKey(ngram))
			return 0;
		int n = ngram.split(" ").length;
		int k = Integer.parseInt(m_ngramsData.get(ngram).split(" ")[0]);
		double p = (double)k/n;
		double score = p*normalizedExpectation(ngram);
		return score;
	}
	
	/**
	 * Calculate normalizedExpectation
	 * @param ngram
	 * @return
	 */
	private double normalizedExpectation(String ngram){
		double score = 0.0;
		String[] ngramData = m_ngramsData.get(ngram).split(" ");
		int k = Integer.parseInt(ngramData[0]);
		int omittedNgramsSum = 0;
		int n = ngram.split(" ").length;
		int i=1; // bigram
		if (n>2)
			i += n;
		for (; i< ngramData.length; i++)
			omittedNgramsSum += Integer.parseInt(ngramData[i]);
		double FPE = ((double)1/n) *(k+omittedNgramsSum);
		score = k/FPE;
		return score;
	}
	
	/**
	 * Treat a cluster of ngrams as {@link mwe.scorers.MWEScorer#score}
	 * @param ngrams
	 * @return
	 */
	public double score(String[] ngrams) {
		int counter = 0;
		for(String ngram:ngrams){
			System.out.println(ngram);
			if (m_ngramsData.containsKey(ngram))
				counter++;
		}
		if (counter == 0)
			return 0;
		
		int n = ngrams[0].split(" ").length;
		int k = 0;
		for(String ngram:ngrams)
			if(m_ngramsData.containsKey(ngram)){
				System.out.println(m_ngramsData.get(ngram));
				k += Integer.parseInt(m_ngramsData.get(ngram).split(" ")[0]);
			}
		double p = (double)k/n;
		double score = p*normalizedExpectation(ngrams);
		return score;
	}
	
	/**
	 * Treat a cluster of ngrams as {@link #normalizedExpectation(String)}
	 * @param ngrams
	 * @return
	 */
	private double normalizedExpectation(String[] ngrams){
		double score = 0.0;
		int k=0;
		for(String ngram:ngrams)
			if(m_ngramsData.containsKey(ngram)) {
				String[] ngramData = m_ngramsData.get(ngram).split(" ");
				k+= Integer.parseInt(ngramData[0]);
			}
		int omittedNgramsSum = 0;
		int n = ngrams[0].split(" ").length;
		int i=1; // bigram
		if (n>2)
			i += n;
		
		for(String ngram:ngrams)
			if(m_ngramsData.containsKey(ngram)) {
				String[] ngramData = m_ngramsData.get(ngram).split(" ");
				for (int index=i ; index< ngramData.length; index++)
					omittedNgramsSum += Integer.parseInt(ngramData[index]);
			}
		
		double FPE = ((double)1/n) *(k+omittedNgramsSum);
		score = k/FPE;
		return score;
	} 
	
	
	public MutualExpectation(String ngramsFileName, int maxN) throws IOException {
		m_ngramCounts = new HashMap<Integer, Integer>();
		m_ngramsData = new HashMap<String, String>();
		for (int i=2; i<maxN+1; i++) {
			String countsFile = ngramsFileName+i+".cnt";
			BufferedReader reader = new BufferedReader(new FileReader(countsFile));
			String line = reader.readLine();
			int lineNum = 1;
			m_ngramCounts.put(i,Integer.parseInt(line));
			line = reader.readLine();
			while (line!=null){
				lineNum++;
				String[] tokens = line.split("<>");
				String ngram = "";
				for(int j=0; j<tokens.length-1; j++)
					ngram = ngram + " " + tokens[j];
				ngram = ngram.trim();
				m_ngramsData.put(ngram, tokens[tokens.length-1]);
				line = reader.readLine();
				if (lineNum%10000==0)
					System.out.println("line: " + lineNum);
//					break;
			}
			reader.close();
			System.out.println("Finished loading data file: " + countsFile);
		}
	}

	/**
	 * Merge data files of the same format
	 * @param ngramsFileName
	 * @param maxN
	 * @throws IOException
	 */
	public void addNgramsData(String ngramsFileName, int maxN) throws IOException {
		for (int i=2; i<maxN+1; i++) {
			String countsFile = ngramsFileName+i+".cnt";
			BufferedReader reader = new BufferedReader(new FileReader(countsFile));
			String line = reader.readLine();
			int lineNum = 1;
			int ngramCount = Integer.parseInt(line);
			int jointCount = 0;
			line = reader.readLine();
			while (line!=null){
				lineNum++;
				String[] tokens = line.split("<>");
				String ngram = "";
				for(int j=0; j<tokens.length-1; j++)
					ngram = ngram + " " + tokens[j];
				ngram = ngram.trim();
				if (!m_ngramsData.containsKey(ngram))
					m_ngramsData.put(ngram, tokens[tokens.length-1]);
				else {
					String[] prevData = m_ngramsData.get(ngram).split(" ");
					String[] data = tokens[tokens.length-1].split(" ");
					String newData = "";
					for(int index=0; index<data.length; index++){
						int sum = Integer.parseInt(data[index])+ Integer.parseInt(prevData[index]);
						newData = newData + " " + String.valueOf(sum);
					}
					m_ngramsData.put(ngram, newData.trim());
					jointCount ++;
				}
				line = reader.readLine();
				if (lineNum%10000==0)
					System.out.println("line: " + lineNum);
//					break;
			}
			reader.close();
			int prevCount = m_ngramCounts.get(i);
			m_ngramCounts.put(i, ngramCount+prevCount-jointCount);
			System.out.println("Finished loading data file: " + countsFile);
		}
	}
	
	private HashMap<String,String> m_ngramsData = null;
	private HashMap<Integer,Integer> m_ngramCounts = null;
	static private String m_name="MutualExpectation";

}
