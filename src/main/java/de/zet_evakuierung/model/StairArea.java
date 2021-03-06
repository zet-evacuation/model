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
import de.zet_evakuierung.model.exception.PolygonNotClosedException;
import de.zet_evakuierung.model.exception.PolygonNotRasterizedException;
import de.zet_evakuierung.model.exception.StairAreaBoundaryException;
import java.util.ListIterator;

/**
 * Represents a Stair (whether in a room or between floors). The "upper" and
 * "lower" parts of the stair are denoted by two series of adjacent edges, which
 * are specified by pointers to the begin and end of each edge progression.
 * The purpose of this description of stairs is, that we can now specify two
 * different SpeedFactors (like for a DelayArea) for going up and going down.
 * Every StairArea is associated to exactly one {@link Room} at every time.
 *
 * @author Timon Kelter
 */
@XStreamAlias("stairArea")
public class StairArea extends AreaImpl {
	private final PlanPoint[] lowerLevel = new PlanPoint[2];
	private final PlanPoint[] upperLevel = new PlanPoint[2];
	@XStreamAsAttribute()
	private double speedFactorDown;
	@XStreamAsAttribute()
	private double speedFactorUp;

	/**
	 * Constucts a new {@code StairArea} with default {@code speedFactors}
	 * and without marked levels.
	 *
	 * @param room The room to which the area belongs
	 */
	StairArea( RoomImpl room ) {
		this( room, null, null, null, null, 0.8d, 0.6d );
	}

	/**
	 * Constucts a new {@code StairArea} with default {@code speedFactors}
	 * @param room The room to which the area belongs
	 * @param lowerLevelStart The plan point at which the edge progression starts that
	 * constitutes the lower part of the stair
	 * @param lowerLevelEnd The plan point at which the edge progression ends that
	 * constitutes the lower part of the stair
	 * @param upperLevelStart The plan point at which the edge progression starts that
	 * constitutes the upper part of the stair
	 * @param upperLevelEnd The plan point at which the edge progression ends that
	 * constitutes the upper part of the stair
	 */
	StairArea( RoomImpl room, PlanPoint lowerLevelStart, PlanPoint lowerLevelEnd,
										PlanPoint upperLevelStart, PlanPoint upperLevelEnd ) {
		this( room, lowerLevelStart, lowerLevelEnd,
						upperLevelStart, upperLevelEnd, 0.8d, 0.6d );
	}

	/**
	 * Constucts a new DelayArea with the given parameters.
	 * @param room to which the area belongs
	 * @param speedFactorDown The remaining speed when going down the stair in comparison
	 * to the speed that is achieved on a plain (0 &lt;= speedFactorDown &lt;= 1)
	 * @param speedFactorUp The remaining speed when going up the stair in comparison
	 * to the speed that is achieved on a plain (0 &lt;= speedFactorUp &lt;= 1)
	 * @param lowerLevelStart The plan point at which the edge progression starts that
	 * constitutes the lower part of the stair
	 * @param lowerLevelEnd The plan point at which the edge progression ends that
	 * constitutes the lower part of the stair
	 * @param upperLevelStart The plan point at which the edge progression starts that
	 * constitutes the upper part of the stair
	 * @param upperLevelEnd The plan point at which the edge progression ends that
	 * constitutes the upper part of the stair
	 */
	StairArea( RoomImpl room, PlanPoint lowerLevelStart, PlanPoint lowerLevelEnd,
										PlanPoint upperLevelStart, PlanPoint upperLevelEnd, double speedFactorDown,
										double speedFactorUp ) {
		super( room, AreaType.Stair );

		setSpeedFactorDown( speedFactorDown );
		setSpeedFactorUp( speedFactorUp );
		setLowerLevel( lowerLevelStart, lowerLevelEnd );
		setUpperLevel( upperLevelStart, upperLevelEnd );
	}

	/**
	 * This method copies the current polygon without it's edges. Every other setting, as f.e. the floor
	 * for Rooms or the associated Room for Areas is kept as in the original polygon.
	 * @return  */
	@Override
	protected PlanPolygon<PlanEdge> createPlainCopy() {
		return new StairArea( getAssociatedRoom(), getLowerLevelStart(),
						getLowerLevelEnd(), getUpperLevelStart(), getUpperLevelEnd(),
						getSpeedFactorDown(), getSpeedFactorUp() );
	}

	/**
	 * Returns the currently set value for the speedFactor. The speedfactor is
	 * the percentage of the original speed that can be achieved on this area.
	 * A speedfactor of 1 would be normal speed and speedfactor of almost 0 would mean
	 * total halt.
	 * @return The speed factor for going downwards on this stair (normally around 0.8)
	 */
	public double getSpeedFactorDown() {
		return speedFactorDown;
	}

