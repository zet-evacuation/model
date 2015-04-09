
package de.tu_berlin.coga.zet.model;

import java.util.LinkedList;


/**
 * Event that is thrown if a single area is created or edited.
 * @author Jan-Philipp Kappmeier
 */
public class ZModelAreaEvent extends ZModelRoomEvent {
	private Area affectedArea;
	private Room affectedRoom;
	public ZModelAreaEvent( final Room affectedRoom, Area affectedArea ) {
		super( new LinkedList<Room>() {{ add( affectedRoom ); }} );
		this.affectedArea = affectedArea;
		this.affectedRoom = affectedRoom;
	}

	public Room getAffectedRoom() {
		return affectedRoom;
	}

	public Area getAffectedArea() {
		return affectedArea;
	}
}
