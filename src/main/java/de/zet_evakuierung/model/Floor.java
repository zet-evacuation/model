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
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import static org.zetool.common.util.Helper.in;
import de.zet_evakuierung.model.exception.AreaNotInsideException;
import de.zet_evakuierung.model.exception.InvalidRoomZModelError;
import de.zet_evakuierung.model.exception.PolygonNotClosedException;
import de.zet_evakuierung.model.exception.RoomIntersectException;
import de.zet_evakuierung.model.exception.TeleportEdgeInvalidTargetException;
import de.zet_evakuierung.model.exception.UnknownZModelError;
import de.zet_evakuierung.io.z.FloorConverter;
import de.zet_evakuierung.io.z.XMLConverter;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * A {@code Floor} is a plane that can contain {@link Room}-objects. It is
 * generally not allowed that the rooms (which are basically polygons) intersect
 * each other. In fact, it is possible to create such a not allowed state. It is
 * recommended to check whether a {@code Floor} is valid, or not before
 * using it for critical operations.
 * @see Floor#check( boolean )
 * @see PlanPolygon
 */
@XStreamAlias( "floor" )
@XMLConverter( FloorConverter.class )
public class Floor implements Serializable, Cloneable, FloorInterface, Named {
	/** The name of the floor. */
	@XStreamAsAttribute()
	private String name;
	/** A list of all rooms contained in the floor. */
	private ArrayList<RoomImpl> rooms;
	/** In the past this was intended to be a list of all single edges contained in the floor.
	 * This concept was never used and now this field is only still here because every example file
	 * has this field. */
	// TODO Delete this field and adjust all example files
	@XStreamOmitField
	private ArrayList<PlanEdge> edges;

	/** The leftmost point coordinate of the {@code Floor}. */
	@XStreamAsAttribute()
	private int xOffset = 0;
	/** The uppermost point coordinate of the {@code Floor}. */
	@XStreamAsAttribute()
	private int yOffset = 0;
	/** The difference between the left- and rightmost point coordinate of the {@code Floor}. */
	@XStreamAsAttribute()
	private int width = 0;
	/** The difference between the upper- and lowermost point coordinate of the {@code Floor}. */
	@XStreamAsAttribute()
	private int height = 0;


	/** The Room that has the minimum x value (xOffset). */
	@XStreamOmitField
	private RoomImpl minX_DefiningRoom;
	/** The Room that has the minimum y value (yOffset). */
	@XStreamOmitField
	private RoomImpl minY_DefiningRoom;
	/** The Room that has the maximum x value (xOffset + width). */
	@XStreamOmitField
	private RoomImpl maxX_DefiningRoom;
	/** The Room that has the maximum y value (yOffset + width). */
	@XStreamOmitField
	private RoomImpl maxY_DefiningRoom;

	/**
	 * Creates a new empty instance of {@code Floor} with the name "NewFloor".
	 */
	public Floor() {
		this ("NewFloor");
	}

	/**
	 * Creates a new empty instance of {@code Floor} with the indicated name.
	 * @param name the name of the floor
	 */
	public Floor( String name ) {
		this.name = name;
		rooms = new ArrayList<>();
	}

	/**
	 * To be called before the floor is finally deleted.
	 */
	void delete () {
		while( !rooms.isEmpty() )
			rooms.get( 0 ).delete();
	}

	/**
	 * Adds a room to the floor and sets this floor as the associated floor of the room to be added.
	 * @param room the room to be added
	 * @throws IllegalArgumentException if the room already exists on this floor
	 */
	void addRoom( RoomImpl room ) throws IllegalArgumentException {
		try {
			if( rooms.contains( room ) )
				throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.RoomAlreadyExistsException" ) + ": " + room.getName() );
			rooms.add( room );
			Collections.sort( rooms );
		} catch( IllegalArgumentException ex ) {
			throw ex;
		}
	}

	/**
	 * Returns the number of rooms on the floor.
	 * @return the number of rooms on the floor
	 */
	public int roomCount() {
		return rooms.size();
	}

	/**
	 * Removes a room from this floor and deletes the associated floor of the removed room.
	 * @param room the room to be removed
	 * @throws IllegalArgumentException if the given room is not associated with this floor
	 */
	public void deleteRoom( Room room ) throws IllegalArgumentException {
		if( !( rooms.contains( room ) ) )
			throw new IllegalArgumentException(ZLocalization.loc.getString("ds.z.NoRoomException"));
		else {
			rooms.remove( room );
			roomDeleteHandler (room);
		}
	}

