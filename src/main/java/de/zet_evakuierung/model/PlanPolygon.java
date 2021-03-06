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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import static org.zetool.common.util.Helper.in;
import org.zetool.math.matrix.Matrix;
import de.zet_evakuierung.model.exception.PolygonNotClosedException;
import de.zet_evakuierung.model.exception.PolygonNotRasterizedException;
import de.zet_evakuierung.io.z.CompactEdgeListConverter;
import de.zet_evakuierung.util.ConversionTools;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * The generic {@literal PlanPolygon} class implements a polygonal area
 * bounded by edges extending the general type {@link PlanEdge}. To allow editing it
 * is possible to represent not valid areas. Thus are areas not open or
 * containing self-cutting edges.
 * <p>The class provides all necessary methods to edit a polygon describing
 * rooms. Supported operations are connecting open polygons, breaking up a
 * closed polygon, inserting edges/points, or mor precisely, replacing an
 * existing {@literal Edge} by some edges fitting into the gap, and
 * calculating the cut of two polygons.</p>
 * @param <T> the type describing the edges (borders) of the polygon
 * @author Jan-Philipp Kappmeier, Timon Kelter
 */
//@XStreamAlias("planPolygon")
//@XMLConverter(PlanPolygonConverter.class)
public class PlanPolygon<T extends PlanEdge> /*implements Iterable<T>*/ {
	/** The class-type of the edges. This is setLocation only one single time in the constructor. */
	//@XStreamOmitField
	private final Class<T> edgeClassType;
	/** Determines, if the polygon is closed. That means that the {@literal end}
	 * and {@literal start} edges have a common point. */
	//@XStreamAsAttribute()
	private boolean closed = false;
	/** The start point of the polygon. At this point new edges or polygons can be added. */
	@XStreamConverter(CompactEdgeListConverter.class)
	private PlanPoint start = null;
	/** The end point of the polygon. At this point new edges or polygons can be added. */
	private PlanPoint end = null;
	/** The number of edges that this polygon contains. */
	private int size = 0;
	/** The leftmost point coordinate of the polygon. */
	@XStreamAsAttribute()
	private int xOffset = 0;
	/** The uppermost point coordinate of the polygon. */
	@XStreamAsAttribute()
	private int yOffset = 0;
	/** The difference between the left- and rightmost point coordinate of the polygon. */
	@XStreamAsAttribute()
	private int width = 0;
	/** The difference between the upper- and lowermost point coordinate of the polygon. */
	@XStreamAsAttribute()
	private int height = 0;
	/** Determines if the polygon has been changed after a validity test. */
	@XStreamOmitField()
	private boolean changed = true;
	@XStreamOmitField
	int minx = Integer.MAX_VALUE;
	@XStreamOmitField
	int maxx = Integer.MIN_VALUE;
	@XStreamOmitField
	int miny = Integer.MAX_VALUE;
	@XStreamOmitField
	int maxy = Integer.MIN_VALUE;

	// For compatibility reasons with old project files
	@XStreamOmitField
	private T maxY_DefiningEdge;
	@XStreamOmitField
	private T maxX_DefiningEdge;
	@XStreamOmitField
	private T minY_DefiningEdge;
	@XStreamOmitField
	private T minX_DefiningEdge;

	/**
	 * three matrix matrixes for flip vertically, horizontally and at the main
	 * diagonal plus identity matrix
	 */
	private static enum Transformation {
		flipXAxis ( new int[][] {{1, 0}, {0, -1}} ),
		flipYAxis( new int[][] {{-1, 0}, {0, 1}} ),
		flipMainDiagonal( new int[][] {{0, 1}, {1, 0}} ),
		identity( new int[][] {{1, 0}, {0, 1}} );
		/** The matrix used for the transformation. */
		public final int[][] matrix;

		private Transformation( int[][] trans ) {
			matrix = trans;
		}
	}

	/**
	 * Creates an new instance of {@literal PlanPolygon} without any assigned
	 * edges or points. All parameters are initialized with {@literal null}.
	 * <p>It is necessary to submit the class type of the generic parameter in
	 * order to create new edges.</p>
	 * @param edgeClassType the type of the generic edges
	 */
	public PlanPolygon( Class<T> edgeClassType ) {
		start = null;
		end = null;
		this.edgeClassType = edgeClassType;
	}

	/**
	 * Creates an new instance of {@literal PlanPolygon} without any assigned
	 * edges or points. All parameters are initialized with {@literal null}.
	 * <p>It is necessary to submit the class type of the generic parameter in
	 * order to create new edges.</p>
	 * @param edgeClassType the type of the generic edges
	 * @param firstEdge An edge that shall be added to the polygon immediately
	 */
	public PlanPolygon( Class<T> edgeClassType, T firstEdge ) {
		start = null;
		end = null;
		this.edgeClassType = edgeClassType;

		addEdge( firstEdge );
	}

	/**
	 * Fills an empty instance of {@literal PlanPolygon} with a setLocation of
	 * {@link PlanPoint}. The border of the polygon is defined by the order of
	 * points, following the points and the necessary edges are created. The last
	 * edge between the {@literal n}-th and {@literal 1}st point closes the
	 * polygon.
	 * <p>Note that the polygon has to empty to use this method. If you want to
	 * change the points of an polygon all in one step, see
	 * {@link #replace( List points )}.</p>
	 * <p>Some tests during the creation process are performed. If two following
	 * points are equal, the second point is discarded. The same holds, if two
	 * consecutive edges that are inserted are equal. </p>
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @param points a list of points defining the shape of the polygon
	 * @return the number of newly created edges
	 * @throws java.lang.IllegalStateException if any point in the list closes the
	 * polygon or the polygon has already edges
	 * @throws java.lang.IllegalArgumentException if the passed list of points is
	 * to small or two points define the same edge
	 * @throws java.lang.NullPointerException if the specified {@literal List}
	 * of points is null
	 */
	public int defineByPoints( List<PlanPoint> points ) throws IllegalArgumentException, IllegalStateException, NullPointerException {
		if( start != null )
			throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.ContainerNotEmptyException" ) );
		if( points == null )
			throw new NullPointerException( ZLocalization.loc.getString( "ds.z.PlanPolygon.PointListIsNullException" ) );
		if( points.size() <= 1 )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.ListDoesNotContainenoughPointsException" ) );

		// At least two points are in the list
		PlanPoint firstPoint = points.get( 0 );

		int createdEdges = 0;

		for( int i = 1; i < points.size(); i++ ) {
			PlanPoint secondPoint = points.get( i );
			// Don't create zero-length edges
			if( secondPoint.matches( firstPoint ) )
				continue;

			createdEdges++;
			newEdge( firstPoint, secondPoint );
			firstPoint = secondPoint;
		}

		// Insert closing edge. addEdge() is automatically called by Edge-Constructor
		// if the polygon is already closed, discard edge
		if( !closed )
			newEdge( firstPoint, points.get( 0 ) );

		return createdEdges;
	}

	public void add( List<PlanPoint> points, boolean close ) {
		if( start == null ) {
			if( points.size() == 1 ) {
				start = points.get(0);
				end = points.get(0);
			} else {
				PlanPoint firstPoint = points.get( 0 );
				for( int i = 1; i < points.size(); i++ ) {
					PlanPoint secondPoint = points.get( i );
					// Don't create zero-length edges
					if( secondPoint.equals( firstPoint ) )
						continue;

					newEdge( firstPoint, secondPoint );
					firstPoint = secondPoint;
				}
			}
		} else {
			PlanPoint firstPoint = end;
			for( int i = 1; i < points.size(); ++i ) {
				PlanPoint secondPoint = points.get( i );
				// Don't create zero-length edges
				if( secondPoint.equals( firstPoint ) )
					continue;

				newEdge( firstPoint, secondPoint );
				firstPoint = secondPoint;
			}

		}
		if( !closed && close && !end.equals( points.get(0) ) )
			newEdge( end, points.get( 0 ) );
	}

	/**
	 * Inserts a new edge at one end of the polygon, if it is open. It is not
	 * possible to defineByPoints further edges to closed polygons, such polygons have to be
	 * opened before adding new edges. This method is only called, if a new
	 * instance of edge is created that should be part of this instance of the
	 * polygon. It is called from the constructor of {@link PlanEdge} and methods of
	 * {@literal PlanPolygon}.
	 * <p>During the adding process the necessary status information is updated.
	 * These are the start and end points, the offset of the entire polygon and
	 * its width and height.</p>
	 * @param e the edge to be added
	 * @throws java.lang.IllegalStateException if the polygon is closed. It is not
	 * possible to defineByPoints further edges to a closed polygon.
	 * @throws java.lang.IllegalArgumentException if the polygon already contains
	 * the edge or if the edge is not connected to the polygon
	 */
	// If the inserted edge closes the polygon, it is stored as the last one!
	// TODO Perform intersection-test
	//final void addEdge( T e ) throws IllegalStateException, IllegalArgumentException {
  public final void addEdge( T e ) throws IllegalStateException, IllegalArgumentException {
		if( start == null ) {
			// The instance is empty

			// Copy the given points before using them to avoid dealing with the case that
			// they are already connected to some other edges...
			start = new PlanPoint( e.getSource() );
			end = new PlanPoint( e.getTarget() );

			// This is neccessary to register the Edge at the PlanPoints
			e.setPoints( start, end, false );

			// Initialize bounds
			width = Math.abs( start.x - end.x );
			height = Math.abs( start.y - end.y );
			xOffset = e.boundLeft();
			yOffset = e.boundUpper();
		} else {
			if( isClosed() )
				throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.AddEdgeToClosedPolygonException" ) );

			if( fitsTogether( e, this ) ) {
				// --> "e" is the closing edge
				e.setPoints( end, start, false );
				end = start;
				closed = true;
			} else if( e.fits( start ) ) {
				// --> "e" must be inserted before the start

				// Copy the given points before using them to avoid dealing with the case that
				// they are already connected to some other edges...
				PlanPoint copyPoint = new PlanPoint( e.getOther( start ) );
				e.setPoints( copyPoint, start, false );
				start = copyPoint;
			} else if( e.fits( end ) ) {
				// --> "e" must be inserted after the end

				// Copy the given points before using them to avoid dealing with the case that
				// they are already connected to some other edges...
				PlanPoint copyPoint = new PlanPoint( e.getOther( end ) );
				e.setPoints( end, copyPoint, false );
				end = copyPoint;
			} else
				throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.CoordinateMismatchException" ) );
		}

		recomputeBoundsCheckEdge( e );
		recomputeBoundsUpdate();

		changed = true;
		size++;
	}

	/**
	 * Adds a new {@link PlanPoint} to the {@literal PlanPolygon}. The point is
	 * added at the beginning of the polygon. That means, an edge from
	 * the new point to {@literal start} is added to the polygon.
	 * @param p the point that is added
	 * @throws java.lang.IllegalStateException if the polygon is closed. It is not
	 * possible to defineByPoints further points to a closed polygon
	 * @throws java.lang.IllegalArgumentException if the new point is the start
	 * point
	 */
	public void addPointFirst( PlanPoint p ) throws IllegalArgumentException, IllegalStateException {
		if( start.equals( p ) )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.StartPointException" ) );
		// Insert closing edge. addEdge is automatically called by Edge-Constructor
		newEdge( p, start );
	}

	/**
	 * Adds a new {@link PlanPoint} to the {@literal PlanPolygon}. The point is
	 * added at the end of the polygon. That means, an edge from {@literal end}
	 * to the new point is added to the polygon.
	 * @param p the point that is added
	 * @throws java.lang.IllegalStateException if the polygon is closed.
	 * It is not possible to defineByPoints further points to a closed polygon
	 * @throws java.lang.IllegalArgumentException if the new point is the end point
	 */
	public void addPointLast( PlanPoint p ) throws IllegalArgumentException, IllegalStateException {
		if( end.equals( p ) )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.EndPointException" ) );
		// Insert closing edge. addEdge() is automatically called by Edge-Constructor
		newEdge( end, p );
	}

