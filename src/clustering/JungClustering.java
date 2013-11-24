package clustering;
import java.util.Set;

import com.aliasi.util.Distance;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;


/**
 * Clustering based on JUNG package
 * @author HZ
 * @see <a href="http://jung.sourceforge.net/">Jung package</a>
 */
public class JungClustering {
	
	private UndirectedGraph<String, Integer> agraph = null;
	
	/**
	 * Builds a graph
	 * @param inputList list of string nodes
	 * @param dist distance function for edge's weighting
	 */
	public void buildGraph(Set<String> inputList,Distance<CharSequence> dist){
		agraph = new UndirectedSparseGraph<String, Integer>();
		for(String term:inputList)
			agraph.addVertex(term);
		
		int id=0;
		for (String v1 : agraph.getVertices()){
			for(String v2: agraph.getVertices()){
				if (!v1.equals(v2) && dist.distance(v1, v2) == 0){
					id++;
					agraph.addEdge(id, v1,v2);
				}
			}
		}
	}

	/**
	 * Jung's weak component clustering
	 * @return a set of clusters
	 */
	public Set<Set<String>> cluster() {
    		
		WeakComponentClusterer<String, Integer> weakclust = new WeakComponentClusterer<String, Integer>();
		return weakclust.transform(agraph);
	}
}

