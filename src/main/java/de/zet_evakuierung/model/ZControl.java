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

import org.zetool.common.util.Formatter;
import org.zetool.common.util.Helper;
import static org.zetool.common.util.Helper.in;
import org.zetool.rndutils.distribution.Distribution;
import org.zetool.rndutils.distribution.continuous.NormalDistribution;
import org.zetool.rndutils.distribution.continuous.UniformDistribution;
import de.zet_evakuierung.model.exception.AreaNotInsideException;
import de.zet_evakuierung.model.exception.AssignmentException;
import de.zet_evakuierung.model.exception.InvalidRoomZModelError;
import de.zet_evakuierung.model.exception.PolygonNotClosedException;
import de.zet_evakuierung.model.exception.RoomIntersectException;
import de.zet_evakuierung.model.exception.UnknownZModelError;
import event.EventServer;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.zetool.common.debug.Debug;

/**
 * The class {@code ZControl} represents a front end class to the Z-model.
 * It is called for whatever action should be performed on the model. It will
 * send appropriate actions to the model and will take care for a consistent
 * model description.
 *
 * Thus, no action-changed methods inside the model should be needed.
 * @author Jan-Philipp Kappmeier
 */
public class ZControl {
	/** The project that is root of the controlled model. */
	private Project project;

	public ZControl() {
		newProject();
	}

	/**
	 * Creates a new instance of {@code ProjectControl} which controls a
	 * given project.
	 * @param filename the path to a file that should be loaded as project.
	 */
	public ZControl( String filename ) {
		this( new File( filename ) );
	}

	/**
	 * Creates a new instance of {@code ProjectControl} which controls a
	 * given project.
	 * @param file the file that should be loaded as project.
	 */
	public ZControl( File file ) {
		if( file == null )
			newProject();
		else if( !loadProject( file ) )
			project = newProject();
	}

	/**
	 * Creates a new instance of {@code ZControl}.
	 * @param p
	 */
	ZControl( Project p ) {
		this.project = p;
	}

	public Project getProject() {
		return project;
	}

	public void loadProject( String projectFile ) {
		loadProject( new File( projectFile ) );
	}

	/**
	 * Loads the specified {@link File}.
	 * @param projectFile the project file
	 * @return returns true, if the project loaded correctly
	 */
	final public boolean loadProject( File projectFile ) {
		try {
			//p = Project.load( projectFile );
			project = ProjectLoader.load( projectFile );
			project.setProjectFile( projectFile );
			// Update the graphical user interface
			//ZETMain.sendMessage( loc.getString( "gui.editor.JEditor.message.loaded" ) );	// TODO output changed, use listener
		} catch( Exception ex ) {
			System.err.println( ZLocalization.loc.getString( "gui.editor.JEditor.error.loadErrorTitle" ) + ":" );
			System.err.println( " - " + ZLocalization.loc.getString( "gui.editor.JEditor.error.loadError" ) );
			ex.printStackTrace( System.err );
			//ZETMain.sendMessage( loc.getString( "gui.editor.JEditor.message.loadError" ) );
			return false;
		}
		return true;
	}

	/**
	 * <p>Creates a new project with default settings and returns it. The old
	 * model controlled by this class is replaced by the new empty model for the
	 * new project.</p>
	 * <p>The model parameters follow the guides from RiMEA (http://www.rimea.de) if they
	 * are specified.</p>
	 * @return the newly created project
	 */
	final public Project newProject() {
		project = new Project();
		Floor fl = new Floor( ZLocalization.loc.getString( "ds.z.DefaultName.Floor" ) + " 1" );
		fl.setMinimumSize( 0, 0, 10000, 8000 );
		project.getBuildingPlan().addFloor( fl );
		Assignment assignment = new Assignment( ZLocalization.loc.getString( "ds.z.DefaultName.DefaultAssignment" ) );
		project.addAssignment( assignment );
		Distribution diameter = getDefaultAssignmentTypeDistribution( "diameter" );
		Distribution age = getDefaultAssignmentTypeDistribution( "age" );
		Distribution familiarity = getDefaultAssignmentTypeDistribution( "familiarity" );
		Distribution panic = getDefaultAssignmentTypeDistribution( "panic" );
		Distribution decisiveness = getDefaultAssignmentTypeDistribution( "decisiveness" );
		Distribution reaction = getDefaultAssignmentTypeDistribution( "reaction" );
		AssignmentType assignmentType = new AssignmentType( ZLocalization.loc.getString( "ds.z.DefaultName.DefaultAssignmentType" ), diameter, age, familiarity, panic, decisiveness, reaction, 10 );
		assignment.addAssignmentType( assignmentType );
		return project;
	}

