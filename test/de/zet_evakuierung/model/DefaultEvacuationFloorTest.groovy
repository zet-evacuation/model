
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
  
  def "adding a room by edge increases size"() {
    setup:
    
    testEdge.isHorizontal() >> true
    testEdge.getRoom() >> edgesRoom
    edgesRoom.getAssociatedFloor() >> Mock(AbstractFloor)
    def defaultFloor = new DefaultEvacuationFloor()
    when:
    defaultFloor.addEvacuationRoom( testEdge )
    then:
    defaultFloor.roomCount() == 1
  }
}
