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
/*
 * PolygonNotClosedException.java
 * Created on 30. November 2007, 15:14
 */

package de.zet_evakuierung.model.exception;

import de.zet_evakuierung.model.PlanPolygon;
import java.io.IOException;

/**
 * The exception {@code PolygonNotClosedException} indicates that a
 * polygon is not closed. You have to submit the polygon in which the error
 * occurs. That gives a possibility to tell the user where the error occured.
 * This exception is especially used by {@link de.zet_evakuierung.model.Room} and {@link de.zet_evakuierung.model.Area}.
 * @author Jan-Philipp Kappmeier
 */
@SuppressWarnings( "serial" )
public class PolygonNotClosedException extends ValidationException {
	
	/**
	 * Creates a new instance of {@code PolygonNotClosedException}. A {@link PlanPolygon} needs
	 * to be passed.
	 * @param polygon the polygon that caused this exception
	 */
	public PolygonNotClosedException( PlanPolygon<?> polygon ) {
		super( polygon );
	}

	/**
	 * Creates a new instance of {@code PolygonNotClosedException} that contains
	 * the errorous {@link PlanPolygon}.
	 * @param polygon the polygon that caused this exception
	 * @param s an additional information string
	 */
	public PolygonNotClosedException( PlanPolygon<?> polygon, String s ) {
		super( polygon, s );
	}
	
	/**
	 * Returns the not closed polygon.
	 * @return the instance of {@link PlanPolygon} that was the cause for this exception.
	 */
	public PlanPolygon<?> getPolygon () {
		return getSource();
	}
	
	/**
	 * @return the room which contains an area that is not inside.
	 */
	@Override
	public PlanPolygon<?> getSource() {
		return (PlanPolygon<?>)source;
	}

	/** Prohibits serialization. */
	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException {
		throw new UnsupportedOperationException( "Serialization not supported" );
	}
}