	/**
	 * Calculates the area of this polygon with the guassian area formula in
	 * square millimeters.
	 * @return the area
	 */
	public int area() {
		List<PlanPoint> points = this.getPolygonPoints();
		if( points.isEmpty() )
			return 0;
		int area = ((points.get( points.size() - 1 ).getYInt() + points.get( 0 ).getYInt()) * (points.get( points.size() - 1 ).getXInt() - points.get( 0 ).getXInt()));
		for( int i = 0; i < points.size() - 1; i++ )
			area += ((points.get( i ).getYInt() + points.get( i + 1 ).getYInt()) * (points.get( i ).getXInt() - points.get( i + 1 ).getXInt()));
		return (int)(Math.abs( area ) * 0.5f);
	}

	/**
	 * Calculates the area of this polygon with the gaussian area formula in
	 * square meters.
	 * @return the area
	 */
	public double areaMeter() {
		List<PlanPoint> points = this.getPolygonPoints();
		if( points.isEmpty() )
			return 0;
		double area = ((points.get( points.size() - 1 ).getYMeter() + points.get( 0 ).getYMeter()) * (points.get( points.size() - 1 ).getXMeter() - points.get( 0 ).getXMeter()));
		for( int i = 0; i < points.size() - 1; i++ )
			area += ((points.get( i ).getYMeter() + points.get( i + 1 ).getYMeter()) * (points.get( i ).getXMeter() - points.get( i + 1 ).getXMeter()));
		return Math.abs( area ) * 0.5f;
	}

	/**
	 * Returns the bounding box of this {@literal PlanPolygon}. The bounding box
	 * is the smallest {@link java.awt.Rectangle} that completely contains the
	 * whole polygon. The calculation of this bounding box is accurate in the
	 * integer coordinates of millimeter positions.
	 * @return a rectangle that defines the bounds
	 */
	public Rectangle bounds() {
		return new Rectangle( xOffset, yOffset, width, height );
	}

	/**
	 * Returns the bounding box of this {@literal PlanPolygon}. The bounding box
	 * is the smallest {@link java.awt.geom.Rectangle2D} that completely contains
	 * the whole polygon. The calculation of this bounding box is accurate in the
	 * integer coordinates of millimeter positions.
	 * @return a rectangle that defines the bounds
	 */
	public Rectangle2D bounds2D() {
		return (Rectangle2D)bounds();
	}

	/**
	 * Checks if this polygon is valid. That means, that it is closed, simple and
	 * has no self-cuts. If any invalid positions are found, an exception is
	 * thrown. If the parameter rasterized is true, it also checks if the polygon is
	 * really rasterized with a call of {@link PlanPolygon#checkRasterized() }).
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @param rasterized indicates that the {@link BuildingPlan} should be
	 * rasterized
	 * @throws PolygonNotClosedException if the polygon is not closed
	 * @throws PolygonNotRasterizedException if the polygon is not rasterized but should be
	 */
	public void check( boolean rasterized ) throws PolygonNotClosedException, PolygonNotRasterizedException {
		if( !isClosed() ) {
      System.err.println( this );
			throw new PolygonNotClosedException( this, this.toString() );
    }
		if( rasterized )
			checkRasterized();
	}

	/**
	 * Checks if this polygon is rasterized. That means, that all the
	 * {@literal x}- and {@literal y}-coordinates of the polygon are divisible
	 * by the default-value of the rastersize (in millimeters) and for all edges
	 * one of the following constraints holds:
	 * <ul>
	 * <li>the x-coordinates of the edge's start -and endpoint are the same</li>
	 * <li>the y-coordinates of the edge's start -and endpoint are the same</li>
	 * </ul>
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @throws PolygonNotRasterizedException if the polygon is not rasterized
	 */
	public void checkRasterized() throws PolygonNotRasterizedException {
		// gehe alle Edges des Polygons durch
		for( T edge : in(this.edgeIterator()) ) {
			// falls die x-Koordinate des Startpunktes der Kante nicht durch 400mm teilbar ist-->nicht gerastert
			if( !((edge.getSource().getXInt() % (BuildingPlan.rasterSize * 1000)) == 0) )
				throw new PolygonNotRasterizedException( this,
								ZLocalization.loc.getString( "ds.z.PlanPolygon.PointNotOnRasterException" ) );
			// falls die y-Koordinate des Startpunktes der Kante nicht durch 400mm teilbar ist-->nicht gerastert
			if( !((edge.getSource().getYInt() % (BuildingPlan.rasterSize * 1000)) == 0) )
				throw new PolygonNotRasterizedException( this,
								ZLocalization.loc.getString( "ds.z.PlanPolygon.PointNotOnRasterException" ) );
			// falls die x-Koordinate des Endpunktes der Kante nicht durch 400mm teilbar ist-->nicht gerastert
			if( !((edge.getTarget().getXInt() % (BuildingPlan.rasterSize * 1000)) == 0) )
				throw new PolygonNotRasterizedException( this,
								ZLocalization.loc.getString( "ds.z.PlanPolygon.PointNotOnRasterException" ) );
			// falls die y-Koordinate des Endpunktes der Kante nicht durch 400mm teilbar ist-->nicht gerastert
			if( !((edge.getTarget().getYInt() % (BuildingPlan.rasterSize * 1000)) == 0) )
				throw new PolygonNotRasterizedException( this,
								ZLocalization.loc.getString( "ds.z.PlanPolygon.PointNotOnRasterException" ) );
			// falls die x-Koordinaten der beiden Punkte der Kante nicht gleich sind
			// UND die y-Koordinaten der beiden Punkte der Kante nicht gleich sind-->nicht gerastert
			if( !edge.isHorizontal() && !edge.isVertical() )
				throw new PolygonNotRasterizedException( this,
								ZLocalization.loc.getString( "ds.z.PlanPolygon.PointNotOnRasterException" ) );
		}
	}

	/**
	 * Closes the polygon, adds the last edge.
	 * @throws java.lang.IllegalArgumentException if the polygon only consists of one edge
	 * @throws java.lang.IllegalStateException if the polygon is closed
	 */
	public void close() throws IllegalArgumentException, IllegalStateException {
		this.addPointLast( start );
	}

	/**
	 * Combines two consecutive edges to one edge with the same end points.
	 * <p>The runtime of this operation is O(1)</p>
	 * @param e1 First edge to combine. The given edge must be part of the polygon.
	 * If you only have the edge coordinates and not the edge instance, you MUST
	 * call getEdge (coords...) to obtain the edge instance first. It is NOT
	 * sufficient to construct a new dummy edge with the coordinates and to supply
	 * this dummy edge to combineEdges ().
	 * @param e2 Second edge to combine. The given edge must be part of the polygon.
	 * If you only have the edge coordinates and not the edge instance, you MUST
	 * call getEdge (coords...) to obtain the edge instance first. It is NOT
	 * sufficient to construct a new dummy edge with the coordinates and to supply
	 * this dummy edge to combineEdges ().
	 * @param keepMinSize If this is true, the polygon will throw an exception when
	 * you try to combine edges in a polygon with 3 edges or less. If it is false
	 * you can combine the edges independently of the polygon size.
	 * @return The joined edge (it was already inserted into the polygon) or null
	 * if two edgs were combined that were of the form (p1,p2)(p2, p1). In this
	 * case the combination is simply to cut them out of the polygon, so no new edge is created
	 * in that case.
	 * @throws java.lang.IllegalArgumentException if the edges are not contained
	 * in the polygon or if they are not consecutive.
	 * @throws java.lang.IllegalStateException If the polygon contains less than
	 * or equal to 3 edges. Then no edge can be deleted / no edges combined.
	 */
	public T combineEdges( T e1, T e2, boolean keepMinSize ) throws IllegalArgumentException,
					IllegalStateException {
		if( e1.getAssociatedPolygon() != this || e2.getAssociatedPolygon() != this )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.EdgeNotContained" ) );
		if( keepMinSize && (size - 1) < 3 )
			throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.NotEnoughEdgesException" ) );
		PlanPoint common = null;
		try {
			common = e1.commonPoint( e2 );
		} catch( IllegalArgumentException ex ) {
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.NoConsecutiveEdgesException" ) );
		}
		T result = null;

		try {
			PlanPoint e2_other = e2.getOther( common );
			PlanPoint e1_other = e1.getOther( common );

			if( e1_other.equals( e2_other ) ) {
				// We are combining two edges who have the same end points

				T edgeAfterE1 = (T)e1.getOtherNeighbour( e2 );
				T edgeAfterE2 = (T)e2.getOtherNeighbour( e1 );
				PlanPoint outwardPointE1 = e1.commonPoint( edgeAfterE1 );
				PlanPoint outwardPointE2 = e2.commonPoint( edgeAfterE2 );
				PlanPoint deletedPoint = e1.getOther( outwardPointE1 );

				boolean not_alone = (edgeAfterE1 != null || edgeAfterE2 != null) && size > 2;
				if( not_alone ) {
					PlanPoint resolvedPoint = null;
					if( edgeAfterE1 != null ) {
						// There are edges after e1
						edgeAfterE1.setPoint( outwardPointE1, outwardPointE2, true );
						resolvedPoint = outwardPointE2;
					} else /* if( edgeAfterE2 != null ) */ {
						// There are edges after e2
						edgeAfterE2.setPoint( outwardPointE2, outwardPointE1, true );
						resolvedPoint = outwardPointE2;
					}

					// Restore start & end if they were deleted
					if( start == deletedPoint )
						start = resolvedPoint;
					if( end == deletedPoint )
						end = resolvedPoint;

					// Update edge count
					size -= 2;
				} else
					// e1 and e2 have no neighbours
					// --> Either they are alone in the polygon, but in this case
					//     we would have thrown an exception above, because size
					//     would be 2 then or something went terribly wrong with
					//     our data structures.
					if( keepMinSize )
						throw new RuntimeException( ZLocalization.loc.getString(
										"ds.z.PlanPolygon.InternalError" ) );
					else {
						e1.delete();
						e2.delete();
					}

				result = null;
			} else {
				// We are combining two edges who are different from one another

				// Delete the edge with the less specialized type --> TeleportEdges + NormalEdge -> TeleportEdge
				boolean deleteE1 = e1.getClass().isAssignableFrom( e2.getClass() );

				// Mind the case that we deleted start or end point of our polygon
				// This must be done prior to the deletion of the points because the
				// event handlers that are triggered by the setting of the point below
				// will try to iterate over the polygon which will fail or lead to
				// infinite loops when start or end have invalid values.
				if( common == start )
					start = deleteE1 ? e1_other : e2_other;
				if( common == end )
					end = deleteE1 ? e1_other : e2_other;

				// really delete the one edge
				if( deleteE1 ) {
					e2.setPoint( common, e1_other, true );
				} else {
					e1.setPoint( common, e2_other, true );
				}

				size--;

				result = deleteE1 ? e2 : e1;
			}
		} finally {

		}
		return result;
	}

	/**
	 * Combines the two edges specified by three consecutive points to one edge
	 * defined by the first and third point.
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges, because we must first search the edges. Combine (e1,e2)
	 * is much more efficient.</p>
	 * @param p1 the first point
	 * @param p2 the second point
	 * @param p3 the third point
	 * @param keepMinSize If this is true, the polygon will throw an exception when
	 * you try to combine edges in a polygon with 3 edges or less. If it is false
	 * you can combine the edges independently of the polygon size.
	 * @throws IllegalArgumentException If the polygon contains less than
	 * or equal to 3 edges. Then no edge can be deleted / no edges combined.
	 * @see #combineEdges(de.tu_berlin.coga.zet.model.PlanEdge, de.tu_berlin.coga.zet.model.PlanEdge, boolean) 
	 */
	public void combineEdges( PlanPoint p1, PlanPoint p2, PlanPoint p3, boolean keepMinSize ) throws IllegalArgumentException {
		combineEdges( this.getEdge( p1, p2 ), getEdge( p2, p3 ), keepMinSize );
	}

