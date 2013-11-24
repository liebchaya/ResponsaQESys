package clustering;

import java.util.Set;

/**
 * Cluster of morphological variants
 * @author HZ
 *
 */
public class Cluster {
	
	private Set<String> m_lemmas = null;
	private Set<String> m_terms = null;
	private double m_score = 0.0;
	private int m_group = 0;
	private boolean m_bAnno = false;
	private boolean m_bIsConflict = false;
	private boolean m_bIsUsed = false;
	
	/**
	 * Check whether there is a common element between two sets 
	 * @param s1
	 * @param s2
	 * @return true/false
	 */
	private boolean ContainsOne(Set<String> s1,Set<String> s2){
		for(String s:s2)
			if(s1.contains(s))
				return true;
		return false;
	}
	
	/**
	 * @param m_lemmas
	 * @param m_terms
	 */
	public Cluster(Set<String> m_lemmas, Set<String> m_terms) {
		super();
		this.m_lemmas = m_lemmas;
		this.m_terms = m_terms;
	}
	
	/**
	 * @param m_lemmas
	 * @param m_terms
	 * @param m_bAnno
	 */
	public Cluster(Set<String> m_lemmas, Set<String> m_terms, boolean m_bAnno) {
		super();
		this.m_lemmas = m_lemmas;
		this.m_terms = m_terms;
		this.m_bAnno = m_bAnno;
	}
	
	/**
	 * @param m_lemmas
	 * @param m_terms
	 * @param m_bAnno
	 * @param m_group
	 */
	public Cluster(Set<String> m_lemmas, Set<String> m_terms, boolean m_bAnno, int m_group) {
		super();
		this.m_lemmas = m_lemmas;
		this.m_terms = m_terms;
		this.m_bAnno = m_bAnno;
		this.m_group = m_group;
	}
	
	/**
	 * @param m_lemmas
	 * @param m_terms
	 * @param m_score
	 */
	public Cluster(Set<String> m_lemmas, Set<String> m_terms, double m_score) {
		super();
		this.m_lemmas = m_lemmas;
		this.m_terms = m_terms;
		this.m_score = m_score;
	}
	
	/**
	 * Gets the cluster's lemmas
	 * @return lemmas' set
	 */
	public Set<String> getLemmas() {
		return m_lemmas;
	}
	
	/**
	 * Sets the cluster's lemmas
	 * @param m_lemmas
	 */
	public void setLemmas(Set<String> m_lemmas) {
		this.m_lemmas = m_lemmas;
	}
	
	/**
	 * Gets the cluster's terms
	 * @return terms' set
	 */
	public Set<String> getTerms() {
		return m_terms;
	}
	
	/**
	 * Sets the cluster's terms
	 * @param m_terms
	 */
	public void setTerms(Set<String> m_terms) {
		this.m_terms = m_terms;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (m_bAnno ? 1231 : 1237);
		result = prime * result
				+ ((m_lemmas == null) ? 0 : 1);
		result = prime * result + ((m_terms == null) ? 0 : 1);
		return result;
	}
	
	
	@Override
	public String toString() {
		return m_terms.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cluster other = (Cluster) obj; 
		
		// different annotation
		if(m_bAnno != other.m_bAnno)
			return false;
		
		// Appearance of a common lemma
		if (m_lemmas == null) {
			if (other.m_lemmas != null)
				return false;
		} else if ((ContainsOne(m_lemmas,other.m_lemmas) || ContainsOne(other.m_lemmas,m_lemmas)) && (!m_lemmas.isEmpty() && !other.m_lemmas.isEmpty()))
			return true;
		
		// Appearance of a common term
		if (m_terms == null) {
			if (other.m_terms != null)
				return false;
		} else if ((ContainsOne(m_terms,other.m_terms) || ContainsOne(other.m_terms,m_terms)) && (!m_terms.isEmpty() && !other.m_terms.isEmpty()))
			return true;
		return false;
	}
	
	/**
	 * Gets the cluster's score
	 * @return cluster's score
	 */
	public double getScore() {
		return m_score;
	}

	/**
	 * Sets the cluster's score
	 * @param m_score
	 */
	public void setScore(double m_score) {
		this.m_score = m_score;
	}
	
	/**
	 * Gets the cluster's annotation
	 * @return true/false
	 */
	public boolean getbAnno() {
		return m_bAnno;
	}
	
	/**
	 * Sets the cluster's annotation
	 * @param anno
	 */
	public void setbAnno(boolean anno) {
		m_bAnno = anno;
	}
	
	/**
	 * Checks whether the cluster was used
	 * @return ture/false
	 */
	public boolean getbIsUsed() {
		return m_bIsUsed;
	}
	
	/**
	 * Sets whether the cluster was used
	 * @param anno
	 */
	public void setbIsUsed(boolean anno) {
		m_bIsUsed = anno;
	}
	
	/**
	 * Checks cluster's conflicts
	 * @return true/false
	 */
	public boolean isConflict() {
		return m_bIsConflict;
	}
	
	/**
	 * Sets cluster's conflicts
	 * @param isConflict
	 */
	public void setIsConflict(boolean isConflict) {
		m_bIsConflict = isConflict;
	}
	
	/**
	 * Gets cluster's group id
	 * @return group id
	 */
	public int getGroup() {
		return m_group;
	}
	
	/**
	 * Sets cluster's group id
	 * @param group
	 */
	public void setGroup(int group) {
		m_group = group;
	}
	
}
