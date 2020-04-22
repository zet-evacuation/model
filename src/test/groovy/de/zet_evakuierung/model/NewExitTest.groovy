/* zet evacuation tool copyright (c) 2007-15 zet evacuation team
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.zet_evakuierung.model

import spock.lang.Specification

/**
 *
 * @author Jan-Philipp Kappmeier
 */
class NewExitsTest extends Specification {
    def testEdge = Mock(RoomEdgeInterface)
    def edgesRoom = Mock(Room)
    def defaultFloor = new DefaultEvacuationFloor()   

    def setup() {
        edgesRoom.getAssociatedFloor() >> Mock(FloorInterface)
        edgesRoom.getPolygon() >> new RoomImpl( new Floor() )
    }
  
    def "only horizontal/parallel adding allowed"() {
        setup:
        when:
        defaultFloor.addEvacuationRoom( testEdge )
        then:
        thrown(IllegalArgumentException)
        and:
        defaultFloor.roomCount() == 0
    }
  
    def "adding a room by edge increases size"() {
        setup:
        initAxisAlignedEdge( orientationHorizontal )
        when:
        defaultFloor.addEvacuationRoom( testEdge )
        then:
        defaultFloor.roomCount() == 1
        where:
        orientationHorizontal << [true, false]
    }

    /**
     * Initializes the test edge as either horizontal or vertical and
     * initializes source, target points and associated room.
     * @param horizontal whether the edge shall be horizontal or vertical
     */
    def initAxisAlignedEdge( horizontal ) {
        if( horizontal ) {
            testEdge.isHorizontal() >> true
            testEdge.getSource() >> new PlanPoint(0, 0)
            testEdge.getTarget() >> new PlanPoint(3000, 0)
        } else {
            testEdge.isVertical() >> true
            testEdge.getSource() >> new PlanPoint(0, 0)
            testEdge.getTarget() >> new PlanPoint(0, 1000)    
        }
        testEdge.getRoom() >> edgesRoom
        return testEdge
    }
}