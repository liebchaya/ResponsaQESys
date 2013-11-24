package fo.similarity;

import fo.scorers.StatScorer;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import obj.SortByWeightName;
import obj.Term;
import obj.WeightedTerm;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

import representation.FeatureRepresentation;
import representation.TargetTermRepresentation.TargetTermType;
import utils.TargetTerm2Id;

import ac.biu.nlp.nlp.general.SimpleBidirectionalMap;

/**
 * Implements first-order statistics extraction
 */
public class FeatureVectorExtractor {
	
	/**
	 * Defines how to treat the terms in the index, previously used for morphological representation
	 * @param scorer first-order statistical scorer
	 * @param featureRp candidates representation
	 */
	public FeatureVectorExtractor(StatScorer scorer, FeatureRepresentation featureRp) throws CorruptIndexException, IOException{
		m_scorer = scorer;
		m_featureRp = featureRp;
	    m_reader = IndexReader.open(FSDirectory.open(new File(m_featureRp.getIndexNameByRepresentation())));
		loadFeatureDataFromIndex();
	}
	
	/**
	 * Defines how to treat the terms in the index, previously used for morphological representation
	 * @param scorer first-order statistical scorer
	 * @param featureRp candidates representation
 	 * @param accum for accumulative measures (not used, default=false)
	 */
	public FeatureVectorExtractor(StatScorer scorer, FeatureRepresentation featureRp, boolean accum) throws CorruptIndexException, IOException{
		m_scorer = scorer;
		m_featureRp = featureRp;
	    m_reader = IndexReader.open(FSDirectory.open(new File(m_featureRp.getIndexNameByRepresentation())));
	    m_bAccum = accum;
		loadFeatureDataFromIndex();
	}
	
	/**
	 * Loads candidates' frequency, currently the data is not loaded to avoid memory problems
	 * @throws IOException
	 */
	private void loadFeatureDataFromIndex() throws IOException{
//		int featureId = 0;
		m_featureDesc2Id = new SimpleBidirectionalMap<String,Integer>();
		m_featureFreq = new TIntIntHashMap();
	    
	     /**
	      * @TODO Assign the correct value of m_totalElementsCount when used
	      * e.g. PMI first order measure
	      * 	m_totalElementsCount = reader.numDocs();
	      */	    
//		TermEnum features = m_reader.terms();
//		while (features.next()) {
//			if (!features.term().field().equals("TERM_VECTOR"))
//				continue;
//			
//			/**
//		      * @TODO Add feature filter
//		      */
//			// remove unigrams
//		    if (features.term().text().split(" ").length<2)
//		    	continue;
//			
//			// for morphological treatment 
//			if( m_featureRp.isRemoveMarkedFeatures() && features.term().text().contains("$"))
//				continue;
//			
//			// insert to id-term description map
//			m_featureDesc2Id.put(features.term().text(), featureId);
//			int freqCount = 0;
//			if (!m_bAccum)
//				freqCount = features.docFreq();
//			// accumulative count - not in use
//			else {
//				while (m_reader.termDocs(features.term()).next())
//					freqCount += m_reader.termDocs(features.term()).freq();	
//			}
//			// insert to id-freq map
//			m_featureFreq.put(featureId, freqCount);
//			featureId++;
//		}
//		System.out.println("Finish loading features " + m_featureFreq.size() + " features");
	}
	