	/**
	 * Returns default values for a {@link org.zetool.rndutils.distribution.Distribution} distribution
	 * for a specified parameter. Reaction time and age follow the guidelines of
	 * RiMEA (http://www.rimea.de).
	 * @param type the parameter
	 * @return the distribution for the parameter
	 * @throws IllegalArgumentException if type is an unknown string
	 */
	public static Distribution<Double> getDefaultAssignmentTypeDistribution( String type ) throws IllegalArgumentException {
		if( type.equals( "diameter" ) ) {
			return new NormalDistribution( 0.5, 1.0, 0.4, 0.7 );
		} else if( type.equals( "age" ) ) {
			return new NormalDistribution( 50, 20, 10, 85 );
		} else if( type.equals( "familiarity" ) ) {
			return new NormalDistribution( 0.8, 1.0, 0.7, 1.0 );
		} else if( type.equals( "panic" ) ) {
			return new NormalDistribution( 0.5, 1.0, 0.0, 1.0 );
		} else if( type.equals( "decisiveness" ) ) {
			return new NormalDistribution( 0.3, 1.0, 0.0, 1.0 );
		} else if( type.equals( "reaction" ) ) {
			return new UniformDistribution( 0, 60 );
		}
		throw new AssertionError( "Unknown parameter type." );
	}

	public boolean deleteFloor( FloorInterface currentFloor ) {
		if( currentFloor instanceof DefaultEvacuationFloor )
			return false;
		getProject().getBuildingPlan().removeFloor( currentFloor );
		return true;
	}

	public void deletePolygon( PlanPolygon p ) {
		if( p instanceof Area )
			delete( (Area)p );
		else if( p instanceof Room )
			deletePolygon( (Room)p );
		else
			throw new IllegalArgumentException( "Polygon not of type Area or Room" );
	}

	public void deletePolygon( Room r ) {
		((RoomImpl)r).delete();
	}

	// Delete Stuff
	public void delete( Area area ) {
		if( area instanceof EvacuationArea ) {
			for( Assignment a : project.getAssignments() )
				for( AssignmentType t : a.getAssignmentTypes() )
					for( AssignmentArea aa : t.getAssignmentAreas() )
						if( aa.getExitArea() != null && aa.getExitArea().equals( (EvacuationArea)area ) )
							aa.setExitArea( null );
			((AreaImpl)area).delete();
		} else
			((AreaImpl)area).delete();
	}

	PlanPolygon newPolygon = null;

	PlanPolygon latestPolygon = null;


	public PlanPolygon latestPolygon() {
		return latestPolygon;
	}

	/**
	 * Creates a new polygonal object in the hierarchy. These objects are rooms
	 * and the different types of areas (also barriers). A new object is created,
	 * but it will contain no points. Methods such as {@link #addPoint} have to
	 * be called afterwards. The creation process is completed with a call to
	 * {@link closePolygon()}.
	 * @param polygonClass the Class type of the object to be created
	 * @param parent the parent object. See details in the polygonal object, which is the correct parent type
	 * @throws AssignmentException if an assignment area is to be created without any valid assignment
	 * @throws IllegalArgumentException if object creation is already started or an invalid class was submitted
	 */
	   public void createNewPolygon(Class<?> polygonClass, Object parent) throws AssignmentException, IllegalArgumentException {
        if (newPolygon != null) {
            throw new IllegalArgumentException("Creation already started.");
        }

        if (polygonClass == Room.class) {
            newPolygon = new RoomImpl((Floor) parent);
        } else if (polygonClass == AssignmentArea.class) {
            Assignment cur2 = getProject().getCurrentAssignment();
            if (cur2 != null) {
                if (cur2.getAssignmentTypes().size() > 0) {
                    newPolygon = new AssignmentArea((RoomImpl) parent, cur2.getAssignmentTypes().get(0));
                } else {
                    throw new AssignmentException(AssignmentException.State.NoAssignmentCreated);
                }
            } else {
                throw new AssignmentException(AssignmentException.State.NoAssignmentSelected);
            }
        } else if (polygonClass == Barrier.class) {
            newPolygon = new Barrier((RoomImpl) parent);
        } else if (polygonClass == DelayArea.class) {
            newPolygon = new DelayArea((RoomImpl) parent, DelayArea.DelayType.OBSTACLE, 0.7d);
        } else if (polygonClass == StairArea.class) {
            newPolygon = new StairArea((RoomImpl) parent);
        } else if (polygonClass == EvacuationArea.class) {
            newPolygon = new EvacuationArea((RoomImpl) parent);
            int count = getProject().getBuildingPlan().getEvacuationAreasCount();
            String name = ZLocalization.loc.getString("ds.z.DefaultName.EvacuationArea") + " " + count;
            ((EvacuationArea) newPolygon).setName(name);
        } else if (polygonClass == InaccessibleArea.class) {
            newPolygon = new InaccessibleArea((RoomImpl) parent);
        } else if (polygonClass == SaveArea.class) {
            newPolygon = new SaveArea((RoomImpl) parent);
        } else if (polygonClass == TeleportArea.class) {
            newPolygon = new TeleportArea((RoomImpl) parent);
        } else {
            throw new IllegalArgumentException("No valid plygon class given");
        }

        latestPolygon = newPolygon;
    }

