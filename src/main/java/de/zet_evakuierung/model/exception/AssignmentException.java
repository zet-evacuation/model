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
package de.zet_evakuierung.model.exception;

import java.io.IOException;

/**
 * The class {@code AssignmentException} ...
 * @author Jan-Philipp Kappmeier
 */
@SuppressWarnings( "serial" )
public class AssignmentException extends RuntimeException {
	public static enum State {
		GeneralAssignmentError,
		NoAssignmentCreated,
		NoAssignmentSelected
	}

	State state;

	/**
	 * Creates a new instance of {@code AssignmentException}.
	 */
	public AssignmentException() {
		super();
		state = State.GeneralAssignmentError;
	}

	public AssignmentException( State state ) {
		super();
		this.state = state;
	}

	public AssignmentException( State state, String message ) {
		super( message );
		this.state = state;
	}

	public AssignmentException( String message ) {
		super( message );
		this.state = State.GeneralAssignmentError;
	}

	/**
	 * Returns the special information for assignment errors.
	 * @return the special information for assignment errors
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the name of the class.
	 * @return the name of the class
	 */
	@Override
	public String toString() {
		return "AssignmentException: " + super.getMessage();
	}
	
	/** Prohibits serialization. */
	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException {
		throw new UnsupportedOperationException( "Serialization not supported" );
	}
}
