
package de.zet_evakuierung.model;

import java.util.List;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface AbstractFloor extends Iterable<Room> {
	public List<Room> getRooms();
	public String getName();
}
