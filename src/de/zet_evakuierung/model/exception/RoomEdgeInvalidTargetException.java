/* zet evacuation tool copyright (c) 2007-14 zet evacuation team
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

import de.zet_evakuierung.model.RoomEdge;
import java.io.IOException;

/**
 * This Exception has to be thrown, if a room edge is connected to another room edge on a different floor.
 */
@SuppressWarnings( "serial" )
public class RoomEdgeInvalidTargetException extends ValidationException {
	public RoomEdgeInvalidTargetException( RoomEdge invalidEdge ) {
		super( invalidEdge );
	}

	public RoomEdgeInvalidTargetException( RoomEdge invalidEdge, String s ) {
		super( invalidEdge, s );
	}

	public RoomEdge getInvalidEdge() {
		return (RoomEdge) getSource();
	}
	
	/** Prohibits serialization. */
	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException {
		throw new UnsupportedOperationException( "Serialization not supported" );
	}
}
