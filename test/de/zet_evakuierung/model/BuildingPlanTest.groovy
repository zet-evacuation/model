
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
class TestBuildingPlanTest extends Specification {
  def bp = new BuildingPlan()

  def "contains default floor"() {
    expect:
    bp.getDefaultFloor() != null
    bp.getFloors().size() == 1
  }
  
}
