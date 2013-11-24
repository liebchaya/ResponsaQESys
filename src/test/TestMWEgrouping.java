package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import search.QueryGenerator;
import search.QueryGenerator.InputType;
import utils.StringUtils;

import mwe.MWEGrouping;

public class TestMWEgrouping {

	
	private static String formatQueryString(String candidate) {
		String formatedQuery = "";
		HashSet<String> candSet = StringUtils.convertStringToSet(candidate);
		for(String candTerm:candSet) {
			//Clean up punctuation and digits
			candTerm = candTerm.replaceAll("\\p{Punct}|\\d","");
			formatedQuery = formatedQuery + candTerm + "\t";
		}
		return formatedQuery.trim();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		BufferedReader reader = new BufferedReader(new FileReader("C:\\Documents and Settings\\HZ\\Desktop\\34_Dice.clusters"));
//		String MWEfile = "C:\\Documents and Settings\\HZ\\Desktop\\clusters2000_SUMSCORE_2\\8_Dice.dataClusters.txt";
//		String groupsFile = "C:\\Documents and Settings\\HZ\\Desktop\\clusters2000_SUMSCORE_2\\8_Dice.dataClusters.groups";
//		MWEGrouping grouping = new MWEGrouping();
//		HashSet<String> strings2group = grouping.loadDataFile(MWEfile, );
//		Set<Set<String>> groups = grouping.groupMWE(strings2group);
//		grouping.printMWEgroups(groupsFile, groups);
		String line = reader.readLine();
		while(line != null){
			if (line.split("\t")[0].equals("[קטן שאין, קטן אין, וקטן אין]"))
				System.out.println("here");
		String formatedQuery = formatQueryString(line.split("\t")[0]);
		if (formatedQuery.isEmpty()){
			System.out.println("Empty");
			line = reader.readLine();
			continue;
		}
		System.out.println(formatedQuery);
		QueryGenerator m_qg = new QueryGenerator(new StandardAnalyzer(Version.LUCENE_31), "TERM_VECTOR");
		m_qg.setType(InputType.Query);
		Query clusterQuery = m_qg.generate(formatedQuery);
		line = reader.readLine();
		}
		reader.close();

	}

}
