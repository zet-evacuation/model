
package de.zet_evakuierung.model;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface Room {

  public PlanPolygon<? extends RoomEdge> getPolygon();
	public String getName();


	// Introduced, but to delete in near future:
	/**
	 * Returns a view of all assignment areas.
	 * @return a list of all assignment areas
	 */
	public List<AssignmentArea> getAssignmentAreas();

	/**
	 * Returns the floor associated with this room.
	 * @return the associated floor of this room
	 */
	public Floor getAssociatedFloor();

	/**
	 * Returns a view of all barriers.
	 * @return the list of all barriers
	 */
	public List<Barrier> getBarriers();

	/**
	 * Returns a view of all delay areas.
	 * @return the list of all delay areas
	 */
	public List<DelayArea> getDelayAreas();

	/**
	 * Returns a view of all evacuation areas.
	 * @return the list of all delay areas
	 */
	public List<EvacuationArea> getEvacuationAreas();

	/**
	 * Returns a view of all inaccessible areas.
	 * @return the list of all inaccessible areas
	 */
	public List<InaccessibleArea> getInaccessibleAreas();

	/**
	 * Returns a view of the list of all save areas.
	 * @return the view of all save areas
	 */
	public List<SaveArea> getSaveAreas();

	/**
	 * Returns a view of the list of all stair areas.
	 * @return the view of all stair areas
	 */
	public List<StairArea> getStairAreas();

	/**
	 * Returns a view of the list of all teleport areas.
	 * @return the view of all teleport areas
	 */
	public List<TeleportArea> getTeleportAreas();

	/** @return All areas that are in the room. */
	public List<Area> getAreas();

  public Collection<Room> getNeighbors();

  // Temporary
    public HashMap<Point, Integer> getDoors();
    public int getLengthOfDoor( Room room );
    public Collection<PlanEdge> getDoorEdges();

}
