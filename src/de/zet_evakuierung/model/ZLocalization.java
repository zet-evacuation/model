package de.zet_evakuierung.model;

import org.zetool.common.localization.Localization;
import org.zetool.common.localization.LocalizationManager;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class ZLocalization {

	public final static String ZET_LOCALIZATION = "de.tu_berlin.coga.zet.model.ZLocalization";
	public final static Localization loc = LocalizationManager.getManager().getLocalization( ZLocalization.ZET_LOCALIZATION );
	
	private ZLocalization()  { }

}
