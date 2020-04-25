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
package de.zet_evakuierung.template;

import java.util.ArrayList;
import java.util.Iterator;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class Templates<T> implements Iterable<T> {
	ArrayList<T> templates;
	String name;

	public Templates( String name ) {
		templates = new ArrayList<>();
		this.name = name;
	}

	public void add( T template ) {
		templates.add( template );
	}
	
	public void remove( T template ) {
		templates.remove( template );
	}

	@Override
	public Iterator<T> iterator() {
		return templates.iterator();
	}

	public String getName() {
		return name;
	}

	public T getDoor( int index ) {
		return templates.get( index );
	}
}