	public boolean addPoints( List<PlanPoint> points ) {
		if( newPolygon == null )
			throw new IllegalStateException( "No polygon creation started." );

		if( points.isEmpty() )
			throw new IllegalArgumentException( "No Points." );
		if( points.size() == 1 )
			return addPoint( points.get(0) );

		for( int i = 0; i < points.size()-1; ++i )
			addPoint( points.get( i ), false );

		return addPoint( points.get( points.size()-1 ), true );
	}

	private static PlanPoint temp = null;

	public boolean addPoint( PlanPoint point ) {
		return addPoint( point, true );
	}

	// requests a new
	private boolean addPoint( PlanPoint point, boolean sendEvent ) {
		if( newPolygon.isClosed() )
			throw new IllegalStateException( "Polygon is closed." );

		if( newPolygon.getEnd() == null ) {
			if( temp == null )
				temp = point;
			else
				newPolygon.newEdge( temp, point );
		} else
			newPolygon.addPointLast( point );

		if( newPolygon.isClosed() ) {
			if( newPolygon instanceof AssignmentArea )
				((AssignmentArea)newPolygon).setEvacuees( Math.min( newPolygon.getMaxEvacuees(), ((AssignmentArea)newPolygon).getAssignmentType().getDefaultEvacuees() ) );
			if( sendEvent )
				throwEvent();
			newPolygon = null;
			temp = null;
			return true;
		}
		if( sendEvent )
			throwEvent();
		return false;
	}

	private void throwEvent() {
		if( newPolygon instanceof Area ) {
			EventServer.getInstance().dispatchEvent( new ZModelAreaEvent( ((AreaImpl)newPolygon).getAssociatedRoom(), (AreaImpl)newPolygon ) );
		} else
			EventServer.getInstance().dispatchEvent( new ZModelRoomEvent( new LinkedList<Room>(){{ add((Room)newPolygon); }} ) );
	}

	public PlanPolygon<?> closePolygon() {
		if( newPolygon.isClosed() )
			throw new IllegalStateException( "Polygon closed." );

		if( newPolygon.getNumberOfEdges() == 0 )
			throw new IllegalStateException( "No edges" );
		else {
			if( newPolygon.area() == 0 && !(newPolygon instanceof Barrier) )
				throw new IllegalStateException( "Area zero" );
			else if( newPolygon.getNumberOfEdges() >= ((newPolygon instanceof Barrier) ? 1 : 2) ) { // The new edge would be the third
				newPolygon.close();
				throwEvent();
				newPolygon = null;
				temp = null;
			} else
				throw new IllegalStateException( "Three edges" );
		}
		return latestPolygon;
	}

	/**
	 * <p>Creates a new floor in the hierarchy. A floor does not have a parent and
	 * is immediately created. It has no explicit bounds that have to be specified
	 * (in contrast to polygonal objects).</p>
	 * <p>The floor will have the default name followed by a number (which is
	 * the current number of floors).</p>
	 * @return the newly created floor
	 */
	public Floor createNewFloor() {
		return createFloor( ZLocalization.loc.getString( "ds.z.DefaultName.Floor" ) + " " + project.getBuildingPlan().floorCount() );
	}