	/**
	 * Extracts co-occurring candidates for the target terms
	 * @param targetDocs target terms' map with the document numbers, where the target terms appear
	 * @param targetType target term's representation type 
	 * @param outputDir output directory
	 * @return path of statistics folder
	 * @throws IOException
	 */
	public String extractTargetTermVectors(HashMap<String,ArrayList<ScoreDoc>> targetDocs, TargetTermType targetType,  File outputDir) throws IOException{
		BufferedWriter writer = null;
		if(!outputDir.exists())
			outputDir.mkdir();
		File configDir = new File(outputDir.getAbsolutePath() + "/" + targetType.toString() + "_" + m_featureRp.getFeatureType());
		if(!configDir.exists())
			configDir.mkdir();
		System.out.println("Config dir created "  + configDir.getAbsolutePath());
		for(String target:targetDocs.keySet()){
			if (targetDocs.get(target).size() == 0 )//|| target==null)
				continue;
//			System.out.println(target);
			File targetFile = new File(configDir + "/" +TargetTerm2Id.getIntDesc(target)+ "_" + m_scorer.getName() + ".txt");
			if(targetFile.exists()) {
				System.out.println("Target term file " + targetFile.getName() + " already exist in " + configDir.getAbsolutePath());
				continue;
			}
			else {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile),"UTF8"));
				for(WeightedTerm wt:getTargetTermVector(targetDocs.get(target)))
					if (!wt.getValue().isEmpty())
						writer.write(wt.getValue()+"\t" + wt.weight() + "\n");
				writer.close();
			}
			System.out.println("Finish target term stat. extraction " + target);
		}
		return configDir.getAbsolutePath();
	}
	
	
	/**
	 * Extracts co-occurring candidates for a target term
	 * @param targetDocs the documents numbers, where the target terms appear
	 * @return list of weighted terms 
	 * @throws IOException
	 */
	private List<WeightedTerm> getTargetTermVector(List<ScoreDoc> targetDocs) throws IOException{
		List<WeightedTerm> featureList = new LinkedList<WeightedTerm>();
		if (targetDocs.size() == 0)
			return featureList;
		
		TIntLongHashMap countsMap = new TIntLongHashMap();
		for (ScoreDoc doc : targetDocs) {
			// each term in the document is a possible candidate
			TermFreqVector tfv = m_reader.getTermFreqVector(doc.doc,"TERM_VECTOR");
//			for (String term : tfv.getTerms()) {
//				term = term.replaceAll("[-+*/=](?![^(]*\\))","");
//				if (!m_featureDesc2Id.leftContains(term))
//					continue;
//
//				if (!countsMap.containsKey(m_featureDesc2Id.leftGet(term)))
//					countsMap.put(m_featureDesc2Id.leftGet(term), 1);
//				else {
//					Long score = countsMap.get(m_featureDesc2Id.leftGet(term));
//					countsMap.put(m_featureDesc2Id.leftGet(term), score + 1);
//				}
//
//			}
			
			for (int i=0; i<tfv.size(); i++) {
				String term = tfv.getTerms()[i];
				term = term.replaceAll("[-+*/=](?![^(]*\\))","");
				// ignore unigrams
				if (term.split(" ").length <2)
					continue;
				
				if (!m_featureDesc2Id.leftContains(term)){
					int freq = m_reader.docFreq(new org.apache.lucene.index.Term("TERM_VECTOR",term));
					/*chaya 6.10.13*/
//					if (freq == 0)
					if (freq < 2)
						continue;
					m_featureDesc2Id.put(term, m_featureId);
					m_featureFreq.put(m_featureId, freq);
					m_featureId++;
				}
				// update the joint count of the target term with the candidate
				if (!countsMap.containsKey(m_featureDesc2Id.leftGet(term)))
					if (!m_bAccum)
						countsMap.put(m_featureDesc2Id.leftGet(term), 1);
					else
						countsMap.put(m_featureDesc2Id.leftGet(term), tfv.getTermFrequencies()[i]);
				else {
					Long score = countsMap.get(m_featureDesc2Id.leftGet(term));
					if (!m_bAccum)
						countsMap.put(m_featureDesc2Id.leftGet(term), score + 1);
					else
						countsMap.put(m_featureDesc2Id.leftGet(term), score + tfv.getTermFrequencies()[i]);
				}

			}
			
		}
		for (int featureId : countsMap.keys()) {
			double score = m_scorer.score(m_totalElementsCount, targetDocs.size(), m_featureFreq.get(featureId), countsMap.get(featureId));

			if (score > 0) {
				featureList.add(new WeightedTerm(new Term(m_featureDesc2Id.rightGet(featureId)),score));
			}
		}
		Collections.sort(featureList,new SortByWeightName());
		return featureList;
	}
	
	
	private SimpleBidirectionalMap<String,Integer> m_featureDesc2Id; 
	private StatScorer m_scorer;
	private FeatureRepresentation m_featureRp;
	private TIntIntHashMap m_featureFreq;
	private int m_totalElementsCount = 0;
	private IndexReader m_reader = null;
	private boolean m_bAccum = false;
	private int m_featureId = 0;
}