	/** Combines a list of points by repeadetly calling combineEdges (e1, e2).
	 * <p>The running time is O(n + points.size ()).</p>
	 *
	 * @param points The points that are to be combined. Every pair of two consecutive
	 * points in this list must be the ending points of an edge within this polygon.
	 * @param keepMinSize If this is true, the polygon will throw an exception when
	 * you try to combine edges in a polygon with (3 + points.size() - 2) edges or less.
	 * If it is false you can combine the edges independently of the polygon size.
	 * @return the new combined edge
	 * @see #combineEdges(de.tu_berlin.coga.zet.model.PlanEdge, de.tu_berlin.coga.zet.model.PlanEdge, boolean)
	 */
	public T combineEdges( List<PlanPoint> points, boolean keepMinSize ) {
		if( points.size() < 3 )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.ListDoesNotContainenoughPointsException" ) );
		if( keepMinSize && (size - (points.size() - 2)) < 3 )
			throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.NotEnoughEdgesException" ) );
		T result = null;

		try {
			boolean reversed = false;
			ListIterator<PlanPoint> itPoints = null;

			// Get the first edge
			ListIterator<PlanPoint> myPoints = pointIterator( false );
			T combinationEdge = null;
			while( myPoints.hasNext() && itPoints == null ) {
				PlanPoint p = myPoints.next();

				if( itPoints == null )
					if( p.equals( points.get( 0 ) ) ) {
						itPoints = points.listIterator();
						reversed = false;
						combinationEdge = (T)p.getNextEdge();
					} else if( p.equals( points.get( points.size() - 1 ) ) ) {
						itPoints = points.listIterator( points.size() - 1 );
						reversed = true;
						combinationEdge = (T)p.getNextEdge();
					}
			}

			// This is *not* an else case!
			if( itPoints != null )
				while( reversed ? itPoints.hasPrevious() : itPoints.hasNext() ) {
					PlanPoint nextPoint = reversed ? itPoints.previous() : itPoints.next();

					if( !combinationEdge.getTarget().equals( nextPoint ) )
						throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.NotEqualException" ) + " (" + nextPoint + ", " + combinationEdge.getTarget() + ")" );
					else {
						// Combine a new edge
						combinationEdge = combineEdges( combinationEdge, (T)combinationEdge.getTarget().getNextEdge(), keepMinSize );

						if( combinationEdge.getTarget().equals( points.get( 0 ) ) || combinationEdge.getTarget().equals( points.get( points.size() - 1 ) ) )
							break;
					}
				}

		} finally {

		}

		return result;
	}

	/**
	 * <p>Checks the relative position of the {@literal PlanPolygon} with respect
	 * to an edge. The mode of the test can be setLocation using a {@link RelativePosition} enumeration
	 * which enables testing if the polygon is on the left or the right side of the polygon.</p>
	 * <p>Note that only edges of the border of the polygon should be used. However, no
	 * test is made and no exception is throws. But using different edges is quite senseless as the result
	 * will always be negative.</p>
	 * <p>The tests checks if a point near to edge (distance 20cm) is in the room. This leads to the
	 * limitation that very small polygons can not be tested correct.</p>
	 * <p>This function works with arbitrary edges and polygons, they <b>don't need do
	 * be rasterized</b>!</p>
	 * @param e the edge that is tested.
	 * @param where the direction, e.g. left or right
	 * @return true if the edge is on the border specified through the direction.
	 */
	public boolean relativePolygonPosition( PlanEdge e, RelativePosition where ) {
		PlanPoint startPoint = e.getSource();
		PlanPoint endPoint = e.getTarget();
		PlanPoint middle = new PlanPoint( (endPoint.x + startPoint.x) / 2, (endPoint.y + startPoint.y) / 2 );
		double newVectorX;
		double newVectorY;
		if( e.isHorizontal() ) {
			newVectorX = 0;
			newVectorY = 1;
		} else if( e.isVertical() ) {
			newVectorX = 1;
			newVectorY = -0;
		} else {
			double slope = (endPoint.y - startPoint.y) / (double)(endPoint.x - startPoint.x);
			double inverseSlope = -1 / slope;
			double norm = Math.sqrt( 1 + inverseSlope * inverseSlope );
			newVectorX = 1 / norm;
			newVectorY = inverseSlope / norm;
		}
		PlanPoint test1 = new PlanPoint( middle.x + 200 * newVectorX, middle.y + 200 * newVectorY, false );
		PlanPoint test2 = new PlanPoint( middle.x - 200 * newVectorX, middle.y + 200 * (-1) * newVectorY, false );
		switch( where ) {
			case Left:
				if( PlanPoint.orientation( endPoint, startPoint, test1 ) == 1 )
					return (contains( test1 ));
				if( PlanPoint.orientation( endPoint, startPoint, test2 ) == 1 )
					return (contains( test2 ));
				break;
			case Right:
				if( PlanPoint.orientation( endPoint, startPoint, test1 ) == -1 )
					return (contains( test1 ));
				if( PlanPoint.orientation( endPoint, startPoint, test2 ) == -1 )
					return (contains( test2 ));
				break;
			default:
				throw new java.lang.UnsupportedOperationException( "This Position is not supported" );
		}
		return false;
	}

	/**
	 * Checks whether a {@literal PlanPoint} is inside a {@literal PlanPolygon}
	 * or not. A point is considered inside if it is inside or on the bordering
	 * edges.
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @param p the tested point
	 * @return true, if it is inside
	 */
	public boolean contains( PlanPoint p ) {
		// it is not defined what happens if the point is _on_ one line
		boolean inside = false;
		if( p.getXInt() < xOffset || p.getYInt() < yOffset ||
						p.getXInt() > xOffset + width || p.getYInt() > yOffset + height )
			return false;
		else {
			// Test all edges, if they are inside
			Iterator<T> iter = edgeIterator( false );
			// Create the edge whose crossings are counted
			//Edge e = new Edge( p, new PlanPoint( p.getX() + 2 * width, p.getY() ), new PlanPolygon() );
			PlanPolygon<?> poly = new PlanPolygon<>( edgeClassType );
			//poly.addEdge( new Edge( p, new PlanPoint (p.getX () + 2 * width, p.getY ()) ) );
			PlanEdge testRay = new PlanEdge( new PlanPoint( p.getXInt(), p.getYInt() ), new PlanPoint( p.getXInt() + 2 * width, p.getYInt() ), poly );
			T current;
			while( iter.hasNext() ) {
				current = iter.next();
				// Check if the point is one of the end-points
				if( current.fits( p ) )
					return true;
				switch( PlanEdge.intersects( testRay, current ) ) {
					case Colinear:
						if( p.getXInt() < current.getMaxX() & p.getXInt() > current.getMinX() )
							return true;
						break;
					case Intersects:
						// "Normal" crossing
						inside = !inside;
						break;
					case IntersectsBorder:
						// Check if p is on the line or the ray goes through the end point of another line
						PlanEdge inverse_e = new PlanEdge( new PlanPoint( p.getXInt(), p.getYInt() ), new PlanPoint( p.getXInt() - 2 * width, p.getYInt() ), new PlanPolygon<>( edgeClassType ) );
						if( PlanEdge.intersects( inverse_e, current ) == PlanEdge.LineIntersectionType.IntersectsBorder )
							return true;
						else
							// the point lies on the ray
							// only count if the point is _not_ the lower one of the edge
							if( current.boundLower() != p.getYInt() )
								inside = !inside;
						break;
					case IntersectsPoint:
						if( p.matches( current.getSource() ) || p.matches( current.getTarget() ) )
							return true;
						break;
					case Connected:
						// Only count point one time. That is done as follows:
						// An edge is only counted if the point is _not_ the lower one of the edge
						//if (current.boundLower () != p.getXInt ()) {
						//	inside = !inside;
						//}
						break;
					case NotIntersects:
						// Nothing to do
						break;
				}
			}
		}
		return inside;
	}
	public boolean containsStrict( PlanPoint p ) {
		// it is not defined what happens if the point is _on_ one line
		boolean inside = false;
		if( p.getXInt() < xOffset || p.getYInt() < yOffset ||
						p.getXInt() > xOffset + width || p.getYInt() > yOffset + height )
			return false;
		else {
			// Test all edges, if they are inside
			Iterator<T> iter = edgeIterator( false );
			// Create the edge whose crossings are counted
			//Edge e = new Edge( p, new PlanPoint( p.getX() + 2 * width, p.getY() ), new PlanPolygon() );
			PlanPolygon<?> poly = new PlanPolygon<>( edgeClassType );
			//poly.addEdge( new Edge( p, new PlanPoint (p.getX () + 2 * width, p.getY ()) ) );
			PlanEdge e = new PlanEdge( new PlanPoint( p.getXInt(), p.getYInt() ), new PlanPoint( p.getXInt() + 2 * width, p.getYInt() ), poly );
			T current;
			while( iter.hasNext() ) {
				current = iter.next();
				// Check if the point is one of the end-points
				if( current.fits( p ) )
					return false;
				switch( PlanEdge.intersects( e, current ) ) {
					case Colinear:
						if( p.getXInt() < current.getMaxX() & p.getXInt() > current.getMinX() )
							return false;
						break;
					case Intersects:
						// "Normal" crossing
						inside = !inside;
						break;
					case IntersectsBorder:
						// Check if p is on the line or the ray goes through the end point of another line
						PlanEdge inverse_e = new PlanEdge( new PlanPoint( p.getXInt(), p.getYInt() ), new PlanPoint( p.getXInt() - 2 * width, p.getYInt() ), new PlanPolygon<>( edgeClassType ) );
						if( PlanEdge.intersects( inverse_e, current ) == PlanEdge.LineIntersectionType.IntersectsBorder )
							return false;
						else
							// the point lies on the ray
							// only count if the point is _not_ the lower one of the edge
							if( current.boundLower() != p.getYInt() )
								inside = !inside;
						break;
					case Connected:
						// Only count point one time. That is done as follows:
						// An edge is only counted if the point is _not_ the lower one of the edge
						//if (current.boundLower () != p.getXInt ()) {
						//	inside = !inside;
						//}
						break;
					case NotIntersects:
						// Nothing to do
						break;
				}
			}
		}
		return inside;
	}

	/**
	 * <p>Checks whether a {@literal PlanPolygon} is inside this polygon, or not.</p>
	 * <p>The current implementation only works on the bounding boxes of the polygons,
	 * which means the the results will only be correct for rectangular shapes.
	 * </p>
	 * @param poly the polygon
	 * @return true if the entire polygon is inside this polygon
	 */
	public boolean contains( PlanPolygon<?> poly ) {
		boolean result = containsI( poly );
		return result;
	}

