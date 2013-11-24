package obj;

/**
 * Weighted term implementation,
 * (document period classification and relevance members are currently not in use)
 * @author HZ
 *
 */
public class WeightedTerm extends Term{
	public WeightedTerm(Term iTerm, double iWeight)
	{
		super(iTerm);
		m_weight = iWeight;
	}

	/**
	 * Gets term weight
	 * @return term weight
	 */
	public double weight()
	{
		return m_weight;
	}
	
	/**
	 * Sets number of term appearances in each of the document periods
	 * @param pp
	 */
	public void setPeriodProfile(int[] pp){
		for (int i=0;i<=4;i++)
			m_periodProfile[i]=pp[i];
	}
	
	/**
	 * Gets number of term appearances in each of the document periods
	 * @return string
	 */
	public int[] getPeriodProfile(){
		return new int[]{m_periodProfile[0],m_periodProfile[1],m_periodProfile[2],m_periodProfile[3],m_periodProfile[4]};
	}
	
	/**
	 * Presents the document periods
	 * @return string
	 */
	public String getPpString(){
		return m_periodProfile[1]+"\t"+m_periodProfile[2]+"\t"+m_periodProfile[3]+"\t"+m_periodProfile[4];
	}
	
	/**
	 * Presents the term-weight
	 * @return string
	 */
	public String getString (){
		return m_value+"\t"+m_weight;
	}
	
	/**
	 * Presents whole the term's data
	 * @return string
	 */
	public String getFullString(){
		String pp = "";
		if (!(m_periodProfile[1]==-1 && m_periodProfile[2]==-1 
				&& m_periodProfile[3]==-1 && m_periodProfile[4]==-1))
			pp = m_periodProfile[1]+"\t"+m_periodProfile[2]+"\t"+m_periodProfile[3]+"\t"+m_periodProfile[4];
		return m_value+"\t"+m_weight+"\t"+pp;
		       
	}
	
	/**
	 * Checks whether the term is relevant
	 * @return true/false
	 */
	public boolean hasRelevance(){
		if (m_relevance == 0 || m_relevance == 1)
			return true;
		return false;
	}
	
	/**
	 * Sets term relevance
	 * @param r
	 */
	public void setRelevance(int r){
		m_relevance = r;
	}
	
	/**
	 * Gets term relevance
	 * @return relevance
	 */
	public int getRelevance(){
		return m_relevance;
	}
	
	private double m_weight = -1;
	private int[] m_periodProfile=new int[]{-1,-1,-1,-1,-1};
	private int m_relevance = -99;

	
	
}
