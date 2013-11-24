package mwe;

import java.util.HashSet;

import utils.StringUtils;

import com.aliasi.util.Distance;
import com.aliasi.util.Proximity;

/**
 * Ngrams distance for ngram containment recognition
 * @author HZ
 * @see <a href="http://alias-i.com/lingpipe/docs/api/com/aliasi/util/Distance.html">Lingpipe distance interface</a>
 */
public class MWEDistance implements Distance<CharSequence>, Proximity<CharSequence>{

	/*
	 * (non-Javadoc)
	 * @see com.aliasi.util.Proximity#proximity(java.lang.Object, java.lang.Object)
	 */
	public double proximity(CharSequence s1, CharSequence s2) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aliasi.util.Distance#distance(java.lang.Object, java.lang.Object)
	 */
	public double distance(CharSequence s1, CharSequence s2) {
		HashSet<String> firstSet = StringUtils.convertStringToSet(s1.toString());
		HashSet<String> secondSet = StringUtils.convertStringToSet(s2.toString());
		int n1 = (!firstSet.isEmpty()?firstSet.iterator().next().split(" ").length:0);
		int n2 = (!secondSet.isEmpty()?secondSet.iterator().next().split(" ").length:0);
		if (n1>=n2){
			if(Contains(secondSet, firstSet))
				return 0;
			return Double.MAX_VALUE;
		} else{
			if(Contains(firstSet, secondSet))
				return 0;
			return Double.MAX_VALUE;
		}
	}
			

	/**
	 * Checks ngram sets containment
	 * @param shortSet
	 * @param longSet
	 * @return true/false
	 */
	private boolean Contains(HashSet<String> shortSet, HashSet<String> longSet ){
		for(String s1:shortSet)
			for(String s2:longSet)
				if (ContainsString(s1, s2))
					return true;
		return false;
	}
	
	/**
	 * Checks ngram strings containment
	 * @param shortString
	 * @param longString
	 * @return true/false
	 */
	private boolean ContainsString(String shortString, String longString){
		int n1 = shortString.split(" ").length;
		int n2 = longString.split(" ").length;
		for(int i=0; i<=n2-n1; i++){
			int eqNum = 0;
			for(int j=i, k=0; k<n1; j++, k++)
				if(shortString.split(" ")[k].equals(longString.split(" ")[j]))
					eqNum++;
			if (eqNum == n1)
				return true;
		}
		return false;
		}
}
	
	
	

