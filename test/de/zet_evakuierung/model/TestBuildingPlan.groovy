
package de.zet_evakuierung.model

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test

/**
 *
 * @author Jan-Philipp Kappmeier
 */
class TestBuildingPlan {

  @Test
  void simpleTest() {
    def bp = new BuildingPlan()
    assertTrue bp.getDefaultFloor() != null
    assertThat bp.getFloors().size(), is(equalTo(1))
  }
  
}
