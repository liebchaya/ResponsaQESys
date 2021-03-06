package fo.scorers;

/**
 * Interface for first-order similarity measures
 * @author HZ
 *
 */
public interface StatScorer {
	public double score(long iTotalElementCount, long iElementCount, long iFeatureCount, long iJointCount);
	/**
	 * Gets the scorer name
	 * @return scorer name
	 */
	public String getName();
}
