/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.gui.android.pinmanagement;

import org.openecard.gui.android.AndroidGui;


/**
 *
 * @author Sebastian Schuberth
 */
public interface PINManagementGui extends AndroidGui {

    /**
     * Get status of the PIN on the card.
     * This method uses the name value of the PinStatus enum type. The enum type has values to check whether CAN must be
     * entered or whether the PIN is operational.
     *
     * @return Status of the PIN.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    PinStatus getPinStatus() throws InterruptedException;

     /**
     * Change the old PIN to the new one.
     *
     * @param oldPin
     * @param newPin
     * @return true if PIN is accepted and changed to new PIN, false otherwise.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    boolean changePin(String oldPin, String newPin) throws InterruptedException;

    /**
     * Enter the CAN.
     *
     * @param can
     * @return true if CAN is accepted, false otherwise.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    boolean enterCan(String can) throws InterruptedException;

    /**
     * Enter the PUK to unblock a blocked PIN.
     *
     * @param puk
     * @return true if PUK is accepted, false otherwise.
     * @throws InterruptedException Thrown if waiting for the value has been interrupted.
     */
    boolean unblockPin(String puk) throws InterruptedException;

    /**
     * Cancel PIN management process.
     */
    void cancel();

}