	private boolean containsI( PlanPolygon<?> poly ) {
		// Check for points
		ListIterator<PlanPoint> pit = poly.pointIterator( false );
		while( pit.hasNext() ) {
			PlanPoint p = pit.next();
			if( !this.contains( p ) )
				return false;
		}
		// Teste auf kantenschnitt
		Iterator<? extends PlanEdge> ei2 = poly.edgeIterator( false );
		while( ei2.hasNext() ) {
			PlanEdge current = ei2.next();
			Iterator<T> ei1 = edgeIterator( false );
			ArrayList<PlanPoint> problemPoints = new ArrayList<>();
			//System.out.println( "List cleared " );
			while( ei1.hasNext() ) {
				T e = ei1.next();
				switch( PlanEdge.intersects( e, current ) ) {
					case Intersects:
						return false;
					case Colinear:
						if( !( current.getMinX() >= e.getMinX() & current.getMaxX() <= e.getMaxX() & current.getMinY() >= e.getMinY() & current.getMaxY() <= e.getMaxY() ) ) { // no problem
							// Points may create a problem
							if( isBetween( current.getSource(), e ) && !problemPoints.contains( current.getSource() ) )
								problemPoints.add( current.getSource() );
							if( isBetween( current.getTarget(), e ) && !problemPoints.contains( current.getTarget() ) )
								problemPoints.add( current.getTarget() );
							if( isBetween( e.getSource(), current ) && !problemPoints.contains( e.getSource() ) )
								problemPoints.add( e.getSource() );
							if( isBetween( e.getTarget(), current ) && !problemPoints.contains( e.getTarget() ) )
								problemPoints.add( e.getTarget() );
						}
						break;
					case IntersectsBorder:
						PlanPoint p = getIntersection( e, current );
						if( !problemPoints.contains( p ) )
							problemPoints.add( p );
						break;
					case Connected:
						if( current.fits( e.getSource() ) && !problemPoints.contains( e.getSource() ) )
							problemPoints.add( e.getSource() );
						else if( current.fits( e.getTarget() ) && !problemPoints.contains( e.getTarget() ) )
							problemPoints.add( e.getTarget() );
						break;
					case NotIntersects:
						break;
					case Superposed:
						if( !problemPoints.contains( e.getSource() ) )
							problemPoints.add( e.getSource() );
						if( !problemPoints.contains( e.getTarget() ) )
							problemPoints.add( e.getTarget() );
						break;
				}
			}
			// Iterieren über die Problempunkte (paarweise)
			for( int i = 0; i < problemPoints.size() - 1; i++ ) {
				PlanPoint middle = new PlanPoint( (problemPoints.get( i ).getXMeter() + problemPoints.get( i + 1 ).getXMeter()) * 0.5,
								(problemPoints.get( i ).getYMeter() + problemPoints.get( i + 1 ).getYMeter()) * 0.5 );
				if( !this.contains( middle ) )
					return false;
			}
		}
		return true;
	}

	static public PlanPoint getIntersection( PlanEdge e1, PlanEdge e2 ) {
		double A1 = e1.getTarget().y - e1.getSource().y; //y2-y1
		double B1 = e1.getTarget().x - e1.getSource().x; //x1-x2
		double C1 = A1 * e1.getTarget().x + B1 * e1.getSource().y;

		double A2 = e2.getTarget().y - e2.getSource().y; //y2-y1
		double B2 = e2.getTarget().x - e2.getSource().x; //x1-x2
		double C2 = A2 * e2.getTarget().x + B2 * e2.getSource().y;

		double det = A1 * B2 - A2 * B1;
		if( Math.abs( det ) <= 0.00000001 )
			//Lines are parallel
			throw new java.lang.IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.ParallelLinesException" ) );
		else {
			double x = (B2 * C1 - B1 * C2) / det;
			double y = (A1 * C2 - A2 * C1) / det;
			return new PlanPoint( (int)Math.rint( ConversionTools.roundScale3( x ) ), (int)Math.rint( ConversionTools.roundScale3( y ) ) );
		}
	}

	private boolean isBetween( PlanPoint p, PlanEdge e ) {
		return (p.getXInt() > e.getMinX() && p.getXInt() < e.getMaxX()) | (p.getYInt() > e.getMinY() && p.getYInt() < e.getMaxY());
	}

	/**
	 * Deletes this {@literal PlanPolygon}. That means, all edges are removed
	 * from the list of {@literal Edge}. After that all used references are setLocation
	 * to {@literal null}.
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @throws java.lang.IllegalArgumentException
	 * @throws java.lang.IllegalStateException
	 */
	void delete() throws IllegalArgumentException, IllegalStateException {
		// Do not use the polygon iterator here - he will fail because you
		// delete his cursor when deleting the current edge
		List<T> edges = getEdges();
		for( PlanEdge e : edges )
			e.delete();

		closed = false;
		changed = true;

		start = null;
		end = null;
	}

	/**
	 * Returns the {@link java.awt.Dimension} of this polygon. The with of the
	 * dimension is of the distance between the right- and leftmost, the height
	 * the distance between the upper- and lowermost point.
	 * @return the dimension of the polygon
	 */
	public Dimension dimension() {
		return new Dimension( width, height );
	}

	/**
	 * <p>Checks the equality of two instances of {@literal PlanPolygon}. Two
	 * instances are equal if and only if they have the same number of nodes and
	 * edges respectively and all odes have the same coordinates and order.</p>
	 * <p>If two instances consists of the same points but the ordering of them is
	 * reversed the polygons are assumed to be equal.</p>
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @param p the polygon whose equality is to be tested
	 * @return true if this instance and p are equal
	 * @see PlanPoint
	 */
	// what happens if p has no edges?
	public boolean equals( PlanPolygon<T> p ) {
		if( p == this )
			return true;
		if( p.getNumberOfEdges() == 0 && this.getNumberOfEdges() == 0 )
			return true;
		if( p.getNumberOfEdges() != this.getNumberOfEdges() )
			return false;
		if( p.isClosed() && !this.isClosed() )
			return false;
		if( p.isClosed() ) {
			PlanPoint pStart = null;
			ListIterator<PlanPoint> piter = p.pointIterator( false );
			while( piter.hasNext() ) {
				PlanPoint pp = piter.next();
				if( pp.equals( start ) ) {
					pStart = pp;
					break; // Stop iterating, we found the start
				}
			}
			if( pStart == null )
				return false;

			PlanPoint curMe = start;
			PlanPoint curP = pStart;
			for( int i = 0; i < this.getNumberOfEdges(); i++ ) {
				T e1 = (T)curMe.getNextEdge();
				T e2 = (T)curP.getNextEdge();
				if( !e1.equals( e2 ) )
					return false;
				curMe = e1.getOther( curMe );
				curP = e2.getOther( curP );
			}

			return true;
		}
		ListIterator<T> iter1;
		ListIterator<T> iter2;
		T current1 = getFirstEdge();
		T current2 = p.getFirstEdge();
		// TODO the polygons may have an arbitrary shift of their starting
		// points, but can still be equal
		if( current1.equals( current2 ) ) {
			iter1 = edgeIterator( false );
			iter2 = p.edgeIterator( false );
			while( iter1.hasNext() )
				if( !iter1.next().equals( iter2.next() ) )
					return false;
			return true;
		} else
			return false;
	}

	/** @return The number of edges that this polygon contains. */
	public int getNumberOfEdges() {
		return size;
	}

	/**
	 * Returns the number of points that this {@link PlanPolygon} contains.
	 * @return the number of points that this polygon contains
	 */
	public int getNumberOfPoints() {
		return closed ? size : size + 1;
	}

	/**
	 * Checks the equality of this instance of {@literal PlanPolygon} and an
	 * object. They are equal if and only if object is an instance of
	 * {@literal PlanPolygon} and if they have the same number of nodes and
	 * edges respectively and all odes have the same coordinates and order.
	 * <p>If two instances consists of the same points but the ordering of them is
	 * reversed the polygons are assumed to be equal.</p>
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @param obj the object whose equality is to be tested
	 * @return true if this instance and obj are equal
	 * @see PlanPoint
	 */
	@Override
	public boolean equals( Object obj ) {
		return obj instanceof PlanPolygon ? this.equals( (PlanPolygon)obj ) : false;
	}

	/**
	 * Checks whether an {@link PlanEdge} fits to the polygon, that means that it can
	 * be added as first or last edge. Note that the result is only positive, if
	 * the polygon is not closed.
	 * @param e the edge to be tested
	 * @return true if the edge can be added to the polygon
	 */
	public boolean fits( T e ) {
		return size == 0 ? true : (fits( e.getSource() ) || fits( e.getTarget() )) && !isClosed();
	}

	/**
	 * Checks whether a specified {@link PlanPoint} fits to the polygon, that
	 * means that it is equal either to the start point or the end point.
	 * @param p the point
	 * @return true if the point is start or end point
	 */
	public boolean fits( PlanPoint p ) {
		return fits( p, this );
	}

	/**
	 * Checks whether a specified {@link PlanPoint} fits to a specified
	 * {@literal PlanPolygon}, that means that it is equal either to the start
	 * point or the end point.
	 * @param p the point
	 * @param poly the polygon
	 * @return true if the point is start or end point of the polygon
	 */
	public static boolean fits( PlanPoint p, PlanPolygon<?> poly ) {
		return p.equals( poly.getStart() ) || p.matches( poly.getEnd() );
	}

	/**
	 * Checks whether a specified {@literal PlanPolygon} fits to the polygon.
	 * That means that they have the start and end point (or vice versa) in
	 * common.
	 * @param p the polygon
	 * @return true if the polygons fit together
	 */
	public boolean fitsTogether( PlanPolygon<?> p ) {
		return fitsTogether( p, this );
	}

	/**
	 * Checks whether an {@link PlanEdge} fits into a {@literal PlanPolygon}. An
	 * edge is said to fit, if the coordinates of the edge are the same as the
	 * {@literal start} and {@literal end} coordinates of the polygon. That
	 * means an edge fits if it will close the polygon.
	 * @param e the edge that is to be tested
	 * @param p the polygon that should fit together with the edge
	 * @return true if the edge fits and will close the polygon
	 */
	public static boolean fitsTogether( PlanEdge e, PlanPolygon<?> p ) {
		return e.fits( p.getEnd() ) && e.fits( p.getStart() ) && !p.isClosed();
	}

	/**
	 * Checks wheather two {@link PlanPoint}s fit to a {@literal PlanPolygon}. If two
	 * points, that are not equal, fit an edge with these two end points will
	 * close the polygon. If the polygon is closed, {@literal false} is returned.
	 * @param p1 one point that is tested
	 * @param p2 the other point that is tested
	 * @param polygon the polygon that is tested to fit to the two points
	 * @return {@literal true} if both points fit to a (not closed) polygon
	 * @see PlanPolygon#fits(PlanPoint)
	 */
	public static boolean fitsTogether( PlanPoint p1, PlanPoint p2, PlanPolygon<?> polygon ) {
		return polygon.fits( p1 ) && polygon.fits( p2 ) && !polygon.isClosed();
	}

	/**
	 * Tests if two instances of {@literal PlanPolygon} fit together, that means
	 * that they have start and end points in common. In this case, it is possible
	 * to merge them and a closed polygon is the result.
	 * @param p1 the first polygon
	 * @param p2 the second polygon
	 * @return true, if the start and end points of the polygons fit together and both polygons are not closed
	 */
	public static boolean fitsTogether( PlanPolygon<?> p1, PlanPolygon<?> p2 ) {
		boolean v = p1.getStart().equals( p2.getStart() ) && p1.getEnd().equals( p2.getEnd() );
		boolean w = p1.getStart().equals( p2.getEnd() ) && p1.getEnd().equals( p2.getStart() );
		return (v || w) && !(p1.isClosed() || p2.isClosed());
	}

	/**
	 * Get the instance of an edge that is in the polygon. This is needed due to
	 * two edges are considered equal if the two end points are equal. But to connect
	 * rooms it is neccessary to have the same instance in both polygons.
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * <p>In the case that the given edge is already contained in the polygon this
	 * method just returns it, and does noting.</p>
	 * @param e the edge
	 * @throws java.lang.IllegalArgumentException if the edge is not contained in the polygon
	 * @return the {@link PlanEdge} instance contained in the polygon
	 */
	public T getEdge( T e ) throws IllegalArgumentException {
		return (e.getAssociatedPolygon() == this) ? e : getEdge( e.getSource(), e.getTarget() );
	}