	/**
	 * Returns the currently set value for the speedFactor. The speedfactor is
	 * the percentage of the original speed that can be achieved on this area.
	 * A speedfactor of 1 would be normal speed and speedfactor of almost 0 would mean
	 * total halt.
	 * @return The speed factor for going upwards on this stair (normally around 0.6)
	 */
	public double getSpeedFactorUp() {
		return speedFactorUp;
	}

	/**
	 * Sets a new value for the speedFactor in the area. The speedfactor is
	 * the percentage of the original speed that can be achieved on this area.
	 * A speedfactor of 1 would be normal speed and speedfactor of almost 0 would mean
	 * total halt.
	 * <p>The speedfactor has to be greater than 0 and smaller than 1 or equal to 1
	 * </p>
	 * @throws java.lang.IllegalArgumentException If the speedFactor is smaller
	 * than 0 or bigger than 1.
	 * @param val The speed factor for going downwards on this stair.
	 */
	public final void setSpeedFactorDown( double val ) throws IllegalArgumentException {
		if( val <= 0 )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.DelayArea.SpeedFactorNegativeException" ) );
		else if( val > 1 )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.DelayArea.SpeedFactorToHighException" ) );
		else
			this.speedFactorDown = val;
	}

	/**
	 * Sets a new value for the speedFactor in the area. The speed factor is
	 * the percentage of the original speed that can be achieved on this area.
	 * A speed factor of 1 would be normal speed and speed factor of almost 0 would mean
	 * total halt.
	 * <p>The speed factor has to be greater than 0 and smaller than 1 or equal to 1
	 * </p>
	 * @throws java.lang.IllegalArgumentException If the speedFactor is smaller
	 * than 0 or bigger than 1.
	 * @param val The speed factor for going upwards on this stair.
	 */
	public final void setSpeedFactorUp( double val ) throws IllegalArgumentException {
		if( val <= 0 )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.DelayArea.SpeedFactorNegativeException" ) );
		else if( val > 1 )
			throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.DelayArea.SpeedFactorToHighException" ) );
		else
			this.speedFactorUp = val;
	}

	/**
	 * Returns the {@link PlanPoint} at which the edge progression begins, which describes
	 * the lower part of the stair. This edge progression can be obtained by repeatedly
	 * calling {@code getLowerLevelStart().getNextEdge().getTarget().getNextEdge()} etc.
	 * until the PlanPoint 'LowerLevelEnd' is reached.
	 * @return
	 */
	public PlanPoint getLowerLevelStart() {
		return lowerLevel[0];
	}

	/**
	 * @return The PlanPoint at which the edge progression ends, which describes
	 * the lower part of the stair. This edge progression can be obtained by repeatedly
	 * calling getLowerLevelStart ().getNextEdge ().getTarget().getNextEdge () etc.
	 * until the PlanPoint 'LowerLevelEnd' is reached.
	 */
	public PlanPoint getLowerLevelEnd() {
		return lowerLevel[1];
	}

	/**
	 * @return The PlanPoint at which the edge progression begins, which describes
	 * the upper part of the stair. This edge progression can be obtained by repeatedly
	 * calling getUpperLevelBegin ().getNextEdge ().getTarget().getNextEdge () etc.
	 * until the PlanPoint 'UpperLevelEnd' is reached.
	 */
	public PlanPoint getUpperLevelStart() {
		return upperLevel[0];
	}

	/**
	 * @return The PlanPoint at which the edge progression ends, which describes
	 * the upper part of the stair. This edge progression can be obtained by repeatedly
	 * calling getUpperLevelBegin ().getNextEdge ().getTarget().getNextEdge () etc.
	 * until the PlanPoint 'UpperLevelEnd' is reached.
	 */
	public PlanPoint getUpperLevelEnd() {
		return upperLevel[1];
	}

