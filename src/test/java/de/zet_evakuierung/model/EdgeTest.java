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

import de.zet_evakuierung.model.PlanPoint;
import de.zet_evakuierung.model.PlanEdge;
import de.zet_evakuierung.model.PlanPolygon;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class EdgeTest extends TestCase {

	@Test
	public void testIntersection () {
    PlanPolygon p = new PlanPolygon( PlanEdge.class );
		PlanEdge e1 = new PlanEdge( new PlanPoint( 6400, 4800), new PlanPoint(4000,4800), p );
    p = new PlanPolygon( PlanEdge.class );
		PlanEdge e2 = new PlanEdge( new PlanPoint( 6400, 5600), new PlanPoint(8000,4800), p );

		PlanEdge.LineIntersectionType a = PlanEdge.intersects( e1, e2 ); // should be NotIntersects
		PlanEdge.LineIntersectionType a2 = PlanEdge.intersects( e2, e1 ); // should be NotIntersects
    assertEquals( a, a2 );
		System.out.println( a + " " + a2 ); // should be the same!

    p = new PlanPolygon( PlanEdge.class );
		PlanEdge e3 = new PlanEdge( new PlanPoint( 7200, 5200 ), new PlanPoint(9000,5200), p );

		PlanEdge.LineIntersectionType b = PlanEdge.intersects( e2, e3 ); // should be IntersectsBorder
		PlanEdge.LineIntersectionType b2 = PlanEdge.intersects( e3, e2 ); // should be IntersectsBorder
    assertEquals( b, b2 );
		System.out.println( b + " " + b2 );// Should be the same!
	}
}