	/**
	 * <p>Creates a new floor in the hierarchy. A floor does not have a parent and
	 * is immediately created. It has no explicit bounds that have to be specified
	 * (in contrast to polygonal objects).</p>
	 * @param name the name of the floor
	 * @return the newly created floor
	 */
	public Floor createFloor( String name ) {
		final Floor f = new Floor( name );
		project.getBuildingPlan().addFloor( f );
		return f;
	}

	private void translatePoint( PlanEdge edge, PlanPoint planPoint, int x, int y ) {
		if( edge instanceof RoomEdge ) {
			RoomEdge e = (RoomEdge)edge;
			if( e.isPassable() ) {
				if( e.getLinkTarget().getSource().matches( planPoint ) ) {
					translateAndHash( e.getLinkTarget().getSource() , x, y );
				} else if( e.getLinkTarget().getTarget().matches( planPoint ) )
					translateAndHash( e.getLinkTarget().getTarget(), x, y );
				else
					throw new AssertionError( "Two passable edges have no matching points!" );
				e.getLinkTarget().getAssociatedPolygon().recomputeBounds();
			}
		}
	}

	private HashSet<PlanPoint> moved = new HashSet<>();

	/**
	 * <p>Translates a point only, if it has not been trnslated before. This method
	 * takes care about {@link PlanPoint}s that have been translated and only
	 * translates them, when they haven't been translated before. This can be used
	 * if a series of operations is to be performed on the Z model, but the
	 * operations may be performed twice, if an edge is passable and the operation
	 * is therefore also operated on attached points.</p>
	 * <p>To work correctly, at the end of every operation, the {@link HashSet}
	 * {@link #moved} has to be cleared or otherwise resetted to an empty
	 * hash set.</p>
	 * @param p
	 * @param x
	 * @param y
	 */
	private void translateAndHash( PlanPoint p, int x, int y ) {
		if( !moved.contains( p ) ) {
			p.translate( x, y );
			moved.add( p );
		}
	}

	/**
	 * Tries to move the submitted areas by the specified distance into the
	 * given room. If the areas do not fit into the room (that means, all the
	 * border points of the area must be contained into the next room), nothing
	 * will happen.
	 * @param areas
	 * @param x
	 * @param y
	 * @param target
	 */
	public void moveAreas( List<Area> areas, int x, int y, Room target ) {
		// check first,
		for( Area a : areas ) {
			for( PlanPoint p : ((AreaImpl)a).getPlanPoints() ) {
				PlanPoint check = new PlanPoint( p.x + x, p.y + y );
				if( !((PlanPolygon<RoomEdge>)target.getPolygon()).contains( check ) )
					return; // illegal!
			}
		}

		// then call the move points method
		List<PlanPoint> draggedPlanPoints = new LinkedList<>();
		for( Area a : areas)
			draggedPlanPoints.addAll( ((AreaImpl)a).getPlanPoints() );

		movePoints( draggedPlanPoints, x, y );
		for( Area a : areas)
			((AreaImpl)a).setAssociatedRoom( (RoomImpl)target );

		HashSet<Room> affectedRooms = new HashSet<>();
		affectedRooms.add( target );

		ZModelRoomEvent zmr = new ZModelRoomEvent( affectedRooms );
		EventServer.getInstance().dispatchEvent( zmr );
	}

	public void movePoints( List<? extends PlanPoint> points, int x, int y ) {
		Iterator<? extends PlanPoint> itPP = points.iterator();

		HashSet<Room> affectedRooms = new HashSet<>();
		PlanPoint planPoint;
		while( itPP.hasNext() && itPP.hasNext() ) {
			// The drag targets are already rasterized, if neccessary
			planPoint = itPP.next();

			translatePoint( planPoint.getNextEdge(), planPoint, x, y );
			translatePoint( planPoint.getPreviousEdge(), planPoint, x, y );

			translateAndHash( planPoint, x, y);

			// Keep track of the areas that we move
			PlanPolygon<?> currentPolygon = planPoint.getNextEdge() != null ? planPoint.getNextEdge().getAssociatedPolygon() : planPoint.getPreviousEdge() != null ? planPoint.getPreviousEdge().getAssociatedPolygon() : null;
			currentPolygon.recomputeBounds();

			// save the affected rooms for the update
			if( currentPolygon instanceof Room ) {
				affectedRooms.add( (Room)currentPolygon );
			} else if( currentPolygon instanceof Area ) {
				affectedRooms.add( ((AreaImpl)currentPolygon).getAssociatedRoom() );
			} else
				throw new AssertionError( "Not supported type of PlanPolygon. Only 'Room' and 'Area<?>' are supported. Was: " + currentPolygon.getClass() );
		}

		moved.clear();
		ZModelRoomEvent zmr = new ZModelRoomEvent( affectedRooms );
		EventServer.getInstance().dispatchEvent( zmr );
	}

