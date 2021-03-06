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
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.zetool.common.datastructure.Tuple;
import de.zet_evakuierung.model.exception.AreaNotInsideException;
import de.zet_evakuierung.model.exception.PolygonNotClosedException;
import de.zet_evakuierung.model.exception.RoomIntersectException;
import de.zet_evakuierung.model.exception.TooManyPeopleException;
import de.zet_evakuierung.io.z.BuildingPlanConverter;
import de.zet_evakuierung.io.z.XMLConverter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * The {@code BuildingPlan} represents a complete building plan, consisting of
 * some floors which contains rooms themselves.
 * <p> Each plan is supposed to have at least two floors. One floor containing the rooms of
 * the building and another one which is the special outside floor. This outside
 * floor is used to automatically create rooms containing an {@link EvacuationArea}
 * to rescue people. </p>
 */
@XStreamAlias("buildingPlan")
@XMLConverter(BuildingPlanConverter.class)
public class BuildingPlan implements Serializable, Iterable<Floor> {
	/** A list of all floors of the plan. */
	@XStreamImplicit()
	private ArrayList<Floor> floors;
	/** Indicates, if the BuildingPlan is rastered, or at least should be. */
	private boolean rasterized;
	/** Static variable that stores the default-value for the raster size in meter. */
	public static double rasterSize = 0.4;
  /** Transformation matrix that flips vertically (mirrors at the {@code x}-axis. */
	public static final int[][] flipXAxis = {{1, 0}, {0, -1}};
  /** Transformation matrix that flips horizontally (mirrors at the {@code y}-axis. */
	public static final int[][] flipYAxis = {{-1, 0}, {0, 1}};
  /** Transformation matrix that mirrors at the main diagonal. */
	public static final int[][] flipMainDiagonal = {{0, 1}, {1, 0}};
  /** Transformation matrix that does nothing. */
	public static final int[][] identity = {{1, 0}, {0, 1}};

	/**
	 * Creates a new instance of {@code BuildingPlan} that is not rastered
	 * by default and contains the {@link DefaultEvacuationFloor}.
	 */
	public BuildingPlan() {
		floors = new ArrayList<>( 10 );
		rasterized = false;
		// Add a new default-floor and sets its raster to 400 millimeter
		DefaultEvacuationFloor def = new DefaultEvacuationFloor();
		def.setRasterSize( de.zet_evakuierung.util.ConversionTools.floatToInt( rasterSize ) );
		addFloor( def );
	}

	/**
	 * Returns the id of the selected floor.
	 * @param floor the selected floor whose index should be computed
	 * @return the id of the selected floor
	 */
	public int getFloorID( FloorInterface floor ) {
		return floors.indexOf( floor );
	}

	/**
	 * Adds the denoted floor to the building plan, only if it has not
	 * been present in the list of floors until now.
	 * @param f the new floor
	 * @return {@code true} if the floor was added, {@code false} otherwise
	 */
	public final boolean addFloor( Floor f ) {
		if( !floors.contains( f ) ) {
			floors.add( f );
			return true;
		}
		return false;
	}

	/**
	 * Tries to add a new floor with a given name.
	 * @param f
	 * @param name
	 * @return
	 */
	public boolean addFloor( Floor f, String name ) {
		String oldName = f.getName();
		f.setName( name );
		if( addFloor( f ) )
			return true;
		f.setName( oldName );
		return false;
	}

	/**
	 * Moves the given floor one position further towards the beginning of the floor list. Only changes
	 * the order of the floors.
	 * @param floor the floor to move
	 * @throws IllegalArgumentException If the given floor is not in the list or if you try to move the
	 * default evacuation floor
	 */
	public void moveFloorUp( FloorInterface floor ) {
    int level = getFloorID(floor);            
    if( level == 0 ) {
      throw new IllegalArgumentException( "You may not move the default evacuation floor." );
    } else if( level < 0 ) {
      throw new IllegalArgumentException( "There is no floor with negative id." );
    } else if( level < floors.size() - 1 ) {
      final Floor f = floors.get( level );
      floors.set( level, floors.get( level + 1 ) );
      floors.set( level + 1, f );
    } else {
      throw new IllegalArgumentException( "The given floor is not on the list." );
    }
	}

	/**
	 * Moves the given floor one position further towards the end of the floor list. Only changes
	 * the order of the floors.
	 * @param floor the floor to move
	 * @throws IllegalArgumentException If the given floor is not in the list or if you try to move the
	 * default evacuation floor
	 */
	public void moveFloorDown( FloorInterface floor ) {
            int level = getFloorID(floor);
		if( level < 0 ) {
			throw new IllegalArgumentException( "There is no floor with negative id." );      
    } else if( level <= 1 ) {
			throw new IllegalArgumentException( "You may not move the default evacuation floor." );      
    } else if( level <= floors.size() - 1 ) {
			final Floor f = floors.get( level );
			floors.set( level, floors.get( level - 1 ) );
			floors.set( level - 1, f );
    } else {
      throw new IllegalArgumentException( "The given floor is not on the list." );      
    }
	}

