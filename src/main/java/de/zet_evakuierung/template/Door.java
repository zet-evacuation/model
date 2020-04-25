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

import java.util.Objects;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class Door {
	String name;
	int size;
	double priority;

	public Door( String name, int size, double priority ) {
		this.name = name;
		this.size = size;
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 41 * hash + Objects.hashCode( this.name );
		hash = 41 * hash + this.size;
		hash = 41 * hash + (int)(Double.doubleToLongBits( this.priority ) ^ (Double.doubleToLongBits( this.priority ) >>> 32));
		return hash;
	}

	@Override
	public boolean equals( Object obj ) {
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		final Door other = (Door)obj;
		if( !Objects.equals( this.name, other.name ) )
			return false;
		if( this.size != other.size )
			return false;
		if( Double.doubleToLongBits( this.priority ) != Double.doubleToLongBits( other.priority ) )
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public double getPriority() {
		return priority;
	}
}