	/**
	 * Sets the lower part of this stair area.
	 * @param lowerLevelStart The begin of the edge progression that constitutes the
	 * lower part of the stair
	 * @param lowerLevelEnd The end of the edge progression that constitutes the
	 * lower part of the stair
	 * @throws IllegalArgumentException If 'lowerLevelStart' and 'lowerLevelEnd'
	 * do not form an edge progression in the current polygon or if the new lower
	 * part intersects with the edge progression for the upper stair part.
	 * @throws StairAreaBoundaryException if two boundary edges overlap
	 */
	public final void setLowerLevel( PlanPoint lowerLevelStart, PlanPoint lowerLevelEnd )
					throws IllegalArgumentException, StairAreaBoundaryException {
		if( lowerLevelStart != null && lowerLevelEnd != null ) {
			boolean foundEnd = false;
			ListIterator<PlanPoint> itP = pointIterator( lowerLevelStart, lowerLevelEnd, false );
			while( itP.hasNext() ) {
				PlanPoint i = itP.next();
				// First check for invalid points then accept eventually
				if( i.equals(upperLevel[0]) || i.equals(upperLevel[1]) )
					throw new StairAreaBoundaryException( ZLocalization.loc.getString(
									"ds.z.StairArea.LevelProgressionsOverlap" ), this, this.getEdge( lowerLevelStart, lowerLevelEnd ) );
				else if( i.equals(lowerLevelEnd) ) {
					foundEnd = true;
					break;
				}
			}
			if( !foundEnd )
				throw new IllegalArgumentException( ZLocalization.loc.getString( "ds.z.StairArea.InvalidEdgeProgression" ) );
		}

		lowerLevel[0] = lowerLevelStart;
		lowerLevel[1] = lowerLevelEnd;
	}

	/** Sets the upper part of this stair area.
	 *
	 * @param upperLevelStart The begin of the edge progression that constitutes the
	 * upper part of the stair
	 * @param upperLevelEnd The end of the edge progression that constitutes the
	 * upper part of the stair
	 * @throws IllegalArgumentException If 'upperLevelStart' and 'upperLevelEnd'
	 * do not form an edge progression in the current polygon or if the new upper
	 * part intersects with the edge progression for the lower stair part.
	 * @throws StairAreaBoundaryException if two boundary edges overlap
	 */
	public final void setUpperLevel( PlanPoint upperLevelStart, PlanPoint upperLevelEnd )
					throws IllegalArgumentException, StairAreaBoundaryException {
		if( upperLevelStart != null && upperLevelEnd != null ) {
			boolean foundEnd = false;
			ListIterator<PlanPoint> itP = pointIterator( upperLevelStart, upperLevelEnd, false );
			while( itP.hasNext() ) {
				PlanPoint i = itP.next();
				// First check for invalid points then accept eventually
				if( i == lowerLevel[0] || i == lowerLevel[1] )
					throw new StairAreaBoundaryException( ZLocalization.loc.getString(
									"ds.z.StairArea.LevelProgressionsOverlap" ), this, this.getEdge( upperLevelStart, upperLevelEnd ) );
				else if( i == upperLevelEnd ) {
					foundEnd = true;
					break;
				}
			}
			if( !foundEnd )
				throw new IllegalArgumentException( ZLocalization.loc.getString(
								"ds.z.StairArea.InvalidEdgeProgression" ) );
		}

		upperLevel[0] = upperLevelStart;
		upperLevel[1] = upperLevelEnd;
	}
        
    public final boolean canBeUsed(PlanPoint upperLevelStart, PlanPoint upperLevelEnd) {
        if (upperLevelStart != null && upperLevelEnd != null) {
            ListIterator<PlanPoint> itP = pointIterator(upperLevelStart, upperLevelEnd, false);
            while (itP.hasNext()) {
                PlanPoint i = itP.next();
                if (i == lowerLevel[0] || i == lowerLevel[1]) {
                    return false;
                } else if (i == upperLevelEnd) {
                    break;
                }
            }
        }
        return true;
    }

	/**
	 * {@inheritDoc }
	 *  Additionally checks whether the two ends of ten stair have been marked.
	 */
	@Override
	public void check( boolean rasterized ) throws PolygonNotClosedException,
																								 PolygonNotRasterizedException {
		super.check( rasterized );

		if( area() <= 0 )
			throw new NullPointerException( ZLocalization.loc.getString( "ds.z.StairArea.ZeroArea" ) );

		if( upperLevel[0] == null || upperLevel[1] == null || lowerLevel[0] == null || lowerLevel[1] == null )
			throw new NullPointerException( ZLocalization.loc.getString(
							"ds.z.StairArea.LevelNotMarked" ) );
	}

	@Override
	public boolean equals( Object o ) {
		if( o instanceof StairArea ) {
			StairArea p = (StairArea)o;
			return super.equals( p ) && speedFactorDown == p.speedFactorDown &&
							speedFactorUp == p.speedFactorUp &&
							(lowerLevel[0] != null ? lowerLevel[0].equals( p.lowerLevel[0] ) : true) &&
							(lowerLevel[1] != null ? lowerLevel[1].equals( p.lowerLevel[1] ) : true) &&
							(upperLevel[0] != null ? upperLevel[0].equals( p.upperLevel[0] ) : true) &&
							(upperLevel[1] != null ? upperLevel[1].equals( p.upperLevel[1] ) : true);
		} else
			return false;
	}
}
