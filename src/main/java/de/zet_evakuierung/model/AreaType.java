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

import org.zetool.common.localization.LocalizationManager;

/**
 * An enumeration of all implemented areas in the z model.
 *
 * @author Jan-Philipp Kappmeier
 */
public enum AreaType {

    Assignment("ds.z.AreaType.AssignmentArea"),
    Barrier("ds.z.AreaType.Barrier"),
    Delay("ds.z.AreaType.DelayArea"),
    Inaccessible("ds.z.AreaType.InaccessibleArea"),
    Evacuation("ds.z.AreaType.EvacuationArea"),
    Save("ds.z.AreaType.SaveArea"),
    Stair("ds.z.AreaType.StairArea"),
    Teleport("ds.z.AreaType.TeleportArea");

    /** The key needed for the localization class to get the area type name. */
    public final String localizationKey;

    private AreaType(String key) {
        this.localizationKey = key;
    }

    /**
     * Returns a (localized) name for the area type.
     *
     * @return a name for the area type
     */
    public String getTypeString() {
        return LocalizationManager.getManager().getLocalization(ZLocalization.ZET_LOCALIZATION).getString(localizationKey);
    }
}
