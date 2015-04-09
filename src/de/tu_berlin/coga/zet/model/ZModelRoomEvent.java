
package de.tu_berlin.coga.zet.model;

import java.util.Collection;
import java.util.Collections;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class ZModelRoomEvent implements ZModelChangedEvent {
	Collection<Room> affectedRooms;

	public ZModelRoomEvent( Collection<Room> affectedRooms ) {
		this.affectedRooms = affectedRooms;
	}

	public Collection<Room> getAffectedRooms() {
		return Collections.unmodifiableCollection( affectedRooms );
	}
}
