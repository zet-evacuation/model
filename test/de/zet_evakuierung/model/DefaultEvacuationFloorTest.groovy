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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test
import spock.lang.Specification

/**
 *
 * @author Jan-Philipp Kappmeier
 */
class DefaultFloorInitializationTest extends Specification {
  def defaultFloor = new DefaultEvacuationFloor()
  
  def "name gets assigned"() {
    setup:
    defaultFloor = new DefaultEvacuationFloor("TestFloorName")
    expect:
    defaultFloor.getName().equals( "TestFloorName" )
    defaultFloor.roomCount() == 0
  }
  
  def "raster size not negative"() {
    expect:
    defaultFloor.getRasterSize() > 0
    defaultFloor.roomCount() == 0
  }
  
  def "default room size not negative and multiple of raster size"() {
    expect:
    defaultFloor.getDefaultRoomSize() > 0
    defaultFloor.getDefaultRoomSize() % defaultFloor.getRasterSize() == 0
  }
  
  def "change raster size"() {
    def oldDefaultSize = defaultFloor.defaultRoomSize
    when:
    def newDefaultSize = ((oldDefaultSize.intdiv(defaultFloor.getRasterSize())) + 1) * defaultFloor.getRasterSize()
    defaultFloor.setDefaultRoomSize( newDefaultSize )
    then:
    newDefaultSize > oldDefaultSize
    defaultFloor.getDefaultRoomSize() == newDefaultSize
  }
  
  def "raster size not decreasing"() {
    expect:
    defaultFloor.defaultRoomSize > 1
    when:
    def oldDefaultValue = defaultFloor.defaultRoomSize
    defaultFloor.setDefaultRoomSize( oldDefaultValue - 1 )
    then:
    thrown( IllegalArgumentException )
    defaultFloor.getDefaultRoomSize() == oldDefaultValue
  }
  
  def "switching to simple mode"() {
    expect:
    defaultFloor.normalMode == true
    when:
    defaultFloor.setSimpleMode(true)
    then:
    defaultFloor.normalMode == false
  }
  
  def "no switching back to normal mode"() {
    when:
    defaultFloor.setSimpleMode(true)
    defaultFloor.setSimpleMode(false)
    then:
    thrown(UnsupportedOperationException)
    defaultFloor.normalMode == false
  }
  
}

class NewExitsTest extends Specification {
  def testEdge = Mock(RoomEdgeInterface)
  def edgesRoom = Mock(Room)
  def defaultFloor = new DefaultEvacuationFloor()   

  def setup() {
    edgesRoom.getAssociatedFloor() >> Mock(AbstractFloor)
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