	/**
	 * Get an edge that is in the polygon. The edge is specified by the two end
	 * points.
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @param p1 one point
	 * @param p2 the other point
	 * @throws java.lang.IllegalArgumentException if the points do not form an
	 * edge in the polygon or if they are equal
	 * @return the {@link PlanEdge} instance contained in the polygon
	 */
	public T getEdge( PlanPoint p1, PlanPoint p2 ) throws IllegalArgumentException {
            if (p1.equals(p2)) {
                throw new IllegalArgumentException(ZLocalization.loc.getString("ds.z.PlanPolygon.EqualPointsException"));
            }
            for (T e : in(this.edgeIterator())) {
                if (e.fits(p1) && e.fits(p2)) {
                    return e;
                }
            }
            throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.EdgeNotFoundException" ) );
	}
        
        public boolean isContained( T edge ) {
            PlanPoint p1 = edge.getSource();
            PlanPoint p2 = edge.getTarget();
            for (T e : in(this.edgeIterator())) {
                if (e.fits(p1) && e.fits(p2)) {
                    return true;
                }
            }
            return false;
        }

	/**
	 * Tries to find an edge of the room, that contains the given point.
	 * @param p the point
	 * @return an edge containing the point, or {@literal null} otherwise
	 */
	public T getEdge( PlanPoint p ) {
		double eps = 0.01;

		for( T e : in(this.edgeIterator()) )  {
			if( isOnEdge( e, p, eps ) )
				return e;
		}
		return null;
	}

	public boolean isOnEdge( PlanEdge e, PlanPoint p, double eps ) {
		double det = PlanPoint.orientationE( e.getTarget(), e.getSource(), p );
		if( det <= eps ) {
			if( PlanEdge.length( e.getTarget(), p ) < e.length() && PlanEdge.length( e.getSource(), p ) < e.length() )
				return true;
		}
		return false;
	}

	/** Returns a view of the {@link java.util.List} of edges of the polygon. The view
	 * is obtained by running through the whole polygon and adding up the edges, so the
	 * running time of this operation is O(n). The returned list is editable, but the
	 * changes that are done to it will in no way be reflected in the polygon, that is,
	 * the list is fully independent from the polygon.
	 *
	 * @return the list of edges
	 */
	public List<T> getEdges() {
		ArrayList<T> values = new ArrayList<>( size );
		for( T e : in(this.edgeIterator() ) )
			values.add( e );
		return values;
	}

	/**
	 * Returns the end point of the polygon. The end and start points are saved
	 * during the process of building the polygon step by step.
	 * @return the end point of the polygon
	 */
	public PlanPoint getEnd() {
		return end;
	}

	/**
	 * Returns the height of the {@literal PlanPolygon}. That is the difference
	 * between the uppermost and lowermost y-coordinates of the contained points.
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the next after next point of a specified {@link PlanPoint} p. To
	 * find the searched point in the correct direction the {@link PlanEdge}
	 * containing the point is needed.
	 * <p>Note, that the next point is not the other point of the specified edge.
	 * </p>
	 * <p>The running time is O(1)</p>
	 * @param e An edge, that is part of the polygon
	 * @param p The source of target of the edge
	 * @return the next after next point of the point
	 * @throws java.lang.IllegalArgumentException if the point is not an end point of the edge
	 * @throws java.lang.IllegalStateException if the polygon is not closed
	 */
	public PlanPoint getPointAfterTheNext( T e, PlanPoint p ) throws IllegalArgumentException, IllegalStateException {
		if( e.getSource() != p && e.getTarget() != p )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.PointNotContainedInEdgeException" ) );
		if( !closed )
			throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.PolygonNotClosedException" ) );
		T e2 = (T)p.getOtherEdge( e ); // First neighbour
		PlanPoint p2 = e2.getOther( p ); // First next point
		T e3 = (T)p2.getOtherEdge( e2 ); // Second neighbour
		return e3.getOther( p2 ); // Second next point
	}

	/**
	 * Returns the points of the {@literal PlanPolygon} as view of a
	 * {@literal List}. The view
	 * is obtained by running through the whole polygon and adding up the edges, so the
	 * running time of this operation is O(n). The returned list is editable, but the
	 * changes that are done to it will in no way be reflected in the polygon, that is,
	 * the list is fully independent from the polygon.
	 * @return {@literal List} with {@link PlanPoint}-objects discribing the edges
	 * of the polygon
	 */
	public List<PlanPoint> getPolygonPoints() {
		ArrayList<PlanPoint> pointList = new ArrayList<>( size + (isClosed() ? 0 : 1) );

		Iterator<PlanPoint> itP = pointIterator( false );
		while( itP.hasNext() )
			pointList.add( itP.next() );

		return pointList;
	}

	/**
	 * Returns the start point of the polygon. The end and start points are
	 * during the process of bilding the polygon step by step.
	 * @return the start point of the polygon
	 */
	public PlanPoint getStart() {
		return start;
	}

	/**
	 * Returns the width of the {@literal PlanPolygon}. That is the difference
	 * between the leftmost and rightmost x-coordinates of the contained points.
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * The leftmost point coordinate of the {@literal PlanPolygon}.
	 * @return the leftmost coordinate
	 */
	public int getxOffset() {
		return this.xOffset;
	}

	/**
	 * The uppermost point coordinate of the {@literal PlanPolygon}.
	 * @return the uppermost coordinate
	 */
	public int getyOffset() {
		return this.yOffset;
	}

	/**
	 * Returns the leftmost {@literal x}-coordinate of the polygon
	 * @return the leftmost {@literal x}-coordinate of the polygon
	 */
	public int boundLeft() {
		return xOffset;
	}

	/**
	 * Returns the rightmost {@literal x}-coordinate of the polygon.
	 * @return the rightmost {@literal x}-coordinate of the polygon
	 */
	public int boundRight() {
		return xOffset + width;
	}

	/**
	 * Returns the biggest {@literal y}-coordinate of the polygon (this is then the lowest coordinate on screen).
	 * @return the biggest {@literal y}-coordinate of the polygon
	 */
	public int boundLower() {
		return yOffset + height;
	}

	/**
	 * Returns the smallest {@literal y}-coordinate of the polygon (this is then the upmost coordinate on screen)
	 * @return the smallest {@literal y}-coordinate of the polygon
	 */
	public int boundUpper() {
		return yOffset;
	}

	private PlanPoint intersectionPoint = null;

	/**
	 * Checks whether the polygon intersects with another one. The test is
	 * performed by the java.awt.Polygon.intersects() method.
	 * @param poly the polygon checked for intersection
	 * @return true if the two polygons intersect each other
	 */
	public boolean intersects( PlanPolygon<?> poly ) {
		for( PlanPoint p : this.getPlanPoints() ) {
			if( poly.contains( p ) ) {
				intersectionPoint = p.clone();
				return true;
			}
		}
		for( PlanPoint p : poly.getPlanPoints() ) {
			if( contains( p ) ) {
				intersectionPoint = p.clone();
				return true;
			}
		}
		intersectionPoint = null;
		return false;
	}


	/**
	 * Checks whether the polygon intersects with another one. The test is
	 * performed by the java.awt.Polygon.intersects() method.
	 * @param poly the polygon checked for intersection
	 * @return true if the two polygons intersect each other
	 */
	public boolean intersectsStrict( PlanPolygon<?> poly ) {
		for( PlanPoint p : this.getPlanPoints() ) {
			if( poly.containsStrict( p ) ) {
				intersectionPoint = p.clone();
				return true;
			}
		}
		for( PlanPoint p : poly.getPlanPoints() ) {
			if( containsStrict( p ) ) {
				intersectionPoint = p.clone();
				return true;
			}
		}
		intersectionPoint = null;
		return false;
	}

	public PlanPoint intersection( PlanPolygon<?> poly ) {
		intersects( poly );
		return intersectionPoint;
	}

	public PlanPoint intersectionStrict( PlanPolygon<?> poly ) {
		intersectsStrict( poly );
		return intersectionPoint;
	}

	/**
	 * Determines if the topology of this polygon has been changed since the last
	 * call of a geometric function like {@link #contains(PlanPolygon)},
	 * {@link #contains(PlanPoint)}, {@link #isClosed()} and
	 * {@link #intersects( PlanPolygon )}. This can be used to check, if the
	 * matrix into a point-based-structure is neccessary.
	 * @return true, if no change occured.
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * Determines whether the polygon is closed, or if it is only a linked list of
	 * edges.
	 * @return true, if the polygon is closed, false elsewise.
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Checks wheather an edge between two distinct points will close the polygon.
	 * @param p1 one point
	 * @param p2 the other point
	 * @return {@literal true} if the edge will close the polygon, {@literal false} otherwise
	 */
	public boolean willClose( PlanPoint p1, PlanPoint p2 ) {
		return fitsTogether( p1, p2, this );
	}

	/**
	 * Returns the maximal number of persons for this area.
	 * @return the maximal number of persons for this area
	 */
	public int getMaxEvacuees() {
		double area = Math.round( areaMeter() * 100 ) / 100.0;	// round correctly...
		return (int)Math.round( area / (0.4 * 0.4) );
	}

	/**
	 * Creates a new edge of the generic type out of two points that is associated
	 * to this instance. The super constuctor that is called needs to accept two
	 * {@link PlanPoint} objects and one {@literal PlanPolygon}.
	 * @param p1 one point of the edge
	 * @param p2 the other point
	 * @return the new instance of {@literal T}
	 */
	protected T newEdge( PlanPoint p1, PlanPoint p2 ) {
		return newEdge( p1, p2, this );
	}

	/**
	 * Creates a new edge of the generic type out of two points. The super
	 * constuctor that is called needs to accept two {@link PlanPoint} objects
	 * and one {@literal PlanPolygon}, which will be the associated polygon.
	 * @param p1 one point of the edge
	 * @param p2 the other point
	 * @param poly the associated polygon
	 * @return the new instance of {@literal T}
	 */
	private T newEdge( PlanPoint p1, PlanPoint p2, PlanPolygon<?> poly ) {
		T edge = null;
		try {
			edge = edgeClassType.getDeclaredConstructor( PlanPoint.class, PlanPoint.class ).newInstance( p1, p2 );
			// This calls addEdge internally
      edge.setAssociatedPolygon( poly );
      // TODO: fails if polygon contains two times the same poiont (closes then!)
		} catch( java.lang.NoSuchMethodException | java.lang.InstantiationException | java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException ex ) {
			throw new RuntimeException( ZLocalization.loc.getString( "ds.z.PlanPolygon.InternalError" ) );
		}
		return edge;
	}

	/**
	 * Creates a new edge of the specified generic type out of two points. The
	 * super
	 * constuctor that is called needs to accept two {@link PlanPoint} objects
	 * and one {@literal PlanPolygon}, which will be the associated polygon.
	 * @param p1 one point of the edge
	 * @param p2 the other point
	 * @param poly the associated polygon
	 * @return the new instance of {@literal T}
	 */
	// TODO exception
	private PlanEdge newEdge( PlanPoint p1, PlanPoint p2, PlanPolygon poly, Class<T> edgeClassType ) {
		T edge = null;
		try {
			edge = edgeClassType.getDeclaredConstructor( PlanPoint.class, PlanPoint.class ).newInstance( p1, p2 );
			// This calls addEdge internally
			edge.setAssociatedPolygon( poly );
		} catch( java.lang.NoSuchMethodException | java.lang.InstantiationException | java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException ex ) {
			//;
		}
		return edge;
	}

	/**
	 * Returns the first edge of the polygon or null if it is empty.
	 * @return The first edge of the polygon or null if it is empty
	 */
	public T getFirstEdge() {
		return (start != null) ? (T)start.getNextEdge() : null;
	}

	/**
	 * Returns the last edge of the polygon or null if it is empty
	 * @return the last edge of the polygon or null if it is empty.
	 */
	public T getLastEdge() {
		return (end != null) ? (T)end.getPreviousEdge() : null;
	}

