package mwe.scorers;

/**
 * Interface for Multi-word expression(MWE) scorers
 * @author HZ
 *
 */
public interface MWEScorer {
	/**
	 * Scores the ngram 
	 * @param ngram
	 * @return ngram score
	 */
	public double score(String ngram);
	/**
	 * Gets the MWE scorer name
	 * @return scorer name
	 */
	public String getName();
}
