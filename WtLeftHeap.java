
package cmsc420_s23;

import java.util.ArrayList;

public class WtLeftHeap<Key extends Comparable<Key>, Value> {
	/*
	 * This is the private class for the Node object, it holds a 
	 * pointer to a locator, parent node, children node, 
	 * the weight of the node, and the key-value pair
	 */
	

	/*
	 * The Locator object class only has one attribute which is a 
	 * pointer to the node is supposed to locate 
	 */
	

	/*
	 * Two attributes, which is the root node for the leftist 
	 * heap data structure, and the total number of nodes in 
	 * the heap (numOfNodes), which getting the size of the 
	 * heap very easy 
	 */
	private Node root; 
	private int numOfNodes; 
	

	/*
	 * default constructor 
	 */
	public WtLeftHeap() {
		this.root = null;
		this.numOfNodes = 0; 
	}


	/*
	 * get the size of the heap by simply returning numOfNodes
	 */
	public int size() { 
		return numOfNodes; 
	}

	/*
	 * sets the root node to null, which essentially clears it, and 
	 * sets numOfNodes back to 0
	 */
	public void clear() { 
		this.root = null;
		this.numOfNodes = 0;  
	}

	/*
	 * insert function, so the user can enter another node into the heap
	 */
	public Locator insert(Key x, Value v) { 
		/*
		 * if this is the first entry in the heap, the function just simply
		 * changes the attributes of the root Node, increments numOfNodes, 
		 * and creates and properly initializes the locator with the root 
		 * node 
		 */
		if(this.numOfNodes == 0) {
			root = new Node(x, v, null); 
			Locator ret = new Locator(root); 
			root.loc = ret; 
			this.numOfNodes++; 
			return ret; 
		}
		/*
		 * if this is not the first entry into the heap, the function 
		 * creates another heap structure with the root equal to the entry 
		 * the user wants to insert into the heap, and call the mergeWith() 
		 * function on the two heap structures, which essentially inserts the 
		 * entry into the heap 
		 */
		WtLeftHeap<Key, Value> oth = new WtLeftHeap<>();
		oth.root = new Node(x, v, null);
		Locator ret = new Locator(oth.root); 
		oth.root.loc = ret; 
		this.mergeWith(oth);
		this.numOfNodes++;
		return ret; 
	}
	
	/*
	 * mergeWith() first checks if the LeftHeap in the argument is not 
	 * null and not the same tree as the current instance. Once that 
	 * check is passed it calls the helper function merge(), and finally 
	 * updates numOfNodes appropriately, and also clears the other 
	 * heap. 
	 */
	public void mergeWith(WtLeftHeap<Key, Value> h2) {
		if(h2 == null || this == h2) {return;}
		this.root = merge(this.root, h2.root);
		this.root.parent = null; 
		this.numOfNodes += h2.numOfNodes;
		h2.clear(); 
	}

	/*
	 * This is the merge() helper function that essentially merges
	 * two heaps together. It essentially follows the structure of 
	 * a normal leftist heap but with some minor changes for this 
	 * projects specification. 
	 */
	private Node merge(Node u, Node v) {
		if(v == null) {return u;}
		if(u == null) {return v;}
		/*
		 * this checks if the key value pair of u is less than 
		 * v, and if so it swaps them since this is a max-heap 
		 * rather than a min-heap. 
		 */
		if((u.k).compareTo(v.k) < 0) {
			Node temp = u;
			u = v;
			v = temp;
		}
		if(u.left == null) {
			/*
			 * sets v as the left child of the current node u, and 
			 * the weight of u is updated. Finally, the parent pointer 
			 * of v is set to u. 
			 */
			u.left = v;
			u.weight += v.weight;
			v.parent = u;
		}
		else {
			/*
			 * the reason u.right is not directly set equal to the recursive call is
			 * because we have to update the parent attribute of the node that is 
			 * returned from the recursive call.  
			 */
			Node merged = merge(u.right, v); 
			u.right = merged;
			merged.parent = u; 
			/*
			 * instead of checking for the npl, the statement checks 
			 * for the weight. Also after the if statement, the weight 
			 * of the current node, u, is calculated. 
			 */
			if(u.left.weight < u.right.weight) {
				Node temp = u.left;
				u.left = u.right;
				u.right = temp;
			}
			u.weight = 1;
			u.weight += (u.left != null) ? u.left.weight : 0;
			u.weight += (u.right != null) ? u.right.weight: 0;
		}
		return u;
	}

	/*
	 * removes the root node from the heap, and restructures the heap. 
	 * If the heap is empty, an exception is thrown, otherwise, it removes 
	 * the root node, saves its value (to be returned), and then calls the 
	 * merge function on the root's children, which essentially restructures
	 * it. 
	 */
	public Value extract() throws Exception {
		if(numOfNodes == 0) {
			throw new Exception("Extract from empty heap");
		}
		else {
			Value ret = (Value) this.root.v;
			this.root = merge(this.root.right, this.root.left);
			/*
			 * this is to ensure that there is nothing connected to the
			 * heap that is still pointing to the previous root node. 
			 */
			if(this.root != null) {this.root.parent = null;}
			this.numOfNodes--;
			return ret;
		}
	}