	/**
	 * Returns the default floor that exists in each {@link BuildingPlan} as  the first registered floor.
	 * @return the default floor that exists in each {@link BuildingPlan} as the first registered floor */
	public DefaultEvacuationFloor getDefaultFloor() {
		return (DefaultEvacuationFloor)floors.get( 0 );
	}

	/**
	 * Returns view of all {@code Floors} that this {@code BuildingPlan} contains.
	 * @return the list of {@link Floor} objects (including the default floor)
	 */
	public List<Floor> getFloors() {
		return Collections.unmodifiableList( floors );
	}

	/**
	 * Returns, if the Flag rastered is set to {@code true}.
	 * @return {@code true}, if the flag rastered is set to {@code true}, {@code false} otherwise
	 */
	public boolean isRastered() {
		return rasterized;
	}

	/** @return If the plan has any rooms at all, e.g. if it is empty or not. */
	public boolean isEmpty() {
		for( FloorInterface f : getFloors() )
			if( f.getRooms().size() > 0 )
				return false;
		return true;
	}

	/** @return If the plan has any evacuation areas at all. */
	public boolean hasEvacuationAreas() {
		boolean hasEvac = false;
		for( FloorInterface f : getFloors() ) {
			for( Room r : f.getRooms() )
				if( r.getEvacuationAreas().size() > 0 ) {
					hasEvac = true;
					break;
				}

			if( hasEvac )
				break;
		}
		return hasEvac;
	}

	/**
	 * Removes a specified {@link Floor} from the floorlist. The
	 * {@link DefaultEvacuationFloor} cannot be removed. If the floor is the first
	 * (and only one), the floor is deleted and a new empty floor is added again
	 * as the first one.
	 * @param floor the floor to be removed
	 * @throws java.lang.IllegalArgumentException if the default floor should be removed.
	 */
	public void removeFloor( FloorInterface floor ) throws java.lang.IllegalArgumentException {
    if( !(floor instanceof Floor ) ) {
      throw new IllegalStateException("Illegal floor!");
    }
    Floor f = (Floor)floor;
		if( f.equals( floors.get( 0 ) ) )
			throw new java.lang.IllegalArgumentException( ZLocalization.loc.getString( "ds.z.BuildingPlan.DeleteDefaultEvacuationFloorException" ) );
		else if( floors.size() == 2 ) {
			// Delete the floor and defineByPoints a new and empty one
			if( floors.remove( f) ) {
				f.delete();
				addFloor( new Floor( ZLocalization.loc.getString( "ds.z.DefaultName.Floor" ) + " " + 1 ) );
			}
		} else if( floors.remove( f ) )
			f.delete();
	}

	/**
	 * This method checks whether the current BuildingPlan is valid. It
	 * essentially just delegates this task to the single floors, by calling
	 * their validation routines.
	 * @throws PolygonNotClosedException if the validation reveals
	 * that the polygon is not closed
	 * @throws AreaNotInsideException if the validation reveals
	 * that an area is not inside its associated room
	 * @throws RoomIntersectException  if two rooms intersect
	 */
	public void check() throws PolygonNotClosedException, AreaNotInsideException, RoomIntersectException {
		for( Floor f : floors )
			f.check( isRastered() );
	}

	@Override
	public boolean equals( Object o ) {
		if( o instanceof BuildingPlan ) {
			BuildingPlan p = (BuildingPlan)o;

			//This is not an implementation error - Lists also have a proper equals method
			return (floors == null) ? p.floors == null : floors.equals( p.floors );
		} else
			return false;
	}

	/**
	 * Returns the number of floors of the building plan, <b>including</b> default
	 * floor.
	 * @return the number of floors
	 */
	public int floorCount() {
		return floors.size();
	}

	/**
	 * Returns the number of floors of the building plan. If the default floor
	 * counts, is decided by a specified boolean parameter.
	 * @param defaultFloor specifies if the default floor is counted, or not
	 * @return the number of floors
	 */
	public int floorCount( boolean defaultFloor ) {
		return defaultFloor ? floors.size() : floors.size() - 1;
	}

	/**
	 * Returns the number of all evacuation areas in the building.
	 * @return the evacuation area count
	 */
	public int getEvacuationAreasCount() {
		int count = 0;
		for( Floor floor : floors )
			for( Room room : floor.getRooms() )
				//for( EvacuationArea evac : room.getEvacuationAreas() )
				//	count++;
				count += room.getEvacuationAreas().size();
		return count;
	}

