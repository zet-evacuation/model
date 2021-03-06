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
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.zetool.common.localization.LocalizationManager;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
@XStreamAlias( "area" )
abstract class AreaImpl extends PlanPolygon<PlanEdge> implements Area {
	private RoomImpl associatedRoom;
  @XStreamOmitField
  private final AreaType areaType;

	AreaImpl( RoomImpl associatedRoom, AreaType areaType ) {
		super( PlanEdge.class );
    this.areaType = areaType;
		setAssociatedRoom( associatedRoom );
	}

	/**
	 * Deletes the area.
	 */
	@Override
	void delete() {
		getAssociatedRoom().deleteArea( this );
		associatedRoom = null;

		// SHORT NOTE: In constructor: Call superconstruktor at the begin
		//             In  destructor: Call  superdestruktor at the end
		// MORE DETAIL:
		// super.delete must be called at last, because otherwise the polygon
		// will have been deleted at the time when you want to tell the associated
		// room that you want to delete this area. this will lead to an exception,
		// because Room.deleteArea() uses the equals method to determine which
		// area it must delete. as we would have deleted the polygon edges before
		// the call to Room.deleteArea() this equals method will throw a
		// null pointer exception.
		super.delete();
	}

	/**
	 * Returns the associatedRoom of the area.
	 * @return associatedRoom of the area.
	 */
	@Override
	public RoomImpl getAssociatedRoom() {
		return associatedRoom;
	}

	/**
	 * This operation takes care of setting the room that is associated to this
	 * area. It also conserves the consistence with the area lists in the rooms.
	 * @param room is the associated room of the area.
	 * @throws java.lang.IllegalArgumentException when room is {@code null}.
	 */
	final void setAssociatedRoom( RoomImpl room ) throws IllegalArgumentException {
		if( room == null )
			throw new IllegalArgumentException( LocalizationManager.getManager().getLocalization( ZLocalization.ZET_LOCALIZATION ).getString( "ds.z.Area.NoRoomException" ) );

		if(associatedRoom != null )
			associatedRoom.deleteArea( this );
		associatedRoom = room;
		associatedRoom.addArea( this );
	}

	@Override
	public boolean equals( Object o ) {
		if( o instanceof AreaImpl ) {
			AreaImpl p = (AreaImpl)o;
			return super.equals( p ) && ( (associatedRoom == null ) ? p.getAssociatedRoom() == null : associatedRoom.equals( (Room)p.getAssociatedRoom() ) );
		} else
			return false;
	}

  @Override
  public PlanPolygon<PlanEdge> getPolygon() {
    return this;
  }

  @Override
  public final AreaType getAreaType() {
    return areaType;
  }
}