	/**
	 * Checks, whether a floor is a valid floor. It is, if all his associated rooms are valid and none of
	 * his rooms intersects.
	 * @throws PolygonNotClosedException if one of the rooms on this floor is not closed
	 * @throws AreaNotInsideException if one of the areas of one room lies not completely inside that room
	 * @throws RoomIntersectException if some rooms on this floor intersects
	 * @throws TeleportEdgeInvalidTargetException if the target of the teleport edge is on the same floor as the start
	 * @param rasterized Indicates, if the BuildingPlan should be rasterized.
	 */
	public void check( boolean rasterized ) throws PolygonNotClosedException, AreaNotInsideException, RoomIntersectException, TeleportEdgeInvalidTargetException {
		for( RoomImpl room : rooms ) {
			room.check( rasterized );
			// Check floors using direct access as rooms is an ArrayList
			for( int i = 0; i < rooms.size(); i++ )
				for( int j = i + 1; j < rooms.size(); j++ ) {

					//PlanPoint intersection = rooms.get( i ).intersection( rooms.get( j ) );
					PlanPoint intersection = rooms.get( i ).intersectionStrict( rooms.get( j ) );
					if( intersection != null )
						throw new RoomIntersectException( rooms.get( i ), rooms.get( j ), intersection );
				}
		}
	}

        // Floors should in general not be equal if they just have the same name!
        // The property that each floor can only occur once by a given name should be maintained
        // by other means that equals method.
//	/**
//	 * Tests, if this floor equals the given floor f. Two floors are equal, if
//	 * they have the same name.
//	 * @param p the floor to be tested for equality
//	 * @return true if the two floors are equal
//	 */
//	public boolean equals( Floor p ) {
//		return p.name.equals( name );
//	}
//
//	/**
//	 * Tests, if this floor equals the given object. First assures that the object is an instance of the floor class.
//	 * Returns false if the object is not a floor. Two floors are equal, if they have the same name.
//	 * @param o the object that is to be tested for equality
//	 * @return
//	 */
//	@Override
//	public boolean equals( Object o ) {
//		return o instanceof Floor ? this.equals( (Floor)o) : false;
//	}

	/**
	 * Returns the height of the {@code Floor}. That is the difference between the
	 * uppermost and lowermost y-coordinates of contained rooms.
	 * @return the height
	 */
	public final int getHeight() {
		return height;
	}

    /**
     * Returns the name of the floor.
     *
     * @return the name of the floor
     */
    @Override
    public final String getName() {
        return name;
    }

	/**
	 * Returns the width of the {@code Floor}. That is the difference between the
	 * leftmost and rightmost x-coordinates of contained rooms.
	 * @return the width
	 */
	public final int getWidth() {
		return width;
	}

	/**
	 * The leftmost point coordinate of the {@code Floor}.
	 * @return the leftmost coordinate
	 */
	public final int getxOffset() {
		return this.xOffset;
	}

	/**
	 * The uppermost point coordinate of the {@code Floor}.
	 * @return the uppermost coordinate
	 */
	public final int getyOffset() {
		return this.yOffset;
	}

	/**
	 * Returns a view of the list of rooms on this floor.
	 * @return a list of rooms
	 */
	public List<Room> getRooms() {
		return Collections.unmodifiableList( rooms );
	}

	/**
	 * Creates a new room name of the form "Room #" where # is a number. If
	 * possible, the number equals the number of rooms, such that rooms are
	 * created in the order "Room 0, Room 1, ...". If the desired name is already
	 * in use, the room with the lowest possible number is returned.
	 * @return a possible name for a room on this floor
	 */
	public String getNewRoomName() {
		int number = rooms.size();
		String newName;
		start:
		do {
			newName = "Room " + Integer.toString( number );
			for( Room room : rooms )
				if( room.getName().equals( newName ) ) {
					number++;
					continue start;
				}
			break;
		} while( true );
		return newName;
	}

	/**
	 * Renames the floor. Must be taken care off that the floor can only takes a
	 * name that has not been given to another floor.
	 * @param val the new name of the floor
     */
    void setName(String val) {
        name = val;
    }