	/**
	 * This is ONLY to be called by Edge.delete(). Other methods must never use
	 * this method!
	 * <p>
	 * Removes an {@link PlanEdge} from the polygon. For open polygons this will only
	 * work, if the edge is the first or last one. If the polygon consists only of
	 * one Edge, the polygon deletes itself in his owner-floor.</p>
	 * <p>Closed polygons are splitted up during delting process. Deleting the
	 * first or last edge needs time O(1). Deleting an edge in the middle of the
	 * polygon needs time O(n), where {@literal n} is the number of edges. This
	 * is because the internal datastructure containing the edges is built up
	 * completly new. The new start point is the first point of the first edge
	 * after the deleted edge, the end point will be the last one of the last
	 * edge before the edge. </p>
	 * <p>It is not possible to break up a not closed polygon because the result
	 * would be two polygons.</p>
	 * <p>The runtime of this operation is O(n), where {@literal n} is the
	 * number of edges.</p>
	 * @param e the edge to remove. Has to be an end edge.
	 * @throws java.lang.IllegalArgumentException sent from super-class, not supposed to occur
	 * @throws java.lang.IllegalStateException if the edge is not the first or last
	 * edge in the polygon. first and last edges also occur in closed polygons.
	 */
	public void removeEdge( T e ) throws IllegalArgumentException, IllegalStateException {
		PlanEdge prev = e.getSource().getPreviousEdge();
		PlanEdge next = e.getTarget().getNextEdge();
		int length = e.length();

		// Edge.delete() already deleted itself from prev and next, we just
		// have to get our new start and end points
		if( length != 0 && isClosed() ) {
			// Break up the polygon
			start = next.getSource();
			end = prev.getTarget();

			closed = false;
		} else {
			T first = getFirstEdge();
			T last = getLastEdge();

			if( length == 0 )
				if( prev != null && next != null )
					// This happens if three points in one row have the same position
					next.setSource( prev.getTarget(), true );
				else
					closed = false;

			// size has not yet been decremented (this happens below), so if we delete
			// the last edge size is still 1 at this position
			if( size == 1 ) {
				start = null;
				end = null;
				closed = false; // Deletion of zero-length edges keeps polygon closed until it is empty
			// At this point it is empty, so we setLocation closed to "false" explicitly
			} else if( first == e )
				start = first.getOther( start );
			else if( last == e )
				end = last.getOther( end );
			else if( length != 0 )
				throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.EdgeIsNotFirstOrLastOneExcpetion" ) );
		}

		changed = true;
		size--;
	}

	/**
	 * Replaces all edges of the polygon with new ones defined throug a list of
	 * points. The behaviour is the same as in {@link #defineByPoints(java.util.List) }. Before
	 * adding the points the polygon is reinitialized and all edges are deleted.
	 * @param points the list of points defining the shape of the polygon
	 * @see #defineByPoints( List )
	 */
	public void replace( List<PlanPoint> points ) {
		// Drop the current edges
		start = null;
		end = null;
		size = 0;
		closed = false;

		// ChangeEvent is thrown within the defineByPoints-Method (indirectly)
		defineByPoints( points );
	}

	/**
	 * Replaces a specified {@link PlanEdge} with some new edges specified as point
	 * list. The edge must fit with the points, that means the first and last
	 * point of the edge and the first and last point element have to be the same.
	 * However, it does not matter if the start point of the edge equals the last
	 * point of the list, or vice versa. The {@literal List} needs to be
	 * iterated in reverse order in the latter case.
	 * <p>The runtime of this operation is O(e), where {@literal e} is the
	 * number of edges that must be inserted.</p>
	 * <p>The operation will only work on closed polygons!</p>
	 * @param e the edge that is replaced. The given edge must be part of the polygon.
	 * If you only have the edge coordinates and not the edge instance, you MUST
	 * call getEdge (coords...) to obtain the edge instance first. It is NOT
	 * sufficient to construct a new dummy edge with the coordinates and to supply
	 * this dummy edge to replaceEdge ().
	 * @param points the list of new points. These first and last point must be equal
	 * to e.getSource() and e.getTarget() but need not be the same instance.
	 * @return The inserted edges as an ArrayList.
	 * @throws IllegalArgumentException If "e" is not in the polygon or if
 an edge would be inserted, that would have equal end points or if points
 contains less than 3 items.
	 * @throws IllegalStateException If the polygon is not closed
	 */
	public ArrayList<T> replaceEdge( T e, List<PlanPoint> points ) throws IllegalArgumentException {
		if( points.size() < 3 ) {
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.ListDoesNotContainenoughPointsException" ) );
    }
		if( !closed )
			throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.PolygonNotClosedException" ) );
		if( e.getAssociatedPolygon() != this )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.EdgeNotFoundException" ) );
		if( !(e.getSource().equals( points.get( 0 ) ) &&
						e.getTarget().equals( points.get( points.size() - 1 ) ) || e.getSource().equals( points.get( points.size() - 1 ) ) &&
						e.getTarget().equals( points.get( 0 ) )) ) {
      System.err.println( "Replacing edge " + e );
      System.err.println( " with " + points );
      e.getTarget().equals( points.get( points.size() - 1 ) );
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.ReplacementEndPointsMismatch" ) );
    }
		// Check for invalid points before having to revert the whole
		// replacement process when recognizing the error later
		PlanPoint lastPoint = null;
		for( PlanPoint currentPoint : points ) {
			if( lastPoint != null )
				if( lastPoint.equals( currentPoint ) )
					throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.SubsequentPointsEqualException" ) );
			lastPoint = currentPoint;
		}

		ArrayList<T> result = new ArrayList<>( points.size() - 1 );
		try {

			PlanPoint old_start = e.getSource();

			e.delete();

			PlanPoint current_start = old_start;
			boolean firstPoint = true;
			for( PlanPoint current_end : points ) {
				if( firstPoint )
					firstPoint = false;
				else
					result.add( (T)newEdge( new PlanPoint( current_start ), new PlanPoint( current_end ), this, (Class<T>)e.getClass() ) );
				current_start = current_end;
			}

		// Edge count is automatically adjusted by defineByPoints/removeEdge methods
		} finally {

		}
		return result;
	}

	/**
	 * Replaces an {@link PlanEdge} with some new edges specified as point list. The
	 * {@literal Edge} is also specified by its two end points.
	 * <p>The runtime of this operation is O(n), where {@literal n} is the number
	 * of edges.</p>
	 * <p>The operation will only work on closed polygons!</p>
	 * @param p1 the start point of the replaced edge
	 * @param p2 the end point of the replaced edge
	 * @param points the list of new points
	 * @return The inserted edges as an ArrayList.
	 * @see  PlanPolygon#replaceEdge(de.tu_berlin.coga.zet.model.PlanEdge, java.util.List)
	 */
	public ArrayList<T> replaceEdge( PlanPoint p1, PlanPoint p2, List<PlanPoint> points ) {
		return replaceEdge( getEdge( p1, p2 ), points );
	}

	/**
	 * This is a convenience method that returns all PlanPoints of all
	 * Edges of this polygon. Note that the intended behavior is of this method
	 * is to return all plan points that are connected to this Polygon, so
	 * {@link Room} f.e. overwrites this method to include all Area PlanPoints.
	 * @return a list of all points of the polygon
	 * @see PlanPolygon#getPlanPoints()
	 */
	public List<PlanPoint> getPlanPoints() {
		// The edges use common PlanPoint instances, so we don't
		// have to defineByPoints (edges.size () * 2) points.
		ArrayList<PlanPoint> planPoints = new ArrayList<>( size + (isClosed() ? 0 : 1) );
		getPlanPoints( planPoints );
		return planPoints;
	}

	/**
	 * This is a convenience method that adds all PlanPoints of all
	 * Edges of this polygon to the given list. Note that the intended behavior
	 * of this method is to return all plan points that are connected to this
	 * PlanPolygon, so {@link Room} f.e. overwrites this method to include
	 * all Area PlanPoints
	 * @param planPoints The list that the points will be added to
	 * @see PlanPolygon#getPlanPoints(java.util.List)
	 */
	public void getPlanPoints( List<PlanPoint> planPoints ) {
		Iterator<PlanPoint> itP = pointIterator( false );
		while( itP.hasNext() )
			planPoints.add( itP.next() );
	}

	/**
	 * Returns a string representation of the {@literal PlanPolygon} that
	 * textually represents the polygon.
	 * <p>A polygon is represented by the sequence of edges separated with
	 * a "-" in between. The result will look like
	 * </p>
	 * <blockquote>
	 * <pre>
	 * [(x1,y1)(x2,y2)] - [(x2,y2)(x3,y3)] - ... - [(xn,yn)(x1,y1)]
	 * </pre></blockquote>
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return getCoordinateString();
	}

	/**
	 * Returns a string representation of the {@literal PlanPolygon} containing the
	 * coordinates.
	 * @return a string representation
	 * @see #toString()
	 */
	public String getCoordinateString() {
		String ret = "";
		for( T e : in( this.edgeIterator() ) )
			if( ret.isEmpty(  ) )
				ret = e.toString();
			else
				ret += " - " + e.toString();
		return ret;
	}

	/**
	 * <p>This method splits up a closed poylgon into two parts. It deletes the
	 * two given edges from the polygon and adds two edges to the resulting parts
	 * of the original polygon so that at the end all resulting poylgons are
	 * closed again.</p>
	 * <p>This method does not check the size of the resulting polygons, so that
	 * you can create polygons that contain only 2 edges when using this method.
	 * In this case the one of the resulting polygons will be degenerated.</p>
	 * <p>The running time is O(n) because every edge that now is part of the
	 * newly created poylgon must be associated to the new polygon.</p>
	 * @param edge1 The first edge at which the polygon should be split up.
	 * (Will be removed fom the polygon)
	 * @param edge2 The second edge at which the polygon should be split up.
	 * (Will be removed fom the polygon)
	 * @return One of the two resulting slip-up polygons will be made the current (this) polygon and
	 * the other one will be returned here.
	 * @throws IllegalArgumentException If the one of the given edges is not part of the polygon of if
	 * they are adjacent.
	 * @throws IllegalStateException If the polygon is not closed
	 */
	public PlanPolygon<T> splitClosedPolygon( T edge1, T edge2 ) throws IllegalArgumentException, IllegalArgumentException {
		if( !closed )
			throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.PolygonNotClosedException" ) );
		if( edge1.getAssociatedPolygon() != this || edge2.getAssociatedPolygon() != this )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.EdgeNotContained" ) );
		if( edge1.isNeighbour( edge2 ) )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.AdjacentEdges" ) );

		// Save the edges' end points
		PlanPoint e1_start = edge1.getSource();
		PlanPoint e1_target = edge1.getTarget();
		PlanPoint e2_start = edge2.getSource();
		PlanPoint e2_target = edge2.getTarget();

		// Break up the polygon
		edge1.delete();

		// Set the new polygon for the edges on the one half of the split-up circle
		PlanPolygon<T> newPolygon = createPlainCopy();
		PlanPoint current = e1_target;
		while( current != e2_start ) {
			PlanPoint nextPoint = current.getNextEdge().getTarget();

			current.getNextEdge().setAssociatedPolygon( newPolygon );
			current = nextPoint;
		}

		// Delete second edge from "this"
		edge2.delete();

		// Close the two resulting polygons
		if( !e1_start.equals( e2_target ) )
			// Endpoints are not equal --> insert new edge
			newEdge( e1_start, e2_target, this );
		else {
			// Endpoints are equal --> Cannot defineByPoints new edge, setLocation new endpoint for last edge
			e1_start.getPreviousEdge().setTarget( e2_target, false );
			this.closed = true;
		}
		if( !e2_start.equals( e1_target ) )
			// Endpoints are not equal --> insert new edge
			newEdge( e2_start, e1_target, newPolygon );/* else {
		// Endpoints are equal --> The new polygon is already closed because the
		// points were added using the normal addEdge() method. This method closes
		// the polygon automatically when the start and end nodes match.
		}*/

		return newPolygon;
	}

