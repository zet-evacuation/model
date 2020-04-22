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

