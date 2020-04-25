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
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
@XStreamAlias("teleportArea")
public class TeleportArea extends AreaImpl implements Named {
	/** The evacuation area representing the exit that the persons this rooms should use. */
	private EvacuationArea exit;
	/** The evacuation area representing the exit that the persons this rooms should use. */
	private TeleportArea target;
 	@XStreamAsAttribute()
	/** The name of the {@code EvacuationArea}. */
	private String name = "TeleportArea";


	/**
	 * Constructs a new {@code TeleportArea}.
	 *
	 * @param room to which the area belongs
	 */
	public TeleportArea( RoomImpl room ) {
		super( room, AreaType.Teleport );
	}

	/**
	 * Returns the exit assigned to this {@code AssignmentArea}
	 * @return the assigned exit
	 */
	public EvacuationArea getExitArea() {
		return exit;
	}

	/**
	 * Sets the exit assigned to this {@code AssignmentArea}.
	 * @param exit the {@link EvacuationArea} representing the exit.
	 */
	public void setExitArea( EvacuationArea exit ) {
		this.exit = exit;
	}

	/**
	 * Returns the exit assigned to this {@code AssignmentArea}
	 * @return the assigned exit
	 */
	public TeleportArea getTargetArea() {
		return target;
	}

	/**
	 * Sets the exit assigned to this {@code AssignmentArea}.
	 * @param exit the {@link EvacuationArea} representing the exit.
	 */
	public void setTargetArea( TeleportArea target ) {
		this.target = target;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name.trim();
	}

	/**
	 * This method copies the current polygon without it's edges. Every other setting, as f.e. the floor
	 * for Rooms or the associated Room for Areas is kept as in the original polygon.
	 * @return the copy
	 */
	@Override
	protected PlanPolygon<PlanEdge> createPlainCopy () {
		final TeleportArea tp = new TeleportArea( getAssociatedRoom() );
		tp.setExitArea( exit );
		tp.setTargetArea( target );
		tp.setName( name );
		return tp;
	}
}