	/**
	 * Rasters each {@link Room} / {@link Area} on every Floor.
	 * Sets the rastered flag to "true" upon completion.
	 */
	public void rasterize() {
		try {
			check();
		} catch( de.zet_evakuierung.model.exception.RoomIntersectException e ) {
			Tuple<Room,Room> rooms = e.getIntersectingRooms();
			System.out.println( "Es schneiden sich die Räume: " + rooms.getU().getName() + " - " + rooms.getV().getName() );
		}
		for( Floor f : floors )
			for( Room r : f.getRooms() ) {
				//AlgorithmTask.getInstance().setProgress( 100 / (Math.max( f.roomCount(), 1 )), ZLocalization.loc.getString( "ds.z.floor" ) + ":" + f.getName(), r.getName() );

				// Checking if r is rasterized before rasterizing it makes no sense??
				// but it makes sense to ensure that all polygons are closed!!
				((RoomImpl)r).check( rasterized );
				((RoomImpl)r).rasterize();
				((RoomImpl)r).cleanUpPassableEdgesForRooms();

			}
		//AlgorithmTask.getInstance().setProgress( 100, ZLocalization.loc.getString( "ds.z.RasterizeFinished" ), "" );
		rasterized = true;
	}

	/** A convenience method that automatically distributes the given number of evcauees
	 * among all assignment areas that were created in the building. Each area gets a
	 * share of the total number of evacuees which is proportional to it's share of the
	 * total surface area of all assignment areas. All preexisting evacuee numbers
	 * are overwritten.
	 *
	 * @param nrOfEvacuees
	 * @throws TooManyPeopleException If you specify a number of evacuees that
	 * exceeds the total space in all assignment areas.
	 */
	public void distributeEvacuees( int nrOfEvacuees ) throws TooManyPeopleException {
		// Get the total assignment area size
		int max_persons = 0;
		int nr_of_assignment_areas = 0;
		for( Floor f : floors )
			for( Room r : f.getRooms() )
				for( AssignmentArea a : r.getAssignmentAreas() ) {
					max_persons += a.getMaxEvacuees();
					nr_of_assignment_areas++;
				}

		if( max_persons < nrOfEvacuees )
			throw new TooManyPeopleException( null, ZLocalization.loc.getString( "ds.TooManyEvacuees" ) );

		// Try to distribute the persons and gather all ass. areas
		int already_distributed = 0;
		int index = 0;
		AssignmentArea[] aareas = new AssignmentArea[nr_of_assignment_areas];
		for( Floor f : floors )
			for( Room r : f.getRooms() )
				for( AssignmentArea a : r.getAssignmentAreas() ) {
					int evacs_for_a = (int)(((double)a.getMaxEvacuees() / (double)max_persons) * nrOfEvacuees);
					a.setEvacuees( evacs_for_a );
					already_distributed += evacs_for_a;

					aareas[index++] = a;
				}

		// Distribute the rest of the eacs (we round down, so possibly there are some
		// of them left) randomly among all ass. areas
		while( already_distributed < nrOfEvacuees ) {
			// We don't use the Random Utils here, because this is not a simulation feature
			// but an editor feature and thus it mustn't forcedly be reproducable
			Random rand = new Random();
			index = rand.nextInt( aareas.length - 1 );
			aareas[index].setEvacuees( aareas[index].getEvacuees() + 1 );
			already_distributed++;
		}
	}

	/**
	 * Returns the maximum number of evacuees that can be placed in all the assignment
	 * areas in the building
	 * @return the maximal number of evacuees in the building */
	public int maximalEvacuees() {
		int maxPersons = 0;
		for( Floor f : floors )
			for( Room r : f.getRooms() )
				for( AssignmentArea a : r.getAssignmentAreas() )
					maxPersons += a.getMaxEvacuees();
		return maxPersons;
	}

	/**
	 * Returns an iterator over the floors of this building plan.
	 * @return an iterator over the floors of this building plan.
	 */
	@Override
        public Iterator<Floor> iterator() {
		return getFloors().iterator();
	}

	StringBuilder summaryBuilder() {
		StringBuilder sb = new StringBuilder( 10000 );

		sb.append( "Gebäude: " ).append( floors.size() ).append( " Stockwerke\n");
		for( Floor f : floors ) {
			sb.append( f.summaryBuilder() );
			sb.append( '\n' );
		}

		return sb;
	}

	public String summary() {
		// first: recompute bounds!
		for( Floor f : floors ) {
			for( Room r : f ) {
				((RoomImpl)r).recomputeBounds();
			}
		}

		return summaryBuilder().toString();
	}

    public List<EvacuationArea> getEvacuationAreas() {
        LinkedList<EvacuationArea> evacuationAreas = new LinkedList<>();
        for (Floor f : this) {
            for (Room r : f) {
                evacuationAreas.addAll(r.getEvacuationAreas());
            }
        }
        return evacuationAreas;
    }

    public boolean canMoveUp(FloorInterface floor) {
        int id = getFloorID(floor);
        return !(floor instanceof DefaultEvacuationFloor) && id < floorCount() - 1;
    }
    
    public boolean canMoveDown(FloorInterface floor) {
            int id = getFloorID(floor);
            return !(floor instanceof DefaultEvacuationFloor) && id > 1;
    }

}
