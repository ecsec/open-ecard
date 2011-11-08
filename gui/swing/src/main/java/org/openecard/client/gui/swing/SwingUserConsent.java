package org.openecard.client.gui.swing;

import org.openecard.client.common.interfaces.UserConsent;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingUserConsent implements UserConsent {

    @Override
    public org.openecard.ws.gui.v1.ObtainUserConsentResponse obtainUserConsent(org.openecard.ws.gui.v1.ObtainUserConsent parameters) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
