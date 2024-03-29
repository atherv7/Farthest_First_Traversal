package cmsc420_s23;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ClusterAssignment<LPoint extends LabeledPoint2D> {
	
	// ------------------------------------------------------------------------
	// The following class is not required, but you may find it helpful. It
	// represents the triple (site, center, squared-distance). Feel free to
	// delete or modify.
	// ------------------------------------------------------------------------

	private SMkdTree<LPoint> kdTree; 
	private ArrayList<LPoint> centers; 
	private LPoint startCenter; 
	public ClusterAssignment(int rebuildOffset, Rectangle2D bbox, LPoint startCenter) {
		this.kdTree = new SMkdTree<>(rebuildOffset, bbox, startCenter);
		this.centers = new ArrayList<>(); 
		centers.add(startCenter); 
		this.startCenter = startCenter;;
	}
	
	/*
	 * adds a site to the kdTree
	 */
	public void addSite(LPoint site) throws Exception {
		kdTree.insert(site);
	}
	
	/*
	 * deletes site from the kdTree
	 */
	public void deleteSite(LPoint site) throws Exception {
		kdTree.delete(site.getPoint2D());
	}
	
	/*
	 * adds a center to the applicable nodes of the kdTree. 
	 * Throws an Exception if the center that is being added
	 * is a site in the kdTree
	 */
	public void addCenter(LPoint center) throws Exception {
		if(kdTree.find(center.getPoint2D()) != null) {
			throw new Exception("The center that is being inserted is already a site"); 
		}
		else {
			kdTree.addCenter(center);
			centers.add(center);
		}
	}
	
	/*
	 * returns the numbers of sites in the kdTree
	 */
	public int sitesSize() {return kdTree.size();}
	
	/*
	 * returns the number of centers in the space 
	 */
	public int centersSize() {return centers.size();}
	
	/*
	 * clears the kdTree and centers, and then 
	 * adds startCenter back the list 
	 */
	public void clear() {
		kdTree.clear();
		centers.clear();
		centers.add(startCenter); 
	}
	
	/*
	 * returns a list of all nodes in the tree 
	 * with their respective contenders 
	 */
	public ArrayList<String> listKdWithCenters() {
		return kdTree.listWithCenters();
	}
	
	/*
	 * returns a list of strings of all the centers
	 * that were added to the tree
	 */
	public ArrayList<String> listCenters() {
		Collections.sort(centers, new byLabel<LPoint>()); 
		ArrayList<String> ret = new ArrayList<>(); 
		
		for(LPoint pt : centers) {
			ret.add(pt.toString()); 
		}
		
		return ret; 
	}
	
	/*
	 * gets all the assignedPairs from the sites 
	 * in the tree, and then adds their toStrings to 
	 * an ArrayList and returns them 
	 */
	public ArrayList<String> listAssignments() {
		ArrayList<AssignedPair<LPoint>> ret = new ArrayList<>(); 
		kdTree.listAssign(kdTree.getRoot(), ret);
		Collections.sort(ret);
		
		ArrayList<String> strRet = new ArrayList<>(); 
		
		for(int i = 0; i < ret.size(); i++) {
			strRet.add(ret.get(i).toString());
		}
		
		return strRet; 
	}
	
	public void deleteCenter(LPoint center) throws Exception { /* ... */ } // For extra credit only

}

