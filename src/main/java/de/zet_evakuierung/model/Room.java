/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package de.zet_evakuierung.model;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface Room extends Named {

    public PlanPolygon<? extends RoomEdge> getPolygon();

    @Override
    public String getName();

    // Introduced, but to delete in near future:
    /**
     * Returns a view of all assignment areas.
     *
     * @return a list of all assignment areas
     */
    public List<AssignmentArea> getAssignmentAreas();

    /**
     * Returns the floor associated with this room.
     *
     * @return the associated floor of this room
     */
    public FloorInterface getAssociatedFloor();

    /**
     * Returns a view of all barriers.
     *
     * @return the list of all barriers
     */
    public List<Barrier> getBarriers();

    /**
     * Returns a view of all delay areas.
     *
     * @return the list of all delay areas
     */
    public List<DelayArea> getDelayAreas();

    /**
     * Returns a view of all evacuation areas.
     *
     * @return the list of all delay areas
     */
    public List<EvacuationArea> getEvacuationAreas();

    /**
     * Returns a view of all inaccessible areas.
     *
     * @return the list of all inaccessible areas
     */
    public List<InaccessibleArea> getInaccessibleAreas();

    /**
     * Returns a view of the list of all save areas.
     *
     * @return the view of all save areas
     */
    public List<SaveArea> getSaveAreas();

    /**
     * Returns a view of the list of all stair areas.
     *
     * @return the view of all stair areas
     */
    public List<StairArea> getStairAreas();

    /**
     * Returns a view of the list of all teleport areas.
     *
     * @return the view of all teleport areas
     */
    public List<TeleportArea> getTeleportAreas();

    /**
     * @return All areas that are in the room.
     */
    public List<Area> getAreas();

    public Collection<Room> getNeighbors();

    // Temporary
    public HashMap<Point, Integer> getDoors();

    public int getLengthOfDoor(Room room);

    public Collection<PlanEdge> getDoorEdges();

}