	/**
	 * Returns the bounding box of this {@code Floor}. The bounding box
	 * is the smallest {@link java.awt.Rectangle} that completely contains the
	 * whole Floor. The calculation of this bounding box is accurate in the
	 * integer coordinates of millimeter positions.
	 * @return a rectangle that defines the bounds
	 */
	public Rectangle bounds () {
		return new Rectangle (xOffset, yOffset, width, height);
	}

	/**
	 * This helper method updates the values returned by {@link #bounds()} after
	 * an {@link de.zet_evakuierung.model.Room} has been deleted.
	 *
	 * @param r An Room that was deleted.
	 *
	 * @see #roomChangeHandler
	 */
	private void roomDeleteHandler (Room r) {
		// Update of offsets, width and height

		// The only thing that can happen due to the deletion of a room is that
		// bounds may shrink. We must test if the deleted room defined any bound
		// and replace it if that is true.

		// The minimum values are updates first, because they are used in the
		// computations that are performed during the updates of the maximums

		// Updates of the minimums
		if (r == minX_DefiningRoom) {
			// Init with feasible data
			minX_DefiningRoom = rooms.isEmpty () ? null : rooms.get (0);
			if (minX_DefiningRoom != null) {
				xOffset = minX_DefiningRoom.boundLeft();

				// Scan all edges
				int value;
				for (RoomImpl scanEdge : rooms) {
					value = scanEdge.boundLeft();

					if (value < xOffset) {
						xOffset = value;
						minX_DefiningRoom = scanEdge;
					}
				}
			}
		}
		if (r == minY_DefiningRoom) {
			// Init with feasible data
			minY_DefiningRoom = rooms.isEmpty () ? null : rooms.get (0);
			if (minY_DefiningRoom != null) {
				yOffset = minY_DefiningRoom.boundUpper ();

				// Scan all edges
				int value;
				for (RoomImpl scanEdge : rooms) {
					value = scanEdge.boundUpper ();

					if (value < yOffset) {
						yOffset = value;
						minY_DefiningRoom = scanEdge;
					}
				}
			}
		}

		// Update of the maximums
		if (r == maxX_DefiningRoom) {
			// Init with feasible data
			maxX_DefiningRoom = rooms.isEmpty () ? null : rooms.get (0);
			if (maxX_DefiningRoom != null) {
				// No Math.abs needed - r.boundRight is always bigger than xOffset
				width = maxX_DefiningRoom.boundRight () - xOffset;

				// Scan all edges
				int value;
				for (RoomImpl scanEdge : rooms) {
					value = scanEdge.boundRight () - xOffset;

					if (value > width) {
						width = value;
						maxX_DefiningRoom = scanEdge;
					}
				}
			}
		}
		if (r == maxY_DefiningRoom) {
			// Init with feasible data
			maxY_DefiningRoom = rooms.isEmpty () ? null : rooms.get (0);
			if (maxY_DefiningRoom != null) {
				// No Math.abs needed - r.boundLower is always bigger than yOffset
				height = maxY_DefiningRoom.boundLower () - yOffset;

				// Scan all edges
				int value;
				for (RoomImpl scanEdge : rooms) {
					value = scanEdge.boundLower () - yOffset;

					if (value > height) {
						height = value;
						maxY_DefiningRoom = scanEdge;
					}
				}
			}
		}
	}

	/** Recomputes all bounds from scratch.
	 *
	 * This method is not intended to be used by any other class except
	 * for io.z.FloorConverter for legacy support of old example files.
	 *
	 * @param holdLastSize does not decrease the size of the floor, if the new
	 * bounds are smaller, than the old ones, if set to {@code true}
	 */
	public void recomputeBounds ( boolean holdLastSize ) {
		minX_DefiningRoom = null;
		minY_DefiningRoom = null;
		maxX_DefiningRoom = null;
		maxY_DefiningRoom = null;

		int minX = 0;
		int minY = 0;
		int maxX = 0;
		int maxY = 0;

		for (RoomImpl r : rooms) {
			if (r.boundLeft () < minX) {
				minX = r.boundLeft ();
				minX_DefiningRoom = r;
			}
			if (r.boundUpper () < minY) {
				minY = r.boundUpper ();
				minY_DefiningRoom = r;
			}
			if (r.boundRight () > maxX) {
				maxX = r.boundRight ();
				maxX_DefiningRoom = r;
			}
			if (r.boundLower () > maxY) {
				maxY = r.boundLower ();
				maxY_DefiningRoom = r;
			}
		}

		xOffset = holdLastSize ? Math.min( minX, xOffset ) : minX;
		yOffset = holdLastSize ? Math.min( minY, yOffset ) : minY;
		width = holdLastSize ? Math.max( maxX - minX, width ) + Math.max( minX - xOffset , 0 ): maxX - minX;
		height = holdLastSize ? Math.max( maxY - minY, height ) + Math.max( minY - yOffset, 0 ) : maxY - minY;
	}

