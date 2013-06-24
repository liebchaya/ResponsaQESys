package representation;


public class NgramsFeatureRepresentation extends FeatureRepresentation{

	public NgramsFeatureRepresentation(FeatureType featureType, String modernJewishIndex) {
		super(featureType);
		m_ngramsIndexName = modernJewishIndex;
	}
	
	
	@Override
	public String getIndexNameByRepresentation() {
		m_removeMarkedFeatures = false;
		return m_ngramsIndexName;	
	}

	private String m_ngramsIndexName;
}
