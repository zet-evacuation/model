
package de.tu_berlin.coga.zet.model;

import org.zetool.common.localization.LocalizationManager;

/**
 * An enumeration of all implemented areas in the z model.
 * @author Jan-Philipp Kappmeier
 */
public abstract class AreaType {

  public final static AreaType Assignment = new AreaType( "ds.z.AreaType.AssignmentArea" ) {};
	public final static AreaType Barrier = new AreaType( "ds.z.AreaType.Barrier" ) {};
	public final static AreaType Delay = new AreaType( "ds.z.AreaType.DelayArea" ) {};
	public final static AreaType Inaccessible = new AreaType( "ds.z.AreaType.InaccessibleArea" ) {};
	public final static AreaType Evacuation = new AreaType( "ds.z.AreaType.EvacuationArea" ) {};
	public final static AreaType Save = new AreaType( "ds.z.AreaType.SaveArea" ) {};
	public final static AreaType Stair = new AreaType( "ds.z.AreaType.StairArea" ) {};
	public final static AreaType Teleport = new AreaType( "ds.z.AreaType.TeleportArea" ) {};

	/** The key needed for the localization class to get the area type name. */
	private String key;

	private AreaType( String key ) {
		this.key = key;
	}

	/**
	 * Returns a (localized) name for the area type.
	 * @return a name for the area type
	 */
	public String getTypeString() {
		return LocalizationManager.getManager().getLocalization( ZLocalization.ZET_LOCALIZATION ).getString( key );
	}

  public String name() {
    return getTypeString();
  }
}