	/**
	 * Clones the floor and adds it to the project. The name of the floor is
	 * extended by '_##' where ## represents a number. If the name of the floor
	 * was ending with a two-digit number, this number is increased by one. It is
	 * not possible to have more than 100 floors with the same name
	 * (only automatically created).
	 * @param f the floor that is copied
	 */
	public void copyFloor( Floor f ) {
		Floor fc = null;
		try {
			 fc = f.clone();
			 fc.recomputeBounds( false );
		} catch( InvalidRoomZModelError ex ) {
			System.err.println( ex.getMessage() );
			JOptionPane.showMessageDialog( null, ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE );
			return;
		} catch( UnknownZModelError ex ) {
			final Logger log = Logger.getGlobal();
      Debug.printException( ex );
			System.err.println( ex.getMessage() );
			JOptionPane.showMessageDialog( null, ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE );
			return;
		}

		int number = 0;
		String newName = f.getName() + "_";

		// Check if floorname ends with '##'
		if( Helper.isBetween( f.getName().charAt( f.getName().length() - 2 ), '0', '9' ) && Helper.isBetween( f.getName().charAt( f.getName().length() - 1 ), '0', '9' )  ) {
			number = Integer.parseInt( f.getName().substring( f.getName().length()-2, f.getName().length()-0 ) ) + 1;
			newName = f.getName().substring( 0, f.getName().length()-2 );
		}
		do {
			fc.setName( newName + Formatter.fillLeadingZeros( number++, 2 ) );
		} while( !project.getBuildingPlan().addFloor( fc ) && number <= 99 );
	}

	public void moveFloorUp(FloorInterface floor ) {
            if (project.getBuildingPlan().canMoveDown(floor)) {
		project.getBuildingPlan().moveFloorUp( floor );
            } else {
                System.out.println("Could not move floor up!");
            }
	}

	public void moveFloorDown( FloorInterface floor ) {
            if(project.getBuildingPlan().canMoveDown(floor) ) {
                project.getBuildingPlan().moveFloorDown(floor);
            } else {
                System.out.println("Floor is bottom!");
            }
	}

	public void deletePoint( PlanPolygon poly, PlanPoint currentPoint ) {
		PlanEdge currentEdge = currentPoint.getNextEdge();
		poly.combineEdges( currentEdge, currentPoint.getOtherEdge( currentEdge ), true );
		EventServer.getInstance().dispatchEvent( new ZModelChangedEvent() {} );
	}

	public ArrayList<PlanEdge> insertPoint( PlanEdge onEdge, PlanPoint newPoint ) {
		// Replace the old edge
		ArrayList<PlanPoint> pointList = new ArrayList<>( 3 );
		pointList.add( onEdge.getSource() );
		pointList.add( newPoint );
		pointList.add( onEdge.getTarget() );
		ArrayList<PlanEdge> ret = onEdge.getAssociatedPolygon().replaceEdge( onEdge, pointList );
		EventServer.getInstance().dispatchEvent( new ZModelChangedEvent() {} );
		return ret;
	}

