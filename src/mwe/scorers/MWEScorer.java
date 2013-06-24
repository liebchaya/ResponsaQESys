package mwe.scorers;

public interface MWEScorer {
	/**
	 * Score the ngram 
	 * @param ngram
	 * @return ngram score
	 */
	
	public double score(String ngram);
	/**
	 * Get the MWE scorer name
	 * @return scorer name
	 */
	public String getName();
}
