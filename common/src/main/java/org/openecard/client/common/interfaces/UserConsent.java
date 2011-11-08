package org.openecard.client.common.interfaces;

import org.openecard.ws.gui.v1.ObtainUserConsent;
import org.openecard.ws.gui.v1.ObtainUserConsentResponse;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface UserConsent {

    public ObtainUserConsentResponse obtainUserConsent(ObtainUserConsent parameters);

}
