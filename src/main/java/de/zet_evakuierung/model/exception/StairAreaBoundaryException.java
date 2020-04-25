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

import de.zet_evakuierung.model.PlanEdge;
import de.zet_evakuierung.model.StairArea;
import java.io.IOException;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
@SuppressWarnings( "serial" )
public class StairAreaBoundaryException extends ZModelErrorException {
	private StairArea sa;
	private PlanEdge causing;
	public StairAreaBoundaryException( StairArea sa, PlanEdge causing ) {
		super( "Edge " + causing.toString() + " could not be set as upper or lower edge, because it overlaps with another lower/upper edge in stair " + sa  );
		this.sa = sa;
	}

	public StairAreaBoundaryException( String message, StairArea sa, PlanEdge causing ) {
		super( message );
		this.sa = sa;
	}

	public StairArea getStairArea() {
		return sa;
	}

	public PlanEdge getCausing() {
		return causing;
	}
	
	/** Prohibits serialization. */
	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException {
		throw new UnsupportedOperationException( "Serialization not supported" );
	}
}
