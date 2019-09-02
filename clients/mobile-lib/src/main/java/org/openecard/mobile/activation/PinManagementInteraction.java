/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation;

import java.util.function.BiFunction;
import java.util.function.Function;
import org.openecard.gui.mobile.pinmanagement.PinStatus;

/**
 *
 * @author Neil Crossley
 */
public interface PinManagementInteraction extends ActivationInteraction {

    void onPinChangeable(int attempts, BiFunction<String, String, Boolean> enterOldNewPins);
    void onCanRequired(Function<String, Boolean> enterCan);
    void onPinBlocked(Function<String, Boolean> unblockWithPuk);
    void onPinStatus(PinStatus status, String cardType);

}
