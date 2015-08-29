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
