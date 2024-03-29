package cmsc420_s23;
import java.util.Comparator;

/*
 * Comparator class for sorting a site by its label 
 */
public class byLabel<LPoint extends LabeledPoint2D> implements Comparator<LPoint> {
	public int compare(LPoint pt1, LPoint pt2) {
		return pt1.getLabel().compareTo(pt2.getLabel()); 
	}
}
