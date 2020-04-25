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
package de.zet_evakuierung.model.exception;

import de.zet_evakuierung.model.Room;
import java.io.IOException;


/**
 * Indicates that an error occurred in some room. Mostly this is because an
 * invalid polygon shape, e. g. if it only contains points at one coordinate.
 * @author Jan-Philipp Kappmeier
 */
@SuppressWarnings( "serial" )
public class InvalidRoomZModelError extends ZModelErrorException {
	private Room room;
	private StackTraceElement[] stack;


	public InvalidRoomZModelError( String message, Room room ) {
		super( message );
		this.room = room;
	}

	/**
	 * Returns an exception that was thrown.
	 * @return an exception that was thrown
	 */
	public Room getRoom() {
		return room;
	}
	
	/** Prohibits serialization. */
	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException {
		throw new UnsupportedOperationException( "Serialization not supported" );
	}
}
