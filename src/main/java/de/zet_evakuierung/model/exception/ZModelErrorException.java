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


/**
 * An abstract base class for all exceptions thrown in the Z model.
 * @author Jan-Philipp Kappmeier
 */
public abstract class ZModelErrorException extends RuntimeException {

	/**
	 * Call to the super constructor with an error message.
	 * @param message the error message
	 */
	public ZModelErrorException( String message ) {
		super( message );
	}

}