	/**
	 * Connect two rooms by a new quadrangular room defined by two edges.
	 * @param firstEdge an edge of the first room
	 * @param secondEdge an edge of the second room
	 * @throws IllegalArgumentException if the two edges belong to the same room
	 */
	public void connectRooms( RoomEdge firstEdge, RoomEdge secondEdge ) throws IllegalArgumentException{
		if( firstEdge.getRoom().equals( secondEdge.getRoom() ) )
			throw new IllegalArgumentException( "Edges must lie in different rooms." );

		// Create new Room
		final PlanEdge test1 = new PlanEdge( firstEdge.getSource(), secondEdge.getSource() );
		final PlanEdge test2 = new PlanEdge( firstEdge.getTarget(), secondEdge.getTarget() );

		createNewPolygon( Room.class, firstEdge.getRoom().getAssociatedFloor() );
		addPoint( new PlanPoint( firstEdge.getSource() ), false );
		addPoint( new PlanPoint( firstEdge.getTarget() ), false );
		switch( PlanEdge.intersects( test1, test2 ) ) {
			case Intersects:
				addPoint( new PlanPoint( secondEdge.getSource() ), false );
				addPoint( new PlanPoint( secondEdge.getTarget() ), false );
				break;
			default:	// add the points in swapped order
				addPoint( new PlanPoint( secondEdge.getTarget() ), false );
				addPoint( new PlanPoint( secondEdge.getSource() ), false );
		}
		closePolygon();

		// connect
		final RoomImpl room = (RoomImpl) latestPolygon;
		room.connectTo( (RoomImpl)firstEdge.getRoom(), firstEdge.getSource(), firstEdge.getTarget() );
		room.connectTo( (RoomImpl)secondEdge.getRoom(), secondEdge.getSource(), secondEdge.getTarget() );
		EventServer.getInstance().dispatchEvent( new ZModelChangedEvent() {} );
	}

	private int check( RoomEdge ed ) {
		if( ed.getRoom() == null )
			throw new IllegalStateException( "An edge is not connected to any room." );
		if( ed.isPassable() ) {
			RoomEdge ed2 = ed.getLinkTarget();
			if( ed == ed2 )
				return 2; // connected to self
			else {
				if( ed2.getRoom() == null )
					return 3; // target edge is not contained in any room (maybe a relict)
				RoomEdge ed3 = ed2.getLinkTarget();
				if( ed3 != ed && ed3 != null ) {
					if( ed3.getLinkTarget() == ed2 )
						return 4; // target edge is part of another door
					return 5; // target edge is connected somehow else
				}
			}
		} else
			return 1; // edge is not passable
		return 0;
	}

	 /**
   * Safely disconnects a given room edge. In the normal case the edge is just made passable. It also checks for errors
   * which means, that falsely connected rooms are disconnected correctly.
   * @param roomEdge
   */
  public void disconnectAtEdge( RoomEdge roomEdge ) {
    switch( check( roomEdge ) ) {
      case 0:	// no error found
        roomEdge.makeImpassable();
        break;
      case 1: // not passable
        break;	// do nothing
      case 2: // connected to self
      case 3: // target edge is not contained in any room (maybe a relict)
      case 4: // target edge is part of another door
      case 5: // target edge is connected somehow else
      case 6: // cycle of 3 doors
        roomEdge.setLinkTarget( null );
        break;
      default:
        throw new AssertionError( "Error code not implemented" );
    }
  }

  public void autoCorrectEdges() {
    for( FloorInterface floor : project.getBuildingPlan() ) {
      for( Room room : floor ) {
        boolean printed = false;
        for( RoomEdge ed : in( ((PlanPolygon<RoomEdge>)room.getPolygon()).edgeIterator() ) ) {
          if( check( ed ) != 0 ) {
            if( !printed ) {
              System.out.println( "Correct " + room.getName() );
              printed = true;
            }
            ed.setLinkTarget( null );
          }
        }
      }
    }
  }

  private int coordinate( int position, int raster ) {
    if( position % raster == 0 ) {
      return position;
    }
    int rest = position % raster;
    if( rest <= raster / 2 ) {
      position -= rest;
    } else {
      position += raster - rest;
    }
    if( raster % 2 == 0 && rest == raster / 2 && (position / raster) % 2 == 1 ) {
      position += raster;
    }
    return position;
  }

	/**
	 * Translates the corner of the given room to multiples of a given raster size.
	 * This may be used if somehow during the edit process some points have been
	 * placed not on the raster.
	 * @param currentRoom the room whose corners are translated
	 * @param rasterSizeSnap the raster size
	 */
	public void refineRoomCoordinates( PlanPolygon<?> currentRoom, int rasterSizeSnap ) {
		for( PlanPoint p : currentRoom.getPlanPoints() ) {
			p.setLocation( coordinate( p.x, rasterSizeSnap), coordinate( p.y, rasterSizeSnap ) );
		}
	}

