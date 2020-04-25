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

import org.zetool.opengl.framework.CameraPosition;
import java.util.ArrayList;
import org.zetool.opengl.helper.TextureFontStrings;

/**
 * The class {@code VisualProperties} stores visualization information
 * that is project specific. That could be current camera positions that allow
 * a good view and so forth.
 * @author Jan-Philipp Kappmeier
 */
public class VisualProperties {

	private ArrayList<TextureFontStrings> tfs;
	private ArrayList<CameraPosition> cameraPositions;

	// Used for 2d-view
	private double currentWidth;
	private double currentHeight;

	/**
	 * Creates a new instance of {@code VisualProperties} with default
	 * camera position and empty texts for video intro.
	 */
	public VisualProperties() {
		tfs = new ArrayList<>( 1 );
		cameraPositions = new ArrayList<>( 1 );
		tfs.add( new TextureFontStrings( true ) );
		cameraPositions.add( new CameraPosition() );
		currentHeight = 1000;
		currentWidth = 1000;
	}

	public CameraPosition getCameraPosition() {
		return cameraPositions.get( 0 );
	}

	public void setCameraPosition( CameraPosition cameraPosition ) {
		cameraPositions.set(  0, cameraPosition );
	}

	public double getCurrentHeight() {
		return currentHeight;
	}

	public void setCurrentHeight( double currentHeight ) {
		this.currentHeight = currentHeight;
	}

	public double getCurrentWidth() {
		return currentWidth;
	}

	public void setCurrentWidth( double currentWidth ) {
		this.currentWidth = currentWidth;
	}

	public ArrayList<TextureFontStrings> getTextureFontStrings() {
		return tfs;
	}

	public void setTextureFontStrings( ArrayList<TextureFontStrings> textureFontStrings ) {
		tfs = textureFontStrings;
	}
}
