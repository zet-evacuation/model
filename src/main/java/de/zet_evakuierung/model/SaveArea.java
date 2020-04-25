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

package de.zet_evakuierung.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents a SaveArea.
 * A SaveArea is an area, where the evacuees are save, but they have still an
 * influence on other evacuees. Every SaveArea is associated to exactly one
 * {@link Room} at every time.
 */
@XStreamAlias( "saveArea" )
public class SaveArea extends AreaImpl {
	/**
	 * Creates a new instance of {@link SaveArea }.
	 * Sets room.
	 * @param room to which the area belongs.
	 */
	SaveArea( RoomImpl room ) {
		super( room, AreaType.Save );
	}

  protected SaveArea( RoomImpl associatedRoom, AreaType areaType ) {
    super( associatedRoom, areaType );
  }

  /** This method copies the current polygon without it's edges. Every other setting, as f.e. the floor
	 * for Rooms or the associated Room for Areas is kept as in the original polygon.
   * @return  */
	@Override
	protected PlanPolygon<PlanEdge> createPlainCopy() {
		return new SaveArea( getAssociatedRoom() );
	}
}
