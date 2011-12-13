package org.openecard.client.gui;

import org.openecard.client.gui.definition.UserConsentDescription;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface UserConsent {

    public UserConsentNavigator obtainNavigator(UserConsentDescription uc);

}