    /**
     * Renames a floor if that is possible.
     *
     * @param floor the floor that is renamed
     * @param name the new name of the floor
     * @return {@code true} if the floor could be renamed, {@code false} otherwise
     */
    public boolean renameFloor(FloorInterface floor, String name) {
        // try to find out if the name is already used
        if (floor.getName().equals(name)) {
            return true;
        }
        for (FloorInterface f : project.getBuildingPlan()) {
            if (f.getName().equals(name)) {
                return false;
            }
        }
        ((Floor) floor).setName(name);
        return true;
    }

    /**
	 * Renames a room if that is possible.
	 * @param room the room that is renamed
	 * @param name the new name of the room
	 * @return {@code true} if the room could be renamed, {@code false} otherwise
	 */
	public boolean renameRoom( Room room, String name ) {
		if( room.getName().equals( name ) )
			return true;
		if( room.getAssociatedFloor() == null ) {
			((RoomImpl)room).setName( name );
			return true;
		}
		for( Room r : room.getAssociatedFloor() )
			if( r.getName().equals( name ) )
				return false;
		((RoomImpl)room).setName( name );
		return true;
	}

	/**
	 * Calls the check methods for the z format objects. If an exception was
	 * thrown, it is caught and the failure is given out to the debug out.
	 */
	public void checkDebugOut() {
//
//		for( Floor f : project.getBuildingPlan().getFloors() ) {
//			for( Room r : f.getRooms() ) {
//				List ia = r.getInaccessibleAreas();
//
//for( Object a : ia ) {
//				if( a == null ) {
//					// repair
//					int count = 0;
//					int nullIndex= 0;
//					int i = ia.size()-1;
//					while( nullIndex < i) {
//						while( ia.get( nullIndex ) != null ) {
//							nullIndex++;
//						}
//
//						if( nullIndex >= i )
//							break;
//
//						if( ia.get( i ) != null ) {
//							//ia.set( nullIndex, ia.get( i ) );
//							//ia.set( i, null );
//							count++;
//						}
//						i--;
//					}
//					if( count == 0 )
//						ia.clear();
//				}
//			}
//
//			}
//		}
//
//
//
//		if( 1 == 1 )
//			return;
		try {
			project.getBuildingPlan().check();
			System.out.println( "Everything OK." );
		} catch( RoomIntersectException ex ) {
			System.out.println( "Räume " + ex.getIntersectingRooms().getU().getName() + " und " + ex.getIntersectingRooms().getV().getName() + " schneiden sich in " + ex.getIntersectionPoiont().toString() );
			System.out.println( ex.getIntersectingRooms().getU() );
			System.out.println( ex.getIntersectingRooms().getV() );
		} catch( AreaNotInsideException ex ) {
			System.out.println( "Im Raum " + ex.getSource().getName() + " liegt eine Area vom Typ " + ex.getArea().getAreaType().name() + " außerhalb." );
			System.out.println( ex.getSource() );
			System.out.println( ex.getArea() );
		} catch ( PolygonNotClosedException ex ) {
			PlanPolygon<?> p = ex.getSource();
			if( p instanceof Room ) {
				Room r = (Room)p;
				System.out.println( "Raum " + r.getName() + " ist nicht geschlossen." );
				System.out.println( ex.getSource() );
			} else if( p instanceof Area ) {
				Area a = (Area)p;
				Room r = a.getAssociatedRoom();
				System.out.println( "In raum " + r + " ist eine offene Area." );
				System.out.println( a.toString() );
			} else {
				System.out.println( "Fehler in polygon" );
			}
		}

	}

