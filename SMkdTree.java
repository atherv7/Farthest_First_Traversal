package cmsc420_s23;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.LinkedList;

public class SMkdTree<LPoint extends LabeledPoint2D> {
	/*
	 * these are Comparator classes for the Collections.sort() method 
	 * for sorting ArrayLists
	 */
	private class ByXThenY implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			if(!(pt1.getPoint2D()).equals(pt2.getPoint2D())) {
				if(pt1.getX() != pt2.getX()) {
					return (pt1.getX() > pt2.getX()) ? 1 : -1; 
				}
				else {
					return (pt1.getY() > pt2.getY()) ? 1 : -1;
				}
			}
			return 0; 
		}
	}
	
	private class ByYThenX implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			if(!(pt1.getPoint2D()).equals(pt2.getPoint2D())) {
				if(pt1.getY() != pt2.getY()) {
					return (pt1.getY() > pt2.getY()) ? 1 : -1;
				}
				else {
					return (pt1.getX() > pt2.getX()) ? 1 : -1;
				}
			}
			return 0; 
		}
	}
	
	private class byLabel implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			return pt1.getLabel().compareTo(pt2.getLabel()); 
		}
	}
	
	/*
	 * the bulkCreate method that helps with a lot of the restructuring and 
	 * insertions. Added an argument that takes in a LinkedList of contenders 
	 * so that when an External node is created, contenders can be added from 
	 * its parents. 
	 */
	private Node bulkCreate(ArrayList<LPoint> pts, Rectangle2D cell, LinkedList<LPoint> contenders) {
		/*
		 * if there are no points in the ArrayList then return 
		 * an ExternalNode that is empty, after iterating 
		 * through LinkedList to add contenders to ExternalNode
		 */
		if(pts.size() == 0) {
			ExternalNode eN = new ExternalNode(); 
			eN.setCell(cell);
			eN.setSize(0);
			for(LPoint pt : contenders) {
				eN.addCenter(pt);
			}
			return eN; 
		}
		/*
		 * if there is one point in the ArrayList then return an 
		 * ExternalNode with the point in the ArrayList after 
		 * iterating through LinkedList to add contenders to the 
		 * node 
		 */
		else if(pts.size() == 1) {
			ExternalNode eN = new ExternalNode(); 
			eN.setPoint(pts.get(0));
			eN.setCell(cell);
			eN.setSize(1);
			for(LPoint pt : contenders) {
				eN.addCenter(pt);
			}
			return eN; 
		}
		/*
		 * this condition is for when the list of points has 2 or more 
		 * points present 
		 */
		else {
			/*
			 * this section is for determining the cutting dimension 
			 * for the internal node to split the points in the list, and
			 * also adjust if we are in a degenerate situation
			 */
			int cutDim = -1; 
			if(cell.getWidth(1) > cell.getWidth(0)) {
				cutDim = 1; 
				Collections.sort(pts, new ByYThenX()); 
				if(pts.get(0).getY() == pts.get(pts.size()-1).getY()) {
					cutDim = 0; 
					Collections.sort(pts, new ByXThenY());
				}
			}
			else {
				cutDim = 0; 
				Collections.sort(pts, new ByXThenY());
				if(pts.get(0).getX() == pts.get(pts.size()-1).getX()) {
					cutDim = 1; 
					Collections.sort(pts, new ByYThenX());
				}
			}
			
			/*
			 * this section is for determining the cutting value 
			 * and sliding the cutting value and sliding if necessary 
			 */
			double cutVal = cell.getCenter().get(cutDim); 
			if(cutVal < pts.get(0).get(cutDim)) {
				cutVal = pts.get(0).get(cutDim);
			}
			else if(cutVal > pts.get(pts.size()-1).get(cutDim)) {
				cutVal = pts.get(pts.size()-1).get(cutDim); 
			}
			
			/*
			 * separate the points to their respective side 
			 * of the cutting value for specific dimension
			 */
			ArrayList<LPoint> leftpts = new ArrayList<>(); 
			
			for(int i = 0; i < pts.size()-1; i++) {
				if(pts.get(i).get(cutDim) < cutVal) {
					leftpts.add(pts.get(i)); 
					pts.remove(i); 
					i--; 
				}
			}
			
			/*
			 * initialize a new InternalNode object with the proper
			 * dimension and value for cutting, and then recursively
			 * call bulkCreate for the left and right children, 
			 * and then finally return the node. 
			 */
			InternalNode i = new InternalNode(cutDim, cutVal); 
			i.setCell(cell);
			/*
			 * after the internal node is made, iterate through 
			 * the contenders list to add contenders to the node 
			 */
			for(LPoint pt : contenders) {
				i.addCenter(pt);
			}
			i.left = bulkCreate(leftpts, cell.leftPart(cutDim, cutVal), i.getContenders()); 
			i.right = bulkCreate(pts, cell.rightPart(cutDim, cutVal), i.getContenders()); 
			
			i.setSize(i.left.getSize() + i.right.getSize());
			i.insertCt = 0; 
			return i; 
		}
	}
	
	/*
	 * the Node abstract class with methods that both 
	 * ExternalNode and InternalNode should have 
	 */
	private abstract class Node {
		LinkedList<LPoint> contenders;
		double rMin; 
		abstract LPoint find(Point2D q); 
		abstract Node insert(LPoint q); 
		abstract void setCell(Rectangle2D c); 
		abstract int getSize(); 
		abstract LinkedList<LPoint> getContenders(); 
		abstract Node delete(Point2D p); 
		abstract Node addCenter(LPoint center);
		abstract void addToList(LPoint p);
	}
	
	/*
	 * InternalNode class, which has the necessary attributes 
	 * and methods for the data structure. 
	 */
	private class InternalNode extends Node {
		/*
		 * this section is just the constructor and 
		 * the basic getters and setters for the 
		 * attributes 
		 */
		int cutDim, size, insertCt; 
		double cutVal; 
		Node left, right; 
		Rectangle2D cell; 
		LinkedList<LPoint> contenders;
		public InternalNode(int cutDim, double cutVal) {
			this.cutDim = cutDim; 
			this.cutVal = cutVal; 
			size = 0; 
			insertCt = 0; 
			contenders = new LinkedList<>(); 
		}
		
		public int getCount() {
			return insertCt; 
		}
		
		public int getSize() {
			return size; 
		}
		
		public void setCount(int count) {
			insertCt = count; 
		}
		
		public void setSize(int size) {
			this.size = size; 
		}
		
		public int getCutDim() {
			return cutDim; 
		}
		
		public void addToList(LPoint p) {
			this.contenders.add(p);
		}
		
		public double cutVal() {
			return cutVal; 
		}
		
		public Node getLeft() {
			return left; 
		}
		
		public Node getRight() {
			return right; 
		}
		
		public void setLeft(Node left) {
			this.left = left; 
		}
		
		public void setRight(Node right) {
			this.right = right; 
		}
		
		public String toString() {
			String ret = ""; 
			if(cutDim == 0) {
				ret += "(x=";
			}
			else {
				ret += "(y=";
			}
			
			ret += cutVal + ") " + size + ":" + insertCt; 
			
			return ret; 
		}
		
		/*
		 * A toString to return the point and the list of 
		 * contenders, if their are 11 or more contenders 
		 * only print the first 10 
		 */
		public String toStringWithCenters() {
			String ret = ""; 
			if(cutDim == 0) {
				ret += "(x=";
			}
			else {
				ret += "(y=";
			}
			
			ret += cutVal + ") " + size + ":" + insertCt + " => {"; 
			
			Collections.sort(contenders, new byLabel());
			
			if(contenders.size() >= 11) {
				for(int i = 0; i < 10; i++) {
					ret += contenders.get(i).getLabel(); 
					if(i != 9) {
						ret += " "; 
					}
				}
				ret += "...";
			}
			else {
				for(int i = 0; i < contenders.size(); i++) {
					ret += contenders.get(i).getLabel(); 
					if(i != contenders.size()-1) {
						ret += " "; 
					}
				}
			}
			
			ret += "}"; 
			
			return ret; 
		}
		
		/*
		 * returns the contenders for the Node 
		 */
		public LinkedList<LPoint> getContenders() {
			return this.contenders; 
		}
		
		/*
		 * the find method, which for the internal node 
		 * is just determining if we should call the find 
		 * method on the left or right child 
		 */
		public LPoint find(Point2D q) {
			if(q.get(cutDim) < cutVal) {
				return this.left.find(q);
			}
			else {
				return this.right.find(q); 
			}
		}
		
		public void setCell(Rectangle2D c) {
			this.cell = c; 
		}
		
		/*
		 * the insert method for the InternalNode, which is 
		 * just determining which child to call depending on
		 * the InternalNode's cutting value and dimension, 
		 * and then once that is done, the size and insert 
		 * counter for this node is incremented 
		 */
		public Node insert(LPoint q) {
			if(cutDim == 0) {
				if(q.getX() < cutVal) {
					this.left = this.left.insert(q); 
				}
				else {
					this.right = this.right.insert(q); 
				}
			}
			else {
				if(q.getY() < cutVal) {
					this.left = this.left.insert(q);
				}
				else {
					this.right = this.right.insert(q); 
				}
			}
			this.insertCt++; 
			this.size++; 
			return this; 
		}
		
		/*
		 * the delete function for the InternalNode, which 
		 * is just determining which child of the node 
		 * should be called, and then decrementing the size 
		 * attribute of this node
		 */
		public Node delete(Point2D p) {
			if(p.get(cutDim) < this.cutVal) {
				this.left = this.left.delete(p);
			}
			else {
				this.right = this.right.delete(p);
			}
			
			this.size--; 
			return this; 
		}
		
		/*
		 * addCenter function to add a center to the internal 
		 * node, and it follows the procedure outlined in 
		 * the handout
		 */
		public Node addCenter(LPoint center) {
			LinkedList<LPoint> contenders = this.getContenders(); 
			double rMini = Double.MAX_VALUE; 
				
			for(LPoint p: contenders) {
				double bigR = this.cell.maxDistanceSq(p.getPoint2D()); 
				if(rMini > bigR) {
					rMini = bigR; 
				}
			}
			
			double cenMaxDis = this.cell.maxDistanceSq(center.getPoint2D()); 
			if(rMini > cenMaxDis) {
				rMini = cenMaxDis;
			}
			
			for(int i = 0; i < contenders.size(); i++) {
				double closeR = this.cell.distanceSq(contenders.get(i).getPoint2D()); 
				if(closeR > rMini) {
					contenders.remove(i); 
					i--; 
				}
			}
			double cenDis = this.cell.distanceSq(center.getPoint2D()); 
			
			if(cenDis <= rMini) {
				contenders.add(center);
				if(this.left != null) {
					this.left = this.left.addCenter(center);
				}
				
				if(this.right != null) {
					this.right = this.right.addCenter(center);
				}
			}
			
			return this;
		}
	}
	
	/*
	 * ExternalNode class, which has the necessary 
	 * attributes and methods for the data structure
	 */
	private class ExternalNode extends Node {
		/*
		 * this section contains the attributes, 
		 * constructor, and the necessary getters and 
		 * setters for the attribute. 
		 */
		LPoint point; 
		Rectangle2D cell; 
		int size; 
		LinkedList<LPoint> contenders;  
		
		public ExternalNode() {
			this.point = null; 
			this.contenders = new LinkedList<LPoint>(); 
		}
		
		public LPoint getPoint() {
			return point; 
		}
		
		public LinkedList<LPoint> getContenders() {
			return this.contenders; 
		}
		public int getSize() {
			return size;
		}
		
		public void setPoint(LPoint p) {
			this.point = p; 
		}
		
		public void setSize(int s) {
			this.size = s; 
		}
		
		public void addToList(LPoint p) {
			this.contenders.add(p);
		}
		
		public String toString() {
			if(point == null) {
				return "[null]"; 
			}
			else {
				return "[" + point.toString() + "]"; 
			}
		}
		
		/*
		 * toString that prints out the contenders list, 
		 * and if there are 11 or more contenders, the 
		 * first 10 are only printed 
		 */
		public String toStringWithCenters() {
			String ret = ""; 
			if(point == null) {
				ret += "[null] ";
			}
			else {
				ret += "[" + point.toString() + "] ";
			}
			
			LinkedList<LPoint> contenders = this.getContenders(); 
			Collections.sort(contenders, new byLabel());
			
			ret += "=> {"; 
			
			if(contenders.size() >= 11) {
				for(int i = 0; i < 10; i++) {
					ret += contenders.get(i).getLabel(); 
					if(i != 9) {
						ret += " "; 
					}
				}
				
				ret += "...";
			}
			else {
				for(int i = 0; i < contenders.size(); i++) {
					ret += contenders.get(i).getLabel(); 
					if(i != contenders.size()-1) {
						ret += " "; 
					}
				}
			}
			
			
			ret += "}"; 
			return ret; 
		}
		
		public void setCell(Rectangle2D c) {
			this.cell = c; 
		}
		
		/*
		 * the find method for the ExternalNode class, which 
		 * is just determining if the point for this node 
		 * matches the point in the argument, and if so 
		 * it returns the point, and if not it returns null 
		 */
		public LPoint find(Point2D q) {
			if(this.point == null) {return null;}
			if(q.getX() == point.getX() && q.getY() == point.getY()) {
				return point; 
			}
			
			return null; 
		}
		
		/*
		 * the insert function for the ExternalNode, first 
		 * checks if this is an empty node, and if so 
		 * it just adds the point that was passed from the 
		 * argument into the ArrayList, if not, then 
		 * it adds both the Node's point and the 
		 * point passed as an argument. At the end
		 * bulkCreate is called. 
		 */
		public Node insert(LPoint q) {
			ArrayList<LPoint> pts = new ArrayList<>(); 
			if(this.point == null) {
				pts.add(q); 
				return bulkCreate(pts, cell, this.getContenders()); 
			}
			else {
				pts.add(q); 
				pts.add(point); 
				return bulkCreate(pts, cell, this.getContenders()); 
			}
		}
		
		/*
		 * this function just passes an empty ExternalNode 
		 * because if it got to this point of the deletion 
		 * process, then this is the Node that is supposed 
		 * to be deleted, which is why no check is done. 
		 */
		public Node delete(Point2D p) {
			this.point = null; 
			return this; 
		}
		
		/*
		 * addCenter functions adds a center to 
		 * the contenders list by following the 
		 * procedure outlined in the handout
		 */
		public Node addCenter(LPoint center) {
			LinkedList<LPoint> contenders = this.getContenders();
			double rMini = Double.MAX_VALUE; 
				
			for(LPoint p: contenders) {
				double bigR = this.cell.maxDistanceSq(p.getPoint2D()); 
				if(rMini > bigR) {
					rMini = bigR; 
				}
			}
			
			double cenMaxDis = this.cell.maxDistanceSq(center.getPoint2D()); 
			if(rMini > cenMaxDis) {
				rMini = cenMaxDis;
			}
			
			for(int i = 0; i < contenders.size(); i++) {
				double closeR = this.cell.distanceSq(contenders.get(i).getPoint2D()); 
				if(closeR > rMini) {
					contenders.remove(i); 
					i--; 
				}
			}
			double cenDis = this.cell.distanceSq(center.getPoint2D()); 
			
			if(cenDis <= rMini) {
				contenders.add(center);
			}
			
			return this;
		}
	}
	
	/*
	 * this section is contains the constructor, and the 
	 * attributes required for the data structure
	 */
	private int numOfNodes; 
	private Node root; 
	private Rectangle2D cell; 
	private int rebuildOffset; 
	private int deleteCount; 
	private LPoint startCenter; 
	
	public SMkdTree(int rebuildOffset, Rectangle2D rootCell, LPoint startCenter) {
		this.root = new ExternalNode(); 
		this.rebuildOffset = rebuildOffset; 
		this.numOfNodes = 0; 
		this.root.setCell(rootCell); 
		this.cell = rootCell; 
		this.deleteCount = 0;
		this.root.addToList(startCenter);
		this.startCenter = startCenter; 
	}
	
	/*
	 * just sets the root cell to an empty ExternalNode
	 * and other attributes that need to be changed 
	 */
	public void clear() {
		this.root = new ExternalNode(); 
		this.root.setCell(this.cell);
		this.root.addToList(startCenter); 
		this.numOfNodes = 0; 
		this.deleteCount = 0; 
	}
	
	/*
	 * returns the numOfNodes attributes, which 
	 * keeps track of the size 
	 */
	public int size() {
		return numOfNodes; 
	}
	
	public int deleteCount() {
		return this.deleteCount; 
	}
	
	/*
	 * calls the find function for the root, and lets 
	 * the InternalNode and ExternalNode find methods
	 * handle the rest
	 */
	public LPoint find(Point2D q) {
		return root.find(q); 
	}
	
	public Node getRoot() {
		return this.root; 
	}
	
	
	/*
	 * this method first checks if the point entered is 
	 * disjoint from the cell or if it is already contained 
	 * in the tree, if so an Exception is thrown. If no Exception
	 * is thrown, then the root node's insert function is invoked, 
	 * after that is done, then the numOfNodes counter is incremented 
	 * and then the restruc helper method is invoked  
	 */
	public void insert(LPoint pt) throws Exception {
		if(!(cell.contains(pt.getPoint2D()))) {
			throw new Exception("Attempt to insert a point outside bounding box");
		}
		else if(find(pt.getPoint2D()) != null) {
			throw new Exception("Insertion of duplicate point"); 
		}
		else {
			this.root = this.root.insert(pt); 
			numOfNodes++; 
			this.root = restruc(this.root); 
		}
	}
	
	/*
	 * this is a helper method that goes down the tree, 
	 * and checks if any internal nodes fit the criteria 
	 * for restructuring, and if so the proper actions are taken
	 * and then the function returns, which should stop 
	 * the restructuring for that subtree 
	 */
	@SuppressWarnings("unchecked")
	private Node restruc(Node n) {
		if(n.getClass() == ExternalNode.class) {
			return n; 
		}
		else {
			InternalNode iN = (InternalNode)n; 
			if(iN.insertCt > (iN.size + rebuildOffset)/2) {
				ArrayList<LPoint> arr = new ArrayList<>(); 
				getPts(iN, arr); 
				iN = (InternalNode)bulkCreate(arr, iN.cell, iN.getContenders()); 
				return iN; 
			}
			
			iN.left = restruc(iN.left); 
			iN.right = restruc(iN.right); 
			return iN; 
		}
	}
	
	/*
	 * helper function to get all ExternalNodes that are 
	 * present in the "n" node's subtree
	 */
	@SuppressWarnings("unchecked")
	private void getPts(Node n, ArrayList<LPoint> pts) {
		if(n.getClass() == ExternalNode.class) {
			if(((ExternalNode)n).point != null) {
				pts.add(((ExternalNode)n).point);
			}
			return; 
		}
		else {
			InternalNode i = (InternalNode) n; 
			getPts(i.left, pts); 
			getPts(i.right, pts);
			return; 
		}
	}
	
	/*
	 * the delete function first checks if the node of interest
	 * exists in the tree. If it does not, then an Exception is thrown. 
	 * If it does exist, then the root's delete function is invoked, and
	 * after the node is successfully deleted, the necessary attributes 
	 * are changed. After that a check is done to see if a restructuring 
	 * is required, and if so the necessary actions are taken, and if not 
	 * nothing is done and the function finishes. 
	 */
	public void delete(Point2D pt) throws Exception {
		if(find(pt) == null) {
			throw new Exception("Deletion of nonexistent point");
		}
		else {
			this.root = this.root.delete(pt); 
			this.deleteCount++; 
			this.numOfNodes--; 
			if(this.deleteCount > this.numOfNodes) {
				ArrayList<LPoint> pts = new ArrayList<>(); 
				getPts(this.root, pts);
				this.root = bulkCreate(pts, this.cell, this.root.getContenders());
				this.deleteCount = 0; 
			}
		}
	}
	
	/*
	 * this function calls the listHelper function, which 
	 * actually does the job of gathering the nodes 
	 */
	public ArrayList<String> list() {
		ArrayList<String> ret = new ArrayList<>(); 
		listHelper(root, ret);
		return ret; 
	}
	
	/*
	 * returns an ArrayList of Strings that contains
	 * the toString of the nodes with the contenders 
	 */
	public ArrayList<String> listWithCenters() {
		ArrayList<String> ret = new ArrayList<>(); 
		listCenterHelper(root, ret); 
		return ret; 
	}
	
	/*
	 * helper function for traversing the kdTree and 
	 * getting the toString with centers of all the 
	 * nodes 
	 */
	@SuppressWarnings("unchecked")
	private void listCenterHelper(Node n, ArrayList<String> arr) {
		if(n.getClass() == ExternalNode.class) {
			arr.add(((ExternalNode)n).toStringWithCenters()); 
			return; 
		}
		else {
			InternalNode i = (InternalNode)n;
			arr.add(i.toStringWithCenters()); 
			listCenterHelper(i.right, arr); 
			listCenterHelper(i.left, arr); 
			return; 
		}
	}
	/*
	 * this function just recursively gathers the nodes in a 
	 * preorder right-left traversal fashion 
	 */
	@SuppressWarnings("unchecked")
	private void listHelper(Node n, ArrayList<String> arr) {
		if(n.getClass() == ExternalNode.class) {
			arr.add(((ExternalNode)n).toString());
			return;
		}
		else {
			InternalNode iN = (InternalNode)n;
			arr.add(iN.toString());
			listHelper(iN.right, arr);
			listHelper(iN.left, arr);
			return;
		}
	}
	
	/*
	 * this function just calls the helper, and returns what the helper returns. 
	 * The ArrayList argument of the helper is set to null since gathering the 
	 * nodes visited is not of interest for the purpose of this function 
	 */
	public LPoint nearestNeighbor(Point2D center) {
		return nNHelper(center, this.cell, this.root, null, null);
	}
	
	/*
	 * this helper function simply performs the standard nearest
	 * neighbor query procedure that was disucssed in lecture 
	 */
	@SuppressWarnings("unchecked")
	public LPoint nNHelper(Point2D qp, Rectangle2D cell, Node n, LPoint best, ArrayList<LPoint> holder) {
		if(n == null) {
			return best; 
		}
		else if(n.getClass() == ExternalNode.class) {
			if(((ExternalNode)n).point == null) {
				return best; 
			}
			
			/*
			 * this if statement is written to check if getting the 
			 * nodes visited is of interest 
			 */
			if(holder != null) {
				holder.add(((ExternalNode)n).point);
			}
			
			if(best == null) {
				best = ((ExternalNode)n).point; 
			}
			else {
				best = (best.getPoint2D().equals(qp.closerOf(((ExternalNode)n).point.getPoint2D(), best.getPoint2D()))) ? best : ((ExternalNode)n).point;
			}
			return best; 
		}
		else {
			InternalNode iN = (InternalNode)n; 
			if(qp.get(iN.cutDim) < iN.cutVal) {
				best = nNHelper(qp, cell.leftPart(iN.cutDim, iN.cutVal), iN.left, best, holder); 
				if(best == null || qp.distanceSq(best.getPoint2D()) >= cell.rightPart(iN.cutDim, iN.cutVal).distanceSq(qp)) {
					best = nNHelper(qp, cell.rightPart(iN.cutDim, iN.cutVal), iN.right, best, holder);
				}
				
				return best; 
			}
			else {
				best = nNHelper(qp, cell.rightPart(iN.cutDim, iN.cutVal), iN.right, best, holder);
				if(best == null || qp.distanceSq(best.getPoint2D()) >= cell.leftPart(iN.cutDim, iN.cutVal).distanceSq(qp)) {
					best = nNHelper(qp, cell.leftPart(iN.cutDim, iN.cutVal), iN.left, best, holder);
				}
				
				return best; 
			}
		}
	}
	
	/*
	 * add a new center to the kdTree
	 */
	public void addCenter(LPoint center) {
		this.root = this.root.addCenter(center);
	}
	
	/*
	 * this function calls the same helper method, but 
	 * sets the ArrayList argument to an actual ArrayList, since 
	 * that is the point of this function. After the helper is finished, 
	 * the ArrayList is sorted, and then returned. 
	 */
	public ArrayList<LPoint> nearestNeighborVisit(Point2D center) {
		ArrayList<LPoint> hold = new ArrayList<>(); 
		nNHelper(center, this.cell, this.root, null, hold); 
		Collections.sort(hold, new ByXThenY());
		return hold; 
	}
	
	/*
	 * this function traverses the kdTree and gets the 
	 * Assigned pair of all the sites 
	 */
	@SuppressWarnings("unchecked")
	public void listAssign(Node n, ArrayList<AssignedPair<LPoint>> arr) {
		if(n.getClass() == ExternalNode.class) {
			LinkedList<LPoint> contend = ((ExternalNode)n).getContenders(); 
			double rMin = Double.MAX_VALUE; 
			LPoint lowCent = null; 
			LPoint exPoint = ((ExternalNode)n).getPoint(); 
			
			if(exPoint != null) {
				for(LPoint pt : contend) {
					double dist = exPoint.getPoint2D().distanceSq(pt.getPoint2D()); 
					if(dist < rMin) {
						rMin = dist; 
						lowCent = pt; 
					}
					else if(dist == rMin) {
						if(lowCent.getX() != pt.getX()) {
							lowCent = (lowCent.getX() < pt.getX()) ? lowCent : pt; 
						}
						else {
							lowCent = (lowCent.getY() < pt.getY()) ? lowCent : pt; 
						}
					}
				}
				
				arr.add(new AssignedPair<LPoint>(((ExternalNode)n).getPoint(), lowCent, rMin)); 
			}
			
		}
		else {
			InternalNode i = (InternalNode)n; 
			listAssign(i.left, arr); 
			listAssign(i.right, arr);
		}
	}
	
	/*
	 * this function finds the assignedPair for a specific 
	 * target site 
	 */
	@SuppressWarnings("unchecked")
	public AssignedPair<LPoint> findAssignedPair(Node n, LPoint target) {
		if(n.getClass() == ExternalNode.class) {
			LinkedList<LPoint> contend = ((ExternalNode)n).getContenders(); 
			double rMin = Double.MAX_VALUE; 
			LPoint lowCent = null; 
			LPoint exPoint = ((ExternalNode)n).getPoint(); 
			
			
			for(LPoint pt : contend) {
				double dist = exPoint.getPoint2D().distanceSq(pt.getPoint2D()); 
				if(dist < rMin) {
					rMin = dist; 
					lowCent = pt; 
				}
			}
				
			return new AssignedPair<LPoint>(((ExternalNode)n).getPoint(), lowCent, rMin); 
		}
		else {
			InternalNode i = (InternalNode) n; 
			if(target.get(i.cutDim) < i.cutVal) {
				return findAssignedPair(i.left, target); 
			}
			else {
				return findAssignedPair(i.right, target);
			}
		}
	}
}

