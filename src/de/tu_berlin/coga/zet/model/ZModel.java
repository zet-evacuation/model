
package de.tu_berlin.coga.zet.model;

import java.util.List;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface ZModel {
	public BuildingPlan getBuildingPlan();

	public List<Assignment> getAssignments();

	public Assignment getCurrentAssignment();

	public List<EvacuationPlan> getEvacuationPlans();

	public EvacuationPlan getCurrentEvacuationPlan();
}