	/**
	 * <p>This method splits up a closed poylgon into two parts. It deletes the
	 * two given edges from the polygon and adds two edges to the resulting parts
	 * of the original polygon so that at the end all resulting poylgons are
	 * closed again.</p>
	 * <p>This method does not check the size of the resulting polygons, so that
	 * you can create polygons that contain only 2 edges when using this method.
	 * In this case the one of the resulting polygons will be degenerated.</p>
	 * <p>The running time is O(n) because every edge that now is part of the
	 * newly created poylgon must be associated to the new polygon.</p>
	 * @param splitEdge The edge at which the polygon should be split up.
	 * (Will be removed fom the polygon)
	 * @return One of the two resulting slip-up polygons will be made the current (this) polygon and
	 * the other one will be returned here.
	 * @throws IllegalArgumentException If the given edge is not part of the polygon
	 * @throws IllegalStateException If the polygon is closed
	 */
	public PlanPolygon<T> splitUnclosedPolygon( T splitEdge ) throws IllegalArgumentException, IllegalArgumentException {
		if( closed )
			throw new IllegalStateException( ZLocalization.loc.getString( "ds.z.PlanPolygon.PolygonClosedException" ) );
		if( splitEdge.getAssociatedPolygon() != this )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.PlanPolygon.EdgeNotContained" ) );

		// Save the edges' end points
		PlanPoint e_start = splitEdge.getSource();
		PlanPoint e_target = splitEdge.getTarget();

		// Set the new polygon for the edges on the one half of the split-up circle
		PlanPolygon<T> newPolygon = createPlainCopy();
		PlanPoint current = start;
		while( current != e_start && current != e_target ) {
			PlanPoint nextPoint = current.getNextEdge().getTarget();

			current.getNextEdge().setAssociatedPolygon( newPolygon );
			current = nextPoint;
		}

		// Delete old edges
		splitEdge.delete();

		return newPolygon;
	}

	/**
	 * This method copies the current polygon without it's edges. Every other
	 * setting, as f.e. the floor for Rooms or the associated Room for Areas is
	 * kept as in the original polygon.
	 * @return the newly created polygon
	 */
	protected PlanPolygon<T> createPlainCopy() {
		return new PlanPolygon<>( edgeClassType );
	}

	public void recomputeBounds() {
		minx = Integer.MAX_VALUE;
		maxx = Integer.MIN_VALUE;
		miny = Integer.MAX_VALUE;
		maxy = Integer.MIN_VALUE;
		for( T e : in(this.edgeIterator() ) )
			recomputeBoundsCheckEdge( e );
		recomputeBoundsUpdate();
	}

	private void recomputeBoundsCheckEdge( T e ) {
		if( e.boundLeft() < minx )
			minx = e.boundLeft();
		if( e.boundRight() > maxx )
			maxx = e.boundRight();
		if( e.boundUpper() < miny )
			miny = e.boundUpper();
		if( e.boundLower() > maxy )
			maxy = e.boundLower();
	}

	private void recomputeBoundsUpdate() {
		xOffset = minx;
		yOffset = miny;
		height = maxy-miny;
		width = maxx-minx;
		if( height < 0 )
			throw new IllegalStateException( "Height < 0" );
		if( width < 0 )
			throw new IllegalStateException( "Width < 0" );
	}

	public final PlanPoint transformPlanPoint( PlanPoint p, int[][] m ) {
		return new PlanPoint( m[0][0] * p.getXInt() + m[0][1] * p.getYInt(), m[1][0] * p.getXInt() + m[1][1] * p.getYInt() );
	}

	public int[][] calculateTransformMatrix( PlanPoint p1, PlanPoint p2 ) {
		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double m = ((y2 - y1) / (x2 - x1));
		if( x1 < x2 )
			if( m >= 0 )
				if( Math.abs( m ) >= 1.0 )
					return Transformation.flipMainDiagonal.matrix;
				else
					return Transformation.identity.matrix;
			else if( Math.abs( m ) >= 1.0 )
				return Matrix.matrixMultiplication( Transformation.flipMainDiagonal.matrix, Transformation.flipXAxis.matrix );
			else
				return Transformation.flipXAxis.matrix;
		else if( m >= 0 )
			if( Math.abs( m ) >= 1.0 )
				return Matrix.matrixMultiplication( Matrix.matrixMultiplication( Transformation.flipMainDiagonal.matrix, Transformation.flipXAxis.matrix ), Transformation.flipYAxis.matrix );
			else
				return Matrix.matrixMultiplication( Transformation.flipYAxis.matrix, Transformation.flipXAxis.matrix );
		else if( Math.abs( m ) >= 1.0 )
			return Matrix.matrixMultiplication( Transformation.flipMainDiagonal.matrix, Transformation.flipYAxis.matrix );
		else
			return Transformation.flipYAxis.matrix;
	}

	public int[][] calculateRetransformMatrix( PlanPoint p1, PlanPoint p2 ) {
		final double m = ((p2.getY() - p1.getY()) / (p2.getX() - p1.getX()));	// the slope
		if( p1.getX() < p2.getX() )
			if( m >= 0 )
				if( Math.abs( m ) >= 1.0 )
					return Transformation.flipMainDiagonal.matrix;
				else
					return Transformation.identity.matrix;
			else if( Math.abs( m ) >= 1.0 )
				return Matrix.matrixMultiplication( Transformation.flipXAxis.matrix, Transformation.flipMainDiagonal.matrix );
			else
				return Transformation.flipXAxis.matrix;
		else if( m >= 0 )
			if( Math.abs( m ) >= 1.0 )
				return Matrix.matrixMultiplication( Matrix.matrixMultiplication( Transformation.flipYAxis.matrix, Transformation.flipXAxis.matrix ), Transformation.flipMainDiagonal.matrix );
			else
				return Matrix.matrixMultiplication( Transformation.flipXAxis.matrix, Transformation.flipYAxis.matrix );
		else if( Math.abs( m ) >= 1.0 )
			return Matrix.matrixMultiplication( Transformation.flipYAxis.matrix, Transformation.flipMainDiagonal.matrix );
		else
			return Transformation.flipYAxis.matrix;
	}

	/**
	 * This method rasters a PlanPolygon. In detail all diagonal edges are
	 * converted to edges with 90 or 180 degree. Also all
	 * start and end edge point are moved to lay on a raster of 40x40cm.
	 */
	void rasterize() {
		try {
			ArrayList<T> alertDoors = new ArrayList<>();

			// Don't use for(T e: this) here - The edge replacements will screw up our iterator
			for( T e : getEdges() ) {
				int oldLength = e.length();
				int newLength = 0;
				newLength = rasterEdge( e, false );
				if( oldLength - 199 > newLength )
					alertDoors.add( e );
			}
			recomputeBounds( );
		} finally {

		}
	}

	/**
	 * This method rasters an edge inside this polygon. At the end the original
	 * edge is replaced by the generated, rastered, edges.
	 * @param edgeToRasterize The edge that shall be rastered
	 * @param check if this variable is true, a cleanUp() is called to erase badly constructed edges
	 * @return
	 */
	private int rasterEdge( T edgeToRasterize, boolean check ) {
		PlanPoint p1 = edgeToRasterize.getSource();
		PlanPoint p2 = edgeToRasterize.getTarget();
		PlanPoint work1, work2, predecessor;
		int[][] transformMatrix, retransformMatrix;
		int deltaX, deltaY, xCord, yCord, yCordPredecessor, newLength = 0;
		double m;
		boolean flip = false;

		//rasterizes the coordinates of an edge

		p1.setLocation( Math.round( p1.getXInt() / 400 ) * 400, Math.round( p1.getYInt() / 400 ) * 400 );
		p2.setLocation( Math.round( p2.getXInt() / 400 ) * 400, Math.round( p2.getYInt() / 400 ) * 400 );

		//rasterizes the gradient of an edge

		//checks, if the gradient is on the raster is not 0, then rasterize it, otherwise do nothing
		if( !((p1.getXInt() == p2.getXInt()) || (p1.getYInt() == p2.getYInt())) ) {
			System.err.println("diagonal edge: edge replace by the following edges:");
			//transform work-points
      
			// due to calculate always deterministic check if the edge is directed in positive y-direction
			// otherwise flip the edge and in the and roll over all created new PlanPoints
			if( p1.getYInt() - p2.getYInt() < 0 ) {
				transformMatrix = this.calculateTransformMatrix( p1, p2 );
				retransformMatrix = this.calculateRetransformMatrix( p1, p2 );
				work1 = this.transformPlanPoint( p1, transformMatrix );
				work2 = this.transformPlanPoint( p2, transformMatrix );
			} else {
				flip = true;
				transformMatrix = this.calculateTransformMatrix( p2, p1 );
				retransformMatrix = this.calculateRetransformMatrix( p2, p1 );
				work1 = this.transformPlanPoint( p2, transformMatrix );
				work2 = this.transformPlanPoint( p1, transformMatrix );
			}

			deltaX = work2.getXInt() - work1.getXInt();
			deltaY = work2.getYInt() - work1.getYInt();
			m = (double)deltaY / deltaX;

			//start is the first point of this edge. so it's the beginning PlanPoint of the following created edges
			PlanPoint start = work1;
			//start.setLocation(this.transformPlanPoint(start,retransformMatrix).getXInt(),
			//this.transformPlanPoint(start,retransformMatrix).getYInt());

			//System.out.println("edge to build: ("+work1.getXInt()+","+
			//work1.getYInt()+") - ("+work2.getXInt()+","+work2.getYInt()+")");

			//contains the new created PlanPoints representing the rasterized edge
			//we chose a linked list here because we will typically only defineByPoints elements
			//and iterate over all elements at the end, so linked lists are best suited here
			LinkedList<PlanPoint> newBetweenPlanPoints = new LinkedList<>();
			if( flip ) {
				newBetweenPlanPoints.add( p2 );
      } else {
				newBetweenPlanPoints.add( p1 );
      }
			PlanPoint nextPlanPoint;

			//System.out.println("parameter for-schleife: ("+work1.getXInt()+","+work2.getXInt()+")");

			for( xCord = work1.getXInt() + 400; xCord <= work2.getXInt() + 100; xCord += 400 ) {
				//crossingpoint with the next x-coordinate
				yCord = (int)Math.round( start.getYInt() + (xCord - start.getXInt()) * m );
        
				//creates the next PlanPoint which has to be setLocation due to rasterization.
        // if we are at the last plan point, insert the original plan point again!
        nextPlanPoint = new PlanPoint( xCord, Math.round( ( yCord / 400.0f) ) * 400 );        

				//check, if nextPlanPoint has the same y-value as its predecessor and the predecessor of this one in newPlanPoints
				//if yes then delete the predecessor.
				//else then insert between-point

				//if the y-value of nextPlanPoint is the same as it's predesessor, we do not have to "walk around the corner",
				//otherwise we have to decide if we go left down or down left
				predecessor = this.transformPlanPoint( newBetweenPlanPoints.get( newBetweenPlanPoints.size() - 1 ), transformMatrix );
				if( predecessor.getYInt() == nextPlanPoint.getYInt() ) {
					//do nothing :)
				} else {
					// case: y-levels are different: insert inbetween point
					// method: calculate the half of the distance between the crosspoints the original edge has with the raster in
					// this square and decides when it's greater than 0.5, it goes over otherwise under the line!! (8=)
					// necessary: the exakt crosspoints, not the rounded ones!
					yCordPredecessor = (int)Math.round( start.getYInt() + (xCord - 400 - start.getXInt()) * m );

					if( yCordPredecessor - this.transformPlanPoint( newBetweenPlanPoints.get( newBetweenPlanPoints.size() - 1 ),
									transformMatrix ).getYInt() + (yCord - yCordPredecessor) / 2 < 200 )
						newBetweenPlanPoints.add( this.transformPlanPoint( new PlanPoint( xCord,
										this.transformPlanPoint( newBetweenPlanPoints.get( newBetweenPlanPoints.size() - 1 ),
										transformMatrix ).getYInt() ), retransformMatrix ) );
					else
						newBetweenPlanPoints.add( this.transformPlanPoint( new PlanPoint( xCord - 400,
										this.transformPlanPoint( newBetweenPlanPoints.get( newBetweenPlanPoints.size() - 1 ),
										transformMatrix ).getYInt() + 400 ), retransformMatrix ) );
				}

				//insert nextPlanPoint
				//System.out.println("inserted: ("+this.transformPlanPoint(nextPlanPoint,retransformMatrix).getXInt()+","+
				//this.transformPlanPoint(nextPlanPoint,retransformMatrix).getYInt()+")");
        
        PlanPoint toAdd = this.transformPlanPoint( nextPlanPoint, retransformMatrix );
        
        if( flip ) {
          if( toAdd.matches( p1 ) ) {
            toAdd = p1;
          }
        } else {
          if( toAdd.matches( p2 ) ) {
            toAdd = p2;
          }
        }
				newBetweenPlanPoints.add( toAdd );
			}
			//not necessary, is inserted by for-loop
			//newBetweenPlanPoints.defineByPoints(p2);
			//check, whether a roll over is necessary
			if( flip ) {
				flip = false;
				LinkedList<PlanPoint> tmpBetweenPlanPoints = new LinkedList<>();
				for( int i = newBetweenPlanPoints.size() - 1; i >= 0; i-- )
					tmpBetweenPlanPoints.add( newBetweenPlanPoints.get( i ) );
				newBetweenPlanPoints = tmpBetweenPlanPoints;
			}

			/* Fast pre-construction cleanup: Delete points that lie on the same connection
			 * line between their predecessor and their successor. Those points insert
			 * unneccessary edges and slow down further computations without being useful
			 * This will not prevent the creation of unneeded edges globally, it's only a
			 * small & fast method to ensure that at least there won't be any new unneccessary
			 * edges from the view of this edge. Nevertheless the generated edges may be
			 * unneccessary from the view of the polygon as a whole.
			 */
			ListIterator<PlanPoint> itED = newBetweenPlanPoints.listIterator();
			PlanPoint current = null;
			PlanPoint last = null;
			PlanPoint secondLast = null;
			PlanPoint thirdLast = null;
			while( itED.hasNext() ) {
				current = itED.next();

				// Check for unneccessary points
				if( current != null && last != null && secondLast != null )
					if( (current.x == last.x && last.x == secondLast.x) ||
									(current.y == last.y && last.y == secondLast.y) ) {

						// Delete the "last" point
						itED.previous(); // moves list pointer back to 'before "current"'
						itED.previous(); // moves list pointer back to 'before "last"'
						itED.remove();
						itED.next(); // moves list pointer to 'behind "current"'

						// Delete the PlanPoint "last" from the temporary data that we keep
						last = secondLast;
						secondLast = thirdLast;
					}

				// Get one step further
				thirdLast = secondLast;
				secondLast = last;
				last = current;
			}

			//uncomment this to get a console-output
			/*for(int i = 0; i < newBetweenPlanPoints.size()-1; i++){
			newLength = newLength + Edge.length(newBetweenPlanPoints.get(i),newBetweenPlanPoints.get(i+1));
			System.out.println(newBetweenPlanPoints.get(i)+" - "+newBetweenPlanPoints.get(i+1));
			}*/
			//System.out.println("===========");
			//replace the existing edge with the new created bunch of rasterized edges
			this.replaceEdge( edgeToRasterize, newBetweenPlanPoints );
			if( check ) {
				//this.cleanUpAfterRasterization();
			}
		} else {
			newLength += PlanEdge.length( p1, p2 );
    }

		return newLength;
	}

