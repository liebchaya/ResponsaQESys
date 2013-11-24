package clustering;

import java.util.Map;
import java.util.Set;

/**
 * Cluster scoring
 * @author HZ
 *
 */
public class ClusterScorer {
	
	/**
	 * Types of scoring methods
	 */
	public enum ScorerType
	{
		SUMSCORE,
		MAXSCORE,
		AVGSCORE,
		MAXLENGTH
	}
	
	/**
	 * @param input
	 * @param type
	 */
	public ClusterScorer(Map<String, Double> input, ScorerType type) {
			m_scoreMap = input;
			m_type = type;
		}
	
	/**
	 * Sets the cluster scorer type
	 * @param type
	 */
	public void setScorerType(ScorerType type) {
		m_type = type;
	}
	
	/**
	 * Assigns cluster score by ScorerType
	 * @param cluster
	 * @return cluster score
	 * @throws MorphDistancePrePException
	 */
	public Double getClusterScore(Set<String> cluster) throws MorphDistancePrePException {
		Double score = 0.0;
		if(m_scoreMap == null)
			throw new MorphDistancePrePException("No scores table");
    	double maxScore = Double.MIN_VALUE;
    	double sumScore = 0;
    	for(String str:cluster) {
    		if(!m_scoreMap.containsKey(str))
				throw new MorphDistancePrePException("Missing term in scores table: " + str);
			double curScore = m_scoreMap.get(str);
			if(curScore > maxScore)
				maxScore = curScore;
			sumScore += curScore;
    	}
    	if (m_type.equals(ScorerType.SUMSCORE)) 
			score = sumScore;
    	if (m_type.equals(ScorerType.MAXSCORE)) 
			score = maxScore;
		else if (m_type.equals(ScorerType.AVGSCORE)) 
			score = sumScore/cluster.size();
		else if (m_type.equals(ScorerType.MAXLENGTH)) 
			score = maxScore*cluster.size();
    	return score;
	}
	

	private Map<String, Double> m_scoreMap = null;
	private ScorerType m_type = ScorerType.MAXSCORE;
	
	
}
