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

package de.zet_evakuierung.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implements a single InaccessibleArea. This is an area, which can not be
 * entered.
 * @author Gordon Schlechter
 */
@XStreamAlias( "inaccessibleArea" )
public class InaccessibleArea extends AreaImpl {
	/**
	 * Creates a new instance of {@link InaccessibleArea } contained in a
	 * specified {@link de.zet_evakuierung.model.Room}.
	 * @param room the room
	 */
	InaccessibleArea( RoomImpl room ) {
		super( room, AreaType.Inaccessible );
	}

  InaccessibleArea( RoomImpl room, AreaType type ) {
    super( room, type );
  }

	/** This method copies the current polygon without it's edges. Every other setting, as f.e. the floor
	 * for Rooms or the associated Room for Areas is kept as in the original polygon.
   * @return  */
	@Override
	protected PlanPolygon<PlanEdge> createPlainCopy() {
		return new InaccessibleArea( getAssociatedRoom() );
	}

}
