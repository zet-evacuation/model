
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
  }
  
  def "raster size not negative"() {
    expect:
    defaultFloor.getRasterSize() > 0
  }
  
  def "default room size not negative and multiple of raster size"() {
    expect:
    defaultFloor.getDefaultRoomSize() > 0
    defaultFloor.getDefaultRoomSize() % defaultFloor.getRasterSize() == 0
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
  
}
