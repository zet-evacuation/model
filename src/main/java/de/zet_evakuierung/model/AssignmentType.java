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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.zetool.common.localization.Localization;
import org.zetool.common.localization.LocalizationManager;
import org.zetool.rndutils.distribution.Distribution;
import de.zet_evakuierung.io.z.AssignmentTypeConverter;
import de.zet_evakuierung.io.z.XMLConverter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * This class pools several {@link AssignmentArea}s.
 * All the areas have the same distributions of the persons' parameters.
 * @author Sylvie Temme, Jan-Philipp Kappmeier
 */
@XStreamAlias("assignmentType")
@XMLConverter(AssignmentTypeConverter.class)
@SuppressWarnings("serial")
public class AssignmentType implements Serializable, Named {
	/** A name for the assignment type such as "Old People" or "Pedestrians". */
	@XStreamAsAttribute()
	private String name;
	/** The distribution of the persons' parameter "diameter". */
	private Distribution<Double> diameter;
	/** The distribution of the persons' parameter "age". */
	private Distribution<Double> age;
	/** The distribution of the persons' parameter "familiarity". */
	private Distribution<Double> familiarity;
	/** The distribution of the persons' parameter "panic". */
	private Distribution<Double> panic;
	/** The distribution of the persons' parameter "decisiveness". */
	private Distribution<Double> decisiveness;
	/** The distribution of the persons' parameter "reaction". */
	private Distribution<Double> reaction;
	/** The default number of evacuees of the assignmentAreas, which belong this type. */
	private int standardEvacuees;
	/** The list of the assignmentAreas, which belong to this assignmentType. */
	private ArrayList<AssignmentArea> assignmentAreas;
	/** Unique ID of this AssignmentType */
	private UUID uid;
	final static Localization loc = LocalizationManager.getManager().getLocalization( ZLocalization.ZET_LOCALIZATION );

	/**
	 * Creates a new instance of {@link AssignmentType}.
	 * Sets Distributions diameter, age, familiarity, panic, decisiveness.
	 * Sets the standard number of evacuees to 0.
	 * @param name The name of the type
	 * @param diameter The {@link Distribution} diameter.
	 * @param age The {@link Distribution} age.
	 * @param familiarity The {@link Distribution} familiarity.
	 * @param panic the {@link Distribution} for the panic of evacuees
	 * @param decisiveness the {@link Distribution} decisiveness.
	 * @param reaction the {@link Distribution } for the reaction time of evacuees
	 */
	public AssignmentType( String name, Distribution<Double> diameter, Distribution<Double> age, Distribution<Double> familiarity, Distribution<Double> panic, Distribution<Double> decisiveness, Distribution<Double> reaction ) {
		this( name, diameter, age, familiarity, panic, decisiveness, reaction, 0 );
	}

	/**
	 * Creates a new instance of {@link AssignmentType}.
	 * Sets Distributions diameter, age, familiarity, panic, decisiveness.
	 * Sets the standard number of evacuees.
	 * This number has to be greater or equal 0.
	 * If it is less than 0, the number of evacuees is set to 0.
	 * @param name The name of the type
	 * @param diameter The {@link Distribution} diameter.
	 * @param age The {@link Distribution} age.
	 * @param familiarity The {@link Distribution} familiarity.
	 * @param panic the {@link Distribution} for the panic of evacuees
	 * @param decisiveness the {@link Distribution} decisiveness.
	 * @param reaction the {@link Distribution } for the reaction time of evacuees
	 * @param standardEvacuees The standard number of evacuees.
	 * @throws java.lang.IllegalArgumentException If the standard number of evacuees is less than 0.
	 */
	public AssignmentType( String name, Distribution<Double> diameter, Distribution<Double> age, Distribution<Double> familiarity, Distribution<Double> panic, Distribution<Double> decisiveness, Distribution<Double> reaction, int standardEvacuees ) throws IllegalArgumentException {
		setName( name );
		this.diameter = diameter;
		this.age = age;
		this.familiarity = familiarity;
		this.panic = panic;
		this.decisiveness = decisiveness;
		this.reaction = reaction;
		assignmentAreas = new ArrayList<>();
		setDefaultEvacuees( standardEvacuees );
		this.uid = UUID.randomUUID();
	}

	@Override
	public String toString() {
		return name;
	}

	public UUID getUid() {
		return uid;
	}

	public void setUid( UUID uid ) {
		this.uid = uid;
	}

	/**
	 * Returns the currently set value for the standard number of evacuees.
	 * @return The standard number of evacuees in this area.
	 */
	public int getDefaultEvacuees() {
		return standardEvacuees;
	}

	/**
	 * Sets a new value for the standard number of evacuees of this AssignmentType.
	 * It has to be greater or equal 0. If it is less than 0, the number of
	 * evacuees is set to 0.
	 * @param evacuees the default number of evacuees of this {@code AssignmentType}.
	 * @throws java.lang.IllegalArgumentException if the standard number of evacuees is less than 0.
	 */
	public final void setDefaultEvacuees( int evacuees ) throws IllegalArgumentException {
		if( evacuees < 0 )
			throw new IllegalArgumentException( loc.getString( "ds.z.AssignmentArea.NegativePersonValueException" ) );
		standardEvacuees = evacuees;
	}

	/**
	 * Returns the currently set {@link Distribution} diameter.
	 * @return The distribution for the diameter.
	 */
	public Distribution<Double> getDiameter() {
		return diameter;
	}

	/**
	 * Sets the {@link Distribution} diameter.
	 * @param diameter the distribution for the evacuees diameter
	 */
	public void setDiameter( Distribution<Double> diameter ) {
		this.diameter = diameter;
	}

