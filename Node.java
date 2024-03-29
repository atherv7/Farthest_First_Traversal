package cmsc420_s23;

public class Node<Key extends Comparable<Key>, Value> {
	Locator loc; 
	Key k;
	Value v;
	Node parent, left, right; 
	int weight; 

	public Node(Key k, Value v, Node parent) {
		this.k = k; 
		this.v = v;
		this.parent = parent;
		this.weight = 1;
	}
}
