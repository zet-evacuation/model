package de.zet_evakuierung.model;

import java.awt.Rectangle;
import java.util.List;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface FloorInterface extends Iterable<Room>, Named {

    /**
     * Returns a list of all rooms that lie on the floor.
     * @return a list of all rooms that lie on the floor
     */
    public List<Room> getRooms();

    /**
     * The name of the floor.
     * @return 
     */
    @Override
    public String getName();

    /**
     * The number of rooms that lie on the floor.
     * @return 
     */
    public int roomCount();
    
    public Rectangle getLocation();
}
