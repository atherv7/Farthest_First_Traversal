package cmsc420_s23;

public class AssignedPair<LPoint extends LabeledPoint2D> implements Comparable<AssignedPair<LPoint>> {
	private LPoint site; // a site
	private LPoint center; // its assigned center
	private double distanceSq; // the squared distance between them
	
	public AssignedPair(LPoint s, LPoint c, double dS) {
		site = s; 
		center = c; 
		distanceSq = dS; 
	}
	
	
	public int compareTo(AssignedPair<LPoint> o) {
		if(distanceSq != o.distanceSq) {
			Double tDis = (Double)distanceSq; 
			Double othDis = (Double)o.distanceSq; 
			return tDis.compareTo(othDis);
		}
		else {
			if(site.getX() != o.site.getX()) {
				return ((int)(site.getX() - o.site.getX()));
			}
			else {
				return ((int)(site.getY() - o.site.getY()));
			}
		}
	} // for sorting
	
	public String toString() {
		String ret = "["; 
		
		ret += site.getLabel(); 
		ret += "->"; 
		ret += center.getLabel(); 
		ret += "] distSq = "; 
		ret += distanceSq; 
		
		return ret; 
	}
	
	public double getDistance() {
		return distanceSq; 
	}
	
	public LPoint getSite() {
		return site; 
	}
	
	public LPoint getCenter() {
		return center; 
	}
	
	public void setDistance(double d) {
		distanceSq = d;
	}
	
	public void setCenter(LPoint c) {
		center = c; 
	}
}
