package representation;
/**
 * Choosing the right index for feature representation
 * @author HZ
 *
 */
public class FeatureRepresentation {
	/**
	 * Possible feature representations
	 */
	public enum FeatureType{
		Surface,
		Best,
		All
	}
	
	/**
	 * @param responsaMainDir
	 * @param featureType
	 */
	public FeatureRepresentation(String responsaMainDir,FeatureType featureType){
		m_indexDir = responsaMainDir+"/indexes/";
		m_type = featureType;
	}
	
	/**
	 * @param featureType
	 */
	public FeatureRepresentation(FeatureType featureType){
		m_type = featureType;
	}
	
	/**
	 * Gets the index for feature extraction, sets indication whether to remove features marked with "$"
	 * @return index name
	 */
	public String getIndexNameByRepresentation() {
		String indexName = null;
		if (m_type == FeatureType.Surface) {
			indexName = "unigIndex";
			m_removeMarkedFeatures = false;
		}
		else if (m_type == FeatureType.Best) {
			indexName = "indexTagger";
			m_removeMarkedFeatures = true;
		}
		else if (m_type == FeatureType.All) {
			indexName = "index0";
			m_removeMarkedFeatures = true;
		}
		return m_indexDir+indexName;		
	}
	
	/**
	 * Checks whether to remove marked ($) features
	 * @return true/false
	 */
	public boolean isRemoveMarkedFeatures() {
		return m_removeMarkedFeatures;
	}
	
	/**
	 * Gets feature type
	 * @return feature type
	 */
	public FeatureType getFeatureType(){
		return m_type;
	}

	protected boolean m_removeMarkedFeatures = false;
	protected String m_indexDir = "C:\\ResponsaNew\\indexes\\";
	protected FeatureType m_type = FeatureType.Surface;

}