	/**
	 * Returns the currently set {@link Distribution} age.
	 * @return The distribution for the age.
	 */
	public Distribution<Double> getAge() {
		return age;
	}

	/**
	 * Sets the {@link Distribution} age.
	 * @param age the distribution for the evacuees age.
	 */
	public void setAge( Distribution<Double> age ) {
		this.age = age;
	}

	/**
	 * Returns the currently set {@link Distribution} familiarity.
	 * @return the distribution for the familiarity.
	 */
	public Distribution<Double> getFamiliarity() {
		return familiarity;
	}

	/**
	 * Sets the {@link Distribution} familiarity.
	 * @param familiarity the distribution for the evacuees familiarity
	 */
	public void setFamiliarity( Distribution<Double> familiarity ) {
		this.familiarity = familiarity;
	}

	/**
	 * Returns the currently set {@link Distribution} panic.
	 * @return The Distribution panic.
	 */
	public Distribution<Double> getPanic() {
		return panic;
	}

	/**
	 * Sets the {@link Distribution} panic.
	 * @param panic the distribution for the evacuees panic
	 */
	public void setPanic( Distribution<Double> panic ) {
		this.panic = panic;
	}

	/**
	 * Returns the currently set {@link Distribution} decisiveness.
	 * @return The Distribution decisiveness.
	 */
	public Distribution<Double> getDecisiveness() {
		return decisiveness;
	}

	/**
	 * Sets the {@link Distribution} decisiveness.
	 * @param decisiveness the distribution for the decisiveness
	 */
	public void setDecisiveness( Distribution<Double> decisiveness ) {
		this.decisiveness = decisiveness;
	}

	/**
	 * Returns the {@link Distribution} for the reaction time of evacuees.
	 * @return the {@link Distribution} for the reaction time of evacuees
	 */
	public Distribution<Double> getReaction() {
		return reaction;
	}

	/**
	 * Sets the {@link Distribution} for the reaction time of evacuees.
	 * @param reaction the distribution for the reaction times
	 */
	public void setReaction( Distribution<Double> reaction ) {
		this.reaction = reaction;
	}

	/**
	 * Returns the list assignmentAreas of this assignmentType.
	 * @return The list assignmentAreas of this assignmentType.
	 */
	public List<AssignmentArea> getAssignmentAreas() {
		return Collections.unmodifiableList( assignmentAreas );
	}

	/**
	 * Adds a new assignmentArea to the list of assignmentAreas of this assignmentType.
	 * @param familiarity The assignmentArea to be added.
	 * @throws java.lang.IllegalArgumentException If the area already is in the list.
	 */
	void addAssignmentArea( AssignmentArea val ) throws IllegalArgumentException {
		if( assignmentAreas.contains( val ) )
			throw new IllegalArgumentException( loc.getString( "ds.z.AssignmentType.DoubleAssignmentAreaException" ) );
		assignmentAreas.add( val );
	}

	/**
	 * Removes an assignmentArea from the list of assignmentAreas of this assignmentType.
	 * @param familiarity The assignmentArea to be removed.
	 * @throws java.lang.IllegalArgumentException If the area is not in the list.
	 */
	void deleteAssignmentArea( AssignmentArea val ) throws IllegalArgumentException {
		if( !assignmentAreas.contains( val ) )
			throw new IllegalArgumentException( loc.getString( "ds.z.AssignmentType.AssignmentAreaNotFound" ) );
		assignmentAreas.remove( val );
	}

	/**
	 * Deletes all references from this assignmentType.
	 * (Calls assignmentArea.delete() for all its assignmentAreas).
	 */
	public void delete() {
		for( AssignmentArea assignmentArea : assignmentAreas )
			assignmentArea.delete();
	}

	/**
	 * Sets the number of evacuees of all its assignmentAreas
	 * to its standard number of evacuees.
	 */
	public void setEvacueesOfAllAreasToStandardEvacuees() {
		for( AssignmentArea assignmentArea : assignmentAreas )
			assignmentArea.setEvacuees( standardEvacuees );
	}

	/**
	 * Two {@code AssignmentType}s are equal if they have the same name.
	 * @param o the object that is compared to this assignment type
	 */
	@Override
	public boolean equals( Object o ) {
		if( o instanceof AssignmentType ) {
			AssignmentType p = (AssignmentType)o;
			return name.equals( p.name );
//			return ((name == null) ? p.getName() == null
//							: name.equals( p.getName() ))
//							&& ((age == null) ? p.getAge() == null
//							: age.equals( p.getAge() ))
//							&& ((decisiveness == null) ? p.getDecisiveness() == null
//							: decisiveness.equals( p.getDecisiveness() ))
//							&& ((diameter == null) ? p.getDiameter() == null
//							: diameter.equals( p.getDiameter() ))
//							&& ((familiarity == null) ? p.getFamiliarity() == null
//							: familiarity.equals( p.getFamiliarity() ))
//							&& ((panic == null) ? p.getPanic() == null
//							: panic.equals( p.getPanic() ));
		} else
			return false;
	}

	/**
	 * Returns the name of the assignment type.
	 * @return the name of the assignment type
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets a name for the assignment type.
	 * @param name the new name for the assignment type
	 * @throws IllegalArgumentException If the given name is null or the empty string.
	 */
	public final void setName( String name ) throws IllegalArgumentException {
		if( name == null || name.equals( "" ) )
			throw new IllegalArgumentException( loc.getString( "ds.z.Assignment.NoNameException" ) );
		this.name = name;
	}
}
