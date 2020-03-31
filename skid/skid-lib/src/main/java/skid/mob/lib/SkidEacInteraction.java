/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.lib;

import org.openecard.mobile.activation.ActivationInteraction;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmPinCanOperation;
import org.openecard.robovm.annotations.FrameworkInterface;


/**
 *
 * @author Tobias Wich
 */
@FrameworkInterface
public interface SkidEacInteraction extends ActivationInteraction {

    void onCanRequest(ConfirmPasswordOperation enterCan);
    void onPinRequest(ConfirmPasswordOperation enterPin);
    void onPinRequest(int attempt, ConfirmPasswordOperation enterPin);
    void onPinCanRequest(ConfirmPinCanOperation enterPinCan);
    void onCardBlocked();
    void onCardDeactivated();
    void onCardAuthenticationSuccessful();

}
