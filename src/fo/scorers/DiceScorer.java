package fo.scorers;

/**
 * Dice coefficient scorer
 * @author HZ
 *
 */
public class DiceScorer implements StatScorer{
	
	/**
	 * Calculates dice coefficient, Dice = (2*iJointCount)/(iElementCount+iFeatureCount)
	 * @param iTotalElementCount
	 * @param iElementCount
	 * @param iFeatureCount
	 * @param iJointCount
	 * @return dice score 
	 */
	public double score(long iTotalElementCount, long iElementCount, long iFeatureCount, long iJointCount)
	{
		//dice = (2*c.doc_clicks)/(a.doc_freq+b.doc_freq) 
		double score = ((double)((2*iJointCount)))/((double)(iElementCount+iFeatureCount));
		
		if(score <= 0)
			return 0;
		
		return score;
	}

	/*
	 * (non-Javadoc)
	 * @see fo.scorers.StatScorer#getName()
	 */
	public String getName() {
		return m_name;
	}

	static private String m_name="Dice";

}	

	