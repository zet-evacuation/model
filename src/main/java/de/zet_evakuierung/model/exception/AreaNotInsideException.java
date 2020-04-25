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

import de.zet_evakuierung.model.Area;
import de.zet_evakuierung.model.Room;
import java.io.IOException;

/** Is thrown when an area is not located inside the room which is is associated
 * to.
 * @author Joscha Kulbatzki, Jan-Philipp Kappmeier
 */
@SuppressWarnings( "serial" )
public class AreaNotInsideException extends ValidationException {
	private final Area area;

	public AreaNotInsideException( Room room, Area area ) {
		this( room, area, "" );
	}

	public AreaNotInsideException( Room room, Area area, String s ) {
		super( room, s );
		this.area = area;
	}

	public Area getArea() {
		return area;
	}

	/**
	 * @return the room which contains an area that is not inside.
	 */
	@Override
	public Room getSource() {
		return (Room)source;
	}

	/** Prohibits serialization. */
	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException {
		throw new UnsupportedOperationException( "Serialization not supported" );
	}
}