	/*
	 * updates the key of a node, and then restructures the heap if necessary. 
	 */
	public void updateKey(Locator loc, Key x) throws Exception {
		Node curr = loc.node;
		/*
		 * checks if the new key is greater than the current one, so we can differentiate 
		 * between needing to bubble up or bubble down the heap 
		 */
		if((curr.k).compareTo(x) < 0) {
			curr.k = x;
			
			/*
			 * since the new key is greater than the original one, we need to check if the 
			 * parent key is less than the new key since it is a max-heap. This loop runs until 
			 * either Node becomes the root node, or the node's parent's key is greater than its own. 
			 * It swaps the key and value of the nodes as well as their locators rather than moving the 
			 * entire object. 
			 */
			while(curr.parent != null && (curr.parent.k).compareTo(curr.k) < 0) {
				//saves the parent's key, value, and locator (as well as the curr locator) in a temp variables 
				Key temp = (Key) curr.parent.k;
				Value te = (Value) curr.parent.v;
				Locator parentLoc = curr.parent.loc; 
				Locator currLoc = curr.loc; 
				
				//switches the locators, so they point to the appropriate entry 
				curr.parent.loc = currLoc; 
				currLoc.node = curr.parent; 
				curr.loc = parentLoc; 
				parentLoc.node = curr; 
				
				//switches the key-value pair of the nodes 
				curr.parent.k = curr.k;
				curr.parent.v = curr.v;
				curr.k = temp;
				curr.v = te;
				
				curr = curr.parent;
			}

		}
		else {
			curr.k = x;
			/*
			 * since the new key is less than the original key, we need to check if the children 
			 * of the current node need to be swapped since this is max heap. The loop keeps 
			 * bubbling down the node until, the node does not have any children or if both of the
			 * children have a key less than the nodes key. 
			 */
			while((curr.left != null && (curr.k).compareTo(curr.left.k) < 0) || (curr.right != null && (curr.k).compareTo(curr.right.k) < 0)) {
				/*
				 * this if statement checks which child we should swap current node 
				 * since we are in the loop we know that the node has at least one child 
				 * whose key is higher than its own, and this if-statement helps us 
				 * determine which child it is. It performs the same actions as we did for the 
				 * parent in the above case, but for the child and the only different between
				 * the two blocks below is one is for the left child and the other is for the 
				 * right child. 
				 */
				if(curr.right == null || (curr.right.k).compareTo(curr.left.k) < 0) {
					Key temp = (Key) curr.left.k; 
					Value t = (Value) curr.left.v; 
					Locator childLoc = curr.left.loc; 
					Locator currLoc = curr.loc; 
					
					curr.left.loc = currLoc; 
					currLoc.node = curr.left; 
					curr.loc = childLoc; 
					childLoc.node = curr; 
					
					curr.left.k = curr.k; 
					curr.left.v = curr.v;
					
					curr.k = temp; 
					curr.v = t; 
					
					curr = curr.left; 
				}
				else {
					Key temp = (Key) curr.right.k; 
					Value t = (Value) curr.right.v; 
					Locator childLoc = curr.right.loc; 
					Locator currLoc = curr.loc; 
					
					curr.right.loc = currLoc; 
					currLoc.node = curr.right; 
					curr.loc = childLoc; 
					childLoc.node = curr; 
					
					curr.right.k = curr.k; 
					curr.right.v = curr.v; 
					
					curr.k = temp; 
					curr.v = t; 
					
					curr = curr.right; 
				}
			}
		}
	}

	/*
	 * checks if the heap is empty, and if it isn't 
	 * then it will return the key of the root. 
	 */
	public Key peekKey() {
		if(numOfNodes == 0) {
			return null; 
		}
		else {
			return (Key) this.root.k;
		}
	}

	/*
	 * checks if the heap is empty, and if it isn't 
	 * then it will return the value of the root. 
	 */
	public Value peekValue() {
		if(numOfNodes == 0) {
			return null;
		}
		else {
			return (Value) this.root.v;
		}
	}

	/*
	 * this function returns a list version of the heap. 
	 * it initializes an ArrayList, and then calls a helper 
	 * function and returns the ArrayList after the helper
	 * function finishes executing. 
	 */
	public ArrayList<String> list() {
		ArrayList<String> ret = new ArrayList<>();
		list_helper(this.root, ret);
		return ret;
	}

	/*
	 * this is a recursive helper function that does a right-to-left 
	 * preorder traversal, and saves the node attributes of interest 
	 * in the ArrayList. 
	 */
	private void list_helper(Node curr, ArrayList<String> accum) {
		if(curr == null) {
			accum.add("[]");
			return;
		}
		else {
			String add = "(" + curr.k + ", " + curr.v + ") [" + curr.weight + "]";
			accum.add(add); 
		}

		list_helper(curr.right, accum);
		list_helper(curr.left, accum);
	}

}