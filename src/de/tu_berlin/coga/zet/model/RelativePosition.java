
package de.tu_berlin.coga.zet.model;

/**
 * Enumeration used by the relative position test. Specifies if a
 * polygon is left or right from the border.
 * @see PlanPolygon#relativePolygonPosition(de.tu_berlin.coga.zet.model.PlanEdge, de.tu_berlin.coga.zet.model.RelativePosition) 
 * @author Jan-Philipp Kappmeier
 */
public enum RelativePosition {
	/**Checks wheater the room is on the right side of an edge. */
	Right,
	/** Checks wheather the room is on the left side of an edge. */
	Left;
}
