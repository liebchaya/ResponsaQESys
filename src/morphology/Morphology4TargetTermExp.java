/**
 * Morphology nice package
 */
package morphology;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

/**
 * Common morphology prefixes for target term expressions' generation
 * @author HZ
 *
 */
public class Morphology4TargetTermExp {
	
	public static String[] prefixes = {"מ", "ש", "ה", "ו", "כ", "ל" , "ב", "ומ", "וש", "וה", "וכ", "ול" , "וב"};

	/**
	 * Generates the target term's morphological variants, file name should end with "_orig.txt" 
	 * @param targetTermFile original target terms' list
	 * @param index in order to avoid complex queries with terms that don't appear in the corpus
	 * @param firstId first id of the terms that should be treated
	 * @return String re-formated target terms' file with morphology prefixes
	 * @throws IOException
	 */
	public static String generateMorphExpFile(String targetTermFile, String index, int firstId) throws IOException {
		IndexReader reader = IndexReader.open(FSDirectory.open(new File(index)));
		BufferedReader fileReader = new BufferedReader(new FileReader(targetTermFile));
		String morphFile = targetTermFile.replace("_orig.txt","_morph.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(morphFile));
		String line = fileReader.readLine();
		while (line != null) {
			String[] tokens = line.split("\t");
			if (Integer.parseInt(tokens[0])<firstId){
				line = fileReader.readLine();
				continue;
			}
			String morphExpan = "";
			for(String clsElem:tokens[1].split(",")){
				String[] ngram = clsElem.trim().split(" ");
				String term;
				int freq;
				if (ngram.length == 2) { // bigram expression
					for(String prefix:prefixes) {
						term = prefix + ngram[0] + " " + ngram[1];
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
						// try to add ה before the second word
						term = prefix + ngram[0] + " ה" + ngram[1];
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
					}
					// try to add ה before the second word, without any other prefix
					term = ngram[0] + " ה" + ngram[1];
					freq = reader.docFreq(new Term("TERM_VECTOR",term));
					if (freq > 0)
						morphExpan = morphExpan + "\t" + term;
					// try to replace ם in 
					if (ngram[1].endsWith("ם")) {
						term = ngram[0] + " " + ngram[1].substring(0,ngram[1].length()-1) + "ן";
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
						}
				}
				if (ngram.length == 3) { // trigram expression
					for(String prefix:prefixes) {
						term = prefix + ngram[0] + " " + ngram[1] + " " + ngram[2];
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
						// try to add ה before the second word
						term = prefix + ngram[0] + " ה" + ngram[1] + " " + ngram[2];
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
						// try to add ה before the third word
						term = prefix + ngram[0] + " " + ngram[1] + " ה" + ngram[2];
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
						// try to add ה before the second and the third word
						term = prefix + ngram[0] + " ה" + ngram[1] + " ה" + ngram[2];
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
					}
					// try to add ה before the second word, without any other prefix
					term = ngram[0] + " ה" + ngram[1] + " " + ngram[2];
					freq = reader.docFreq(new Term("TERM_VECTOR",term));
					if (freq > 0)
						morphExpan = morphExpan + "\t" + term;
					// try to add ה before the second and the third word without any other prefix
					term = ngram[0] + " ה" + ngram[1]  + " ה" + ngram[2];
					freq = reader.docFreq(new Term("TERM_VECTOR",term));
					if (freq > 0)
						morphExpan = morphExpan + "\t" + term;
					// try to add ה before the  the third word without any other prefix
					term = ngram[0] + " " + ngram[1]  + " ה" + ngram[2];
					freq = reader.docFreq(new Term("TERM_VECTOR",term));
					if (freq > 0)
						morphExpan = morphExpan + "\t" + term;
					// try to replace ם in 
					if (ngram[2].endsWith("ם")) {
						term = ngram[0] + " " + ngram[1] + " " + ngram[2].substring(0,ngram[2].length()-1) + "ן";
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
						}
				}
				if (ngram.length == 1) { // unigram expression
					for(String prefix:prefixes) {
						term = prefix + ngram[0];
						freq = reader.docFreq(new Term("TERM_VECTOR",term));
						if (freq > 0)
							morphExpan = morphExpan + "\t" + term;
					}
				}
			}
			String newLine = line.trim() + "\t" + morphExpan.trim();
			writer.write(newLine.trim() + "\n");
			line = fileReader.readLine();
		}
		reader.close();
		writer.close();
		return morphFile;
	}

}
