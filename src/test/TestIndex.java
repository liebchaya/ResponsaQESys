package test;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

public class TestIndex {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException {
		IndexSearcher searcher = new IndexSearcher(IndexReader.open(FSDirectory.open(new File("/home/ir/liebesc/ResponsaSys/indexes//allCorpora"))));
	    Document d = searcher.doc(2000);
	    System.out.println((2000) + ". " + d.get("ID") + "\t" + d.get("TERM_VECTOR") + "\n");
	    d = searcher.doc(2010);
	    System.out.println((3010) + ". " + d.get("ID") + "\t" + d.get("TERM_VECTOR") + "\n");
	    d = searcher.doc(3200);
	    System.out.println((3200) + ". " + d.get("ID") + "\t" + d.get("TERM_VECTOR") + "\n");
	    d = searcher.doc(5400);
	    System.out.println((5400) + ". " + d.get("ID") + "\t" + d.get("TERM_VECTOR") + "\n");
	    d = searcher.doc(5600);
	    System.out.println((5600) + ". " + d.get("ID") + "\t" + d.get("TERM_VECTOR") + "\n");
		
		

	}

}
