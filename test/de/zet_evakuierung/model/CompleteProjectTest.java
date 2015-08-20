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

package de.zet_evakuierung.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * A test case that builds (in code) a complex evacuation mode and asserts properties.
 *
 * @author Jan-Philipp Kappmeier
 */
public class CompleteProjectTest {

    @Test
    public void testFloor() {
        ZControl control = new ZControl();
        Project p = control.getProject();
        FloorInterface floor1 = p.getBuildingPlan().getFloors().get(1);

        control.createNewPolygon(Room.class, floor1);
        control.addPoint(new PlanPoint(0, 0));
        control.addPoint(new PlanPoint(1000, 0));
        control.addPoint(new PlanPoint(1000, 1000));
        control.addPoint(new PlanPoint(0, 1000));
        control.addPoint(new PlanPoint(0, 0));
        Room room1 = (Room) control.latestPolygon();

        // create some areas
        int areaCount = 0;
        control.createNewPolygon(EvacuationArea.class, room1);
        control.addPoint(new PlanPoint(0, 0));
        control.addPoint(new PlanPoint(400, 0));
        control.addPoint(new PlanPoint(400, 400));
        control.addPoint(new PlanPoint(0, 400));
        control.addPoint(new PlanPoint(0, 0));
        assertThat(room1.getAreas().size(),  is(equalTo(++areaCount)));
        assertThat(room1.getEvacuationAreas().size(), is(equalTo(1)));
        EvacuationArea exit = room1.getEvacuationAreas().get(0);

        control.createNewPolygon(SaveArea.class, room1);
        control.addPoint(new PlanPoint(400, 0));
        control.addPoint(new PlanPoint(800, 0));
        control.addPoint(new PlanPoint(800, 400));
        control.addPoint(new PlanPoint(400, 400));
        control.addPoint(new PlanPoint(400, 0));
        assertThat(room1.getAreas().size(), is(equalTo(++areaCount)));

        control.createNewPolygon(DelayArea.class, room1);
        control.addPoint(new PlanPoint(400, 400));
        control.addPoint(new PlanPoint(800, 400));
        control.addPoint(new PlanPoint(800, 800));
        control.addPoint(new PlanPoint(400, 800));
        control.addPoint(new PlanPoint(400, 400));
        assertThat(room1.getAreas().size(), is(equalTo(++areaCount)));

        control.createNewPolygon(InaccessibleArea.class, room1);
        control.addPoint(new PlanPoint(0, 400));
        control.addPoint(new PlanPoint(400, 400));
        control.addPoint(new PlanPoint(400, 800));
        control.addPoint(new PlanPoint(0, 800));
        control.addPoint(new PlanPoint(0, 400));
        assertThat(room1.getAreas().size(), is(equalTo(++areaCount)));

        control.createNewPolygon(Room.class, floor1);
        control.addPoint(new PlanPoint(4000, 3000));
        control.addPoint(new PlanPoint(5000, 3000));
        control.addPoint(new PlanPoint(5000, 4000));
        control.addPoint(new PlanPoint(4000, 4000));
        control.addPoint(new PlanPoint(4000, 3000));
        Room room2 = (Room) control.latestPolygon();

        assertThat(p.getCurrentAssignment(), is(notNullValue()));
        assertThat(p.getCurrentAssignment().getAssignmentTypes().size(), is(equalTo(1)));
        AssignmentType standardAssignment = p.getCurrentAssignment().getAssignmentTypes().get(0);
        AssignmentType newAssignment = new AssignmentType("Second Assignment", standardAssignment.getDiameter(),
                standardAssignment.getAge(), standardAssignment.getFamiliarity(), standardAssignment.getPanic(),
                standardAssignment.getDecisiveness(), standardAssignment.getReaction());
        p.getCurrentAssignment().addAssignmentType(newAssignment);

        areaCount = 0;

        int xOffset = room2.getPolygon().bounds().x;
        int yOffset = room2.getPolygon().bounds().y;
        System.out.println(xOffset);
        System.out.println(yOffset);

        control.createNewPolygon(AssignmentArea.class, room2);
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 0));
        control.addPoint(new PlanPoint(xOffset + 800, yOffset + 0));
        control.addPoint(new PlanPoint(xOffset + 800, yOffset + 400));
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 400));
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 0));
        assertThat(room2.getAreas().size(), is(equalTo(++areaCount)));

        control.createNewPolygon(TeleportArea.class, room2);
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 400));
        control.addPoint(new PlanPoint(xOffset + 400, yOffset + 400));
        control.addPoint(new PlanPoint(xOffset + 400, yOffset + 800));
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 800));
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 400));
        assertThat(room2.getAreas().size(), is(equalTo(++areaCount)));
        assertThat(room2.getTeleportAreas().size(), is(equalTo(1)));
        TeleportArea area1 = room2.getTeleportAreas().get(0);

        control.createNewPolygon(Room.class, floor1);
        control.addPoint(new PlanPoint(0000, 3000));
        control.addPoint(new PlanPoint(1000, 3000));
        control.addPoint(new PlanPoint(1000, 4000));
        control.addPoint(new PlanPoint(0000, 4000));
        control.addPoint(new PlanPoint(0000, 3000));
        assertThat(floor1.getRooms().size(), is(equalTo(3)));
        Room room3 = (Room) control.latestPolygon();

        areaCount = 0;

        xOffset = room3.getPolygon().bounds().x;
        yOffset = room3.getPolygon().bounds().y;

        control.createNewPolygon(TeleportArea.class, room3);
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 0));
        control.addPoint(new PlanPoint(xOffset + 400, yOffset + 0));
        control.addPoint(new PlanPoint(xOffset + 400, yOffset + 400));
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 400));
        control.addPoint(new PlanPoint(xOffset + 0, yOffset + 0));
        assertThat(room3.getAreas().size(), is(equalTo(++areaCount)));
        assertThat(room3.getTeleportAreas().size(), is(equalTo(1)));
        TeleportArea area2 = room3.getTeleportAreas().get(0);

        // Only one target set for now
        area1.setTargetArea(area2);

        // set exit for the other
        area2.setExitArea(exit);

        control.createNewPolygon(StairArea.class, room3);
        //TODO: change implementation such that new, equal points can be used!
        PlanPoint lowerLevelStart = new PlanPoint(xOffset + 400, yOffset + 400);
        PlanPoint lowerLevelEnd = new PlanPoint(xOffset + 800, yOffset + 400);
        PlanPoint upperLevelStart = new PlanPoint(xOffset + 800, yOffset + 800);
        PlanPoint upperLevelEnd = new PlanPoint(xOffset + 400, yOffset + 800);
        control.addPoint(lowerLevelStart);
        control.addPoint(lowerLevelEnd);
        control.addPoint(upperLevelStart);
        control.addPoint(upperLevelEnd);
        control.addPoint(new PlanPoint(xOffset + 400, yOffset + 400));
        assertThat(room3.getAreas().size(), is(equalTo(++areaCount)));
        assertThat(room3.getStairAreas().size(), is(equalTo(1)));
        StairArea s = room3.getStairAreas().get(0);

        PlanEdge pe = s.getEdge(lowerLevelStart, lowerLevelEnd);
        s.setLowerLevel(pe.getSource(), pe.getTarget());
        pe = s.getEdge(upperLevelStart, upperLevelEnd);
        s.setUpperLevel(pe.getSource(), pe.getTarget());
    }
}
