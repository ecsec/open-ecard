package org.openecard.client.common.interfaces;

import org.openecard.ws.gui.v1.ObtainUserConsent;
import org.openecard.ws.gui.v1.ObtainUserConsentResponse;


/**
 * Interface for a UserConsent, so the implementation is exchangeable.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface UserConsent {

    /**
     * Display UserConsent and return result. Blocks execution until the user closes the dialog.
     * @param parameters Abstract description of the dialog.
     * @return Values collected from the user. E.g. checkboxes, pin field etc.
     */
    public ObtainUserConsentResponse obtainUserConsent(ObtainUserConsent parameters);

    /**
     * Cancel the currently displayed UserConsent dialog. Does nothing if no dialog is shown.<br/>
     * This function may be used by the Cancel call in the IFD.
     */
    public void cancel();

}