	/**
	 * Convenience method that always iterates over the whole polygon.
	 * @param fromEnd if the iteration should start at the beginning or at the end
	 * @return the edge iterator pointing at the start or end point
	 * @see PlanPolygon#edgeIterator(PlanPoint,PlanPoint,boolean)
	 */
	public ListIterator<T> edgeIterator( boolean fromEnd ) {
		return new EdgeIterator( this.start, this.end, fromEnd );
	}

	/**
	 * Returns a new iterator that the caller can use to iterate over the PlanPolygon's
	 * edges. The iterator will be sensitive to changes that are made during it's
	 * iteration, which means that f.e. edges which are inserted during the iteration
	 * will be returned by calls to next() in case they were inserted before the
	 * iterator's cursor.
	 * @param start The starting point of the iterator.
	 * @param end The end point of the iterator.
	 * @param fromEnd If this flag is "true" the iterator's initial position will be at
	 * the "end" PlanPoint so that the first call to previous () will return the edge
	 * before "end".
	 * @return a new iterator over all edges
	 */
	public ListIterator<T> edgeIterator( PlanPoint start, PlanPoint end, boolean fromEnd ) {
		return new EdgeIterator( start, end, fromEnd );
	}

	/**
	 * Returns a new iterator that the caller can use to iterate over the PlanPolygon's
	 * edges. The iterator will be sensitive to changes that are made during it's
	 * iteration, which means that f.e. edges which are inserted during the iteration
	 * will be returned by calls to next() in case they were inserted before the
	 * iterator's cursor.
	 * @return a new iterator over all edges
	 */
	/*@Override*/
	public ListIterator<T> edgeIterator() {
		return edgeIterator( false );
	}

	/**
	 * Convenience method that always iterates over the whole polygon.
	 * @param fromEnd indicates if the iteration starts at the end point
	 * @return an iterator over all points
	 * @see PlanPolygon#pointIterator(PlanPoint,PlanPoint,boolean)
	 */
	public ListIterator<PlanPoint> pointIterator( boolean fromEnd ) {
		return new PointIterator( this.start, this.end, fromEnd );
	}

	/**
	 * Returns a new iterator that the caller can use to iterate over the PlanPolygon's
	 * PlanPoints. The iterator will be sensitive to changes that are made during it's
	 * iteration, which means that f.e. PlanPoints of edges which are inserted during the
	 * iteration will be returned by calls to next() in case they were inserted before
	 * the iterator's cursor.
	 * @param start The starting point of the iterator.
	 * @param end The end point of the iterator.
	 * @param fromEnd If this flag is "true" the iterator's initial position will be at
	 * the "end" PlanPoint so that the first call to previous () will return "end".
	 * @return a new iterator over all points between the two specified points
	 */
	public ListIterator<PlanPoint> pointIterator( PlanPoint start, PlanPoint end, boolean fromEnd ) {
		return new PointIterator( start, end, fromEnd );
	}

	/** An iterator to iterate over a polygon's edges. */
	private class EdgeIterator implements ListIterator<T> {
		private PlanPoint start;
		private PlanPoint end;
		private T cursor;
		private boolean beforeStart;

		/** Iterates over the given part of the polygon.
		 * @param start The starting point of the iterator.
		 * @param end The end point of the iterator.
		 * @param startAtEnd Whether the iteration should start at the
		 * given end point (true) or at the start point (false). */
		public EdgeIterator( PlanPoint start, PlanPoint end, boolean startAtEnd ) {
			this.start = start;
			this.end = end;
			this.cursor = (T)(startAtEnd ? ((end != null) ? end.getPreviousEdge() : null) : ((start != null)
							? start.getNextEdge() : null));

			beforeStart = !startAtEnd;
		}

		@Override
		public boolean hasNext() {
			return cursor != null &&
							(beforeStart || (cursor.getTarget() != null && cursor.getTarget() != end));
		}

		@Override
		public T next() {
			if( hasNext() ) {
				if( beforeStart )
					beforeStart = false;
				else
					cursor = (T)cursor.getTarget().getNextEdge();

				return cursor;
			} else
				return null;
		}

		@Override
		public boolean hasPrevious() {
			return cursor != null &&
							!beforeStart && cursor.getSource() != null;
		}

		@Override
		public T previous() {
			if( hasPrevious() ) {
				cursor = (T)cursor.getSource().getPreviousEdge();

				if( cursor == start.getNextEdge() )
					beforeStart = true;

				return cursor;
			} else
				return null;
		}

		@Override
		public int nextIndex() {
			throw new UnsupportedOperationException(
							"Indexes are not supported within PlanPolygon iterators!" );
		}

		@Override
		public int previousIndex() {
			throw new UnsupportedOperationException(
							"Indexes are not supported within PlanPolygon iterators!" );
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(
							"Use 'Edge.delete()' to delete Edges from PlanPolygons!" );
		}

		@Override
		public void set( T e ) {
			throw new UnsupportedOperationException(
							"Use 'new Edge()' / 'edge.delete()' to modify the edge list!" );
		}

		@Override
		public void add( T e ) {
			throw new UnsupportedOperationException(
							"Use 'new Edge()' to create new Edges!" );
		}
	}

    /**
     * An iterator to iterate over a PlanPolygon's PlanPoints.
     */
    private class PointIterator implements ListIterator<PlanPoint> {

        private PlanPoint start;
        private PlanPoint end;
        private PlanPoint cursor;
        private boolean beyondEnd;
        private boolean beforeStart;

        /**
         * Iterates over the given part of the polygon.
         *
         * @param start The starting point of the iterator.
         * @param end The end point of the iterator.
         * @param startAtEnd Whether the iteration should start at the given end point (true) or at the start point
         * (false).
         */
        public PointIterator(PlanPoint start, PlanPoint end, boolean startAtEnd) {
            this.start = start;
            this.end = end;
            this.cursor = startAtEnd ? end : start;

            beforeStart = !startAtEnd;
            beyondEnd = startAtEnd;
        }

        @Override
        public boolean hasNext() {
            return cursor != null
                    && (beforeStart || (cursor.getNextEdge() != null
                    && // If the polygon is closed, we don't return the "end" point a second time (end == start)
                    ((closed && (start.equals(end))) ? !cursor.getNextEdge().getTarget().equals(end) : !beyondEnd)));
        }

        @Override
        public PlanPoint next() {
            if (hasNext()) {
                if (beforeStart) {
                    beforeStart = false;
                } else {
                    cursor = cursor.getNextEdge().getTarget();
                    if (cursor.equals(end)) {
                        beyondEnd = true;
                    }
                }

                return cursor;
            } else {
                return null;
            }
        }

        @Override
        public boolean hasPrevious() {
            return cursor != null
                    && (beyondEnd || (cursor.getPreviousEdge() != null
                    && // If the polygon is closed and start==end, we don't return the "start" point a second time
                    ((closed && (start.equals(end))) ? !cursor.getPreviousEdge().getSource().equals(start) : !beforeStart)));
        }

        @Override
        public PlanPoint previous() {
            if (hasPrevious()) {
                if (beyondEnd) {
                    beyondEnd = false;
                } else {
                    cursor = cursor.getPreviousEdge().getOther(cursor);
                    if (cursor.equals(start)) {
                        beforeStart = true;
                    }
                }

                return cursor;
            } else {
                return null;
            }
        }

        @Override
        public int nextIndex() {
            throw new UnsupportedOperationException("Indexes are not supported within PlanPolygon iterators!");
        }

        @Override
        public int previousIndex() {
            throw new UnsupportedOperationException("Indexes are not supported within PlanPolygon iterators!");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Use 'Edge.delete()' to delete Edges from PlanPolygons!");
        }

        @Override
        public void set(PlanPoint e) {
            throw new UnsupportedOperationException("Use 'new Edge()' / 'edge.delete()' to modify the edge list!");
        }

        @Override
        public void add(PlanPoint e) {
            throw new UnsupportedOperationException("Use 'new Edge()' to create new Edges!");
        }
    }
}
