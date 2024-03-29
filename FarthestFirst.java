package cmsc420_s23;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap; 
public class FarthestFirst<LPoint extends LabeledPoint2D> {

	// ------------------------------------------------------------------------
	// The following class is not required, but you may find it helpful. It
	// represents the triple (site, center, squared-distance). Feel free to
	// delete or modify.
	// ------------------------------------------------------------------------
	
	private LPoint startCenter; 
	private WtLeftHeap<Double, AssignedPair<LPoint>> heap; 
	private HashMap<String, Locator> map;
	private SMkdTree<LPoint> kdtree; 
	private ArrayList<LPoint> traversal; 
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FarthestFirst(int rebuildOffset, Rectangle2D bbox, LPoint startCenter) {
		this.startCenter = startCenter; 
		this.heap = new WtLeftHeap(); 
		this.map = new HashMap<>(); 
		this.kdtree = new SMkdTree<>(rebuildOffset, bbox, startCenter);
		this.traversal = new ArrayList<>(); 
		this.traversal.add(startCenter);
	}
	
	/*
	 * add a site to the kdTree, and then gets its 
	 * AssignedPair, which is used to add to the 
	 * heap, and finally puts the locator that is 
	 * returned from the heap, to a map, and use 
	 * the sites label for key of the locator 
	 */
	public void addSite(LPoint site) throws Exception {
		kdtree.insert(site);
		AssignedPair<LPoint> ap = kdtree.findAssignedPair(kdtree.getRoot(), site);
		Locator loc = heap.insert(ap.getDistance(), ap);
		map.put(ap.getSite().getLabel(), loc);
	}
	
	/*
	 * gets the next site in the farthest first traversal, 
	 * and this is done by performing the procedure outlined 
	 * in the handout
	 */
	@SuppressWarnings("unchecked")
	public LPoint extractNext() {
		if(kdtree.size() == 0) {
			return null; 
		}
		else {
			try {
				AssignedPair<LPoint> s = heap.extract();
				kdtree.delete(s.getSite().getPoint2D());
				LPoint ck = (LPoint)s.getSite();
				traversal.add(ck);
				kdtree.addCenter(ck);
				ArrayList<AssignedPair<LPoint>> scPairs = new ArrayList<>(); 
				kdtree.listAssign(kdtree.getRoot(), scPairs);
				for(AssignedPair<LPoint> ap : scPairs) {
					Locator loc = map.get(ap.getSite().getLabel()); 
					AssignedPair<LPoint> locAc = (AssignedPair<LPoint>)loc.node.v;
					LPoint pastCenter = (LPoint)locAc.getCenter(); 
					LPoint currCenter = (LPoint)ap.getCenter(); 
					if(!(pastCenter.getPoint2D().equals(currCenter.getPoint2D()))) {
						locAc.setDistance(ap.getDistance());
						locAc.setCenter(currCenter);
						heap.updateKey(loc, ap.getDistance());
					}
				}
				
				return ck; 
			}
			catch(Exception e) {
				e.getCause();
			}
			
			return null; 
		}
	}
	
	/*
	 * returns the number of sites that were 
	 * added
	 */
	public int sitesSize() {
		return kdtree.size(); 
	}
	
	/*
	 * returns the size of the traversal list 
	 */
	public int traversalSize() {
		return traversal.size(); 
	}
	
	/*
	 * this method clears the data structures 
	 * that are being used 
	 */
	public void clear() {
		kdtree.clear();
		heap.clear();
		map.clear();
		traversal.clear();
		//traversal.add(startCenter); 
	}
	
	/*
	 * this is the same function as the ClusterAssignment
	 */
	public ArrayList<String> listKdWithCenters() {
		return kdtree.listWithCenters();
	}
	
	/*
	 * returns the traversal ArrayList 
	 */
	public ArrayList<LPoint> getTraversal() {
		return traversal; 
	}
	
	/*
	 * returns a toString version of the traversal list 
	 * and sorts it before returning 
	 */
	public ArrayList<String> listCenters() {
		ArrayList<LPoint> travSort = new ArrayList<>(traversal); 
		Collections.sort(travSort, new byLabel<LPoint>());
		ArrayList<String> ret = new ArrayList<>(); 
		
		for(LPoint pt : travSort) {
			ret.add(pt.toString()); 
		}
		
		return ret; 
	}
	
	/*
	 * this function gets the ArrayList of all
	 * the AssignedPair for the sites in the kdTree, 
	 * and then gets the toString of all AssignedPairs
	 * and adds them to an ArrayList of Strings, and returns
	 * that ArrayList 
	 */
	public ArrayList<String> listAssignments() { 
		ArrayList<AssignedPair<LPoint>> ret = new ArrayList<>(); 
		kdtree.listAssign(kdtree.getRoot(), ret);
		Collections.sort(ret);
		
		ArrayList<String> strRet = new ArrayList<>(); 
		
		for(int i = 0; i < ret.size(); i++) {
			strRet.add(ret.get(i).toString());
		}
		
		return strRet; 
	}
}
