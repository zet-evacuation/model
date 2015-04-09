/* zet evacuation tool copyright (ageCollector) 2007-10 zet evacuation team
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

package de.tu_berlin.coga.zet.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.zetool.common.localization.Localization;
import org.zetool.common.localization.LocalizationManager;
import io.z.AssignmentConverter;
import io.z.XMLConverter;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * The {@code Assignment} class holds all information about an assignment
 * in the Z-format. It is a container for some assignment types and can submit
 * lists of all assignments.
 * @author Sylvie Temme, Jan-Philipp Kappmeier
 */
@XStreamAlias("assignment")
@XMLConverter(AssignmentConverter.class)
public class Assignment implements Serializable {
	/** The name of the assignment. */
	@XStreamAsAttribute()
	private String name;
	/** The List of assignmentTypes, which belong to this assignment. */
	private ArrayList<AssignmentType> assignmentTypes;
	/** Static variable that stores the default-value for the area, that a person needs. Its unit is square millimeter. */
	public static int spacePerPerson = 160000;
	final static Localization loc = LocalizationManager.getManager().getLocalization( ZLocalization.ZET_LOCALIZATION );

	/**
	 * Creates a new instance of {@link Assignment}.
	 * Sets the name of the assignment.
	 * @param name The name of the Assignment.
	 */
	public Assignment( String name ) {
		setName( name );
		assignmentTypes = new ArrayList<AssignmentType>();
	}

	/**
	 * Returns the name of the assignment.
	 * @return The name of the assignment.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the assignment.
	 * @param val The name of the assignment.
	 * @throws IllegalArgumentException If the given name is {@code null} or "".
	 */
	public void setName( String val ) throws IllegalArgumentException {
		if( val == null || val.equals( "" ) )
			throw new IllegalArgumentException( loc.getString( "ds.z.Assignment.NoNameException" ) );
		name = val;
	}

	/**
	 * Returns all assignmentTypes of this assignment.
	 * @return All assignmentTypes of this assignment.
	 */
	public List<AssignmentType> getAssignmentTypes() {
		return Collections.unmodifiableList( assignmentTypes );
	}

	/**
	 * Adds a new assignmentType to this assignment.
	 * @param val The assignmentType to be added.
	 * @throws java.lang.IllegalArgumentException If the new assignmentType already is an assignmentType of this assignment.
	 */
	public void addAssignmentType( AssignmentType val ) throws IllegalArgumentException {
		if( assignmentTypes.contains( val ) )
			throw new IllegalArgumentException( loc.getString( "ds.z.Assignment.DoubleAssignmentTypeException" ) );
		else {
			assignmentTypes.add( val );
		}
	}

	/**
	 * Removes an assignmentType from the list of assignmentTypes of this assignment.
	 * @param val The assignmentType to be removed.
	 * @throws java.lang.IllegalArgumentException If the assignmentType is not in the list of assignmentTypes of this assignment.
	 */
	public void deleteAssignmentType( AssignmentType val ) throws IllegalArgumentException {
		if( !assignmentTypes.contains( val ) )
			throw new IllegalArgumentException( loc.getString( "ds.z.Assignment.AssignmentTypeNotNotFoundException" ) );
		else {
			assignmentTypes.remove( val );

			// Delete corresponding assignment areas
			// The areas deregister themselves out of the getAssignmentAreas() list, so
			// this list has to be copied before deleting
			AssignmentArea[] areaCopy = val.getAssignmentAreas().toArray( new AssignmentArea[val.getAssignmentAreas().size()] );
			for( AssignmentArea a : areaCopy )
				a.delete();
		}
	}

	/**
	 * Deletes all references from this assignment.
	 * (Sets the list of assignmentTypes of this assignment to null.)
	 */
	public void delete() {
		// Delete all assignment areas
		for( AssignmentType t : assignmentTypes )
			// Don't use for loop here - will throw a concurrent modification exception - TIMON
			//for( AssignmentArea a : t.getAssignmentAreas() )
			while(!t.getAssignmentAreas().isEmpty())
				t.getAssignmentAreas().get( 0 ).delete();
		assignmentTypes.clear();
		assignmentTypes = null;
	}

	/**
	 * Two assignments are defined to be equal, if they have the same name.
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals( Object o ) {
		if( o instanceof Assignment ) {
			Assignment p = (Assignment) o;
			return name.equals( p.name );
			//return assignmentTypes.equals( p.getAssignmentTypes() ) && ((name == null) ? p.getName() == null : name.equals( p.getName() ));
		} else
			return false;
	}

	/**
	 * Gives notice to all the assignmentAreas of this assignment
	 * that this assignment is now the current assignment of the project.
	 */
	public void setActive() {
		// Iterate over all assignments and their assignment types
		for( AssignmentType assignmentType : getAssignmentTypes() )
			for( AssignmentArea assignmentArea : assignmentType.getAssignmentAreas() )
				assignmentArea.setActive();
	}

	/**
	 * Gives notice to all the assignmentAreas of this assignment
	 * that this assignment is not any more the current assignment of the project.
	 */
	public void setInactive() {
		// Iterate over all assignments and their assignment types
		for( AssignmentType assignmentType : getAssignmentTypes() )
			for( AssignmentArea assignmentArea : assignmentType.getAssignmentAreas() )
				assignmentArea.setInactive();
	}
}