	/**
	 * Sets a minimum size that this floor should have.
	 * @param xOffset the x-coordinate of the left upper corner
	 * @param yOffset the y-coordinate of the left upper corner
	 * @param width the width of the usable area
	 * @param height the height of the usable area
	 */
	public void setMinimumSize( int xOffset, int yOffset, int width, int height ) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.width = width;
		this.height = height;
		recomputeBounds( true );
	}

	/**
	 * Indicates whether this is a {@code Floor} that was loaded from a legacy file.
	 * @return {@code true} if the squared bounding box is known, {@code false} otherwise
	 */
//	public boolean boundStructureAvailable () {
//		return minX_DefiningRoom != null;
//	}

	/**
	 * Returns a copy of the {@code Floor}, but deletes all {@link AssignmentArea} objects
	 * as they would most likely refer to assignments that do not exist.
	 * @throws UnknownZModelError if an unexpected error occurred. This usually means that something in the model is incorrect.
	 * @throws InvalidRoomZModelError if a room contains to much points at the same position. These would be deleted during copy process.
	 * @return a copy of the floor
	 */
	@Override
	public Floor clone() throws UnknownZModelError, InvalidRoomZModelError {
		String roomName = "";
    RoomImpl clonedRoom = null;
		Floor deepCopy = new Floor( this.name );
		try {
			HashMap<RoomImpl,RoomImpl> m = new HashMap<>();

			for( RoomImpl r : rooms ) {
				RoomImpl newRoom = new RoomImpl( deepCopy, r.getName() );
				roomName = r.getName();
        clonedRoom = r;
        System.out.println( "Clonging " + roomName );
				int createdEdges = newRoom.defineByPoints( PlanPoint.pointCopy( r.getBorderPlanPoints() ) );

				if( createdEdges <= 1 )
					throw new InvalidRoomZModelError( "Error copying Room " + r.getName() + ". \n The number of edges was too low after eliminating points at the same place.", r );

				// Reconnect the rooms
				m.put( r, newRoom );
				for( RoomEdge e : in(r.edgeIterator()) ) {
					if( e.isPassable() ) {
						RoomImpl connectedRoom = (RoomImpl)e.getLinkTarget().getRoom();
						if( m.containsKey( connectedRoom ) ) {
							RoomImpl connectToRoom = m.get( connectedRoom );
							newRoom.connectTo( connectToRoom, e );
						}
					}
				}

				// Recreate the areas: Inaccessible, Save, Evacuation, Delay, Stair
				// _NOT_ AssignmentArea
				for( Barrier t : r.getBarriers() ) {
					Barrier b = new Barrier( newRoom );
					if( b.defineByPoints( PlanPoint.pointCopy( t.getPlanPoints() ) ) <= 1 )
						throw new InvalidRoomZModelError( "Error copying Room " + r.getName() + ". \n The number of edges in a barrier was too low after eliminating points.", r );
				}
				for( DelayArea t : r.getDelayAreas() ) {
					DelayArea d = new DelayArea( newRoom, t.getDelayType(), t.getSpeedFactor() );
					try { if( d.defineByPoints( PlanPoint.pointCopy ( t.getPlanPoints() ) ) <= 1 )
						;//throw new InvalidRoomZModelError( "Error copying Room " + r.getName() + ". \n The number of edges in a delay area was too low after eliminating points.", r );
          } catch (Exception ex) {
            System.err.println( "Failure with fucked up delay area!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
          }
				}
				for( InaccessibleArea t : r.getInaccessibleAreas() ) {
					InaccessibleArea i = new InaccessibleArea( newRoom );
					if( i.defineByPoints( PlanPoint.pointCopy ( t.getPlanPoints() ) ) <= 1 )
						throw new InvalidRoomZModelError( "Error copying Room " + r.getName() + ". \n The number of edges in an inaccessible area was too low after eliminating points.", r );
				}
				for( SaveArea t : r.getSaveAreas() ) { // Evacuation areas are contained!
					if( !(t instanceof EvacuationArea) ) {
						SaveArea s = new SaveArea( newRoom );
						if( s.defineByPoints( PlanPoint.pointCopy( t.getPlanPoints() ) ) <= 1 )
							throw new InvalidRoomZModelError( "Error copying Room " + r.getName() + ". \n The number of edges in a save area was too low after eliminating points.", r );
					}
				}
				for( TeleportArea t : r.getTeleportAreas() ) {
          throw new UnsupportedOperationException( "Teleport Area cloning not supported!" );
				}
				for( EvacuationArea t : r.getEvacuationAreas() ) {
					EvacuationArea e = new EvacuationArea( newRoom, t.getAttractivity(), t.getName() );
					if( e.defineByPoints( PlanPoint.pointCopy( t.getPlanPoints() ) ) <= 1 )
						throw new InvalidRoomZModelError( "Error copying Room " + r.getName() + ". \n The number of edges in an evacuation area was too low after eliminating points.", r );
				}
				for( StairArea t : r.getStairAreas() ) {
					StairArea s = new StairArea( newRoom );
					List<PlanPoint> points = PlanPoint.pointCopy( t.getPlanPoints() );
					if( s.defineByPoints( points ) <= 1 )
						throw new InvalidRoomZModelError( "Error copying Room " + r.getName() + ". \n The number of edges in a stair area was too low after eliminating points.", r );

					PlanPoint lowerStart = null;
					PlanPoint lowerEnd = null;
					PlanPoint upperStart = null;
					PlanPoint upperEnd = null;
					for( PlanPoint p : s.getPlanPoints() ) {
						if( p.x == t.getLowerLevelStart().x && p.y == t.getLowerLevelStart().y )
							lowerStart = p;
						if( p.x == t.getLowerLevelEnd().x && p.y == t.getLowerLevelEnd().y )
							lowerEnd = p;
						if( p.x == t.getUpperLevelStart().x && p.y == t.getUpperLevelStart().y )
							upperStart = p;
						if( p.x == t.getUpperLevelEnd().x && p.y == t.getUpperLevelEnd().y )
							upperEnd = p;
					}
					s.setLowerLevel( lowerStart, lowerEnd );
					s.setUpperLevel( upperStart, upperEnd );
					s.setSpeedFactorDown( t.getSpeedFactorDown() );
					s.setSpeedFactorUp( t.getSpeedFactorUp() );
				}
			}
		} catch( InvalidRoomZModelError ex ) {
			throw ex;
		} catch ( Exception ex ) {
      System.err.println( ex );
      ex.printStackTrace( System.err );
      System.err.println( clonedRoom );
      System.err.println( "Points: " );
      System.err.println( PlanPoint.pointCopy( clonedRoom.getBorderPlanPoints() ) );
			throw new UnknownZModelError( "Unexpected error during copying. Try to check the model. \n The failure occured copying the room '" + roomName + "'", ex );
		}
		return deepCopy;
	}

	/**
	 * Returns an iterator over all rooms contained in this floor.
	 * @return an iterator over all rooms contained in this floor
	 */
	@Override
	public Iterator<Room> iterator() {
		return this.getRooms().iterator();
	}

	/**
	 * Returns the name of the floor as string representation.
	 * @return the name of the floor as string representation
	 */
	@Override
	public String toString() {
		return this.getName();
	}

	StringBuilder summaryBuilder() {
		StringBuilder sb = new StringBuilder();

		sb.append( "Floor: " ).append( name ).append( ", " ).append( rooms.size() ).append( " Räume.\n");
		for( RoomImpl room : rooms ) {
			sb.append( room.summaryBuilder() );
			sb.append( '\n' );
		}

		return sb;
	}

	public String summary() {
		return summaryBuilder().toString();
	}

    @Override
    public Rectangle getLocation() {
        return new Rectangle(getxOffset(), getyOffset(), getWidth(), getHeight());
    }
}