	public void createDoor( RoomEdge edge, PlanPoint newPoint, double doorSize ) {
		Room anchorRoom = edge.getRoom(); // store here, the edge will be destroyed and the room will be invalid afterwards

		System.out.println( "Zeichne Tür um " + newPoint + " herum." );

		PlanPoint p1 = edge.getPoint( newPoint, doorSize / 2 );

		PlanPoint p2 = edge.getPoint( newPoint, -doorSize / 2 );

		System.out.println( "Center: " + newPoint );
		System.out.println( "P1: " + p1 );
		System.out.println( "P2: " + p2 );

		ArrayList<PlanEdge> newEdges = insertPoint( edge, p1 );

		insertPoint( newEdges.get( 1 ), p2 ); // has to be point 1, due to internal implementation of replaceEdges in PlanPolygon.java


		// Door points on the original edge have been created. Now search for a possible partner edge in adjacent rooms
		RoomEdge partner = null;

		Room target = null;
		for( Room r : anchorRoom.getAssociatedFloor().getRooms() ) {
			if( !r.equals( anchorRoom ) )
				partner = ((PlanPolygon<RoomEdge>)r.getPolygon()).getEdge( newPoint );
			if( partner != null ) {
				target = r;
				break;
			}
		}

		if( partner == null )
			throw new IllegalStateException( "Door can only be created between two rooms sharing an edge!" );
		else {

			p1 = partner.getPoint( newPoint, doorSize / 2 );

			p2 = partner.getPoint( newPoint, -doorSize / 2 );

			System.out.println( "Center: " + newPoint );
			System.out.println( "P1: " + p1 );
			System.out.println( "P2: " + p2 );

			newEdges = insertPoint( partner, p1 );

			insertPoint( newEdges.get( 1 ), p2 ); // has to be point 1, due to internal implementation of replaceEdges in PlanPolygon.java


		}

		RoomEdge door1 = ((PlanPolygon<RoomEdge>)anchorRoom.getPolygon()).getEdge( p1, p2 );
		RoomEdge door2 = ((PlanPolygon<RoomEdge>)target.getPolygon()).getEdge( p1, p2 );


		if( door1 != null ) {
			((RoomEdge) door2).setLinkTarget( door1 );
			door1.setLinkTarget( (RoomEdge) door2 );
		} else
			throw new IllegalStateException( "something went wrong" );
	}

	public void createExitDoor( RoomEdge edge, PlanPoint newPoint, int doorSize ) {
		Room anchorRoom = edge.getRoom(); // store here, the edge will be destroyed and the room will be invalid afterwards

		System.out.println( "Zeichne Tür um " + newPoint + " herum." );

		PlanPoint p1 = edge.getPoint( newPoint, doorSize / 2 );

		PlanPoint p2 = edge.getPoint( newPoint, -doorSize / 2 );

		System.out.println( "Center: " + newPoint );
		System.out.println( "P1: " + p1 );
		System.out.println( "P2: " + p2 );

		ArrayList<PlanEdge> newEdges = insertPoint( edge, p1 );

		insertPoint( newEdges.get( 1 ), p2 ); // has to be point 1, due to internal implementation of replaceEdges in PlanPolygon.java
		RoomEdge door1 = ((PlanPolygon<RoomEdge>)anchorRoom.getPolygon()).getEdge( p1, p2 );

		getProject().getBuildingPlan().getDefaultFloor().addEvacuationRoom( door1 );
	}
	
	public void addAssignment( Assignment a ) {
		project.addAssignment( a );
	}
	
	public void deleteAssignment( Assignment a ) {
		project.deleteAssignment( a );
	}

	public void setCurrentAssignment( Assignment currentAssignment ) {
		project.setCurrentAssignment( currentAssignment );
	}

	public void connectToWithTeleportEdge( RoomEdge firstEdge, RoomEdge secondEdge ) {
		RoomImpl.connectToWithTeleportEdge( firstEdge, secondEdge );
	}

	public AssignmentArea createNewArea( Room room, AssignmentType myType, ArrayList<PlanPoint> newPoints ) {
		AssignmentArea aa = new AssignmentArea( (RoomImpl)room, myType );
		aa.replace( newPoints );
		return aa;
	}

    public void setFloorSize(FloorInterface model, Rectangle floorSize) {
        Floor f = (Floor) model;
        f.setMinimumSize(floorSize.x, floorSize.y, floorSize.width, floorSize.height);
    }

    public void setDelaySpeedFactor(DelayArea model, double defaultSpeed) {
        model.setSpeedFactor(defaultSpeed);
    }

    public void setDelayType(DelayArea model, DelayArea.DelayType type) {
        model.setDelayType(type);
    }

    public void setRoomName(Room room, String name) {
        RoomImpl r = (RoomImpl)room;
        r.setName(name);
    }

    public boolean makePassable(RoomEdge edge) {
        Room room = edge.getRoom();
        RoomEdge partner = null;
        for (Room r : room.getAssociatedFloor().getRooms()) {
            if (r != room) {
                PlanPolygon<RoomEdge> p = (PlanPolygon<RoomEdge>) r.getPolygon();
                if (p.isContained(edge)) {
                    partner = p.getEdge(edge);
                    break; // Break when successful
                }
            }
        }
        if (partner != null) {
            edge.setLinkTarget(partner);
            partner.setLinkTarget(edge);
            return true;
        }
        return false;
    }
}
