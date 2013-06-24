package morphology;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vohmm.application.SimpleTagger3;
import vohmm.corpus.AffixFiltering;
import vohmm.corpus.Sentence;
import vohmm.corpus.TokenExt;
import vohmm.lexicon.BGULexicon;

/**
 * BGU POS-tagger 
 * @author HZ
 *
 */
public class Tagger {
	private static SimpleTagger3 m_tagger;
	
	/**
	 * Initiate the tagger
	 * @param taggerHomdir
	 * @throws Exception
	 */
	public static void init(String taggerHomdir) throws Exception{  
		m_tagger = new SimpleTagger3(taggerHomdir,vohmm.application.Defs.TAGGER_OUTPUT_FORMAT_BASIC,false,false,false,false,null,AffixFiltering.NONE);
		BGULexicon._bHazal = true;
	}
	
	/**
	 * Tagging a string
	 * @param str
	 * @return
	 * @throws Exception
	 */
	private static List<Sentence> getTaggedSentences(String str) throws Exception  {
		return m_tagger.getTaggedSentences(str);
	}
	
	/**
	 * Get the most probable lemma, supports ngrams
	 * @param str
	 * @return Set - containing the most probable lemma - a single string
	 * @throws Exception
	 */
	public static Set<String>  getTaggerLemmas(String str) throws Exception{
		HashSet<String> lemmas = new HashSet<String>();
		String lemma = "";
		for (Sentence sentence : getTaggedSentences(str)) {
			for (TokenExt token : sentence.getTokens()) {
				lemma = lemma + " " + token._token.getSelectedAnal().getLemma().getBaseformStr();
		 }
		}
		if (!lemma.equals(""))
			lemmas.add(lemma.trim());
		return lemmas;
	}
	
	/**
	 * This function is not in use,
	 * to support this functionality see ResponsaNewSystem.morphology.MorphLemmatizer
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static Set<String>  getAllPossibleLemmas(String str) throws Exception{
		HashSet<String> lemmas = new HashSet<String>();
		return lemmas;
	}
	

}
