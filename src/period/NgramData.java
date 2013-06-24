package period;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import obj.Pair;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import search.QueryGenerator;
import utils.StringUtils;

public class NgramData {
	
	public NgramData(String oldIndex, String ngramsIndex, String modernIndex) throws CorruptIndexException, IOException{
		m_oldSearcher =  new IndexSearcher(FSDirectory.open(new File(oldIndex)));
		m_ngramsSearcher = new IndexSearcher(FSDirectory.open(new File(ngramsIndex)));
		m_modernSearcher = new IndexSearcher(FSDirectory.open(new File(modernIndex)));
		
	}
	
	/**
	 * 
	 * @param clusterQuery
	 * @param targetTerm
	 * @param termsData
	 * @return Pair<Integer,Integer> - Added documents by the ngram, Intersecting documents 
	 * @throws IOException
	 */
	public Pair<Integer,Integer> NgramPossibleContribution(Query clusterQuery, String targetTerm, HashMap<String,HashSet<Integer>>termsData) throws IOException{
		ScoreDoc[] docs = m_ngramsSearcher.search(clusterQuery,1000).scoreDocs;
		int addDocsNum = 0;
		int comDocsNum = 0;
		for(int i=0; i<docs.length; i++){
			if(!termsData.get(targetTerm).contains(docs[i].doc))
				addDocsNum++;
			else 
				comDocsNum++;
		}
		return new Pair<Integer, Integer>(addDocsNum,comDocsNum);
	}
	
	/**
	 * Period classification by appearances in the old corpus
	 * @param clusterQuery
	 * @return
	 * @throws IOException
	 */
	public int countOldPeriod(Query clusterQuery) throws IOException{
		TotalHitCountCollector collector = new TotalHitCountCollector();
		m_oldSearcher.search(clusterQuery, collector);
		int docFreq = collector.getTotalHits();
		return docFreq;
	}
	
	/**
	 * Counts the intersection between the ngram and the target term in the modern corpus
	 * @param clusterQuery
	 * @param morphTargetTermQuery
	 * @return
	 * @throws IOException
	 */
	public int countModernIntersaction(Query clusterQuery, Query morphTargetTermQuery) throws IOException{
		TotalHitCountCollector collector = new TotalHitCountCollector();
		BooleanQuery bq = new BooleanQuery();
		bq.add(clusterQuery,Occur.MUST);
		bq.add(morphTargetTermQuery,Occur.MUST);
		m_modernSearcher.search(bq, collector);
		return collector.getTotalHits();
	}

	private IndexSearcher m_oldSearcher;
	private IndexSearcher m_ngramsSearcher;
	private IndexSearcher m_modernSearcher;
	
}
